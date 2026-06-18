'use client';

import type { CSSProperties } from 'react';
import Link from 'next/link';
import DashboardCardEffect from '@/components/DashboardCardEffect';
import NgoMonHero from '@/components/NgoMonHero';
import { useI18n } from '@/i18n';
import { IconCalendar, IconExplore, IconFlagVn, IconFood, IconGlobe, IconLocation, IconMountain } from '@/components/icons/UiIcons';
import styles from './page.module.css';

const mockFeedItems = [
  {
    id: 1,
    icon: '🏛️',
    category: 'culture',
    badge: 'Trending',
    title_vi: 'Hội An - Phố cổ ngàn năm',
    title_en: 'Hoi An - Ancient Town of a Thousand Years',
    desc_vi: 'Khám phá vẻ đẹp của phố cổ Hội An với những ngôi nhà cổ, đèn lồng và ẩm thực đặc sắc.',
    desc_en: 'Discover the beauty of Hoi An ancient town with its old houses, lanterns, and distinctive cuisine.',
    location: 'Quảng Nam',
    time: '2h',
  },
  {
    id: 2,
    icon: <IconFood className={styles.feedIconSvg} />,
    category: 'food',
    badge: 'Popular',
    title_vi: 'Phở Hà Nội - Tinh hoa ẩm thực Việt',
    title_en: 'Hanoi Pho - The Essence of Vietnamese Cuisine',
    desc_vi: 'Câu chuyện đằng sau tô phở Hà Nội chính gốc với hơn 100 năm lịch sử.',
    desc_en: 'The story behind authentic Hanoi Pho with over 100 years of history.',
    location: 'Hà Nội',
    time: '5h',
  },
  {
    id: 3,
    icon: <IconMountain className={styles.feedIconSvg} />,
    category: 'nature',
    badge: 'New',
    title_vi: 'Ruộng bậc thang Mù Cang Chải',
    title_en: 'Mu Cang Chai Terraced Rice Fields',
    desc_vi: 'Mùa lúa chín vàng óng trên những thửa ruộng bậc thang tuyệt đẹp của Tây Bắc.',
    desc_en: 'The golden harvest season on the stunning terraced rice fields of the Northwest.',
    location: 'Yên Bái',
    time: '1d',
  },
  {
    id: 4,
    icon: '🎭',
    category: 'culture',
    badge: null,
    title_vi: 'Múa rối nước - Nghệ thuật độc đáo',
    title_en: 'Water Puppetry - A Unique Art Form',
    desc_vi: 'Nghệ thuật múa rối nước truyền thống Việt Nam với hơn nghìn năm lịch sử.',
    desc_en: 'Traditional Vietnamese water puppetry with over a thousand years of history.',
    location: 'Hà Nội',
    time: '3d',
  },
  {
    id: 5,
    icon: '🏖️',
    category: 'nature',
    badge: 'Hot',
    title_vi: 'Vịnh Hạ Long - Kỳ quan thiên nhiên',
    title_en: 'Ha Long Bay - Natural Wonder',
    desc_vi: 'Di sản thiên nhiên thế giới UNESCO với hàng ngàn đảo đá vôi giữa biển xanh.',
    desc_en: 'UNESCO World Heritage site with thousands of limestone islands in turquoise waters.',
    location: 'Quảng Ninh',
    time: '6h',
  },
  {
    id: 6,
    icon: '☕',
    category: 'food',
    badge: null,
    title_vi: 'Cà phê trứng Hà Nội',
    title_en: 'Hanoi Egg Coffee',
    desc_vi: 'Thức uống độc đáo ra đời từ thời kỳ khan hiếm sữa, nay trở thành biểu tượng Hà Nội.',
    desc_en: 'A unique beverage born from a milk shortage era, now an iconic Hanoi symbol.',
    location: 'Hà Nội',
    time: '2d',
  },
];

const categoryLabels: Record<string, Record<string, string>> = {
  culture: { vi: 'Văn hóa', en: 'Culture' },
  food: { vi: 'Ẩm thực', en: 'Food' },
  nature: { vi: 'Thiên nhiên', en: 'Nature' },
};

export default function HomePage() {
  const { t, locale } = useI18n();
  const withRevealDelay = (index: number): CSSProperties => ({
    '--reveal-delay': `${index * 90}ms`,
  } as CSSProperties);

  return (
    <div>
      {/* Hero */}
      <section className={styles.hero} id="hero-section">
        <div className={styles.heroBg}></div>
        <div className={styles.heroPattern}></div>
        <div className={styles.heroInner}>
          <div className={styles.heroContent}>
            <div className={styles.heroTag}>
              <span className={styles.heroTagIcon} aria-hidden="true">
                <IconFlagVn />
              </span>
              {t('common.tagline')}
            </div>
            <h1 className={styles.heroTitle}>
              {locale === 'vi' ? (
                <>Khám phá <span className={styles.heroGradient}>Việt Nam</span><br />Với chiều sâu văn hóa</>
              ) : (
                <>Discover <span className={styles.heroGradient}>Vietnam</span><br />With Cultural Depth</>
              )}
            </h1>
            <p className={styles.heroSubtitle}>
              {locale === 'vi'
                ? 'Khám phá di sản, ẩm thực và nhịp sống của Việt Nam — bắt đầu từ Cố đô Huế, qua từng câu chuyện kể bằng hình ảnh, bản đồ và hành trình.'
                : 'Discover the heritage, cuisine, and rhythm of Vietnam — starting in the old capital of Huế, told through images, maps, and journeys.'
              }
            </p>
            <div className={styles.heroCta}>
              <Link href="/explore" className={styles.btnPrimary}>
                <span className={styles.ctaIcon} aria-hidden="true">
                  <IconExplore />
                </span>
                {t('nav.explore')}
              </Link>
              <Link href="/events" className={styles.btnSecondary}>
                <span className={styles.ctaIcon} aria-hidden="true">
                  <IconCalendar />
                </span>
                {t('nav.events')}
              </Link>
            </div>
          </div>
          <div className={styles.heroStage}>
            <NgoMonHero className={styles.heroCanvas} />
          </div>
        </div>
      </section>

      {/* Stats */}
      <section className={styles.stats} data-reveal>
        <div className={styles.statsBg}></div>
        <div className={styles.statsGrid}>
          {[
            { icon: <IconLocation className={styles.statIconSvg} />, value: '1,200+', label: locale === 'vi' ? 'Địa điểm' : 'Places' },
            { icon: <IconFood className={styles.statIconSvg} />, value: '800+', label: locale === 'vi' ? 'Món ăn' : 'Dishes' },
            { icon: <IconCalendar className={styles.statIconSvg} />, value: '150+', label: locale === 'vi' ? 'Sự kiện/tháng' : 'Events/month' },
            { icon: <IconGlobe className={styles.statIconSvg} />, value: '100%', label: locale === 'vi' ? 'Song ngữ' : 'Bilingual' },
          ].map((stat, i) => (
            <div
              key={i}
              className={styles.statCard}
              data-reveal
              style={withRevealDelay(i)}
            >
              <DashboardCardEffect className={styles.threeCardCanvas} />
              <div className={styles.statIcon}>{stat.icon}</div>
              <div className={styles.statValue}>{stat.value}</div>
              <div className={styles.statLabel}>{stat.label}</div>
            </div>
          ))}
        </div>
      </section>

      {/* Feed */}
      <section className={styles.feedSection} id="feed-section">
        <div className={styles.dragonVideoWrap} aria-hidden="true">
          <video
            className={styles.dragonVideo}
            src="/videos/dragon-video.mp4"
            autoPlay
            loop
            muted
            playsInline
          />
        </div>
        <div className={styles.sectionHeader} data-reveal>
          <h2 className={styles.sectionTitle}>
            {t('feed.recommended')} ✨
          </h2>
          <Link href="/explore" className={styles.sectionLink}>
            {t('common.viewAll')} →
          </Link>
        </div>
        <div className={styles.feedGrid}>
          {mockFeedItems.map((item) => (
            <Link
              key={item.id}
              href={`/place/${item.id}`}
              className={styles.feedCard}
              data-reveal
              style={{ ...withRevealDelay(item.id), textDecoration: 'none' }}
            >
              <DashboardCardEffect className={styles.threeCardCanvas} />
              <div className={styles.feedCardImage}>
                <div className={styles.feedCardImagePlaceholder}>{item.icon}</div>
                {item.badge && (
                  <span className={styles.feedCardBadge}>{item.badge}</span>
                )}
              </div>
              <div className={styles.feedCardBody}>
                <div className={styles.feedCardCategory}>
                  {categoryLabels[item.category]?.[locale] || item.category}
                </div>
                <h3 className={styles.feedCardTitle}>
                  {locale === 'vi' ? item.title_vi : item.title_en}
                </h3>
                <p className={styles.feedCardDesc}>
                  {locale === 'vi' ? item.desc_vi : item.desc_en}
                </p>
                <div className={styles.feedCardMeta}>
                  <span className={styles.feedCardLocation}>📍 {item.location}</span>
                  <span>{item.time}</span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      </section>

      {/* Features */}
      <section className={styles.features} id="features-section" data-reveal>
        <div className={styles.featuresBg}></div>
        <div className={styles.sectionHeader} data-reveal>
          <h2 className={styles.sectionTitle}>
            {locale === 'vi' ? 'Tại sao chọn BeVietnam?' : 'Why BeVietnam?'}
          </h2>
          <span></span>
        </div>
        <div className={styles.featuresGrid}>
          <div className={styles.featureCard} data-reveal style={withRevealDelay(0)}>
            <DashboardCardEffect className={styles.threeCardCanvas} />
            <div className={`${styles.featureIcon} ${styles.featureIconGold}`}>🤖</div>
            <h3 className={styles.featureTitle}>
              {locale === 'vi' ? 'AI Văn hóa' : 'Cultural AI'}
            </h3>
            <p className={styles.featureDesc}>
              {locale === 'vi'
                ? 'Gemini AI bổ sung câu chuyện văn hóa cho mỗi địa điểm và món ăn, không chỉ thông tin khô khan.'
                : 'Gemini AI enriches every place and dish with cultural stories, not just dry information.'
              }
            </p>
          </div>
          <div className={styles.featureCard} data-reveal style={withRevealDelay(1)}>
            <DashboardCardEffect className={styles.threeCardCanvas} />
            <div className={`${styles.featureIcon} ${styles.featureIconRed}`}>🌏</div>
            <h3 className={styles.featureTitle}>
              {locale === 'vi' ? 'Song ngữ 100%' : '100% Bilingual'}
            </h3>
            <p className={styles.featureDesc}>
              {locale === 'vi'
                ? 'Mọi nội dung đều có phiên bản Việt - Anh, dịch thuật bảo toàn giá trị văn hóa.'
                : 'All content available in Vietnamese and English, translated to preserve cultural value.'
              }
            </p>
          </div>
          <div className={styles.featureCard} data-reveal style={withRevealDelay(2)}>
            <DashboardCardEffect className={styles.threeCardCanvas} />
            <div className={`${styles.featureIcon} ${styles.featureIconGreen}`}>🔄</div>
            <h3 className={styles.featureTitle}>
              {locale === 'vi' ? 'Luôn cập nhật' : 'Always Fresh'}
            </h3>
            <p className={styles.featureDesc}>
              {locale === 'vi'
                ? 'Dữ liệu tự động cập nhật mỗi 6 giờ từ nhiều nguồn, đảm bảo thông tin luôn chính xác.'
                : 'Data auto-updates every 6 hours from multiple sources, ensuring accuracy at all times.'
              }
            </p>
          </div>
        </div>
      </section>

      {/* Gallery Banner - Ha Long Bay */}
      <section className={styles.galleryBanner} id="gallery-section" data-reveal>
        <div className={styles.galleryBannerBg}></div>
        <div className={styles.galleryContent}>
          <h2 className={styles.galleryTitle} data-reveal>
            {locale === 'vi' ? 'Khám phá vẻ đẹp Việt Nam' : 'Discover Vietnam\'s Beauty'}
          </h2>
          <p className={styles.gallerySubtitle} data-reveal style={withRevealDelay(1)}>
            {locale === 'vi'
              ? 'Từ vịnh Hạ Long kỳ vĩ đến phố cổ Hội An lung linh, Việt Nam luôn đợi bạn.'
              : 'From majestic Ha Long Bay to enchanting Hoi An ancient town, Vietnam awaits.'
            }
          </p>
          <div className={styles.galleryGrid}>
            {[
              { src: '/images/hero-hue-citadel.png', label: locale === 'vi' ? 'Kinh thành Huế' : 'Hue Imperial Citadel' },
              { src: '/images/halong-bay.png', label: locale === 'vi' ? 'Vịnh Hạ Long' : 'Ha Long Bay' },
              { src: '/images/hoian-lanterns.png', label: locale === 'vi' ? 'Phố cổ Hội An' : 'Hoi An Ancient Town' },
              { src: '/images/terraced-rice-fields.png', label: locale === 'vi' ? 'Ruộng bậc thang Mù Cang Chải' : 'Mu Cang Chai Terraces' },
            ].map((item, i) => (
              <div key={i} className={styles.galleryItem} data-reveal style={withRevealDelay(i)}>
                <img src={item.src} alt={item.label} />
                <div className={styles.galleryItemOverlay}>
                  <span className={styles.galleryItemLabel}>{item.label}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}
