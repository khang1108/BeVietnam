'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useI18n } from '@/i18n';
import { useAuth } from '@/hooks/useAuth';
import styles from '@/styles/pages.module.css';

const MIN_PASSWORD_LENGTH = 8;

export function RegisterForm() {
    const { t, locale } = useI18n();
    const { register } = useAuth();
    const router = useRouter();

    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const validate = (): string | null => {
        if (!name.trim() || !email.trim() || !password || !confirmPassword) {
            return locale === 'vi'
                ? 'Vui lòng điền đầy đủ thông tin'
                : 'Please fill in all fields';
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            return locale === 'vi'
                ? `Mật khẩu phải có ít nhất ${MIN_PASSWORD_LENGTH} ký tự`
                : `Password must be at least ${MIN_PASSWORD_LENGTH} characters`;
        }
        if (password !== confirmPassword) {
            return locale === 'vi'
                ? 'Mật khẩu xác nhận không khớp'
                : 'Passwords do not match';
        }
        return null;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        const validationError = validate();
        if (validationError) {
            setError(validationError);
            return;
        }

        setIsSubmitting(true);
        const err = await register(name.trim(), email.trim(), password);
        setIsSubmitting(false);

        if (err) {
            setError(err);
        } else {
            router.push('/');
        }
    };

    return (
        <div className={styles.authPage} id="register-page">
            <div className={styles.authCard}>
                <div className={styles.authLogo}>
                    <div className={styles.authLogoIcon}>B</div>
                </div>
                <h1 className={styles.authTitle}>{t('auth.register.title')}</h1>

                {error && (
                    <div className={styles.formError} role="alert" id="register-error">
                        ⚠ {error}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div className={styles.formGroup}>
                        <label className={styles.formLabel} htmlFor="register-name">
                            {t('auth.register.name')}
                        </label>
                        <input
                            id="register-name"
                            type="text"
                            className={styles.formInput}
                            placeholder={locale === 'vi' ? 'Nhập họ và tên...' : 'Enter full name...'}
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            disabled={isSubmitting}
                            autoComplete="name"
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
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            disabled={isSubmitting}
                            autoComplete="email"
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.formLabel} htmlFor="register-password">
                            {t('auth.register.password')}
                        </label>
                        <input
                            id="register-password"
                            type="password"
                            className={`${styles.formInput} ${
                                error && password.length < MIN_PASSWORD_LENGTH
                                    ? styles.formInputError
                                    : ''
                            }`}
                            placeholder={locale === 'vi' ? 'Nhập mật khẩu...' : 'Enter password...'}
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            disabled={isSubmitting}
                            autoComplete="new-password"
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.formLabel} htmlFor="register-confirm">
                            {t('auth.register.confirmPassword')}
                        </label>
                        <input
                            id="register-confirm"
                            type="password"
                            className={`${styles.formInput} ${
                                error && password !== confirmPassword
                                    ? styles.formInputError
                                    : ''
                            }`}
                            placeholder={
                                locale === 'vi' ? 'Xác nhận mật khẩu...' : 'Confirm password...'
                            }
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            disabled={isSubmitting}
                            autoComplete="new-password"
                        />
                    </div>

                    <button
                        type="submit"
                        className={`${styles.formButton} ${isSubmitting ? styles.formButtonLoading : ''}`}
                        style={{ width: '100%', justifyContent: 'center' }}
                        id="register-submit"
                        disabled={isSubmitting}
                    >
                        {isSubmitting
                            ? (locale === 'vi' ? 'Đang đăng ký...' : 'Registering...')
                            : t('auth.register.title')}
                    </button>
                </form>

                <div className={styles.authDivider}>{locale === 'vi' ? 'hoặc' : 'or'}</div>

                <div className={styles.authFooter}>
                    {t('auth.register.hasAccount')}{' '}
                    <Link href="/auth/login">{t('auth.register.loginHere')}</Link>
                </div>
            </div>
        </div>
    );
}
