'use client';

import { useParams } from 'next/navigation';
import { useI18n } from '@/i18n';
import styles from '@/styles/pages.module.css';

export type PlaceDetail = {
    category_vi: string;
    category_en: string;
    title_vi: string;
    title_en: string;
    subtitle_vi: string;
    subtitle_en: string;
    image: string;
    intro_vi: string;
    intro_en: string;
    context_vi: string;
    context_en: string;
    address_vi: string;
    address_en: string;
    hours_vi: string;
    hours_en: string;
    price_vi: string;
    price_en: string;
    rating: string;
};

export const placeDetails: Record<string, PlaceDetail> = {
    '1': {
        category_vi: 'Văn hóa',
        category_en: 'Culture',
        title_vi: 'Chùa Một Cột',
        title_en: 'One Pillar Pagoda',
        subtitle_vi: 'Ba Đình, Hà Nội',
        subtitle_en: 'Ba Dinh, Hanoi',
        image: '/images/one-pillar-pagoda.png',
        intro_vi: 'Chùa Một Cột là một trong những công trình Phật giáo biểu tượng của Hà Nội, nổi bật với kiến trúc như một bông sen vươn lên từ mặt nước.',
        intro_en: 'One Pillar Pagoda is one of Hanoi’s signature Buddhist landmarks, known for its lotus-inspired structure rising above the water.',
        context_vi: 'Công trình gắn với truyền thuyết thời Lý và thể hiện tinh thần thanh cao, giản dị trong mỹ học Phật giáo Việt Nam.',
        context_en: 'The pagoda is tied to Ly dynasty legend and reflects the purity and restraint of Vietnamese Buddhist aesthetics.',
        address_vi: 'Chùa Một Cột, Ba Đình, Hà Nội',
        address_en: 'One Pillar Pagoda, Ba Dinh, Hanoi',
        hours_vi: '7:00 - 18:00',
        hours_en: '7:00 AM - 6:00 PM',
        price_vi: 'Miễn phí',
        price_en: 'Free',
        rating: '4.8',
    },
    '2': {
        category_vi: 'Ẩm thực',
        category_en: 'Food',
        title_vi: 'Bún chả Hương Liên',
        title_en: 'Bun Cha Huong Lien',
        subtitle_vi: 'Hai Bà Trưng, Hà Nội',
        subtitle_en: 'Hai Ba Trung, Hanoi',
        image: '/images/buncha-huonglien.png',
        intro_vi: 'Bún chả Hương Liên nổi tiếng với phần thịt nướng than hoa, nước chấm chua ngọt và câu chuyện từng đón tiếp nhiều thực khách quốc tế.',
        intro_en: 'Bun Cha Huong Lien is known for charcoal-grilled pork, sweet-sour dipping sauce, and its place in modern Hanoi food lore.',
        context_vi: 'Món bún chả cho thấy nhịp sống ẩm thực Hà Nội: giản dị, đậm mùi khói than và cân bằng giữa rau sống, bún, thịt nướng.',
        context_en: 'Bun cha captures Hanoi’s everyday dining rhythm: simple, smoky, fresh, and balanced between herbs, noodles, and grilled pork.',
        address_vi: '24 Lê Văn Hưu, Hai Bà Trưng, Hà Nội',
        address_en: '24 Le Van Huu, Hai Ba Trung, Hanoi',
        hours_vi: '8:00 - 20:30',
        hours_en: '8:00 AM - 8:30 PM',
        price_vi: 'Từ 60.000đ',
        price_en: 'From VND 60,000',
        rating: '4.7',
    },
    '3': {
        category_vi: 'Thiên nhiên',
        category_en: 'Nature',
        title_vi: 'Vườn quốc gia Phong Nha',
        title_en: 'Phong Nha National Park',
        subtitle_vi: 'Quảng Bình',
        subtitle_en: 'Quang Binh',
        image: '/images/phongnha.png',
        intro_vi: 'Phong Nha là vùng hang động kỳ vĩ với sông ngầm, núi đá vôi và hệ sinh thái rừng nhiệt đới phong phú.',
        intro_en: 'Phong Nha is a dramatic cave region shaped by underground rivers, limestone mountains, and rich tropical forest ecosystems.',
        context_vi: 'Khu vực này là điểm nhấn của dải Trường Sơn, nơi địa chất, sinh học và văn hóa bản địa cùng tạo nên trải nghiệm khám phá đặc biệt.',
        context_en: 'The area is a highlight of the Truong Son range, where geology, biodiversity, and local culture meet in a distinctive travel experience.',
        address_vi: 'Bố Trạch, Quảng Bình',
        address_en: 'Bo Trach, Quang Binh',
        hours_vi: '7:00 - 16:00',
        hours_en: '7:00 AM - 4:00 PM',
        price_vi: 'Tùy tuyến tham quan',
        price_en: 'Varies by tour route',
        rating: '4.9',
    },
    '4': {
        category_vi: 'Lịch sử',
        category_en: 'History',
        title_vi: 'Hoàng thành Thăng Long',
        title_en: 'Imperial Citadel of Thang Long',
        subtitle_vi: 'Ba Đình, Hà Nội',
        subtitle_en: 'Ba Dinh, Hanoi',
        image: '/images/citadel-thanglong.png',
        intro_vi: 'Hoàng thành Thăng Long là di sản văn hóa thế giới, từng là trung tâm quyền lực qua nhiều triều đại Việt Nam.',
        intro_en: 'The Imperial Citadel of Thang Long is a World Heritage site and former center of power across multiple Vietnamese dynasties.',
        context_vi: 'Các lớp di tích khảo cổ tại đây giúp đọc lại lịch sử kinh đô Thăng Long qua kiến trúc, gạch ngói và nền móng cung điện.',
        context_en: 'Its archaeological layers reveal the history of Thang Long through architecture, tiles, foundations, and royal urban planning.',
        address_vi: '19C Hoàng Diệu, Ba Đình, Hà Nội',
        address_en: '19C Hoang Dieu, Ba Dinh, Hanoi',
        hours_vi: '8:00 - 17:00',
        hours_en: '8:00 AM - 5:00 PM',
        price_vi: 'Từ 30.000đ',
        price_en: 'From VND 30,000',
        rating: '4.6',
    },
    '5': {
        category_vi: 'Ẩm thực',
        category_en: 'Food',
        title_vi: 'Bánh mì Phượng',
        title_en: 'Banh Mi Phuong',
        subtitle_vi: 'Hội An, Quảng Nam',
        subtitle_en: 'Hoi An, Quang Nam',
        image: '/images/banhmi-phuong.png',
        intro_vi: 'Bánh mì Phượng là điểm ăn quen thuộc ở Hội An, nổi bật bởi ổ bánh giòn, nhân phong phú và nước sốt đậm vị.',
        intro_en: 'Banh Mi Phuong is a familiar Hoi An food stop, known for crisp bread, generous fillings, and deeply flavored sauces.',
        context_vi: 'Ổ bánh mì Hội An thể hiện sự giao thoa giữa kỹ thuật bánh phương Tây và khẩu vị Việt qua rau thơm, pate, thịt và nước sốt.',
        context_en: 'Hoi An banh mi reflects a blend of Western bread technique and Vietnamese taste through herbs, pate, meats, and sauces.',
        address_vi: '2B Phan Châu Trinh, Hội An',
        address_en: '2B Phan Chau Trinh, Hoi An',
        hours_vi: '6:30 - 21:30',
        hours_en: '6:30 AM - 9:30 PM',
        price_vi: 'Từ 30.000đ',
        price_en: 'From VND 30,000',
        rating: '4.8',
    },
    '6': {
        category_vi: 'Giải trí đêm',
        category_en: 'Nightlife',
        title_vi: 'Phố Tây Bùi Viện',
        title_en: 'Bui Vien Walking Street',
        subtitle_vi: 'Quận 1, TP.HCM',
        subtitle_en: 'District 1, Ho Chi Minh City',
        image: '/images/buivien.png',
        intro_vi: 'Bùi Viện là tuyến phố đêm sôi động với âm nhạc, hàng quán và nhịp giao lưu đa văn hóa giữa trung tâm Sài Gòn.',
        intro_en: 'Bui Vien is a lively night street filled with music, food spots, and multicultural energy in central Saigon.',
        context_vi: 'Không gian này đại diện cho mặt đô thị trẻ của TP.HCM: náo nhiệt, cởi mở và luôn chuyển động về đêm.',
        context_en: 'It represents the youthful urban side of Ho Chi Minh City: energetic, open, and constantly moving after dark.',
        address_vi: 'Bùi Viện, Phường Phạm Ngũ Lão, Quận 1',
        address_en: 'Bui Vien, Pham Ngu Lao Ward, District 1',
        hours_vi: '18:00 - khuya',
        hours_en: '6:00 PM - late',
        price_vi: 'Tùy địa điểm',
        price_en: 'Varies by venue',
        rating: '4.3',
    },
    'hoi-an': {
        category_vi: 'Văn hóa',
        category_en: 'Culture',
        title_vi: 'Hội An - Phố cổ ngàn năm',
        title_en: 'Hoi An - Ancient Town of a Thousand Years',
        subtitle_vi: 'Quảng Nam',
        subtitle_en: 'Quang Nam',
        image: '/images/hoian-lanterns.png',
        intro_vi: 'Hội An lưu giữ vẻ đẹp của một thương cảng cổ với nhà mái ngói, hội quán, phố đèn lồng và nhịp sống chậm ven sông Hoài.',
        intro_en: 'Hoi An preserves the charm of an old trading port with tiled houses, assembly halls, lantern streets, and a slow riverside rhythm.',
        context_vi: 'Sự giao thoa Việt, Hoa, Nhật và phương Tây tạo nên bản sắc đô thị đặc biệt, rõ nhất trong kiến trúc, ẩm thực và lễ hội ánh sáng.',
        context_en: 'Vietnamese, Chinese, Japanese, and Western influences shape its architecture, cuisine, and lantern-lit festival culture.',
        address_vi: 'Phố cổ Hội An, Quảng Nam',
        address_en: 'Hoi An Ancient Town, Quang Nam',
        hours_vi: 'Cả ngày',
        hours_en: 'All day',
        price_vi: 'Tùy điểm tham quan',
        price_en: 'Varies by attraction',
        rating: '4.9',
    },
    'pho-ha-noi': {
        category_vi: 'Ẩm thực',
        category_en: 'Food',
        title_vi: 'Phở Hà Nội - Tinh hoa ẩm thực Việt',
        title_en: 'Hanoi Pho - The Essence of Vietnamese Cuisine',
        subtitle_vi: 'Hà Nội',
        subtitle_en: 'Hanoi',
        image: '/images/hanoi-pho.png',
        intro_vi: 'Phở Hà Nội hấp dẫn bởi nước dùng trong, thơm mùi quế hồi, bánh phở mềm và lát thịt bò được chần vừa tới.',
        intro_en: 'Hanoi pho is loved for its clear broth, warm spice aroma, soft rice noodles, and thin slices of tender beef.',
        context_vi: 'Tô phở phản ánh sự tinh tế của ẩm thực Hà Nội: ít thành phần nhưng chú trọng độ trong, độ ngọt và mùi hương của nước dùng.',
        context_en: 'A bowl of pho reflects Hanoi’s culinary restraint: few components, but careful attention to clarity, sweetness, and aroma.',
        address_vi: 'Các quán phở truyền thống tại Hà Nội',
        address_en: 'Traditional pho shops across Hanoi',
        hours_vi: 'Sáng sớm - trưa',
        hours_en: 'Early morning - noon',
        price_vi: 'Từ 45.000đ',
        price_en: 'From VND 45,000',
        rating: '4.8',
    },
    'ruong-bac-thang': {
        category_vi: 'Thiên nhiên',
        category_en: 'Nature',
        title_vi: 'Ruộng bậc thang Mù Cang Chải',
        title_en: 'Mu Cang Chai Terraced Rice Fields',
        subtitle_vi: 'Yên Bái',
        subtitle_en: 'Yen Bai',
        image: '/images/terraced-rice-fields.png',
        intro_vi: 'Ruộng bậc thang Mù Cang Chải uốn theo triền núi, đẹp nhất vào mùa nước đổ và mùa lúa chín vàng.',
        intro_en: 'Mu Cang Chai’s terraced fields curve along mountain slopes, at their best during the watering season and golden harvest.',
        context_vi: 'Những thửa ruộng là kết quả của tri thức canh tác vùng cao, vừa thích nghi địa hình vừa tạo nên cảnh quan văn hóa đặc sắc.',
        context_en: 'The terraces come from highland farming knowledge, adapting to steep terrain while creating a remarkable cultural landscape.',
        address_vi: 'Mù Cang Chải, Yên Bái',
        address_en: 'Mu Cang Chai, Yen Bai',
        hours_vi: 'Cả ngày',
        hours_en: 'All day',
        price_vi: 'Miễn phí',
        price_en: 'Free',
        rating: '4.9',
    },
    'mua-roi-nuoc': {
        category_vi: 'Văn hóa',
        category_en: 'Culture',
        title_vi: 'Múa rối nước - Nghệ thuật độc đáo',
        title_en: 'Water Puppetry - A Unique Art Form',
        subtitle_vi: 'Hà Nội',
        subtitle_en: 'Hanoi',
        image: '/images/water-puppet.png',
        intro_vi: 'Múa rối nước là nghệ thuật sân khấu dân gian biểu diễn trên mặt nước, kết hợp rối gỗ, âm nhạc và tích truyện làng quê.',
        intro_en: 'Water puppetry is a folk stage art performed on water, blending wooden puppets, live music, and village stories.',
        context_vi: 'Loại hình này ra đời từ văn minh lúa nước Bắc Bộ, nơi ao làng và mùa vụ trở thành chất liệu sân khấu.',
        context_en: 'The art grew from the wet-rice culture of northern Vietnam, turning village ponds and farming life into performance material.',
        address_vi: 'Các nhà hát múa rối nước tại Hà Nội',
        address_en: 'Water puppet theaters in Hanoi',
        hours_vi: 'Theo lịch diễn',
        hours_en: 'By show schedule',
        price_vi: 'Từ 100.000đ',
        price_en: 'From VND 100,000',
        rating: '4.7',
    },
    'vinh-ha-long': {
        category_vi: 'Thiên nhiên',
        category_en: 'Nature',
        title_vi: 'Vịnh Hạ Long - Kỳ quan thiên nhiên',
        title_en: 'Ha Long Bay - Natural Wonder',
        subtitle_vi: 'Quảng Ninh',
        subtitle_en: 'Quang Ninh',
        image: '/images/halong-bay.png',
        intro_vi: 'Vịnh Hạ Long nổi bật với hàng nghìn đảo đá vôi, mặt nước xanh và các hang động tạo nên cảnh quan biển đảo đặc trưng Việt Nam.',
        intro_en: 'Ha Long Bay is defined by thousands of limestone islands, green water, and caves that create Vietnam’s iconic seascape.',
        context_vi: 'Cảnh quan karst trên biển cùng truyền thuyết rồng đáp xuống đã khiến Hạ Long trở thành biểu tượng du lịch miền Bắc.',
        context_en: 'Its sea karst landscape and dragon legend have made Ha Long one of northern Vietnam’s defining travel symbols.',
        address_vi: 'Vịnh Hạ Long, Quảng Ninh',
        address_en: 'Ha Long Bay, Quang Ninh',
        hours_vi: 'Theo tour tàu',
        hours_en: 'By cruise schedule',
        price_vi: 'Tùy tuyến tham quan',
        price_en: 'Varies by cruise route',
        rating: '4.9',
    },
    'ca-phe-trung': {
        category_vi: 'Ẩm thực',
        category_en: 'Food',
        title_vi: 'Cà phê trứng Hà Nội',
        title_en: 'Hanoi Egg Coffee',
        subtitle_vi: 'Hà Nội',
        subtitle_en: 'Hanoi',
        image: '/images/egg-coffee.png',
        intro_vi: 'Cà phê trứng kết hợp cà phê đậm với lớp kem trứng béo mịn, tạo nên thức uống đặc trưng của Hà Nội.',
        intro_en: 'Egg coffee combines strong Vietnamese coffee with a smooth egg cream layer, creating a signature Hanoi drink.',
        context_vi: 'Món uống này ra đời từ sự linh hoạt trong thời kỳ thiếu sữa, rồi trở thành trải nghiệm ẩm thực gắn với các quán cà phê phố cổ.',
        context_en: 'The drink began as a clever response to milk scarcity and became a culinary experience tied to old-quarter cafes.',
        address_vi: 'Các quán cà phê trứng tại phố cổ Hà Nội',
        address_en: 'Egg coffee shops in Hanoi Old Quarter',
        hours_vi: '7:00 - 22:00',
        hours_en: '7:00 AM - 10:00 PM',
        price_vi: 'Từ 35.000đ',
        price_en: 'From VND 35,000',
        rating: '4.8',
    },
};

export function PlaceDetailPage() {
    const { locale } = useI18n();
    const params = useParams<{ id: string }>();
    const place = placeDetails[params.id] ?? placeDetails['1'];

    return (
        <div className={styles.pageContainer} id="place-detail-page">
            <div className={`${styles.detailHero} ${styles.detailHeroImage}`}>
                <img
                    src={place.image}
                    alt={locale === 'vi' ? place.title_vi : place.title_en}
                />
            </div>

            <div className={styles.pageHeader}>
                <div className={styles.pageTag}>
                    📍 {locale === 'vi' ? place.category_vi : place.category_en}
                </div>
                <h1 className={styles.pageTitle}>
                    {locale === 'vi' ? place.title_vi : place.title_en}
                </h1>
                <p className={styles.pageSubtitle}>
                    {locale === 'vi' ? place.subtitle_vi : place.subtitle_en}
                </p>
            </div>

            <div className={styles.detailContent}>
                <div className={styles.detailMain}>
                    <h2>{locale === 'vi' ? 'Giới thiệu' : 'Introduction'}</h2>
                    <p>{locale === 'vi' ? place.intro_vi : place.intro_en}</p>

                    <h2>{locale === 'vi' ? 'Bối cảnh văn hóa' : 'Cultural Context'}</h2>
                    <p>{locale === 'vi' ? place.context_vi : place.context_en}</p>

                    <div className={styles.eventPhoto}>
                        <img
                            src={place.image}
                            alt={locale === 'vi' ? place.title_vi : place.title_en}
                            loading="lazy"
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
                            🕐 {locale === 'vi' ? place.hours_vi : place.hours_en}
                        </div>
                        <div className={styles.sidebarItem}>
                            💰 {locale === 'vi' ? place.price_vi : place.price_en}
                        </div>
                        <div className={styles.sidebarItem}>⭐ {place.rating}/5</div>
                    </div>

                    <div className={styles.sidebarCard}>
                        <h3>{locale === 'vi' ? 'Hành động' : 'Actions'}</h3>
                        <div className={styles.sidebarItem}>
                            🗺️ {locale === 'vi' ? 'Chỉ đường' : 'Get Directions'}
                        </div>
                        <div className={styles.sidebarItem}>
                            📤 {locale === 'vi' ? 'Chia sẻ' : 'Share'}
                        </div>
                        <div className={styles.sidebarItem}>
                            💾 {locale === 'vi' ? 'Lưu lại' : 'Save'}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
