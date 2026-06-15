'use client';

import Link from 'next/link';
import { useI18n } from '@/i18n';
import { IconCalendar } from '@/components/icons/UiIcons';
import styles from '@/styles/pages.module.css';
import {
    events,
    getEventDateDay,
    getEventDateMonth,
    getEventStatus,
    getEventStatusLabel,
    isFreeEvent,
} from '../data/events';

const filters = [
    { key: 'all', label_vi: 'Tất cả', label_en: 'All' },
    { key: 'ongoing', label_vi: 'Đang diễn ra', label_en: 'Ongoing' },
    { key: 'upcoming', label_vi: 'Sắp diễn ra', label_en: 'Upcoming' },
    { key: 'free', label_vi: 'Miễn phí', label_en: 'Free' },
];

const eventBadgeStatusClass = {
    ongoing: styles.eventBadgeOngoing,
    completed: styles.eventBadgeCompleted,
    upcoming: styles.eventBadgeUpcoming,
};

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
                {events.map((event) => {
                    const status = getEventStatus(event.date);
                    const statusLabel = getEventStatusLabel(status, locale);

                    return (
                        <Link key={event.id} href={`/event/${event.id}`} className={styles.eventCard}>
                            <div className={styles.eventDate}>
                                <span className={styles.eventDateMonth}>{getEventDateMonth(event.date)}</span>
                                <span className={styles.eventDateDay}>{getEventDateDay(event.date)}</span>
                            </div>
                            <div className={styles.eventImage}>
                                <img
                                    src={event.image}
                                    alt={event.name[locale]}
                                />
                            </div>
                            <div className={styles.eventInfo}>
                                <h3 className={styles.eventTitle}>
                                    {event.name[locale]}
                                </h3>
                                <p className={styles.eventDesc}>
                                    {event.description[locale]}
                                </p>
                                <div className={styles.eventMeta}>
                                    <span>📍 {event.place.summary[locale]}</span>
                                    <span>
                                        🏷️ {statusLabel}
                                    </span>
                                    {isFreeEvent(event.price) && <span>🎫 {t('events.free')}</span>}
                                </div>
                            </div>
                            <div className={`${styles.eventBadge} ${eventBadgeStatusClass[status]}`}>
                                {statusLabel}
                            </div>
                        </Link>
                    );
                })}
            </div>
        </div>
    );
}
