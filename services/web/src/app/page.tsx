'use client';

import type { CSSProperties } from 'react';
import Link from 'next/link';
import DashboardCardEffect from '@/components/DashboardCardEffect';
import NgoMonHero from '@/components/NgoMonHero';
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

const places: { img: string; cat: Bi; name: Bi }[] = [
  {
    img: '/images/hero-hue-citadel.png',
    cat: { vi: 'Thừa Thiên Huế', en: 'Thừa Thiên Huế' },
    name: { vi: 'Đại Nội Huế', en: 'Huế Imperial City' },
  },
  {
    img: '/images/hoian-lanterns.png',
    cat: { vi: 'Quảng Nam', en: 'Quảng Nam' },
    name: { vi: 'Phố cổ Hội An', en: 'Hội An Ancient Town' },
  },
  {
    img: '/images/one-pillar-pagoda.png',
    cat: { vi: 'Hà Nội', en: 'Hà Nội' },
    name: { vi: 'Chùa Một Cột', en: 'One Pillar Pagoda' },
  },
];

const team: { name: string; role: Bi; initials: string }[] = [
  { name: 'Nguyễn Phúc Khang', role: { vi: 'Trưởng nhóm · AI & Dữ liệu', en: 'Lead · AI & Data' }, initials: 'PK' },
  { name: 'Pumpowhat', role: { vi: 'Giao diện Web', en: 'Web Frontend' }, initials: 'PW' },
  { name: 'VietThaiNg', role: { vi: 'Ứng dụng & Bản đồ', en: 'Mobile & Maps' }, initials: 'VT' },
  { name: 'Nhóm Backend', role: { vi: 'Hệ thống & API', en: 'Backend & API' }, initials: 'BE' },
];

export default function HomePage() {
  const { t, locale } = useI18n();
  const L = (vi: string, en: string) => (locale === 'vi' ? vi : en);
  const revealDelay = (i: number): CSSProperties => ({ '--reveal-delay': `${i * 90}ms` } as CSSProperties);

  return (
    <div className={styles.page}>
      {/* ───────── Hero ───────── */}
      <section className={styles.hero}>
        <div className={styles.heroBg} aria-hidden="true" />
        <div className={styles.heroScrim} aria-hidden="true" />
        <div className={styles.heroContent}>
          <span className={styles.tag}>{L('Di sản · Văn hoá Việt Nam', 'Heritage · Vietnamese Culture')}</span>
          <h1 className={styles.heroTitle}>
            {L('Khám phá vẻ đẹp', 'Discover the beauty')}{' '}
            <span className={styles.heroEm}>{L('văn hoá Việt', 'of Vietnam')}</span>{' '}
            {L('qua từng câu chuyện', 'one story at a time')}
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

      {/* ───────── Gate band (Three.js) ───────── */}
      <section className={styles.band}>
        <div className={styles.bandPattern} aria-hidden="true" />
        <div className={styles.gateStage} data-reveal>
          <NgoMonHero className={styles.gateCanvas} />
        </div>
        <div className={styles.bandText} data-reveal>
          <h2 className={styles.bandTitle}>{L('Mỗi cánh cổng mở ra một câu chuyện', 'Every gate opens a story')}</h2>
          <p className={styles.bandSub}>
            {L('Ngọ Môn — cổng chính của Hoàng thành Huế, nơi hành trình của bạn bắt đầu.',
              'Ngọ Môn, the main gate of the Huế Citadel, where your journey begins.')}
          </p>
          <Link href="/storyline" className={styles.btnPrimary}>
            {L('Bước vào hành trình', 'Enter the journey')}
            <ArrowRight className={styles.btnIcon} weight="bold" aria-hidden="true" />
          </Link>
        </div>
      </section>

      {/* ───────── Explore places ───────── */}
      <section className={styles.explore}>
        <header className={styles.exploreHead}>
          <h2 className={styles.sectionTitle}>{L('Khám phá điểm đến', 'Explore destinations')}</h2>
          <Link href="/explore" className={styles.headLink}>
            {t('common.viewAll')}
            <ArrowRight weight="bold" aria-hidden="true" />
          </Link>
        </header>
        <div className={styles.placeGrid}>
          {places.map((p, i) => (
            <Link key={p.name.en} href="/explore" className={styles.placeCard} data-reveal style={revealDelay(i)}>
              <DashboardCardEffect className={styles.threeCardCanvas} />
              <span className={styles.placeImg} style={{ backgroundImage: `url(${p.img})` }} aria-hidden="true" />
              <div className={styles.placeBody}>
                <span className={styles.placeCat}>{L(p.cat.vi, p.cat.en)}</span>
                <h3 className={styles.placeName}>{L(p.name.vi, p.name.en)}</h3>
              </div>
            </Link>
          ))}
        </div>
      </section>

      {/* ───────── Team ───────── */}
      <section className={styles.team}>
        <header className={styles.sectionHead} data-reveal>
          <span className={styles.kicker}>{L('Nhóm thực hiện', 'The team')}</span>
          <h2 className={styles.sectionTitle}>{L('Nhóm 09 · Dự án văn hoá Việt Nam', 'Group 09 · A Vietnamese culture project')}</h2>
        </header>
        <div className={styles.teamGrid}>
          {team.map((m, i) => (
            <article key={m.name} className={styles.memberCard} data-reveal style={revealDelay(i)}>
              <span className={styles.avatar} aria-hidden="true">{m.initials}</span>
              <h3 className={styles.memberName}>{m.name}</h3>
              <p className={styles.memberRole}>{L(m.role.vi, m.role.en)}</p>
            </article>
          ))}
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
