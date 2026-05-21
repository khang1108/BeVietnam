'use client';

import { useI18n } from '@/i18n';
import { IconCalendar, IconLink } from '@/components/icons/UiIcons';
import styles from '@/styles/pages.module.css';

export function EventDetailPage() {
    const { locale } = useI18n();

    return (
        <div className={styles.pageContainer} id="event-detail-page">
            <div className={styles.detailHero}>🎉</div>

            <div className={styles.pageHeader}>
                <div className={styles.pageTag}>🎉 {locale === 'vi' ? 'Sự kiện' : 'Event'}</div>
                <h1 className={styles.pageTitle}>
                    {locale === 'vi' ? 'Lễ hội Áo dài TP.HCM 2026' : 'HCMC Ao Dai Festival 2026'}
                </h1>
                <p className={styles.pageSubtitle}>
                    {locale === 'vi' ? 'TP. Hồ Chí Minh, Việt Nam' : 'Ho Chi Minh City, Vietnam'}
                </p>
            </div>

            <div className={styles.detailContent}>
                <div className={styles.detailMain}>
                    <h2>{locale === 'vi' ? 'Về sự kiện' : 'About the Event'}</h2>
                    <p>
                        {locale === 'vi'
                            ? 'Lễ hội Áo dài TP.HCM 2026 là sự kiện văn hóa thường niên tôn vinh vẻ đẹp của trang phục truyền thống Việt Nam. Chương trình bao gồm trình diễn thời trang, triển lãm áo dài qua các thời kỳ, workshop thiết kế và nhiều hoạt động nghệ thuật phong phú.'
                            : 'HCMC Ao Dai Festival 2026 is an annual cultural event celebrating the beauty of traditional Vietnamese dress. The program includes fashion shows, exhibitions of ao dai through the ages, design workshops, and various art activities.'}
                    </p>
                    <p>
                        {locale === 'vi'
                            ? 'Sự kiện quy tụ hơn 100 nhà thiết kế và hàng ngàn người tham dự từ khắp cả nước và quốc tế. Đây là cơ hội tuyệt vời để trải nghiệm và tìm hiểu về văn hóa Việt Nam.'
                            : "The event gathers over 100 designers and thousands of attendees from across the country and internationally. It's a wonderful opportunity to experience and learn about Vietnamese culture."}
                    </p>

                    <div className={styles.placeholderSection}>
                        <div className={styles.placeholderIcon}>📸</div>
                        <div className={styles.placeholderTitle}>
                            {locale === 'vi' ? 'Hình ảnh sự kiện' : 'Event Photos'}
                        </div>
                        <div className={styles.placeholderDesc}>
                            {locale === 'vi'
                                ? 'Hình ảnh sẽ được tải từ API'
                                : 'Photos will be loaded from API'}
                        </div>
                        <div className={styles.placeholderBadge}>
                            🔌 {locale === 'vi' ? 'Sẵn sàng tích hợp' : 'Ready for integration'}
                        </div>
                    </div>
                </div>

                <div className={styles.detailSidebar}>
                    <div className={styles.sidebarCard}>
                        <h3>{locale === 'vi' ? 'Thông tin' : 'Details'}</h3>
                        <div className={styles.sidebarItem}>📅 15-20 May 2026</div>
                        <div className={styles.sidebarItem}>
                            📍{' '}
                            {locale === 'vi'
                                ? 'Phố đi bộ Nguyễn Huệ'
                                : 'Nguyen Hue Walking Street'}
                        </div>
                        <div className={styles.sidebarItem}>🕐 18:00 - 22:00</div>
                        <div className={styles.sidebarItem}>
                            💰 {locale === 'vi' ? 'Miễn phí' : 'Free admission'}
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
