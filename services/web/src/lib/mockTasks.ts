export interface Task {
  id: string;
  title_vi: string;
  title_en: string;
  description_vi: string;
  description_en: string;
  culturalExplanation_vi: string;
  culturalExplanation_en: string;
  completionRequirement_vi: string;
  completionRequirement_en: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  image: string;
  location_vi: string;
  location_en: string;
}

export const mockTasks: Task[] = [
  {
    id: "1",
    title_vi: "Thăm Chùa Một Cột",
    title_en: "Visit One Pillar Pagoda",
    description_vi: "Hãy đến thăm ngôi chùa cổ kính Một Cột tại Hà Nội và chụp một bức ảnh kỷ niệm tại đây.",
    description_en: "Visit the historic One Pillar Pagoda in Hanoi and take a commemorative photo.",
    culturalExplanation_vi: "Chùa Một Cột được xây dựng năm 1049 dưới triều Lý Thái Tông, tượng trưng cho hoa sen nở trên mặt hồ — biểu tượng của sự thuần khiết trong Phật giáo Việt Nam.",
    culturalExplanation_en: "Built in 1049 under King Ly Thai Tong, the pagoda is shaped like a lotus blossom on a single pillar — a symbol of purity in Vietnamese Buddhism.",
    completionRequirement_vi: "Chụp ảnh tại chùa và chia sẻ lên ứng dụng",
    completionRequirement_en: "Take a photo at the pagoda and upload it",
    difficulty: "EASY",
    image: "/images/hero-hue-citadel.png",
    location_vi: "Ba Đình, Hà Nội",
    location_en: "Ba Dinh, Hanoi"
  },
  {
    id: "2",
    title_vi: "Thử món Bánh Mì Hội An",
    title_en: "Taste Hoi An Banh Mi",
    description_vi: "Tìm một tiệm bánh mì truyền thống tại Hội An và thưởng thức hương vị đặc trưng của ẩm thực miền Trung.",
    description_en: "Find a traditional banh mi shop in Hoi An and savor the unique Central Vietnamese flavor.",
    culturalExplanation_vi: "Bánh mì Hội An là sự giao thoa văn hóa độc đáo giữa ẩm thực Pháp và Việt Nam, với nhân đặc trưng gồm pate, thịt xá xíu và rau thơm địa phương.",
    culturalExplanation_en: "Hoi An banh mi represents a unique fusion of French and Vietnamese cuisines, filled with pate, char siu pork, and fresh local herbs.",
    completionRequirement_vi: "Ghé ít nhất 2 tiệm bánh mì và đánh giá trên ứng dụng",
    completionRequirement_en: "Visit at least 2 banh mi shops and rate them",
    difficulty: "EASY",
    image: "/images/hoian-lanterns.png",
    location_vi: "Hội An, Quảng Nam",
    location_en: "Hoi An, Quang Nam"
  },
  {
    id: "3",
    title_vi: "Học làm đèn lồng Hội An",
    title_en: "Learn Hoi An Lantern Making",
    description_vi: "Tham gia lớp học làm đèn lồng truyền thống tại một xưởng thủ công ở phố cổ Hội An.",
    description_en: "Join a traditional lantern making class at a craft workshop in the Hoi An ancient town.",
    culturalExplanation_vi: "Nghề làm đèn lồng Hội An có lịch sử hơn 400 năm, là biểu tượng văn hóa phi vật thể quan trọng. Mỗi chiếc đèn lồng được làm thủ công với khung tre và vải lụa.",
    culturalExplanation_en: "With over 400 years of history, Hoi An lantern making is a key intangible heritage. Each lantern is handcrafted with bamboo frames and silk fabric.",
    completionRequirement_vi: "Hoàn thành 1 chiếc đèn lồng và chụp ảnh sản phẩm",
    completionRequirement_en: "Complete 1 lantern and snap a photo of your product",
    difficulty: "MEDIUM",
    image: "/images/hoian-lanterns.png",
    location_vi: "Hội An, Quảng Nam",
    location_en: "Hoi An, Quang Nam"
  },
  {
    id: "4",
    title_vi: "Khám phá hang động Tràng An",
    title_en: "Explore Trang An Caves",
    description_vi: "Chèo thuyền qua ít nhất 3 hang động trong quần thể Tràng An và ghi chép lại những điều thú vị.",
    description_en: "Row through at least 3 caves in the Trang An complex and document your findings.",
    culturalExplanation_vi: "Tràng An là cái nôi của người Việt cổ với bằng chứng sinh sống từ 30.000 năm trước. Hệ thống hang động nơi đây từng là nơi trú ẩn và thờ cúng của người Việt thời tiền sử.",
    culturalExplanation_en: "Trang An is a cradle of ancient Vietnamese with habitation trace from 30,000 years ago. Its caves served as prehistoric shelters and places of worship.",
    completionRequirement_vi: "Hoàn thành tour thuyền qua 3 hang động, chụp ảnh tại mỗi hang",
    completionRequirement_en: "Complete a boat tour through 3 caves and take photos at each cave",
    difficulty: "MEDIUM",
    image: "/images/terraced-rice-fields.png",
    location_vi: "Ninh Bình",
    location_en: "Ninh Binh"
  },
  {
    id: "5",
    title_vi: "Leo núi Bà Nà Hills",
    title_en: "Trek Ba Na Hills",
    description_vi: "Chinh phục cung đường trekking lên đỉnh Bà Nà Hills và tận hưởng cảnh quan toàn cảnh Đà Nẵng từ trên cao.",
    description_en: "Conquer the trekking trail up Ba Na Hills and enjoy the panoramic view of Da Nang from above.",
    culturalExplanation_vi: "Bà Nà Hills không chỉ là khu nghỉ dưỡng hiện đại mà còn là nơi người Pháp xây dựng các biệt tẩy nghỉ dưỡng từ thế kỷ 20, tạo nên sự giao thoa kiến trúc Đông-Tây độc đáo.",
    culturalExplanation_en: "Ba Na Hills is not only a modern resort but also where the French built luxury villas in the 20th century, merging East-West architectural elements.",
    completionRequirement_vi: "Hoàn thành cung đường trekking 5km và check-in tại đỉnh núi",
    completionRequirement_en: "Complete the 5km trekking route and check-in at the summit",
    difficulty: "HARD",
    image: "/images/halong-bay.png",
    location_vi: "Đà Nẵng",
    location_en: "Da Nang"
  }
];
