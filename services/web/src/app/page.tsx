'use client';

import type { CSSProperties } from 'react';
import Link from 'next/link';
import NonLaHero from '@/components/NonLaHero';
import HoiAnHouse from '@/components/HoiAnHouse';
import { useI18n } from '@/i18n';
import { ArrowRight } from '@phosphor-icons/react';
import styles from './page.module.css';

type Bi = { vi: string; en: string };

const stats: { value: string; label: Bi }[] = [
  { value: '14', label: { vi: 'Di tích Huế', en: 'Huế landmarks' } },
  { value: '120+', label: { vi: 'Câu chuyện văn hoá', en: 'Cultural stories' } },
  { value: '2', label: { vi: 'Ngôn ngữ', en: 'Languages' } },
];

// Honest source attribution — text, not invented brand logos.
const sources: string[] = [
  'Trung tâm Bảo tồn Di tích Cố đô Huế',
  'UNESCO',
  'Cổng TTĐT Thừa Thiên Huế',
  'Tư liệu sách văn hoá',
];

const places: { img: string; cat: Bi; name: Bi; desc: Bi }[] = [
  {
    img: '/images/hero-hue-citadel.png',
    cat: { vi: 'Thừa Thiên Huế', en: 'Thừa Thiên Huế' },
    name: { vi: 'Đại Nội Huế', en: 'Huế Imperial City' },
    desc: {
      vi: 'Hoàng cung cổ kính của triều Nguyễn, nơi lưu giữ những dấu ấn kiến trúc cung đình tinh tế và những câu chuyện lịch sử ngàn năm lịch sử.',
      en: 'The ancient imperial palace of the Nguyễn Dynasty, preserving exquisite royal architecture and stories of a thousand-year history.'
    }
  },
  {
    img: '/images/hoian-lanterns.png',
    cat: { vi: 'Quảng Nam', en: 'Quảng Nam' },
    name: { vi: 'Phố cổ Hội An', en: 'Hội An Ancient Town' },
    desc: {
      vi: 'Thương cảng cổ sầm uất mang đậm nét giao thoa văn hoá Á - Âu, rực rỡ với sắc màu đèn lồng và kiến trúc gỗ truyền thống.',
      en: 'A bustling ancient trading port reflecting Asian-European cultural fusion, vibrant with colorful lanterns and traditional wooden architecture.'
    }
  },
  {
    img: '/images/one-pillar-pagoda.png',
    cat: { vi: 'Hà Nội', en: 'Hà Nội' },
    name: { vi: 'Chùa Một Cột', en: 'One Pillar Pagoda' },
    desc: {
      vi: 'Biểu tượng tâm linh ngàn năm tuổi giữa lòng thủ đô Hà Nội, sở hữu kiến trúc độc đáo tựa như một bông hoa sen nở trên mặt nước.',
      en: 'A thousand-year-old spiritual symbol in the heart of Hanoi, featuring a unique architecture resembling a lotus flower blooming on the water.'
    }
  },
];

export default function HomePage() {
  const { t, locale } = useI18n();
  const L = (vi: string, en: string) => (locale === 'vi' ? vi : en);
  const revealDelay = (i: number): CSSProperties => ({ '--reveal-delay': `${i * 90}ms` } as CSSProperties);

  return (
    <div className={styles.page}>
      {/* ───────── Hero ───────── */}
      <section className={styles.hero}>
        <div className={styles.heroBg} aria-hidden="true">
          <iframe
            src="https://www.youtube.com/embed/L5Jk1Koi3Kg?autoplay=1&mute=1&controls=0&loop=1&playlist=L5Jk1Koi3Kg&start=33&end=85&playsinline=1&iv_load_policy=3&disablekb=1&fs=0&rel=0"
            frameBorder="0"
            allow="autoplay; encrypted-media"
            style={{
              position: 'absolute',
              top: '50%',
              left: '50%',
              // 16:9 cover, then overscan to crop the clip's letterbox bars
              width: 'max(100vw, 177.78vh)',
              height: 'max(100vh, 56.25vw)',
              transform: 'translate(-50%, -50%) scale(1.35)',
              pointerEvents: 'none',
              border: 'none',
            }}
          />
        </div>
        <div className={styles.heroScrim} aria-hidden="true" />
        <div className={styles.heroCanvasContainer}>
          <NonLaHero className={styles.heroCanvas} />
        </div>
        <div className={styles.heroContent}>
          <h1 className={styles.heroTitle}>
            {L('Khám phá văn hoá Việt Nam', 'Explore the culture of Vietnam')}
          </h1>
          <p className={styles.heroSub}>
            {L('Bắt đầu từ Cố đô Huế, qua bản đồ, hành trình và tư liệu được tuyển chọn.',
              'Beginning in the old capital of Huế, through curated maps, journeys, and material.')}
          </p>
          <div className={styles.heroCta}>
            <Link href="/explore" className={styles.btnPrimary}>
              {t('nav.explore')}
              <ArrowRight className={styles.btnIcon} weight="bold" aria-hidden="true" />
            </Link>
            <Link href="/storyline" className={styles.btnGhost}>{L('Bắt đầu hành trình', 'Start a journey')}</Link>
          </div>
        </div>
        <div className={styles.heroCut} aria-hidden="true" />
      </section>

      {/* ───────── Who we are ───────── */}
      <section className={styles.who}>
        <div className={styles.collage} data-reveal="fade-right">
          <span className={`${styles.photo} ${styles.photo1}`} aria-hidden="true" />
          <span className={`${styles.photo} ${styles.photo2}`} aria-hidden="true" />
          <span className={`${styles.photo} ${styles.photo3}`} aria-hidden="true" />
        </div>
        <div className={styles.whoText} data-reveal="fade-left">
          <span className={styles.kicker}>{L('Về chúng tôi', 'Who we are')}</span>
          <h2 className={styles.whoTitle}>
            {L('Nhịp cầu đưa văn hoá Việt đến gần bạn hơn', 'A bridge that brings Vietnamese culture closer')}
          </h2>
          <p className={styles.whoBody}>
            {L('BeVietnam ra đời từ một mong muốn giản dị: giữ cho những câu chuyện văn hoá không bị lãng quên. Mỗi cổng thành, ngôi chùa và món ăn đều mang một lớp ký ức, và chúng tôi kể lại chúng một cách trung thực.',
              'BeVietnam began with a simple wish: to keep cultural stories from being forgotten. Every gate, pagoda, and dish carries a layer of memory, and we retell them faithfully.')}
          </p>
          <dl className={styles.statRow}>
            {stats.map((s) => (
              <div key={s.value} className={styles.stat}>
                <dt className={styles.statValue}>{s.value}</dt>
                <dd className={styles.statLabel}>{L(s.label.vi, s.label.en)}</dd>
              </div>
            ))}
          </dl>
        </div>
      </section>

      {/* ───────── Sources strip ───────── */}
      <section className={styles.sources} aria-label={L('Nguồn tư liệu', 'Sources')}>
        <span className={styles.sourcesLabel}>{L('Tư liệu từ', 'Sourced from')}</span>
        <ul className={styles.sourcesList}>
          {sources.map((s) => (
            <li key={s} className={styles.sourceItem}>{s}</li>
          ))}
        </ul>
      </section>

      {/* ───────── Gate band (Three.js/Sketchfab) ───────── */}
      <section className={styles.band}>
        <div className={styles.bandPattern} aria-hidden="true" />
        <div className={styles.gateStage} data-reveal>
          <HoiAnHouse className={styles.gateCanvas} />
        </div>
        <div className={styles.bandText} data-reveal>
          <h2 className={styles.bandTitle}>{L('Thành phố của những lăng mộ', 'The City of Ghosts')}</h2>
          <p className={styles.bandSub}>
            {L('Nghĩa trang An Bằng với những lăng mộ nguy nga, rực rỡ sắc màu giao thoa kiến trúc truyền thống và hiện đại tại xứ Huế.',
              'An Bằng Cemetery, featuring grand and colorful tombs blending traditional and modern architectural styles in Huế.')}
          </p>
          <Link href="/storyline" className={styles.btnPrimary}>
            {L('Bước vào hành trình', 'Enter the journey')}
            <ArrowRight className={styles.btnIcon} weight="bold" aria-hidden="true" />
          </Link>
        </div>
      </section>

      {/* ───────── Explore places (Zigzag Layout) ───────── */}
      <section className={styles.exploreZigzag}>
        <header className={styles.exploreHead}>
          <span className={styles.kicker}>{L('Khám phá điểm đến', 'Destinations')}</span>
          <h2 className={styles.sectionTitle}>{L('Khám phá các di sản văn hoá tiêu biểu', 'Iconic Cultural Heritage')}</h2>
        </header>
        <div className={styles.zigzagContainer}>
          {places.map((p, i) => {
            const isEven = i % 2 === 0;
            const num = (i + 1).toString().padStart(2, '0');
            return (
              <article
                key={p.name.en}
                className={`${styles.placeRow} ${isEven ? styles.rowNormal : styles.rowReverse}`}
                data-reveal
                style={revealDelay(i)}
              >
                <div className={styles.imagePanel}>
                  <div className={styles.imageWrapper}>
                    <span className={styles.rowImg} style={{ backgroundImage: `url(${p.img})` }} aria-hidden="true" />
                    <div className={styles.imageOverlay} />
                  </div>
                </div>
                <div className={styles.textPanel}>
                  <div className={styles.numberBadge}>
                    <span className={styles.numberLine} />
                    <span className={styles.numberText}>{num}</span>
                  </div>
                  <span className={styles.rowCat}>{L(p.cat.vi, p.cat.en)}</span>
                  <h3 className={styles.rowName}>{L(p.name.vi, p.name.en)}</h3>
                  <p className={styles.rowDesc}>{L(p.desc.vi, p.desc.en)}</p>
                  <Link href="/explore" className={styles.rowLink}>
                    {L('Khám phá thêm', 'Explore details')}
                    <ArrowRight className={styles.linkIcon} weight="bold" aria-hidden="true" />
                  </Link>
                </div>
              </article>
            );
          })}
        </div>
      </section>

      {/* ───────── CTA band ───────── */}
      <section className={styles.ctaBand}>
        <div className={styles.bandPattern} aria-hidden="true" />
        <div className={styles.ctaInner} data-reveal>
          <h2 className={styles.bandTitle}>{L('Bắt đầu hành trình của bạn', 'Begin your journey')}</h2>
          <p className={styles.bandSub}>{L('Mở bản đồ và để Huế kể cho bạn nghe câu chuyện đầu tiên.', 'Open the map and let Huế tell you the first story.')}</p>
          <Link href="/explore" className={styles.btnPrimary}>
            {t('nav.explore')}
            <ArrowRight className={styles.btnIcon} weight="bold" aria-hidden="true" />
          </Link>
        </div>
      </section>
    </div>
  );
}
