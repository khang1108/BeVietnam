'use client';

import Link from 'next/link';
import { useI18n } from '@/i18n';
import { IconCalendar } from '@/components/icons/UiIcons';
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
    },
];

const filters = [
    { key: 'all', label_vi: 'Tất cả', label_en: 'All' },
    { key: 'ongoing', label_vi: 'Đang diễn ra', label_en: 'Ongoing' },
    { key: 'upcoming', label_vi: 'Sắp diễn ra', label_en: 'Upcoming' },
    { key: 'free', label_vi: 'Miễn phí', label_en: 'Free' },
];

export function EventsPage() {
    const { t, locale } = useI18n();

    return (
        <div className={styles.pageContainer} id="events-page">
            <div className={styles.pageHeader}>
                <div className={styles.pageTag}>
                    <span className={styles.pageTagIcon} aria-hidden="true">
                        <IconCalendar />
                    </span>
                    {t('nav.events')}
                </div>
                <h1 className={styles.pageTitle}>{t('events.title')}</h1>
                <p className={styles.pageSubtitle}>{t('events.subtitle')}</p>
            </div>

            {/* Filters */}
            <div className={styles.filterRow}>
                {filters.map((f) => (
                    <button
                        key={f.key}
                        className={`${styles.filterPill} ${f.key === 'all' ? styles.filterPillActive : ''}`}
                    >
                        {locale === 'vi' ? f.label_vi : f.label_en}
                    </button>
                ))}
            </div>

            {/* Events List */}
            <div className={styles.eventList}>
                {mockEvents.map((event) => (
                    <Link key={event.id} href={`/event/${event.id}`} className={styles.eventCard}>
                        {event.image && (
                            <div style={{ width: '80px', height: '80px', borderRadius: 'var(--radius-lg)', overflow: 'hidden', flexShrink: 0 }}>
                                <img src={event.image} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                            </div>
                        )}
                        <div className={styles.eventDate}>
                            <span className={styles.eventDateMonth}>{event.month}</span>
                            <span className={styles.eventDateDay}>{event.day}</span>
                        </div>
                        <div className={styles.eventInfo}>
                            <h3 className={styles.eventTitle}>
                                {locale === 'vi' ? event.title_vi : event.title_en}
                            </h3>
                            <p className={styles.eventDesc}>
                                {locale === 'vi' ? event.desc_vi : event.desc_en}
                            </p>
                            <div className={styles.eventMeta}>
                                <span>📍 {event.location}</span>
                                <span>
                                    🏷️ {locale === 'vi' ? event.type_vi : event.type_en}
                                </span>
                                {event.free && <span>🎫 {t('events.free')}</span>}
                            </div>
                        </div>
                        <div className={styles.eventBadge}>
                            {locale === 'vi' ? event.type_vi : event.type_en}
                        </div>
                    </Link>
                ))}
            </div>
        </div>
    );
}
