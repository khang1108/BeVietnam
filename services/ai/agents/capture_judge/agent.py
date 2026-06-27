"""
Capture Judge — verifies that a user's photo actually matches the task.

Pipeline:
  1. Decode the uploaded image (base64 data URL or hosted URL).
  2. Fetch a reference photo of the task's subject (SerpAPI → cached on disk).
  3. Ask Gemini vision to score the upload against the reference + task text on a
     deterministic rubric, returning a confidence in [0, 1].
  4. Map the score to approved / needs_review / rejected.

If no reference image is available (no SerpAPI key) it still judges the upload
against the task description alone. If vision is unavailable (no Gemini key /
call fails) it returns needs_review rather than blindly approving.
"""

from __future__ import annotations

import base64
import logging

import httpx

from services.ai.agents.capture_judge.reference import get_reference_image
from services.ai.common.llm import llm_gateway

logger = logging.getLogger(__name__)

# Score thresholds (see rubric in the system prompt).
_APPROVE_AT = 0.7
_REVIEW_AT = 0.4

_RUBRIC_SYSTEM = """\
Bạn là giám khảo kiểm chứng ảnh cho một ứng dụng du lịch văn hóa Việt Nam.
Nhiệm vụ: chấm điểm mức độ ảnh người dùng tải lên KHỚP với chủ thể/địa điểm mà
nhiệm vụ yêu cầu. Hãy nghiêm khắc và bám sát rubric (thang điểm 0.0–1.0):

- 0.0–0.2: Hoàn toàn khác, không liên quan đến chủ thể yêu cầu.
- 0.2–0.4: Vẫn khác, chỉ nhỉnh hơn một chút (cùng chủ đề chung nhưng sai đối tượng).
- 0.4–0.6: Gần đúng nhưng vẫn KHÁC địa điểm. Ví dụ: yêu cầu "Ngọ Môn (Huế)" nhưng
  ảnh là "Tử Cấm Thành" — cùng quần thể nhưng sai công trình → coi là khác.
- 0.7–0.9: Ảnh tốt, đúng chủ thể/địa điểm được yêu cầu.
- 1.0: Trùng khớp rõ ràng, không nghi ngờ.

Nếu có ẢNH THAM CHIẾU: ảnh đầu là chuẩn, ảnh sau là của người dùng — so sánh trực
tiếp công trình/đối tượng. Nếu KHÔNG có ảnh tham chiếu: chấm dựa trên mô tả nhiệm vụ.
Đừng khoan nhượng với ảnh chung chung, ảnh chụp màn hình, hay ảnh không phải địa điểm.

Chỉ trả về JSON đúng schema:
{"score": <float 0..1>, "label": "<nhãn ngắn>", "reason": "<giải thích ngắn bằng tiếng Việt>"}
"""

_UA = "Mozilla/5.0 (BeVietnam CaptureJudge)"


def _decode_data_url(media: str) -> tuple[bytes, str] | None:
    if not isinstance(media, str) or "base64," not in media:
        return None
    header, b64 = media.split("base64,", 1)
    mime = "image/jpeg"
    if header.startswith("data:") and ";" in header:
        mime = header[5:].split(";", 1)[0] or mime
    try:
        return base64.b64decode(b64), mime
    except (ValueError, TypeError):
        return None


def _download(url: str) -> tuple[bytes, str] | None:
    try:
        resp = httpx.get(url, timeout=15.0, follow_redirects=True, headers={"User-Agent": _UA})
        resp.raise_for_status()
        ctype = resp.headers.get("content-type", "")
        if not ctype.startswith("image/"):
            return None
        return resp.content, ctype.split(";", 1)[0]
    except httpx.HTTPError:
        return None


def _subject_query(task: dict) -> str:
    title = str(task.get("title") or "").strip()
    place = str(task.get("place_name") or "").strip()
    parts = [p for p in (title, place, "Việt Nam") if p]
    return " ".join(parts) if parts else "địa điểm du lịch Việt Nam"


def _user_prompt(task: dict, has_reference: bool) -> str:
    lines = [
        f"Nhiệm vụ: {task.get('title', '')}",
        f"Địa điểm: {task.get('place_name', '')}",
        f"Yêu cầu: {task.get('question_text', '')}",
    ]
    if has_reference:
        lines.append(
            "Ảnh 1 = ảnh tham chiếu chuẩn của địa điểm. Ảnh 2 = ảnh người dùng tải lên. "
            "So sánh và chấm điểm ảnh người dùng."
        )
    else:
        lines.append(
            "Chỉ có ảnh người dùng tải lên (không có ảnh tham chiếu). "
            "Chấm điểm dựa trên mô tả nhiệm vụ ở trên."
        )
    return "\n".join(lines)


class CaptureJudge:
    """Vision-grounded verification of task-completion photos."""

    def verify(self, context: dict) -> dict:
        task = context.get("task") or {}
        capture = context.get("capture") or {}
        media = capture.get("media_url") or capture.get("image_url") or ""
        note = capture.get("note") or ""

        user_image = _decode_data_url(media)
        if user_image is None and isinstance(media, str) and media.startswith(("http://", "https://")):
            user_image = _download(media)

        if user_image is None:
            return {
                "status": "rejected",
                "reason": note or "Chưa có ảnh minh chứng. Hãy tải lên một ảnh chụp tại địa điểm.",
                "confidence": 0.0,
            }

        task_id = str(task.get("question_id") or task.get("task_id") or "unknown")
        reference = get_reference_image(task_id, _subject_query(task))

        images: list[tuple[bytes, str]] = []
        if reference is not None:
            images.append(reference)
        images.append(user_image)

        result = llm_gateway.generate_json_multimodal(
            system_prompt=_RUBRIC_SYSTEM,
            user_prompt=_user_prompt(task, has_reference=reference is not None),
            images=images,
        )

        if not result or "score" not in result:
            # Vision unavailable — do NOT auto-approve; route to manual review.
            return {
                "status": "needs_review",
                "reason": "Chưa thể tự động kiểm chứng ảnh. Cần duyệt thủ công.",
                "confidence": 0.0,
            }

        try:
            score = max(0.0, min(1.0, float(result["score"])))
        except (TypeError, ValueError):
            score = 0.0
        reason = str(result.get("reason") or result.get("label") or "").strip()

        if score >= _APPROVE_AT:
            status = "approved"
        elif score >= _REVIEW_AT:
            status = "needs_review"
        else:
            status = "rejected"

        return {
            "status": status,
            "reason": reason or "Đã đánh giá ảnh minh chứng.",
            "confidence": round(score, 2),
        }
