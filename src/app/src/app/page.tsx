'use client';
/* eslint-disable react-hooks/set-state-in-effect */

import React, { useState, useEffect } from 'react';
import { useI18n } from '@/i18n';
import styles from './page.module.css';

// Vietnam map check-in locations for the homepage
const mapCheckins = [
  { id: "hanoi", x: "44%", y: "18%", name_vi: "Hà Nội", name_en: "Hanoi" },
  { id: "halong", x: "53%", y: "21%", name_vi: "Vịnh Hạ Long", name_en: "Ha Long Bay" },
  { id: "ninhbinh", x: "47%", y: "24%", name_vi: "Ninh Bình", name_en: "Ninh Binh" },
  { id: "hue", x: "50%", y: "44%", name_vi: "Huế", name_en: "Hue" },
  { id: "hoian", x: "57%", y: "52%", name_vi: "Hội An", name_en: "Hoi An" },
  { id: "hcmc", x: "38%", y: "88%", name_vi: "TP. Hồ Chí Minh", name_en: "Ho Chi Minh City" }
];

export default function HomePage() {
  const { t, locale } = useI18n();
  const [isClient, setIsClient] = useState(false);

  useEffect(() => {
    setIsClient(true);
  }, []);

  // Text values based on locale
  const homepageContent = {
    tagline: locale === 'vi' ? 'KHÁM PHÁ' : 'EXPLORE',
    titlePrefix: locale === 'vi' ? 'KHÁM PHÁ' : 'EXPLORE',
    titleSuffix: locale === 'vi' ? 'VIỆT NAM' : 'VIETNAM',
    desc: locale === 'vi' 
      ? 'Khám phá vẻ đẹp bất tận của Việt Nam thông qua những di sản văn hóa, ẩm thực đặc trưng và cảnh quan thiên nhiên kỳ vĩ với sự hỗ trợ của trợ lý ảo AI thông minh.'
      : 'Explore the endless beauty of Vietnam through its cultural heritage, signature cuisine, and magnificent natural landscapes, all powered by our smart AI travel assistant.',
    cards: [
      {
        icon: '⛺',
        title: locale === 'vi' ? 'CHUYẾN ĐI' : 'TOURS',
        text: locale === 'vi'
          ? 'Xem lịch trình cho các chuyến đi sắp tới và đặt ngay để khám phá những điều tuyệt vời nhất Việt Nam.'
          : 'View schedule for upcoming tours and book yours to see all the wonderful things Vietnam has to offer!'
      },
      {
        icon: '🏨',
        title: locale === 'vi' ? 'KHÁCH SẠN' : 'HOTELS',
        text: locale === 'vi'
          ? 'Tìm những khách sạn tốt nhất phù hợp với mọi kế hoạch trong hành trình khám phá của bạn.'
          : 'Find the best hotel that accommodates all of the plans on your travel itinerary for your stay.'
      },
      {
        icon: '🍲',
        title: locale === 'vi' ? 'ẨM THỰC' : 'DINING',
        text: locale === 'vi'
          ? 'Trải nghiệm những hương vị mới lạ và thưởng thức tinh hoa ẩm thực độc đáo đặc trưng của Việt Nam.'
          : 'Try something new and get a taste of the unique cuisine Vietnam is most known for.'
      }
    ]
  };

  if (!isClient) {
    return (
      <div className={styles.container}>
        <div className={styles.bgContainer}>
          <div className={styles.bgOverlay}></div>
        </div>
        <div style={{ textAlign: 'center', padding: '200px 50px', color: '#e2ddd5' }}>
          {t('common.loading')}
        </div>
      </div>
    );
  }

  // Draw smooth S-shape path connecting major nodes
  const pathD = "M 44 18 L 53 21 L 47 24 L 50 44 L 57 52 L 38 88";

  return (
    <div className={styles.container}>
      {/* Background Media */}
      <div className={styles.bgContainer}>
        <div className={styles.bgOverlay}></div>
      </div>

      {/* Social margin Media bar */}
      <div className={styles.socialLinks}>
        <a className={styles.socialItem}>FB</a>
        <a className={styles.socialItem}>TW</a>
        <a className={styles.socialItem}>IG</a>
        <a className={styles.socialItem}>YT</a>
      </div>

      {/* Hero Body Split */}
      <div className={styles.mainLayout}>
        <div className={styles.leftColumn}>
          <div className={styles.tagline}>{homepageContent.tagline}</div>
          <h1 className={styles.heroTitle}>
            {homepageContent.titlePrefix}
            <span className={styles.brushedText}>{homepageContent.titleSuffix}</span>
          </h1>
          <p className={styles.description}>
            {homepageContent.desc}
          </p>
        </div>

        {/* Interactive Map */}
        <div className={styles.rightColumn}>
          <div className={styles.mapContainer}>
            {/* Vietnam S-Shape SVG */}
            <svg viewBox="0 0 100 100" className={styles.mapSvg}>
              {/* Background map coast silhouette */}
              <path 
                d="M 44,15 C 33,18 36,23 45,26 C 50,28 49,33 46,38 C 42,43 36,47 37,53 C 38,59 48,65 52,70 C 54,73 54,77 52,82 C 49,87 40,94 42,98 C 43,100 48,102 46,105 C 44,108 35,112 36,115"
                className={styles.mapOutline}
              />
              
              {/* Connected pulsing dotted path */}
              <path 
                d={pathD}
                className={styles.activePath}
              />
            </svg>

            {/* Check-in Nodes */}
            {mapCheckins.map((coord) => (
              <div
                key={coord.id}
                className={`${styles.mapNode} ${styles.nodeActive}`}
                style={{ left: coord.x, top: coord.y }}
              >
                <div className={styles.mapNodeInner}></div>
                <span className={styles.nodeLabel}>
                  {locale === 'vi' ? coord.name_vi : coord.name_en}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Bottom Glassmorphism Cards */}
      <div className={styles.bottomSection}>
        {homepageContent.cards.map((card, idx) => (
          <div key={idx} className={styles.glassCard}>
            <div className={styles.cardHeader}>
              <span className={styles.cardIcon}>{card.icon}</span>
              <h3 className={styles.cardTitle}>{card.title}</h3>
            </div>
            <p className={styles.cardText}>{card.text}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
