'use client';

import Link from 'next/link';
import { useI18n } from '@/i18n';
import styles from '@/styles/pages.module.css';

export function LoginForm() {
    const { t, locale } = useI18n();

    return (
        <div className={styles.authPage} id="login-page">
            <div className={styles.authCard}>
                <div className={styles.authLogo}>
                    <div className={styles.authLogoIcon}>B</div>
                </div>
                <h1 className={styles.authTitle}>{t('auth.login.title')}</h1>

                <form onSubmit={(e) => e.preventDefault()}>
                    <div className={styles.formGroup}>
                        <label className={styles.formLabel} htmlFor="login-email">
                            {t('auth.login.email')}
                        </label>
                        <input
                            id="login-email"
                            type="email"
                            className={styles.formInput}
                            placeholder={locale === 'vi' ? 'Nhập email...' : 'Enter email...'}
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.formLabel} htmlFor="login-password">
                            {t('auth.login.password')}
                        </label>
                        <input
                            id="login-password"
                            type="password"
                            className={styles.formInput}
                            placeholder={locale === 'vi' ? 'Nhập mật khẩu...' : 'Enter password...'}
                        />
                    </div>

                    <div style={{ textAlign: 'right', marginBottom: 'var(--space-4)' }}>
                        <Link href="#" style={{ fontSize: 'var(--fs-sm)' }}>
                            {t('auth.login.forgotPassword')}
                        </Link>
                    </div>

                    <button
                        type="submit"
                        className={styles.formButton}
                        style={{ width: '100%', justifyContent: 'center' }}
                        id="login-submit"
                    >
                        {t('auth.login.title')}
                    </button>
                </form>

                <div className={styles.authDivider}>{locale === 'vi' ? 'hoặc' : 'or'}</div>

                <div className={styles.authFooter}>
                    {t('auth.login.noAccount')}{' '}
                    <Link href="/auth/register">{t('auth.login.signUpHere')}</Link>
                </div>
            </div>
        </div>
    );
}
