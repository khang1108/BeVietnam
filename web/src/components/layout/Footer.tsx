'use client';

import React from 'react';
import Link from 'next/link';
import { useI18n } from '@/i18n';
import { IconCalendar, IconMail } from '@/components/icons/UiIcons';
import styles from './Footer.module.css';

export default function Footer() {
  const { t } = useI18n();

  return (
    <footer className={styles.footer} id="main-footer">
      <div className={styles.footerPattern}></div>
      <div className={styles.footerInner}>
        <div className={styles.footerGrid}>
          {/* Brand */}
          <div className={styles.footerBrand}>
            <div className={styles.footerLogo}>
              <div className={styles.footerLogoIcon}>B</div>
              <span className={styles.footerLogoText}>
                Be<span className={styles.footerLogoAccent}>Vietnam</span>
              </span>
            </div>
            <p className={styles.footerDesc}>{t('footer.aboutDesc')}</p>
          </div>

          {/* Quick Links */}
          <div className={styles.footerColumn}>
            <h4>{t('footer.quickLinks')}</h4>
            <div className={styles.footerLinks}>
              <Link href="/" className={styles.footerLink}>📰 {t('nav.feed')}</Link>
              <Link href="/explore" className={styles.footerLink}>🗺️ {t('nav.explore')}</Link>
              <Link href="/storyline" className={styles.footerLink}>🧭 {t('nav.storyline')}</Link>
              <Link href="/events" className={styles.footerLink}>
                <span className={styles.footerIcon} aria-hidden="true">
                  <IconCalendar />
                </span>
                {t('nav.events')}
              </Link>
              <Link href="/contribute" className={styles.footerLink}>✨ {t('nav.contribute')}</Link>
            </div>
          </div>

          {/* Contact */}
          <div className={styles.footerColumn}>
            <h4>{t('footer.contact')}</h4>
            <div className={styles.footerLinks}>
              <span className={styles.footerLink}>
                <span className={styles.footerIcon} aria-hidden="true">
                  <IconMail />
                </span>
                bevietnam@hcmus.edu.vn
              </span>
              <span className={styles.footerLink}>🏫 HCMUS - VNU-HCM</span>
              <span className={styles.footerLink}>📍 Ho Chi Minh City, Vietnam</span>
            </div>
          </div>
        </div>

        {/* Bottom */}
        <div className={styles.footerBottom}>
          <span className={styles.footerCopyright}>{t('footer.copyright')}</span>
          <span className={styles.footerMadeWith}>{t('footer.madeWith')}</span>
        </div>
      </div>
    </footer>
  );
}
