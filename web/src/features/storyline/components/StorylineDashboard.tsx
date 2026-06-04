'use client';

import React, { useState } from 'react';
import { useI18n } from '@/i18n';
import styles from '../styles/storyline.module.css';

const QUESTS_METADATA = [
    {
        id: 1,
        image: '/images/hanoi-pho.png',
        rotation: -4,
    },
    {
        id: 2,
        image: '/images/halong-bay.png',
        rotation: 3,
    },
    {
        id: 3,
        image: '/images/hoian-lanterns.png',
        rotation: -2,
    },
    {
        id: 4,
        image: '/images/hero-hue-citadel.png',
        rotation: 5,
    },
    {
        id: 5,
        image: '/images/terraced-rice-fields.png',
        rotation: -3,
    },
];

export function StorylineDashboard() {
    const { t } = useI18n();
    const [currentStep, setCurrentStep] = useState(1);

    const quests = QUESTS_METADATA.map((q) => ({
        ...q,
        title: t(`storyline.quests.quest${q.id}.title`),
        desc: t(`storyline.quests.quest${q.id}.desc`),
    }));

    const handleComplete = (id: number) => {
        if (id === currentStep) {
            setCurrentStep((prev) => Math.min(prev + 1, quests.length + 1));
        }
    };

    return (
        <div className={styles.pageContainer}>
            <header className={styles.mapHeader}>
                <div className={styles.kieuKyTag}>{t('storyline.kieuKyTag')}</div>
                <h1 className={styles.mainTitle}>
                    {t('storyline.mainTitlePrefix')} <span>{t('storyline.mainTitleSuffix')}</span>
                </h1>
                <p className={styles.subTitle}>
                    {t('storyline.subTitle')}
                </p>
            </header>

            <div className={styles.mapArea}>
                {/* Footprint patterns in background */}
                <div
                    className={styles.footprint}
                    style={{ top: '20%', left: '30%', transform: 'rotate(45deg)' }}
                >
                    🐾
                </div>
                <div
                    className={styles.footprint}
                    style={{ top: '25%', left: '35%', transform: 'rotate(50deg)' }}
                >
                    🐾
                </div>
                <div
                    className={styles.footprint}
                    style={{ top: '60%', right: '25%', transform: 'rotate(-30deg)' }}
                >
                    🐾
                </div>
                <div
                    className={styles.footprint}
                    style={{ top: '65%', right: '30%', transform: 'rotate(-25deg)' }}
                >
                    🐾
                </div>

                {/* Background decorative elements */}
                <div className={`${styles.decorativeDrawing} ${styles.drawing1}`}>🐉</div>
                <div className={`${styles.decorativeDrawing} ${styles.drawing2}`}>⛵</div>
                <div className={`${styles.decorativeDrawing} ${styles.drawing3}`}>⛰️</div>
                <div className={`${styles.decorativeDrawing} ${styles.drawing4}`}>🏮</div>

                <div className={styles.handWrittenNote} style={{ top: '15%', right: '15%' }}>
                    {t('storyline.noteThangLong')}
                </div>
                <div className={styles.handWrittenNote} style={{ top: '60%', left: '10%' }}>
                    {t('storyline.noteSea')}
                </div>

                <svg className={styles.journeyPath} preserveAspectRatio="none" viewBox="0 0 100 100">
                    <path d="M 50 0 Q 30 20, 50 40 T 70 70 T 40 100" />
                </svg>

                <div className={styles.questGrid}>
                    {quests.map((quest) => {
                        const isCompleted = currentStep > quest.id;
                        const isActive = currentStep === quest.id;
                        const isLocked = currentStep < quest.id;

                        return (
                            <div key={quest.id} className={styles.questRow}>
                                <div
                                    className={`${styles.polaroidCard} ${isActive ? styles.activeCard : ''}`}
                                    style={{ transform: `rotate(${quest.rotation}deg)` }}
                                >
                                    <img
                                        src={quest.image}
                                        alt={quest.title}
                                        className={styles.polaroidImage}
                                    />

                                    <div className={styles.polaroidContent}>
                                        <h2 className={styles.questTitle}>{quest.title}</h2>
                                        <p className={styles.questDesc}>{quest.desc}</p>

                                        {isActive && (
                                            <button
                                                className={styles.actionButton}
                                                onClick={() => handleComplete(quest.id)}
                                            >
                                                {t('storyline.exploreBtn')}
                                            </button>
                                        )}
                                    </div>

                                    {isLocked && (
                                        <div className={styles.lockedOverlay}>
                                            <span className={styles.lockedIcon}>🔒</span>
                                            <span
                                                style={{
                                                    fontFamily: 'Dancing Script',
                                                    fontSize: '1.5rem',
                                                    color: '#4a3219',
                                                }}
                                            >
                                                {t('storyline.lockedText')}
                                            </span>
                                        </div>
                                    )}

                                    {isCompleted && (
                                        <div className={styles.completedMark}>✓</div>
                                    )}
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div>
        </div>
    );
}
