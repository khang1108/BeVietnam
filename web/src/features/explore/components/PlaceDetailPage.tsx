'use client';

import { useI18n } from '@/i18n';
import styles from '@/styles/pages.module.css';

export function PlaceDetailPage() {
    const { locale } = useI18n();

    return (
        <div className={styles.pageContainer} id="place-detail-page">
            <div className={styles.detailHero}>🏛️</div>

            <div className={styles.pageHeader}>
                <div className={styles.pageTag}>📍 {locale === 'vi' ? 'Văn hóa' : 'Culture'}</div>
                <h1 className={styles.pageTitle}>
                    {locale === 'vi' ? 'Chùa Một Cột' : 'One Pillar Pagoda'}
                </h1>
                <p className={styles.pageSubtitle}>
                    {locale === 'vi' ? 'Hà Nội, Việt Nam' : 'Hanoi, Vietnam'}
                </p>
            </div>

            <div className={styles.detailContent}>
                <div className={styles.detailMain}>
                    <h2>{locale === 'vi' ? 'Giới thiệu' : 'Introduction'}</h2>
                    <p>
                        {locale === 'vi'
                            ? 'Chùa Một Cột (tên chữ là Diên Hựu tự) là một ngôi chùa nằm giữa lòng thủ đô Hà Nội. Chùa được xây dựng vào năm 1049 dưới thời vua Lý Thái Tông. Đây là một trong những ngôi chùa có kiến trúc độc đáo nhất châu Á, với hình dáng một bông sen mọc trên mặt nước.'
                            : 'One Pillar Pagoda (official name: Dien Huu Pagoda) is a temple located in the heart of Hanoi. Built in 1049 under King Ly Thai Tong, it is one of the most uniquely designed temples in Asia, shaped like a lotus blossom rising from the water.'}
                    </p>

                    <h2>{locale === 'vi' ? 'Bối cảnh văn hóa' : 'Cultural Context'}</h2>
                    <p>
                        {locale === 'vi'
                            ? 'Theo truyền thuyết, vua Lý Thái Tông nằm mộng thấy Phật bà Quan Âm ngồi trên đài sen dẫn vua lên đài. Sau khi tỉnh dậy, nhà vua đã cho xây chùa theo hình ảnh trong giấc mơ. Ngôi chùa thể hiện triết lý Phật giáo về sự sinh ra từ bùn lầy nhưng vươn lên trong sáng.'
                            : 'According to legend, King Ly Thai Tong dreamt of Avalokitesvara Bodhisattva sitting on a lotus throne, guiding him up. After awakening, the king ordered the construction of the pagoda based on the dream. The temple embodies the Buddhist philosophy of rising pure from muddy origins.'}
                    </p>

                    <div className={styles.placeholderSection}>
                        <div className={styles.placeholderIcon}>🖼️</div>
                        <div className={styles.placeholderTitle}>
                            {locale === 'vi' ? 'Thư viện ảnh' : 'Photo Gallery'}
                        </div>
                        <div className={styles.placeholderDesc}>
                            {locale === 'vi'
                                ? 'Hình ảnh sẽ được tải từ API'
                                : 'Images will be loaded from API'}
                        </div>
                        <div className={styles.placeholderBadge}>
                            🔌 {locale === 'vi' ? 'Sẵn sàng tích hợp' : 'Ready for integration'}
                        </div>
                    </div>
                </div>

                <div className={styles.detailSidebar}>
                    <div className={styles.sidebarCard}>
                        <h3>{locale === 'vi' ? 'Thông tin' : 'Info'}</h3>
                        <div className={styles.sidebarItem}>
                            📍{' '}
                            {locale === 'vi'
                                ? 'Chùa Một Cột, Ba Đình, Hà Nội'
                                : 'One Pillar Pagoda, Ba Dinh, Hanoi'}
                        </div>
                        <div className={styles.sidebarItem}>
                            🕐 {locale === 'vi' ? '7:00 - 18:00' : '7:00 AM - 6:00 PM'}
                        </div>
                        <div className={styles.sidebarItem}>
                            💰 {locale === 'vi' ? 'Miễn phí' : 'Free'}
                        </div>
                        <div className={styles.sidebarItem}>⭐ 4.8/5</div>
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
