'use client';

import React from 'react';
import { useParams } from 'next/navigation';
import { useI18n } from '@/i18n';
import { IconCalendar, IconLink } from '@/components/icons/UiIcons';
import styles from '@/styles/pages.module.css';

const mockEvents = [
    {
        id: 1,
        month: 'May',
        day: '15',
        title_vi: 'Lễ hội Áo dài TP.HCM 2026',
        title_en: 'HCMC Ao Dai Festival 2026',
        desc_vi: 'Lễ hội tôn vinh trang phục truyền thống Việt Nam với trình diễn thời trang và văn hóa.',
        desc_en: 'Festival celebrating traditional Vietnamese dress with fashion shows and cultural events.',
        location: 'TP.HCM',
        type_vi: 'Đang diễn ra',
        type_en: 'Ongoing',
        free: true,
        image: '/images/event-aodai.png',
        date: '15-20 May 2026',
        time: '18:00 - 22:00',
        venue_vi: 'Phố đi bộ Nguyễn Huệ',
        venue_en: 'Nguyen Hue Walking Street',
        about_vi: 'Lễ hội Áo dài TP.HCM 2026 là sự kiện văn hóa thường niên tôn vinh vẻ đẹp của trang phục truyền thống Việt Nam. Chương trình bao gồm trình diễn thời trang, triển lãm áo dài qua các thời kỳ, workshop thiết kế và nhiều hoạt động nghệ thuật phong phú. Sự kiện quy tụ hơn 100 nhà thiết kế và hàng ngàn người tham dự từ khắp cả nước và quốc tế. Đây là cơ hội tuyệt vời để trải nghiệm và tìm hiểu về văn hóa Việt Nam.',
        about_en: 'HCMC Ao Dai Festival 2026 is an annual cultural event celebrating the beauty of traditional Vietnamese dress. The program includes fashion shows, exhibitions of ao dai through the ages, design workshops, and various art activities. The event gathers over 100 designers and thousands of attendees from across the country and internationally. It\'s a wonderful opportunity to experience and learn about Vietnamese culture.'
    },
    {
        id: 2,
        month: 'Jun',
        day: '01',
        title_vi: 'Festival Huế 2026',
        title_en: 'Hue Festival 2026',
        desc_vi: 'Lễ hội văn hóa quốc tế lớn nhất miền Trung, tổ chức 2 năm một lần tại cố đô Huế.',
        desc_en: 'The largest international cultural festival in Central Vietnam, held biennially in the ancient capital.',
        location: 'Huế',
        type_vi: 'Sắp diễn ra',
        type_en: 'Upcoming',
        free: false,
        image: '/images/event-hue.png',
        date: '01-08 June 2026',
        time: '08:00 - 23:00',
        venue_vi: 'Đại Nội Huế và các khu vực lân cận',
        venue_en: 'Hue Imperial Citadel and surrounding areas',
        about_vi: 'Festival Huế 2026 quy tụ các đoàn nghệ thuật đặc sắc từ khắp nơi trên thế giới và Việt Nam. Điểm nhấn là các chương trình biểu diễn nghệ thuật cung đình, lễ hội áo dài, lễ hội đường phố sôi động và các triển lãm di sản độc đáo, mang đến không gian văn hóa đặc sắc.',
        about_en: 'Festival Hue 2026 brings together outstanding art troupes from around the world and Vietnam. Highlights include imperial art performances, ao dai festivals, vibrant street festivals, and unique heritage exhibitions, creating a grand cultural atmosphere.'
    },
    {
        id: 3,
        month: 'Jun',
        day: '10',
        title_vi: 'Đêm nhạc Jazz Sài Gòn',
        title_en: 'Saigon Jazz Night',
        desc_vi: 'Đêm nhạc Jazz với sự tham gia của nghệ sĩ quốc tế và Việt Nam tại Nhà hát Thành phố.',
        desc_en: 'Jazz night featuring international and Vietnamese artists at the City Opera House.',
        location: 'TP.HCM',
        type_vi: 'Sắp diễn ra',
        type_en: 'Upcoming',
        free: false,
        image: '/images/event-jazz.png',
        date: '10 June 2026',
        time: '20:00 - 22:30',
        venue_vi: 'Nhà hát Thành phố',
        venue_en: 'Saigon Opera House',
        about_vi: 'Đêm nhạc Jazz đặc biệt mang đến những bản tình ca lãng mạn và những giai điệu ngẫu hứng đỉnh cao. Sự giao thoa giữa kèn saxophone huyền ảo, tiếng dương cầm réo rắt cùng giọng hát truyền cảm của các ca sĩ nổi tiếng sẽ mang lại trải nghiệm âm nhạc không thể nào quên.',
        about_en: 'A special Jazz night delivering romantic ballads and top-tier improvisations. The fusion of soulful saxophone, crisp piano chords, and expressive vocals from renowned artists will create an unforgettable musical journey.'
    },
    {
        id: 4,
        month: 'Jun',
        day: '20',
        title_vi: 'Chợ phiên ẩm thực đường phố',
        title_en: 'Street Food Market Fair',
        desc_vi: 'Tập hợp hơn 50 gian hàng ẩm thực đường phố từ khắp Việt Nam tại công viên 23/9.',
        desc_en: 'Over 50 street food stalls from all over Vietnam gathered at September 23rd Park.',
        location: 'TP.HCM',
        type_vi: 'Sắp diễn ra',
        type_en: 'Upcoming',
        free: true,
        image: '/images/event-streetfood.png',
        date: '20-22 June 2026',
        time: '16:00 - 23:00',
        venue_vi: 'Công viên 23 tháng 9',
        venue_en: 'September 23rd Park',
        about_vi: 'Chợ phiên ẩm thực quy tụ những món ăn đường phố đặc sắc từ khắp ba miền Bắc - Trung - Nam. Từ bánh mì giòn rụm, xiên nướng thơm phức đến các món chè ngọt mát, tất cả đều được chế biến trực tiếp bởi các nghệ nhân ẩm thực đường phố trong không gian âm nhạc sôi động.',
        about_en: 'The culinary fair gathers unique street dishes from all three regions of Vietnam. From crispy banh mi and aromatic grilled skewers to refreshing sweet soups, everything is freshly prepared by street food masters amidst a lively musical background.'
    },
    {
        id: 5,
        month: 'Jul',
        day: '05',
        title_vi: 'Triển lãm nghệ thuật đương đại',
        title_en: 'Contemporary Art Exhibition',
        desc_vi: 'Triển lãm tranh và điêu khắc của các nghệ sĩ trẻ Việt Nam tại Bảo tàng Mỹ thuật.',
        desc_en: 'Painting and sculpture exhibition by young Vietnamese artists at the Fine Arts Museum.',
        location: 'Hà Nội',
        type_vi: 'Sắp diễn ra',
        type_en: 'Upcoming',
        free: true,
        image: '/images/event-art.png',
        date: '05-20 July 2026',
        time: '09:00 - 17:00',
        venue_vi: 'Bảo tàng Mỹ thuật Việt Nam',
        venue_en: 'Vietnam National Museum of Fine Arts',
        about_vi: 'Triển lãm nghệ thuật đương đại trưng bày các tác phẩm đột phá mang đậm hơi thở thời đại từ các nghệ sĩ trẻ tài năng của Việt Nam. Đây là cầu nối giữa nghệ thuật truyền thống và xu hướng sáng tạo hiện đại, truyền tải nhiều thông điệp sâu sắc về cuộc sống.',
        about_en: 'The contemporary art exhibition showcases groundbreaking artworks that capture the spirit of modern times by talented young Vietnamese artists. It acts as a bridge between traditional art and modern creative trends, conveying profound life messages.'
    },
];

export function EventDetailPage() {
    const { locale, t } = useI18n();
    const params = useParams();
    const id = params?.id ? Number(params.id) : 1;
    const event = mockEvents.find((e) => e.id === id) || mockEvents[0];

    return (
        <div className={styles.pageContainer} id="event-detail-page">
            <div className={styles.detailHero} style={{ opacity: 1, overflow: 'hidden', padding: 0 }}>
                {event.image ? (
                    <img 
                        src={event.image} 
                        alt={locale === 'vi' ? event.title_vi : event.title_en}
                        style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    />
                ) : (
                    '🎉'
                )}
            </div>

            <div className={styles.pageHeader}>
                <div className={styles.pageTag}>🎉 {locale === 'vi' ? 'Sự kiện' : 'Event'}</div>
                <h1 className={styles.pageTitle}>
                    {locale === 'vi' ? event.title_vi : event.title_en}
                </h1>
                <p className={styles.pageSubtitle}>
                    {locale === 'vi' ? event.location : event.location}
                </p>
            </div>

            <div className={styles.detailContent}>
                <div className={styles.detailMain}>
                    <h2>{locale === 'vi' ? 'Về sự kiện' : 'About the Event'}</h2>
                    <p>
                        {locale === 'vi' ? event.about_vi : event.about_en}
                    </p>

                    <h2 style={{ marginTop: 'var(--space-8)' }}>{locale === 'vi' ? 'Hình ảnh sự kiện' : 'Event Photos'}</h2>
                    <div style={{ borderRadius: 'var(--radius-xl)', overflow: 'hidden', border: '1px solid var(--border-primary)' }}>
                        <img 
                            src={event.image} 
                            alt={locale === 'vi' ? event.title_vi : event.title_en} 
                            style={{ width: '100%', height: 'auto', display: 'block' }}
                        />
                    </div>
                </div>

                <div className={styles.detailSidebar}>
                    <div className={styles.sidebarCard}>
                        <h3>{locale === 'vi' ? 'Thông tin' : 'Details'}</h3>
                        <div className={styles.sidebarItem}>📅 {event.date}</div>
                        <div className={styles.sidebarItem}>
                            📍 {locale === 'vi' ? event.venue_vi : event.venue_en}
                        </div>
                        <div className={styles.sidebarItem}>🕐 {event.time}</div>
                        <div className={styles.sidebarItem}>
                            💰 {event.free ? (locale === 'vi' ? 'Miễn phí' : 'Free admission') : (locale === 'vi' ? 'Có phí' : 'Paid ticket')}
                        </div>
                    </div>

                    <div className={styles.sidebarCard}>
                        <h3>{locale === 'vi' ? 'Chia sẻ' : 'Share'}</h3>
                        <div className={styles.sidebarItem} style={{ cursor: 'pointer' }}>
                            <span className={styles.sidebarIcon} aria-hidden="true">
                                <IconLink />
                            </span>
                            {locale === 'vi' ? 'Sao chép liên kết' : 'Copy link'}
                        </div>
                        <div className={styles.sidebarItem} style={{ cursor: 'pointer' }}>
                            <span className={styles.sidebarIcon} aria-hidden="true">
                                <IconCalendar />
                            </span>
                            {locale === 'vi' ? 'Thêm vào lịch' : 'Add to calendar'}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
