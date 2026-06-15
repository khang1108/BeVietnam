'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useI18n } from '@/i18n';
import { useTheme } from '@/hooks/useTheme';
import { localeNames } from '@/i18n/translations';
import { IconCalendar, IconExplore, IconFeed, IconSparkle, IconStoryline } from '@/components/icons/UiIcons';
import styles from './Navbar.module.css';

const navItems = [
  { key: 'feed', href: '/', icon: <IconFeed className={styles.navIconSvg} /> },
  { key: 'explore', href: '/explore', icon: <IconExplore className={styles.navIconSvg} /> },
  { key: 'storyline', href: '/storyline', icon: <IconStoryline className={styles.navIconSvg} /> },
  { key: 'events', href: '/events', icon: <IconCalendar className={styles.navIconSvg} /> },
  { key: 'contribute', href: '/contribute', icon: <IconSparkle className={styles.navIconSvg} /> },
];

export default function Navbar() {
  const { t, locale, setLocale } = useI18n();
  const { theme, toggleTheme } = useTheme();
  const pathname = usePathname();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  useEffect(() => {
    if (mobileOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [mobileOpen]);

  const isActive = (href: string) => {
    if (href === '/') return pathname === '/';
    return pathname.startsWith(href);
  };

  const toggleLocale = () => {
    setLocale(locale === 'vi' ? 'en' : 'vi');
  };

  const closeMobile = () => setMobileOpen(false);

  return (
    <nav className={`${styles.navbar} ${scrolled ? styles.navbarScrolled : ''}`} id="main-navbar">
      <div className={styles.navInner}>
        {/* Logo */}
        <Link href="/" className={styles.logo} onClick={closeMobile}>
          <div className={styles.logoIcon}>B</div>
          <span className={styles.logoText}>
            Be<span className={styles.logoAccent}>Vietnam</span>
          </span>
        </Link>

        {/* Desktop Nav Links */}
        <div className={styles.navLinks}>
          {navItems.map((item) => (
            <Link
              key={item.key}
              href={item.href}
              className={`${styles.navLink} ${isActive(item.href) ? styles.navLinkActive : ''}`}
            >
              <span className={styles.navIcon}>{item.icon}</span>
              {t(`nav.${item.key}`)}
            </Link>
          ))}
        </div>

        {/* Right section */}
        <div className={styles.navRight}>
          <button
            className={styles.navControl}
            onClick={toggleTheme}
            aria-label={t('nav.theme')}
            title={t('nav.theme')}
          >
            {theme === 'light' ? '🌙' : '☀️'}
          </button>

          <button
            className={`${styles.navControl} ${styles.langSwitch}`}
            onClick={toggleLocale}
            aria-label={t('nav.language')}
            title={localeNames[locale === 'vi' ? 'en' : 'vi'] as string}
          >
            {locale === 'vi' ? 'EN' : 'VI'}
          </button>

          <Link href="/auth/login" className={styles.authButton}>
            {t('nav.login')}
          </Link>

          {/* Hamburger */}
          <button
            className={`${styles.hamburger} ${mobileOpen ? styles.hamburgerOpen : ''}`}
            onClick={() => setMobileOpen(!mobileOpen)}
            aria-label="Menu"
            aria-expanded={mobileOpen}
          >
            <span className={styles.hamburgerLine}></span>
            <span className={styles.hamburgerLine}></span>
            <span className={styles.hamburgerLine}></span>
          </button>
        </div>
      </div>

      {/* Mobile Menu */}
      <div className={`${styles.mobileMenu} ${mobileOpen ? styles.mobileMenuOpen : ''}`}>
        {navItems.map((item) => (
          <Link
            key={item.key}
            href={item.href}
            className={`${styles.mobileNavLink} ${isActive(item.href) ? styles.mobileNavLinkActive : ''}`}
            onClick={closeMobile}
          >
            <span className={styles.navIcon}>{item.icon}</span>
            {t(`nav.${item.key}`)}
          </Link>
        ))}

        <div className={styles.mobileDivider}></div>

        <div className={styles.mobileControls}>
          <button className={styles.navControl} onClick={toggleTheme}>
            {theme === 'light' ? '🌙' : '☀️'} {t('nav.theme')}
          </button>
          <button className={`${styles.navControl} ${styles.langSwitch}`} onClick={toggleLocale}>
            {locale === 'vi' ? 'EN' : 'VI'} {localeNames[locale === 'vi' ? 'en' : 'vi'] as string}
          </button>
        </div>

        <Link href="/auth/login" className={styles.mobileAuthButton} onClick={closeMobile}>
          {t('nav.login')}
        </Link>
      </div>
    </nav>
  );
}
