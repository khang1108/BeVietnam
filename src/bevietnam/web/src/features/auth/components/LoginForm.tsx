'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useI18n } from '@/i18n';
import { useAuth } from '@/hooks/useAuth';
import styles from '@/styles/pages.module.css';

export function LoginForm() {
    const { t, locale } = useI18n();
    const { login } = useAuth();
    const router = useRouter();

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        if (!email.trim() || !password) {
            setError(locale === 'vi' ? 'Vui lòng nhập email và mật khẩu' : 'Please enter email and password');
            return;
        }

        setIsSubmitting(true);
        const err = await login(email, password);
        setIsSubmitting(false);

        if (err) {
            setError(err);
        } else {
            router.push('/');
        }
    };

    return (
        <div className={styles.authPage} id="login-page">
            <div className={styles.authCard}>
                <div className={styles.authLogo}>
                    <div className={styles.authLogoIcon}>B</div>
                </div>
                <h1 className={styles.authTitle}>{t('auth.login.title')}</h1>

                {error && (
                    <div className={styles.formError} role="alert" id="login-error">
                        ⚠ {error}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div className={styles.formGroup}>
                        <label className={styles.formLabel} htmlFor="login-email">
                            {t('auth.login.email')}
                        </label>
                        <input
                            id="login-email"
                            type="email"
                            className={`${styles.formInput} ${error ? styles.formInputError : ''}`}
                            placeholder={locale === 'vi' ? 'Nhập email...' : 'Enter email...'}
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            disabled={isSubmitting}
                            autoComplete="email"
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.formLabel} htmlFor="login-password">
                            {t('auth.login.password')}
                        </label>
                        <input
                            id="login-password"
                            type="password"
                            className={`${styles.formInput} ${error ? styles.formInputError : ''}`}
                            placeholder={locale === 'vi' ? 'Nhập mật khẩu...' : 'Enter password...'}
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            disabled={isSubmitting}
                            autoComplete="current-password"
                        />
                    </div>

                    <div style={{ textAlign: 'right', marginBottom: 'var(--space-4)' }}>
                        <Link href="#" style={{ fontSize: 'var(--fs-sm)' }}>
                            {t('auth.login.forgotPassword')}
                        </Link>
                    </div>

                    <button
                        type="submit"
                        className={`${styles.formButton} ${isSubmitting ? styles.formButtonLoading : ''}`}
                        style={{ width: '100%', justifyContent: 'center' }}
                        id="login-submit"
                        disabled={isSubmitting}
                    >
                        {isSubmitting
                            ? (locale === 'vi' ? 'Đang đăng nhập...' : 'Logging in...')
                            : t('auth.login.title')}
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
