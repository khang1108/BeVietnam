'use client';

import { useParams } from 'next/navigation';
import { useI18n } from '@/i18n';
import { IconCalendar, IconLink } from '@/components/icons/UiIcons';
import styles from '@/styles/pages.module.css';
import { events } from '../data/events';

export function EventDetailPage() {
    const { locale } = useI18n();
    const params = useParams<{ id?: string }>();
    const event = events.find((item) => item.id === params.id) ?? events[0];

    return (
        <div className={styles.pageContainer} id="event-detail-page">
            <div className={`${styles.detailHero} ${styles.detailHeroImage}`}>
                <img
                    src={event.image}
                    alt={event.name[locale]}
                />
            </div>

            <div className={styles.pageHeader}>
                <div className={styles.pageTag}>🎉 {locale === 'vi' ? 'Sự kiện' : 'Event'}</div>
                <h1 className={styles.pageTitle}>
                    {event.name[locale]}
                </h1>
                <p className={styles.pageSubtitle}>
                    {event.place.location[locale]}
                </p>
            </div>

            <div className={styles.detailContent}>
                <div className={styles.detailMain}>
                    <h2>{locale === 'vi' ? 'Về sự kiện' : 'About the Event'}</h2>
                    <p>
                        {event.about[locale]}
                    </p>

                    <h2 style={{ marginTop: 'var(--space-8)' }}>
                        {locale === 'vi' ? 'Hình ảnh sự kiện' : 'Event Photos'}
                    </h2>
                    <div className={styles.eventPhoto}>
                        <img
                            src={event.image}
                            alt={event.name[locale]}
                        />
                    </div>
                </div>

                <div className={styles.detailSidebar}>
                    <div className={styles.sidebarCard}>
                        <h3>{locale === 'vi' ? 'Thông tin' : 'Details'}</h3>
                        <div className={styles.sidebarItem}>📅 {event.date.label}</div>
                        <div className={styles.sidebarItem}>
                            📍{' '}
                            {event.place.venue[locale]}
                        </div>
                        <div className={styles.sidebarItem}>🕐 {event.date.time}</div>
                        <div className={styles.sidebarItem}>
                            💰 {event.price.label[locale]}
                        </div>
                    </div>

                    <div className={styles.sidebarCard}>
                        <h3>{locale === 'vi' ? 'Chia sẻ' : 'Share'}</h3>
                        <div className={styles.sidebarItem}>
                            <span className={styles.sidebarIcon} aria-hidden="true">
                                <IconLink />
                            </span>
                            {locale === 'vi' ? 'Sao chép liên kết' : 'Copy link'}
                        </div>
                        <div className={styles.sidebarItem}>
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
