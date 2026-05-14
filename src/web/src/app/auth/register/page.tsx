'use client';

import Link from 'next/link';
import { useI18n } from '@/i18n';
import styles from '@/styles/pages.module.css';

export default function RegisterPage() {
  const { t, locale } = useI18n();

  return (
    <div className={styles.authPage} id="register-page">
      <div className={styles.authCard}>
        <div className={styles.authLogo}>
          <div className={styles.authLogoIcon}>B</div>
        </div>
        <h1 className={styles.authTitle}>{t('auth.register.title')}</h1>

        <form onSubmit={(e) => e.preventDefault()}>
          <div className={styles.formGroup}>
            <label className={styles.formLabel} htmlFor="register-name">
              {t('auth.register.name')}
            </label>
            <input
              id="register-name"
              type="text"
              className={styles.formInput}
              placeholder={locale === 'vi' ? 'Nhập họ và tên...' : 'Enter full name...'}
            />
          </div>

          <div className={styles.formGroup}>
            <label className={styles.formLabel} htmlFor="register-email">
              {t('auth.register.email')}
            </label>
            <input
              id="register-email"
              type="email"
              className={styles.formInput}
              placeholder={locale === 'vi' ? 'Nhập email...' : 'Enter email...'}
            />
          </div>

          <div className={styles.formGroup}>
            <label className={styles.formLabel} htmlFor="register-password">
              {t('auth.register.password')}
            </label>
            <input
              id="register-password"
              type="password"
              className={styles.formInput}
              placeholder={locale === 'vi' ? 'Nhập mật khẩu...' : 'Enter password...'}
            />
          </div>

          <div className={styles.formGroup}>
            <label className={styles.formLabel} htmlFor="register-confirm">
              {t('auth.register.confirmPassword')}
            </label>
            <input
              id="register-confirm"
              type="password"
              className={styles.formInput}
              placeholder={locale === 'vi' ? 'Xác nhận mật khẩu...' : 'Confirm password...'}
            />
          </div>

          <button
            type="submit"
            className={styles.formButton}
            style={{ width: '100%', justifyContent: 'center' }}
            id="register-submit"
          >
            {t('auth.register.title')}
          </button>
        </form>

        <div className={styles.authDivider}>
          {locale === 'vi' ? 'hoặc' : 'or'}
        </div>

        <div className={styles.authFooter}>
          {t('auth.register.hasAccount')}{' '}
          <Link href="/auth/login">{t('auth.register.loginHere')}</Link>
        </div>
      </div>
    </div>
  );
}
