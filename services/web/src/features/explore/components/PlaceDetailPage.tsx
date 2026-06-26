'use client';

import { useEffect, useMemo, useState } from 'react';
import { useParams } from 'next/navigation';
import { useI18n } from '@/i18n';
import { feedApi } from '@/lib/api-client';
import type { FeedItem } from '@/lib/types';
import styles from '@/styles/pages.module.css';

type PlaceDetailState = {
    placeId: string | null;
    item: FeedItem | null;
    error: string | null;
};

function normalizeParam(value: string | string[] | undefined) {
    if (Array.isArray(value)) return value[0] ?? '';
    return value ?? '';
}

function formatCategory(category: string | undefined) {
    if (!category) return 'Văn hóa';
    return category.replaceAll('-', ' ').replaceAll('_', ' ');
}

export function PlaceDetailPage() {
    const { locale, t } = useI18n();
    const params = useParams<{ id?: string | string[] }>();
    const placeId = normalizeParam(params.id);
    const [state, setState] = useState<PlaceDetailState>({ placeId: null, item: null, error: null });

    useEffect(() => {
        if (!placeId) return;

        let cancelled = false;
        feedApi.getFeed({ limit: '50' }).then((result) => {
            if (cancelled) return;
            const item = result.data?.items.find((feedItem) => feedItem.place_id === placeId) ?? null;
            setState({
                placeId,
                item,
                error: result.error,
            });
        });

        return () => {
            cancelled = true;
        };
    }, [placeId]);

    const isLoading = state.placeId !== placeId;
    const title = state.item?.name ?? placeId.replaceAll('-', ' ');
    const category = formatCategory(state.item?.category);
    const explanation = state.item?.explanation ?? (locale === 'vi'
        ? 'Thông tin địa điểm sẽ được tải từ feed gợi ý của backend.'
        : 'Place details will be loaded from the backend recommendation feed.');

    const intro = useMemo(() => {
        if (isLoading) return t('common.loading');
        if (state.error) return state.error;
        return explanation;
    }, [explanation, isLoading, state.error, t]);

    return (
        <div className={styles.pageContainer} id="place-detail-page">
            <div className={`${styles.detailHero} ${state.item?.thumbnail_url ? styles.detailHeroImage : ''}`}>
                {state.item?.thumbnail_url ? (
                    <img src={state.item.thumbnail_url} alt={title} />
                ) : (
                    '🏛️'
                )}
            </div>

            <div className={styles.pageHeader}>
                <div className={styles.pageTag}>📍 {category}</div>
                <h1 className={styles.pageTitle}>{title}</h1>
                <p className={styles.pageSubtitle}>{locale === 'vi' ? 'Huế, Việt Nam' : 'Hue, Vietnam'}</p>
            </div>

            <div className={styles.detailContent}>
                <div className={styles.detailMain}>
                    <h2>{locale === 'vi' ? 'Giới thiệu' : 'Introduction'}</h2>
                    <p>{intro}</p>

                    <h2>{locale === 'vi' ? 'Vì sao gợi ý địa điểm này?' : 'Why this place?'}</h2>
                    <p>{explanation}</p>
                </div>

                <div className={styles.detailSidebar}>
                    <div className={styles.sidebarCard}>
                        <h3>{locale === 'vi' ? 'Thông tin' : 'Info'}</h3>
                        <div className={styles.sidebarItem}>📍 {placeId}</div>
                        <div className={styles.sidebarItem}>🏷️ {category}</div>
                        {state.item && <div className={styles.sidebarItem}>⭐ {Math.round(state.item.score * 100)} điểm gợi ý</div>}
                    </div>

                    <div className={styles.sidebarCard}>
                        <h3>{locale === 'vi' ? 'Hành động' : 'Actions'}</h3>
                        <div className={styles.sidebarItem}>🗺️ {locale === 'vi' ? 'Mở trên bản đồ' : 'Open on map'}</div>
                        <div className={styles.sidebarItem}>💾 {locale === 'vi' ? 'Lưu lại' : 'Save'}</div>
                    </div>
                </div>
            </div>
        </div>
    );
}
