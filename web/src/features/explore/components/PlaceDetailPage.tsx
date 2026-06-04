'use client';

import React from 'react';
import { useParams } from 'next/navigation';
import { useI18n } from '@/i18n';
import styles from '@/styles/pages.module.css';

const mockPlaces = [
    {
        id: 1,
        title_vi: 'Chùa Một Cột',
        title_en: 'One Pillar Pagoda',
        category_vi: 'Văn hóa',
        category_en: 'Culture',
        location_vi: 'Ba Đình, Hà Nội',
        location_en: 'Ba Dinh, Hanoi',
        image: '/images/one-pillar-pagoda.png',
        intro_vi: 'Chùa Một Cột (tên chữ là Diên Hựu tự) là một ngôi chùa nằm giữa lòng thủ đô Hà Nội. Chùa được xây dựng vào năm 1049 dưới thời vua Lý Thái Tông. Đây là một trong những ngôi chùa có kiến trúc độc đáo nhất châu Á, với hình dáng một bông sen mọc trên mặt nước.',
        intro_en: 'One Pillar Pagoda (official name: Dien Huu Pagoda) is a temple located in the heart of Hanoi. Built in 1049 under King Ly Thai Tong, it is one of the most uniquely designed temples in Asia, shaped like a lotus blossom rising from the water.',
        context_vi: 'Theo truyền thuyết, vua Lý Thái Tông nằm mộng thấy Phật bà Quan Âm ngồi trên đài sen dẫn vua lên đài. Sau khi tỉnh dậy, nhà vua đã cho xây chùa theo hình ảnh trong giấc mơ. Ngôi chùa thể hiện triết lý Phật giáo về sự sinh ra từ bùn lầy nhưng vươn lên trong sáng.',
        context_en: 'According to legend, King Ly Thai Tong dreamt of Avalokitesvara Bodhisattva sitting on a lotus throne, guiding him up. After awakening, the king ordered the construction of the pagoda based on the dream. The temple embodies the Buddhist philosophy of rising pure from muddy origins.',
        address_vi: 'Chùa Một Cột, Ba Đình, Hà Nội',
        address_en: 'One Pillar Pagoda, Ba Binh, Hanoi',
        time: '7:00 - 18:00',
        fee_vi: 'Miễn phí',
        fee_en: 'Free',
        rating: '4.8'
    },
    {
        id: 2,
        title_vi: 'Bún chả Hương Liên',
        title_en: 'Bun Cha Huong Lien',
        category_vi: 'Ẩm thực',
        category_en: 'Food',
        location_vi: 'Hai Bà Trưng, Hà Nội',
        location_en: 'Hai Ba Trung, Hanoi',
        image: '/images/buncha-huonglien.png',
        intro_vi: 'Bún chả Hương Liên là một trong những quán bún chả nổi tiếng nhất Hà Nội với hơn 25 năm hoạt động. Quán trở nên nổi tiếng toàn cầu sau khi đón tiếp cựu Tổng thống Mỹ Barack Obama và đầu bếp huyền thoại Anthony Bourdain ghé thăm năm 2016.',
        intro_en: 'Bun Cha Huong Lien is one of Hanoi\'s most famous bun cha establishments, operating for over 25 years. It achieved global fame when it hosted former US President Barack Obama and legendary chef Anthony Bourdain in 2016.',
        context_vi: 'Bún chả là món ăn mang tính biểu tượng của Hà Nội, kết hợp hài hòa giữa thịt lợn nướng thơm lừng, bún sợi nhỏ mềm mịn, và nước dùng chua ngọt thanh nhẹ kèm đu đủ xanh muối chua. Đây là đỉnh cao của sự cân bằng hương vị ẩm thực Bắc Bộ.',
        context_en: 'Bun cha is an iconic Hanoi dish, harmoniously combining smoky grilled pork, soft rice noodles, and a balanced sweet-and-sour dipping sauce with green papaya. It represents the pinnacle of northern Vietnamese culinary balance.',
        address_vi: '24 Lê Văn Hưu, Hai Bà Trưng, Hà Nội',
        address_en: '24 Le Van Huu, Hai Ba Trung, Hanoi',
        time: '8:00 - 20:30',
        fee_vi: 'Từ 50.000đ - 100.000đ',
        fee_en: '50,000 VND - 100,000 VND',
        rating: '4.7'
    },
    {
        id: 3,
        title_vi: 'Vườn quốc gia Phong Nha - Kẻ Bàng',
        title_en: 'Phong Nha - Ke Bang National Park',
        category_vi: 'Thiên nhiên',
        category_en: 'Nature',
        location_vi: 'Quảng Bình, Việt Nam',
        location_en: 'Quang Binh, Vietnam',
        image: '/images/phongnha.png',
        intro_vi: 'Vườn quốc gia Phong Nha - Kẻ Bàng được UNESCO công nhận là Di sản thiên nhiên thế giới. Nơi đây sở hữu hệ thống hang động đá vôi cổ nhất và tráng lệ nhất thế giới với hàng trăm hang động độc đáo.',
        intro_en: 'Phong Nha - Ke Bang National Park is a UNESCO World Heritage site. It houses the oldest and most magnificent limestone karst ecosystems in the world, featuring hundreds of unique caves.',
        context_vi: 'Hệ thống hang động tại Phong Nha được mệnh danh là đệ nhất kỳ quan hang động. Dòng sông ngầm chảy xuyên qua lòng hang động mang lại vẻ đẹp huyền ảo, phản chiếu các cột thạch nhũ hàng triệu năm tuổi vô cùng lung linh.',
        context_en: 'The cave system at Phong Nha is dubbed the king of caves. The underground river running through the caverns brings out a magical beauty, reflecting millions-of-years-old stalactites and stalagmites in high detail.',
        address_vi: 'Phong Nha, Bố Trạch, Quảng Bình',
        address_en: 'Phong Nha, Bo Trach, Quang Binh',
        time: '7:30 - 17:30',
        fee_vi: 'Vé vào cửa từ 150.000đ (chưa gồm thuyền)',
        fee_en: 'From 150,000 VND (excluding boat)',
        rating: '4.9'
    },
    {
        id: 4,
        title_vi: 'Hoàng thành Thăng Long',
        title_en: 'Imperial Citadel of Thang Long',
        category_vi: 'Lịch sử',
        category_en: 'History',
        location_vi: 'Ba Đình, Hà Nội',
        location_en: 'Ba Dinh, Hanoi',
        image: '/images/citadel-thanglong.png',
        intro_vi: 'Hoàng thành Thăng Long là quần thể di tích gắn liền với lịch sử kinh thành Thăng Long - Hà Nội. Công trình kiến trúc đồ sộ này được các triều đại vua xây dựng trong nhiều giai đoạn lịch sử và trở thành Di sản thế giới UNESCO năm 2010.',
        intro_en: 'The Imperial Citadel of Thang Long is an outstanding complex of historic relic sites associated with the history of Hanoi. This massive architectural work was built by successive dynasties and named a UNESCO World Heritage site in 2010.',
        context_vi: 'Nơi đây minh chứng cho sự giao thoa văn hóa đặc sắc suốt hơn 13 thế kỷ của Việt Nam. Cổng Đoan Môn nhuốm màu rêu phong cổ kính là biểu tượng trường tồn bảo vệ và chứng kiến sự thịnh suy của các vương triều phong kiến Việt Nam.',
        context_en: 'This site stands as proof of over 13 centuries of cultural intersection in Vietnam. The moss-covered ancient Doan Mon Gate stands as an enduring symbol, witnessing the rise and fall of feudal Vietnamese empires.',
        address_vi: '19C Hoàng Diệu, Ba Đình, Hà Nội',
        address_en: '19C Hoang Dieu, Ba Dinh, Hanoi',
        time: '8:00 - 17:00',
        fee_vi: '30.000đ (Học sinh/Sinh viên giảm 50%)',
        fee_en: '30,000 VND (50% student discount)',
        rating: '4.6'
    },
    {
        id: 5,
        title_vi: 'Bánh mì Phượng Hội An',
        title_en: 'Banh Mi Phuong Hoi An',
        category_vi: 'Ẩm thực',
        category_en: 'Food',
        location_vi: 'Hội An, Quảng Nam',
        location_en: 'Hoi An, Quang Nam',
        image: '/images/banhmi-phuong.png',
        intro_vi: 'Bánh mì Phượng là tiệm bánh mì trứ danh tại phố cổ Hội An, được đầu bếp nổi tiếng Anthony Bourdain ca ngợi là "bánh mì ngon nhất thế giới". Chiếc bánh mì giòn xốp ngập tràn hương vị đặc trưng.',
        intro_en: 'Banh Mi Phuong is a legendary banh mi shop in Hoi An ancient town, praised by Anthony Bourdain as "the best bread in the world". The crispy bread is packed with flavorful local ingredients.',
        context_vi: 'Sự kỳ diệu nằm ở lớp pate béo ngậy được làm theo công thức gia truyền, kết hợp với các loại thịt xá xíu, chả lụa thơm ngon, rau thơm tươi mát của làng Trà Quế và nước sốt nước tương sánh đặc vô cùng kích thích vị giác.',
        context_en: 'The secret lies in the rich homemade pate prepared with a traditional family recipe, combined with savory barbecued pork, Vietnamese cold cuts, fresh Tra Que herbs, and a signature sauce that elevates the taste.',
        address_vi: '2B Phan Chu Trinh, Hội An, Quảng Nam',
        address_en: '2B Phan Chu Trinh, Hoi An, Quang Nam',
        time: '6:30 - 21:30',
        fee_vi: 'Từ 20.000đ - 40.000đ',
        fee_en: '20,000 VND - 40,000 VND',
        rating: '4.8'
    },
    {
        id: 6,
        title_vi: 'Phố đi bộ Bùi Viện',
        title_en: 'Bui Vien Walking Street',
        category_vi: 'Giải trí',
        category_en: 'Nightlife',
        location_vi: 'Quận 1, TP.HCM',
        location_en: 'District 1, HCMC',
        image: '/images/buivien.png',
        intro_vi: 'Phố đi bộ Bùi Viện là trung tâm giải trí đêm sôi động bậc nhất Sài Gòn, nổi tiếng với biệt danh "Phố Tây". Nơi đây thu hút đông đảo du khách trong và ngoài nước đến trải nghiệm không khí náo nhiệt.',
        intro_en: 'Bui Vien Walking Street is the most vibrant nightlife hub in Saigon, famously dubbed "Western Street". It draws massive crowds of domestic and international visitors seeking energetic night activities.',
        context_vi: 'Dưới ánh đèn neon rực rỡ sắc màu, con phố tràn ngập âm nhạc từ các quán bar, pub sôi động, cùng vô số món ăn đường phố hấp dẫn. Đây là điểm đến lý tưởng để tận hưởng cuộc sống không ngủ đầy hứng khởi của Sài Gòn.',
        context_en: 'Under glowing neon lights, the street comes alive with loud music from bars, pubs, and delicious street food vendors. It is the perfect spot to experience Saigon\'s sleepless, high-energy lifestyle.',
        address_vi: 'Bùi Viện, Phạm Ngũ Lão, Quận 1, TP.HCM',
        address_en: 'Bui Vien, Pham Ngu Lao, District 1, HCMC',
        time: 'Mở cửa cả ngày (Sôi động từ 19:00 - 3:00)',
        fee_vi: 'Miễn phí vào cửa',
        fee_en: 'Free admission',
        rating: '4.5'
    },
    {
        id: 11,
        title_vi: 'Phố cổ Hội An',
        title_en: 'Hoi An Ancient Town',
        category_vi: 'Văn hóa',
        category_en: 'Culture',
        location_vi: 'Quảng Nam, Việt Nam',
        location_en: 'Quang Nam, Vietnam',
        image: '/images/hoian-lanterns.png',
        intro_vi: 'Phố cổ Hội An là một đô thị cổ nằm ở hạ lưu sông Thu Bồn, thuộc tỉnh Quảng Nam. Nơi đây từng là thương cảng quốc tế sầm uất thế kỷ XVII-XVIII và được bảo tồn gần như nguyên vẹn cho đến nay.',
        intro_en: 'Hoi An Ancient Town is an exceptionally well-preserved example of a Southeast Asian trading port from the 15th to the 19th century. Its buildings and street plan reflect a unique blend of local and foreign influences.',
        context_vi: 'Vẻ đẹp của Hội An nằm ở những ngôi nhà cổ sơn vàng, giàn hoa giấy hồng rực rỡ, dòng sông Hoài thơ mộng lung linh hoa đăng, và hàng ngàn chiếc đèn lồng lụa sắc màu ấm áp thắp sáng mỗi đêm.',
        context_en: 'The charm of Hoi An lies in its yellow-painted houses, vibrant pink bougainvillea, the romantic Hoai river shimmering with paper lanterns, and thousands of warm silk lanterns lighting up the night.',
        address_vi: 'Phố cổ Hội An, Quảng Nam',
        address_en: 'Hoi An Ancient Town, Quang Nam',
        time: 'Tự do cả ngày',
        fee_vi: 'Miễn phí (Mua vé nếu tham quan các nhà cổ di tích)',
        fee_en: 'Free (Ticket required for heritage house entries)',
        rating: '4.9'
    },
    {
        id: 12,
        title_vi: 'Phở Hà Nội',
        title_en: 'Hanoi Pho',
        category_vi: 'Ẩm thực',
        category_en: 'Food',
        location_vi: 'Hà Nội, Việt Nam',
        location_en: 'Hanoi, Vietnam',
        image: '/images/hanoi-pho.png',
        intro_vi: 'Phở Hà Nội không chỉ là một món ăn sáng thông thường mà còn là quốc hồn quốc túy của ẩm thực Việt Nam. Một bát phở nóng hổi hòa quyện tinh hoa đất trời Thăng Long hơn 100 năm qua.',
        intro_en: 'Hanoi Pho is not just a breakfast dish but the national soul of Vietnamese cuisine. A steaming hot bowl of Pho embodies the culinary essence of Hanoi developed over a century.',
        context_vi: 'Bí quyết của phở ngon nằm ở nước dùng trong, ngọt thanh từ xương bò ninh nhừ cùng các loại gia vị nướng sấy khô như gừng, thảo quả, đại hồi, quế, đinh hương, kết hợp cùng bánh phở dai mềm và thịt bò tái chín.',
        context_en: 'The secret of outstanding Pho lies in its clear, sweet broth simmered from beef bones, spiced with charred ginger, star anise, cinnamon, cardamom, and cloves, poured over soft flat rice noodles and tender beef.',
        address_vi: 'Khắp ngõ phố Hà Nội (Phở Bát Đàn, Phở Thìn, Phở Gia Truyền)',
        address_en: 'Across Hanoi old quarter streets (Bat Dan, Thin, Gia Truyen)',
        time: '6:00 - 22:00',
        fee_vi: 'Từ 40.000đ - 75.000đ/bát',
        fee_en: '40,000 VND - 75,000 VND per bowl',
        rating: '4.8'
    },
    {
        id: 13,
        title_vi: 'Ruộng bậc thang Mù Cang Chải',
        title_en: 'Mu Cang Chai Terraced Rice Fields',
        category_vi: 'Thiên nhiên',
        category_en: 'Nature',
        location_vi: 'Yên Bái, Việt Nam',
        location_en: 'Yen Bai, Vietnam',
        image: '/images/terraced-rice-fields.png',
        intro_vi: 'Ruộng bậc thang Mù Cang Chải là kiệt tác điêu khắc đất đai độc đáo được kiến tạo bởi đôi bàn tay khéo léo của người dân tộc Mông nơi miền núi Tây Bắc hoang sơ tráng lệ.',
        intro_en: 'Mu Cang Chai Terraced Rice Fields are a spectacular landscape masterpiece carved into mountainsides by the skillful hands of the Hmong minority in the wild Northwest highlands.',
        context_vi: 'Mỗi mùa thu hoạch về (tháng 9-10), những sườn đồi bậc thang uốn lượn trùng điệp lại khoác lên mình màu vàng óng của lúa chín, tạo nên bức tranh thiên nhiên rực rỡ và kỳ vĩ, thu hút vô vàn tay máy.',
        context_en: 'Every harvest season (September-October), the undulating terraces transform into a shimmering sea of golden rice, presenting a magnificent, breathtaking natural canvas that draws photographers globally.',
        address_vi: 'Mù Cang Chải, Yên Bái',
        address_en: 'Mu Cang Chai, Yen Bai',
        time: 'Tự do cả ngày',
        fee_vi: 'Miễn phí',
        fee_en: 'Free',
        rating: '4.9'
    },
    {
        id: 14,
        title_vi: 'Múa rối nước Việt Nam',
        title_en: 'Vietnamese Water Puppetry',
        category_vi: 'Văn hóa',
        category_en: 'Culture',
        location_vi: 'Hà Nội, Việt Nam',
        location_en: 'Hanoi, Vietnam',
        image: '/images/water-puppet.png',
        intro_vi: 'Múa rối nước là loại hình nghệ thuật sân khấu dân gian truyền thức độc đáo ra đời từ nền văn minh lúa nước đồng bằng sông Hồng của Việt Nam với hơn một nghìn năm lịch sử.',
        intro_en: 'Water puppetry is a highly unique traditional Vietnamese folk art form that originated in the wet rice civilization of the Red River Delta with over a thousand years of rich history.',
        context_vi: 'Nghệ thuật này sử dụng mặt nước làm sân khấu. Các nghệ nhân ngâm mình dưới nước điều khiển các con rối gỗ sơn son thếp vàng di chuyển khéo léo thông qua hệ thống sào, dây ẩn dưới nước kết hợp nhạc chèo réo rắt.',
        context_en: 'This unique art uses a pool of water as the stage. Puppeteers stand waist-deep in water behind a screen, controlling lacquered wooden puppets using underwater bamboo poles and strings, synchronized with live folk opera.',
        address_vi: 'Nhà hát múa rối nước Thăng Long, 57B Đinh Tiên Hoàng, Hà Nội',
        address_en: 'Thang Long Water Puppet Theatre, 57B Dinh Tien Hoang, Hanoi',
        time: 'Các ca diễn: 15:00, 16:10, 17:20, 18:30, 20:00',
        fee_vi: 'Từ 100.000đ - 200.000đ/vé',
        fee_en: '100,000 VND - 200,000 VND per ticket',
        rating: '4.7'
    },
    {
        id: 15,
        title_vi: 'Vịnh Hạ Long',
        title_en: 'Ha Long Bay',
        category_vi: 'Thiên nhiên',
        category_en: 'Nature',
        location_vi: 'Quảng Ninh, Việt Nam',
        location_en: 'Quang Ninh, Vietnam',
        image: '/images/halong-bay.png',
        intro_vi: 'Vịnh Hạ Long là kỳ quan thiên nhiên thế giới được UNESCO công nhận nhiều lần. Vịnh sở hữu hàng ngàn đảo đá vôi khổng lồ, muôn hình vạn trạng nhô lên giữa dòng nước biển xanh ngọc bích phẳng lặng.',
        intro_en: 'Ha Long Bay is a world-renowned natural wonder, twice recognized by UNESCO. The bay features thousands of dramatic limestone karsts and isles rising majestically out of calm, turquoise waters.',
        context_vi: 'Hàng ngàn hòn đảo mang tên gọi độc đáo như Hòn Trống Mái, Đỉnh Hương, Hòn Chó Đá cùng vô số hang động kỳ vĩ đầy thạch nhũ như Hang Sửng Sốt, Động Thiên Cung kiến tạo nên một tiên cảnh nhân gian kỳ ảo.',
        context_en: 'Thousands of islands with descriptive names like Fighting Cocks, Stone Dog, Incense Burner alongside magnificent caves like Sung Sot or Thien Cung create a mythical, otherworldly paradise on earth.',
        address_vi: 'Thành phố Hạ Long, Quảng Ninh',
        address_en: 'Ha Long City, Quang Ninh',
        time: 'Du thuyền tham quan theo tour: 8:00 - 17:00',
        fee_vi: 'Vé tham quan vịnh từ 290.000đ (chưa gồm giá tàu)',
        fee_en: 'Bay ticket from 290,000 VND (excluding cruise hire)',
        rating: '4.9'
    },
    {
        id: 16,
        title_vi: 'Cà phê trứng Hà Nội',
        title_en: 'Hanoi Egg Coffee',
        category_vi: 'Ẩm thực',
        category_en: 'Food',
        location_vi: 'Hà Nội, Việt Nam',
        location_en: 'Hanoi, Vietnam',
        image: '/images/egg-coffee.png',
        intro_vi: 'Cà phê trứng là thức uống huyền thoại đặc trưng của Hà Nội. Sự kết hợp độc đáo giữa vị đắng đậm đà của cà phê Robusta và lớp bọt trứng đánh bông ngọt ngào, béo ngậy vô cùng quyến rũ.',
        intro_en: 'Egg coffee is a legendary beverage born in Hanoi. It uniquely combines the robust bitterness of dark espresso coffee with a thick, sweet, and velvety whipped egg yolk foam.',
        context_vi: 'Món nước được cụ Nguyễn Văn Giảng sáng tạo vào những năm 1940 trong thời kỳ khan hiếm sữa tươi. Lòng đỏ trứng được đánh bông mịn với đường và mật ong thay sữa, tạo nên hương vị béo ngậy thơm ngon tựa bánh tiramisu.',
        context_en: 'The drink was invented by Mr. Nguyen Van Giang in the 1940s during a fresh milk shortage. Whipped egg yolk with sugar and honey substituted milk, creating a rich, creamy taste reminiscent of tiramisu.',
        address_vi: 'Cà phê Giảng (39 Nguyễn Hữu Huân), Cà phê Lâm (60 Nguyễn Hữu Huân)',
        address_en: 'Cafe Giang (39 Nguyen Huu Huan), Cafe Lam (60 Nguyen Huu Huan)',
        time: '7:00 - 22:00',
        fee_vi: 'Từ 30.000đ - 45.000đ/tách',
        fee_en: '30,000 VND - 45,000 VND per cup',
        rating: '4.8'
    }
];

export function PlaceDetailPage() {
    const { locale } = useI18n();
    const params = useParams();
    const id = params?.id ? Number(params.id) : 1;
    const place = mockPlaces.find((p) => p.id === id) || mockPlaces[0];

    return (
        <div className={styles.pageContainer} id="place-detail-page">
            <div className={styles.detailHero} style={{ opacity: 1, overflow: 'hidden', padding: 0 }}>
                {place.image ? (
                    <img 
                        src={place.image} 
                        alt={locale === 'vi' ? place.title_vi : place.title_en}
                        style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    />
                ) : (
                    '🏛️'
                )}
            </div>

            <div className={styles.pageHeader}>
                <div className={styles.pageTag}>📍 {locale === 'vi' ? place.category_vi : place.category_en}</div>
                <h1 className={styles.pageTitle}>
                    {locale === 'vi' ? place.title_vi : place.title_en}
                </h1>
                <p className={styles.pageSubtitle}>
                    {locale === 'vi' ? place.location_vi : place.location_en}
                </p>
            </div>

            <div className={styles.detailContent}>
                <div className={styles.detailMain}>
                    <h2>{locale === 'vi' ? 'Giới thiệu' : 'Introduction'}</h2>
                    <p>
                        {locale === 'vi' ? place.intro_vi : place.intro_en}
                    </p>

                    <h2>{locale === 'vi' ? 'Bối cảnh văn hóa' : 'Cultural Context'}</h2>
                    <p>
                        {locale === 'vi' ? place.context_vi : place.context_en}
                    </p>

                    <h2 style={{ marginTop: 'var(--space-8)' }}>{locale === 'vi' ? 'Thư viện ảnh' : 'Photo Gallery'}</h2>
                    <div style={{ borderRadius: 'var(--radius-xl)', overflow: 'hidden', border: '1px solid var(--border-primary)' }}>
                        <img 
                            src={place.image} 
                            alt={locale === 'vi' ? place.title_vi : place.title_en} 
                            style={{ width: '100%', height: 'auto', display: 'block' }}
                        />
                    </div>
                </div>

                <div className={styles.detailSidebar}>
                    <div className={styles.sidebarCard}>
                        <h3>{locale === 'vi' ? 'Thông tin' : 'Info'}</h3>
                        <div className={styles.sidebarItem}>
                            📍 {locale === 'vi' ? place.address_vi : place.address_en}
                        </div>
                        <div className={styles.sidebarItem}>
                            🕐 {place.time}
                        </div>
                        <div className={styles.sidebarItem}>
                            💰 {locale === 'vi' ? place.fee_vi : place.fee_en}
                        </div>
                        <div className={styles.sidebarItem}>⭐ {place.rating}/5</div>
                    </div>

                    <div className={styles.sidebarCard}>
                        <h3>{locale === 'vi' ? 'Hành động' : 'Actions'}</h3>
                        <div className={styles.sidebarItem} style={{ cursor: 'pointer' }}>
                            🗺️ {locale === 'vi' ? 'Chỉ đường' : 'Get Directions'}
                        </div>
                        <div className={styles.sidebarItem} style={{ cursor: 'pointer' }}>
                            📤 {locale === 'vi' ? 'Chia sẻ' : 'Share'}
                        </div>
                        <div className={styles.sidebarItem} style={{ cursor: 'pointer' }}>
                            💾 {locale === 'vi' ? 'Lưu lại' : 'Save'}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
