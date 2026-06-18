"""Prompts for the Spotlight Maker agent (feed post generation)."""

from typing import Any

from services.ai.common.post_schema import SEASON_TAGS, TIME_TAGS, WEATHER_TAGS

SYSTEM_PROMPT = (
    "Bạn là biên tập viên du lịch văn hóa Huế. Viết một 'spotlight' ngắn, hấp dẫn, "
    "TRUNG THỰC cho một địa điểm, chỉ dựa trên các sự thật văn hóa được cung cấp. "
    "Không bịa đặt. Chỉ trả về JSON, không thêm chữ nào khác."
)


def _facts_block(facts: list[dict[str, Any]]) -> str:
    lines = []
    for i, fact in enumerate(facts, 1):
        text = str(fact.get("text", "")).strip()
        if text:
            lines.append(f"  {i}. {text}")
    return "\n".join(lines) if lines else "  (không có sự thật nào)"


def build_user_prompt(
    place_name: str,
    facts: list[dict[str, Any]],
    language: str = "vi",
) -> str:
    """Build the spotlight generation prompt grounded in retrieved facts."""
    lang_name = "tiếng Việt" if language == "vi" else "tiếng Anh"
    return (
        f"Địa điểm: {place_name}\n"
        f"Sự thật văn hóa (chỉ dùng những điều này):\n{_facts_block(facts)}\n\n"
        f"Viết bằng {lang_name}. Trả về JSON đúng định dạng:\n"
        "{\n"
        '  "title": "<tiêu đề hấp dẫn, <= 12 từ>",\n'
        '  "body": "<2-4 câu: nên ghé khi nào và vì sao, dựa trên sự thật trên>",\n'
        '  "cultural_hook": "<một sự thật văn hóa nổi bật, lấy từ danh sách trên>",\n'
        f'  "weather_tags": <tập con của {sorted(WEATHER_TAGS)}>,\n'
        f'  "time_tags": <tập con của {sorted(TIME_TAGS)}>,\n'
        f'  "season_tags": <tập con của {sorted(SEASON_TAGS)}>,\n'
        '  "categories": ["<1-3 thẻ chủ đề slug>"]\n'
        "}\n"
        "Quy tắc thẻ điều kiện: chỉ thêm thẻ khi địa điểm THỰC SỰ phù hợp hơn vào "
        "điều kiện đó (vd: điểm trong nhà -> rainy; ngắm cảnh -> sunny, evening). "
        "Để mảng rỗng nếu phù hợp mọi điều kiện. Chỉ dùng thẻ trong danh sách."
    )
