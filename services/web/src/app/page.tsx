'use client';

import type { CSSProperties } from 'react';
import Link from 'next/link';
import DashboardCardEffect from '@/components/DashboardCardEffect';
import NgoMonHero from '@/components/NgoMonHero';
import { useI18n } from '@/i18n';
import { ArrowRight, CalendarBlank, Compass, Path } from '@phosphor-icons/react';
import styles from './page.module.css';

type Bi = { vi: string; en: string };

const stats: { value: string; label: Bi }[] = [
  { value: '14', label: { vi: 'Di tích Huế', en: 'Huế landmarks' } },
  { value: '120+', label: { vi: 'Câu chuyện văn hoá', en: 'Cultural stories' } },
  { value: '2', label: { vi: 'Ngôn ngữ', en: 'Languages' } },
];

const pillars: { href: string; icon: React.ReactNode; title: Bi; desc: Bi }[] = [
  {
    href: '/explore',
    icon: <Compass weight="regular" />,
    title: { vi: 'Bản đồ khám phá', en: 'Explore the map' },
    desc: {
      vi: 'Tìm địa danh, món ăn và di sản trên bản đồ tương tác, kèm bối cảnh văn hoá cho từng điểm đến.',
      en: 'Find places, food, and heritage on an interactive map, each with its cultural context.',
    },
  },
  {
    href: '/storyline',
    icon: <Path weight="regular" />,
    title: { vi: 'Hành trình văn hoá', en: 'Cultural journeys' },
    desc: {
      vi: 'Theo những chặng dừng được kể lại, check-in tại chỗ và lưu giữ khoảnh khắc của riêng bạn.',
      en: 'Follow narrated stops, check in on site, and keep your own moments along the way.',
    },
  },
  {
    href: '/events',
    icon: <CalendarBlank weight="regular" />,
    title: { vi: 'Sự kiện & lễ hội', en: 'Events & festivals' },
    desc: {
      vi: 'Biết điều gì đang diễn ra quanh bạn — từ lễ hội cung đình đến phiên chợ và đêm nhạc.',
      en: 'Know what is happening around you — from royal festivals to markets and music nights.',
    },
  },
];

// Group 09 — replace handles/initials with full names and photos when ready.
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

        <h1 className={styles.heroTitle}>
          {L('Khám phá', 'Explore')}{' '}
          <span className={styles.heroEm}>{L('Di sản Việt', "Vietnam's Heritage")}</span>
        </h1>

        <div className={styles.heroBody}>
          <div className={styles.heroLede}>
            <span className={styles.connector} aria-hidden="true" />
            <p>
              {L(
                'Khám phá những địa danh vượt ngoài lối mòn du lịch — qua từng câu chuyện, bản đồ và hành trình được tuyển chọn kỹ lưỡng từ di sản Huế.',
                'Discover places beyond the tourist path — through stories, maps, and journeys carefully drawn from the heritage of Huế.',
              )}
            </p>
            <div className={styles.heroCta}>
              <Link href="/explore" className={styles.btnPrimary}>
                {t('nav.explore')}
                <ArrowRight className={styles.btnIcon} weight="bold" aria-hidden="true" />
              </Link>
              <Link href="/storyline" className={styles.btnGhost}>
                {L('Bắt đầu hành trình', 'Start a journey')}
              </Link>
            </div>
          </div>

          <dl className={styles.heroStats}>
            {stats.map((s) => (
              <div key={s.value} className={styles.stat}>
                <dt className={styles.statValue}>{s.value}</dt>
                <dd className={styles.statLabel}>{L(s.label.vi, s.label.en)}</dd>
              </div>
            ))}
          </dl>
        </div>

        <Link href="/storyline" className={styles.storyCard} data-reveal="fade-right">
          <div className={styles.storyImg} aria-hidden="true" />
          <div className={styles.storyText}>
            <h3>{L('Mỗi chặng dừng là một câu chuyện', 'Every stop, a story')}</h3>
            <span className={styles.storyMore}>
              {L('Tìm hiểu thêm', 'Learn more')}
              <ArrowRight weight="bold" aria-hidden="true" />
            </span>
          </div>
        </Link>
      </section>

      {/* ───────── Motivation + gate ───────── */}
      <section className={styles.about}>
        <div className={styles.aboutText} data-reveal>
          <span className={styles.kicker}>{L('Vì sao BeVietnam', 'Why BeVietnam')}</span>
          <h2 className={styles.aboutTitle}>
            {L('Giữ cho những câu chuyện không bị lãng quên', 'Keeping the stories from being forgotten')}
          </h2>
          <p>
            {L(
              'BeVietnam ra đời từ một mong muốn giản dị: đưa chiều sâu văn hoá Việt Nam đến gần người trẻ và du khách hơn. Chúng tôi bắt đầu từ Cố đô Huế — nơi mỗi cổng thành, ngôi chùa và món ăn đều mang một lớp ký ức.',
              'BeVietnam began with a simple wish: to bring the depth of Vietnamese culture closer to young people and travellers. We start in the old capital of Huế — where every gate, pagoda, and dish carries a layer of memory.',
            )}
          </p>
          <p>
            {L(
              'Bằng bản đồ, hành trình và tư liệu được tuyển chọn từ sách và nguồn chính thống, mỗi địa điểm trên BeVietnam đều được kể lại một cách trung thực và sống động.',
              'Through maps, journeys, and material curated from books and official sources, every place on BeVietnam is retold faithfully and vividly.',
            )}
          </p>
        </div>
        <div className={styles.gateStage} data-reveal="fade-left">
          <NgoMonHero className={styles.gateCanvas} />
          <span className={styles.gateCaption}>
            {L('Ngọ Môn — cổng chính Hoàng thành Huế', 'Ngọ Môn — the main gate of the Huế Citadel')}
          </span>
        </div>
      </section>

      {/* ───────── What you can do ───────── */}
      <section className={styles.pillars}>
        <header className={styles.sectionHead} data-reveal>
          <span className={styles.kicker}>{L('Bạn có thể làm gì', 'What you can do')}</span>
          <h2 className={styles.sectionTitle}>
            {L('Ba cách để bước vào văn hoá Việt', 'Three ways into Vietnamese culture')}
          </h2>
        </header>
        <div className={styles.pillarGrid}>
          {pillars.map((p, i) => (
            <Link key={p.href} href={p.href} className={styles.pillarCard} data-reveal style={revealDelay(i)}>
              <DashboardCardEffect className={styles.threeCardCanvas} />
              <span className={styles.pillarIcon} aria-hidden="true">{p.icon}</span>
              <h3 className={styles.pillarTitle}>{L(p.title.vi, p.title.en)}</h3>
              <p className={styles.pillarDesc}>{L(p.desc.vi, p.desc.en)}</p>
              <span className={styles.pillarMore} aria-hidden="true"><ArrowRight weight="bold" /></span>
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
        <div className={styles.ctaPattern} aria-hidden="true" />
        <div className={styles.ctaInner} data-reveal>
          <h2 className={styles.ctaTitle}>{L('Bắt đầu hành trình của bạn', 'Begin your journey')}</h2>
          <p className={styles.ctaText}>
            {L('Mở bản đồ và để Huế kể cho bạn nghe câu chuyện đầu tiên.', 'Open the map and let Huế tell you the first story.')}
          </p>
          <Link href="/explore" className={styles.btnPrimary}>
            {t('nav.explore')}
            <ArrowRight className={styles.btnIcon} weight="bold" aria-hidden="true" />
          </Link>
        </div>
      </section>
    </div>
  );
}
