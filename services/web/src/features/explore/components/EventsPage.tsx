'use client';

import { useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import { IconCalendar } from '@/components/icons/UiIcons';
import { useAuth } from '@/hooks/useAuth';
import { useI18n } from '@/i18n';
import { feedApi } from '@/lib/api-client';
import type { FeedItem } from '@/lib/types';
import styles from '@/styles/pages.module.css';

type FeedState = {
    userId: string | null;
    items: FeedItem[];
    error: string | null;
};

const filters = [
    { key: 'all', label_vi: 'Tất cả', label_en: 'All' },
    { key: 'history', label_vi: 'Lịch sử', label_en: 'History' },
    { key: 'culture', label_vi: 'Văn hóa', label_en: 'Culture' },
    { key: 'nature', label_vi: 'Thiên nhiên', label_en: 'Nature' },
    { key: 'food', label_vi: 'Ẩm thực', label_en: 'Food' },
];

const categoryAliases: Record<string, string> = {
    architecture: 'history',
    art: 'culture',
    culture: 'culture',
    'di-san': 'culture',
    'di-san-the-gioi': 'culture',
    'di-tich': 'history',
    'di-tich-lịch-su': 'history',
    'di-tích': 'history',
    history: 'history',
    'hương-trầm': 'culture',
    huong_tram: 'culture',
    huế: 'culture',
    'kien-truc-lich-su': 'history',
    'lễ_hội': 'culture',
    'lịch-sử': 'history',
    nature: 'nature',
    religion: 'culture',
    'thuc-an': 'food',
    'thần_thánh': 'culture',
    'tín_ngưỡng': 'culture',
    'van-hoa': 'culture',
    'văn-hóa': 'culture',
    'văn-hóa-huế': 'culture',
    'ẩm-thực': 'food',
};

function normalizeCategory(category: string) {
    return categoryAliases[category.toLowerCase()] ?? category.toLowerCase();
}

function categoryLabel(category: string, locale: 'vi' | 'en') {
    const normalized = normalizeCategory(category);
    const match = filters.find((f) => f.key === normalized);
    if (match) return locale === 'vi' ? match.label_vi : match.label_en;
    return category.replaceAll('-', ' ');
}

export function EventsPage() {
    const { t, locale } = useI18n();
    const { isAuthenticated, user } = useAuth();
    const [activeFilter, setActiveFilter] = useState('all');
    const [feedState, setFeedState] = useState<FeedState>({ userId: null, items: [], error: null });

    useEffect(() => {
        if (!isAuthenticated || !user) return;

        let cancelled = false;

        feedApi.getFeed({ limit: '20' }).then((result) => {
            if (cancelled) return;

            if (result.error || !result.data) {
                setFeedState({ userId: user.id, items: [], error: result.error || t('common.error') });
                return;
            }

            setFeedState({ userId: user.id, items: result.data.items, error: null });
        });

        return () => {
            cancelled = true;
        };
    }, [isAuthenticated, t, user]);

    const isLoading = isAuthenticated && feedState.userId !== user?.id;
    const error = isAuthenticated && feedState.userId === user?.id ? feedState.error : null;

    const filteredItems = useMemo(() => {
        const displayItems = isAuthenticated && feedState.userId === user?.id ? feedState.items : [];
        if (activeFilter === 'all') return displayItems;
        return displayItems.filter((item) => normalizeCategory(item.category) === activeFilter);
    }, [activeFilter, feedState, isAuthenticated, user]);

    return (
        <div className={styles.pageContainer} id="events-page">
            <div className={styles.pageHeader}>
                <div className={styles.pageTag}>
                    <span className={styles.pageTagIcon} aria-hidden="true">
                        <IconCalendar />
                    </span>
                    {t('nav.events')}
                </div>
                <h1 className={styles.pageTitle}>{t('feed.title')}</h1>
                <p className={styles.pageSubtitle}>{t('feed.subtitle')}</p>
            </div>

            <div className={styles.filterRow}>
                {filters.map((f) => (
                    <button
                        key={f.key}
                        className={`${styles.filterPill} ${activeFilter === f.key ? styles.filterPillActive : ''}`}
                        onClick={() => setActiveFilter(f.key)}
                    >
                        {locale === 'vi' ? f.label_vi : f.label_en}
                    </button>
                ))}
            </div>

            {!isAuthenticated && (
                <div className={styles.placeholderSection}>
                    <div className={styles.placeholderIcon}>🔐</div>
                    <div className={styles.placeholderTitle}>{locale === 'vi' ? 'Đăng nhập để xem feed' : 'Sign in to view your feed'}</div>
                    <div className={styles.placeholderDesc}>{locale === 'vi' ? 'Backend xếp hạng feed theo sở thích và lịch sử của từng tài khoản.' : 'The backend ranks feed items from your preferences and visit history.'}</div>
                    <Link href="/auth/login" className={styles.placeholderBadge}>
                        {t('nav.login')}
                    </Link>
                </div>
            )}

            {isAuthenticated && isLoading && (
                <div className={styles.placeholderSection}>
                    <div className={styles.placeholderIcon}>🍃</div>
                    <div className={styles.placeholderTitle}>{t('common.loading')}</div>
                </div>
            )}

            {isAuthenticated && error && (
                <div className={styles.formError} role="alert">
                    ⚠ {error}
                </div>
            )}

            {isAuthenticated && !isLoading && !error && filteredItems.length === 0 && (
                <div className={styles.placeholderSection}>
                    <div className={styles.placeholderIcon}>🪷</div>
                    <div className={styles.placeholderTitle}>{t('feed.empty')}</div>
                </div>
            )}

            {isAuthenticated && !isLoading && !error && filteredItems.length > 0 && (
                <div className={styles.eventList}>
                    {filteredItems.map((item) => (
                        <Link key={item.id} href={`/place/${item.place_id}`} className={styles.eventCard}>
                            <div className={styles.eventDate}>
                                <span className={styles.eventDateMonth}>{locale === 'vi' ? 'Điểm' : 'Score'}</span>
                                <span className={styles.eventDateDay}>{Math.round(item.score * 100)}</span>
                            </div>
                            <div className={styles.eventImage}>
                                {item.thumbnail_url ? (
                                    <img src={item.thumbnail_url} alt={item.name} />
                                ) : (
                                    <div className={styles.cardImage}>🏛️</div>
                                )}
                            </div>
                            <div className={styles.eventInfo}>
                                <h3 className={styles.eventTitle}>{item.name}</h3>
                                <p className={styles.eventDesc}>{item.explanation}</p>
                                <div className={styles.eventMeta}>
                                    <span>📍 {item.place_id}</span>
                                    <span>🏷️ {categoryLabel(item.category, locale)}</span>
                                </div>
                            </div>
                            <div className={`${styles.eventBadge} ${styles.eventBadgeOngoing}`}>
                                {t('feed.recommended')}
                            </div>
                        </Link>
                    ))}
                </div>
            )}
        </div>
    );
}
