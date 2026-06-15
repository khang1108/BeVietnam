'use client';

import { useRef, useState } from 'react';
import { useI18n } from '@/i18n';
import { IconFood, IconMountain, IconSparkle } from '@/components/icons/UiIcons';
import styles from '@/styles/pages.module.css';
import { placeDetails } from './PlaceDetailPage';

const categories = [
    { key: 'all', icon: <IconSparkle className={styles.filterIconSvg} /> },
    { key: 'food', icon: <IconFood className={styles.filterIconSvg} /> },
    { key: 'culture', icon: '🏛️' },
    { key: 'nature', icon: <IconMountain className={styles.filterIconSvg} /> },
    { key: 'history', icon: '📜' },
    { key: 'nightlife', icon: '🌙' },
];

const mockPlaces = [
    {
        id: 1,
        icon: '🏛️',
        category: 'culture',
        image: '/images/one-pillar-pagoda.png',
        title_vi: 'Chùa Một Cột',
        title_en: 'One Pillar Pagoda',
        desc_vi: 'Ngôi chùa có kiến trúc độc đáo nhất Việt Nam, xây dựng năm 1049.',
        desc_en: "Vietnam's most uniquely designed pagoda, built in 1049.",
        location: 'Hà Nội',
        rating: '4.8',
    },
    {
        id: 2,
        icon: <IconFood className={styles.cardIconSvg} />,
        category: 'food',
        image: '/images/buncha-huonglien.png',
        title_vi: 'Bún chả Hương Liên',
        title_en: 'Bun Cha Huong Lien',
        desc_vi: 'Nơi Obama từng ghé thăm, nổi tiếng với bún chả Hà Nội chính gốc.',
        desc_en: 'Where Obama visited, famous for authentic Hanoi bun cha.',
        location: 'Hà Nội',
        rating: '4.7',
    },
    {
        id: 3,
        icon: <IconMountain className={styles.cardIconSvg} />,
        category: 'nature',
        image: '/images/phongnha.png',
        title_vi: 'Vườn quốc gia Phong Nha',
        title_en: 'Phong Nha National Park',
        desc_vi: 'Hệ thống hang động kỳ vĩ nhất thế giới, di sản UNESCO.',
        desc_en: "World's most spectacular cave system, UNESCO heritage.",
        location: 'Quảng Bình',
        rating: '4.9',
    },
    {
        id: 4,
        icon: '📜',
        category: 'history',
        image: '/images/citadel-thanglong.png',
        title_vi: 'Hoàng thành Thăng Long',
        title_en: 'Imperial Citadel of Thang Long',
        desc_vi: 'Di sản văn hóa thế giới, trung tâm quyền lực qua nhiều triều đại.',
        desc_en: 'World cultural heritage, center of power through many dynasties.',
        location: 'Hà Nội',
        rating: '4.6',
    },
    {
        id: 5,
        icon: <IconFood className={styles.cardIconSvg} />,
        category: 'food',
        image: '/images/banhmi-phuong.png',
        title_vi: 'Bánh mì Phượng',
        title_en: 'Banh Mi Phuong',
        desc_vi: 'Bánh mì Hội An nổi tiếng, được bình chọn ngon nhất thế giới.',
        desc_en: 'Famous Hoi An banh mi, voted the best in the world.',
        location: 'Hội An',
        rating: '4.8',
    },
    {
        id: 6,
        icon: '🌙',
        category: 'nightlife',
        image: '/images/buivien.png',
        title_vi: 'Phố Tây Bùi Viện',
        title_en: 'Bui Vien Walking Street',
        desc_vi: 'Con phố sôi động nhất Sài Gòn về đêm với đa dạng giải trí.',
        desc_en: "Saigon's most vibrant night street with diverse entertainment.",
        location: 'TP.HCM',
        rating: '4.3',
    },
];

export function ExplorePage() {
    const { t, locale } = useI18n();
    const [selectedPlaceId, setSelectedPlaceId] = useState<string | null>(null);
    const detailCardRef = useRef<HTMLDivElement>(null);
    const selectedPlace = selectedPlaceId ? placeDetails[selectedPlaceId] : null;

    function handleExplorePlace(placeId: number) {
        setSelectedPlaceId(String(placeId));
        window.setTimeout(() => {
            detailCardRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }, 0);
    }

    return (
        <div className={styles.pageContainer} id="explore-page">
            <div className={styles.pageHeader}>
                <div className={styles.pageTag}>🗺️ {t('nav.explore')}</div>
                <h1 className={styles.pageTitle}>{t('explore.title')}</h1>
                <p className={styles.pageSubtitle}>{t('explore.subtitle')}</p>
            </div>

            {/* Search */}
            <div className={styles.searchBar} id="explore-search">
                <span className={styles.searchIcon}>🔍</span>
                <input
                    type="text"
                    className={styles.searchInput}
                    placeholder={t('explore.searchPlaceholder')}
                />
            </div>

            {/* Filters */}
            <div className={styles.filterRow}>
                {categories.map((cat) => (
                    <button
                        key={cat.key}
                        className={`${styles.filterPill} ${cat.key === 'all' ? styles.filterPillActive : ''}`}
                    >
                        {cat.icon} {t(`explore.categories.${cat.key}`)}
                    </button>
                ))}
            </div>

            {/* Map Placeholder */}
            <div className={styles.mapPlaceholder} id="explore-map">
                <span className={styles.mapPlaceholderIcon}>🗺️</span>
                <span className={styles.mapPlaceholderText}>
                    {locale === 'vi'
                        ? 'Google Maps sẽ hiển thị ở đây'
                        : 'Google Maps will be displayed here'}
                </span>
                <span className={styles.placeholderBadge}>
                    🔌 {locale === 'vi' ? 'Sẵn sàng tích hợp API' : 'Ready for API integration'}
                </span>
            </div>

            {/* Places Grid */}
            <div className={styles.cardsGrid}>
                {mockPlaces.map((place) => (
                    <article
                        key={place.id}
                        className={styles.card}
                    >
                        <div className={styles.cardImage}>
                            {place.image ? (
                                <img
                                    src={place.image}
                                    alt={locale === 'vi' ? place.title_vi : place.title_en}
                                    loading="lazy"
                                />
                            ) : (
                                place.icon
                            )}
                        </div>
                        <div className={styles.cardBody}>
                            <div className={styles.cardCategory}>
                                {t(`explore.categories.${place.category}`)}
                            </div>
                            <h3 className={styles.cardTitle}>
                                {locale === 'vi' ? place.title_vi : place.title_en}
                            </h3>
                            <p className={styles.cardDesc}>
                                {locale === 'vi' ? place.desc_vi : place.desc_en}
                            </p>
                            <div className={styles.cardMeta}>
                                <span>📍 {place.location}</span>
                                <span>⭐ {place.rating}</span>
                            </div>
                            <button
                                type="button"
                                className={styles.cardActionButton}
                                onClick={() => handleExplorePlace(place.id)}
                            >
                                {t('nav.explore')}
                            </button>
                        </div>
                    </article>
                ))}
            </div>

            {selectedPlace && (
                <section ref={detailCardRef} className={`${styles.placeDetailCard} ${styles.placeDetailInlineCard}`}>
                    <button
                        type="button"
                        className={styles.placeDetailClose}
                        onClick={() => setSelectedPlaceId(null)}
                        aria-label={t('common.close')}
                    >
                        ×
                    </button>
                    <div className={styles.placeDetailImage}>
                        <img
                            src={selectedPlace.image}
                            alt={locale === 'vi' ? selectedPlace.title_vi : selectedPlace.title_en}
                        />
                    </div>
                    <div className={styles.placeDetailBody}>
                        <div className={styles.cardCategory}>
                            {locale === 'vi' ? selectedPlace.category_vi : selectedPlace.category_en}
                        </div>
                        <h2 className={styles.placeDetailTitle}>
                            {locale === 'vi' ? selectedPlace.title_vi : selectedPlace.title_en}
                        </h2>
                        <p className={styles.placeDetailSubtitle}>
                            {locale === 'vi' ? selectedPlace.subtitle_vi : selectedPlace.subtitle_en}
                        </p>
                        <p className={styles.placeDetailText}>
                            {locale === 'vi' ? selectedPlace.intro_vi : selectedPlace.intro_en}
                        </p>
                        <div className={styles.placeDetailInfoGrid}>
                            <div>📍 {locale === 'vi' ? selectedPlace.address_vi : selectedPlace.address_en}</div>
                            <div>🕐 {locale === 'vi' ? selectedPlace.hours_vi : selectedPlace.hours_en}</div>
                            <div>💰 {locale === 'vi' ? selectedPlace.price_vi : selectedPlace.price_en}</div>
                            <div>⭐ {selectedPlace.rating}/5</div>
                        </div>
                        <div className={styles.placeDetailContext}>
                            <strong>{t('place.culturalContext')}</strong>
                            <p>{locale === 'vi' ? selectedPlace.context_vi : selectedPlace.context_en}</p>
                        </div>
                    </div>
                </section>
            )}
        </div>
    );
}
