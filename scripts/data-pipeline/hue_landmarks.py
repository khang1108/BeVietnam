"""
Famous Huế landmark registry for grounded quest + spotlight generation.

Each entry binds a place_id (stable slug) to GPS + radius and a list of search
aliases used to pull relevant passages out of the OCR'd Huế source books. The
coordinates are approximate centroids — every generated artifact stays
review_status=needs_review, and the radius is sized to cover the site.
"""

from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class Landmark:
    place_id: str
    place_name: str  # Vietnamese display name
    latitude: float
    longitude: float
    radius_meters: int
    aliases: list[str]  # case-insensitive substrings to match in book text
    categories: list[str]
    indoor_outdoor: str = "outdoor"
    quest_title: str = ""  # storyline title; defaults to place_name
    title_en: str = ""

    def quest_id(self) -> str:
        return f"quest-hue-{self.place_id}"

    def display_quest_title(self) -> str:
        return self.quest_title or self.place_name


# 14 famous / common Huế landmarks. ~9 questions each -> >=100 question pool.
HUE_LANDMARKS: list[Landmark] = [
    Landmark(
        place_id="hue-imperial-city",
        place_name="Kinh thành Huế (Đại Nội)",
        latitude=16.4699,
        longitude=107.5796,
        radius_meters=700,
        aliases=["Đại Nội", "Hoàng thành", "Kinh thành", "Ngọ Môn", "Thái Hòa", "Tử Cấm Thành"],
        categories=["history", "architecture", "culture"],
        quest_title="Dấu ấn Hoàng cung",
        title_en="Traces of the Imperial Court",
    ),
    Landmark(
        place_id="thien-mu-pagoda",
        place_name="Chùa Thiên Mụ",
        latitude=16.4536,
        longitude=107.5448,
        radius_meters=400,
        aliases=["Thiên Mụ", "Linh Mụ"],
        categories=["religion", "history", "architecture"],
        quest_title="Tiếng chuông Thiên Mụ",
        title_en="The Bell of Thiên Mụ",
    ),
    Landmark(
        place_id="perfume-river",
        place_name="Sông Hương",
        latitude=16.4690,
        longitude=107.5850,
        radius_meters=1500,
        aliases=["sông Hương", "Hương Giang", "dòng Hương"],
        categories=["nature", "culture", "art"],
        indoor_outdoor="outdoor",
        quest_title="Xuôi dòng Hương Giang",
        title_en="Down the Perfume River",
    ),
    Landmark(
        place_id="lang-minh-mang",
        place_name="Lăng Minh Mạng",
        latitude=16.4486,
        longitude=107.5536,
        radius_meters=600,
        aliases=["Minh Mạng", "Hiếu Lăng"],
        categories=["history", "architecture", "religion"],
        quest_title="Uy nghi Hiếu Lăng",
        title_en="The Majesty of Minh Mạng's Tomb",
    ),
    Landmark(
        place_id="lang-tu-duc",
        place_name="Lăng Tự Đức",
        latitude=16.4570,
        longitude=107.5530,
        radius_meters=600,
        aliases=["Tự Đức", "Khiêm Lăng"],
        categories=["history", "architecture", "art"],
        quest_title="Thi vị Khiêm Lăng",
        title_en="The Poetry of Tự Đức's Tomb",
    ),
    Landmark(
        place_id="lang-khai-dinh",
        place_name="Lăng Khải Định",
        latitude=16.3997,
        longitude=107.5870,
        radius_meters=400,
        aliases=["Khải Định", "Ứng Lăng"],
        categories=["history", "architecture", "art"],
        quest_title="Nghệ thuật Ứng Lăng",
        title_en="The Art of Khải Định's Tomb",
    ),
    Landmark(
        place_id="lang-gia-long",
        place_name="Lăng Gia Long",
        latitude=16.3760,
        longitude=107.5460,
        radius_meters=800,
        aliases=["Gia Long", "Thiên Thọ Lăng"],
        categories=["history", "architecture", "nature"],
        quest_title="Cội nguồn triều Nguyễn",
        title_en="Roots of the Nguyễn Dynasty",
    ),
    Landmark(
        place_id="dan-nam-giao",
        place_name="Đàn Nam Giao",
        latitude=16.4400,
        longitude=107.5790,
        radius_meters=400,
        aliases=["Nam Giao", "tế Giao", "đàn tế"],
        categories=["history", "religion", "tradition"],
        quest_title="Lễ tế trời đất",
        title_en="The Rite of Heaven and Earth",
    ),
    Landmark(
        place_id="ho-quyen",
        place_name="Hổ Quyền",
        latitude=16.4530,
        longitude=107.5530,
        radius_meters=300,
        aliases=["Hổ Quyền", "đấu trường"],
        categories=["history", "architecture", "tradition"],
        quest_title="Đấu trường voi - hổ",
        title_en="The Elephant-Tiger Arena",
    ),
    Landmark(
        place_id="van-mieu-hue",
        place_name="Văn Miếu Huế",
        latitude=16.4580,
        longitude=107.5430,
        radius_meters=400,
        aliases=["Văn Miếu", "Văn Thánh", "bia tiến sĩ"],
        categories=["history", "culture", "tradition"],
        quest_title="Bia đá Văn Thánh",
        title_en="The Stelae of the Temple of Literature",
    ),
    Landmark(
        place_id="cau-truong-tien",
        place_name="Cầu Trường Tiền",
        latitude=16.4690,
        longitude=107.5920,
        radius_meters=300,
        aliases=["Trường Tiền", "Tràng Tiền"],
        categories=["history", "architecture", "art"],
        quest_title="Nhịp cầu Trường Tiền",
        title_en="The Spans of Trường Tiền Bridge",
    ),
    Landmark(
        place_id="cho-dong-ba",
        place_name="Chợ Đông Ba",
        latitude=16.4719,
        longitude=107.5849,
        radius_meters=400,
        aliases=["Đông Ba"],
        categories=["food", "culture", "tradition"],
        indoor_outdoor="any",
        quest_title="Chợ quê giữa lòng Cố đô",
        title_en="A Market in the Heart of the Old Capital",
    ),
    Landmark(
        place_id="hue-cuisine",
        place_name="Ẩm thực Huế",
        latitude=16.4637,
        longitude=107.5909,
        radius_meters=2000,
        aliases=["ẩm thực", "bún bò", "cơm hến", "món Huế", "chè Huế", "bánh khoái", "cung đình"],
        categories=["food", "culture", "tradition"],
        indoor_outdoor="any",
        quest_title="Hương vị Cố đô",
        title_en="Flavors of the Old Capital",
    ),
    Landmark(
        place_id="dien-hon-chen",
        place_name="Điện Hòn Chén",
        latitude=16.4170,
        longitude=107.5470,
        radius_meters=400,
        aliases=["Hòn Chén", "Huệ Nam", "Thiên Y A Na"],
        categories=["religion", "tradition", "history"],
        quest_title="Tín ngưỡng Mẫu Hòn Chén",
        title_en="The Mother Goddess of Hòn Chén",
    ),
]


# Books to mine for Huế landmark passages (paths relative to repo root).
HUE_BOOKS: dict[str, str] = {
    "Cố đô Huế xưa và nay": "data/books/Co-do-hue-xua-va-nay.md",
    "30 năm nghiên cứu văn hóa dân gian Huế": "data/books/30-nam-nghien-cuu-van-hoa-dan-gian.md",
}
