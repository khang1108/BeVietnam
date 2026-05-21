'use client';

import React, { useState } from 'react';
import styles from '../styles/storyline.module.css';

const quests = [
    {
        id: 1,
        title: 'Hồ Hoàn Kiếm',
        desc: 'Trái tim của Thủ đô, nơi gắn liền với truyền thuyết rùa vàng trả gươm báu. Dạo bước quanh hồ và cảm nhận nhịp sống chậm rãi.',
        image: '/images/hero-hue-citadel.png',
        rotation: -4,
    },
    {
        id: 2,
        title: 'Vịnh Hạ Long',
        desc: 'Kỳ quan thiên nhiên thế giới với hàng ngàn đảo đá vôi kỳ vĩ vươn lên từ mặt nước xanh ngọc bích.',
        image: '/images/halong-bay.png',
        rotation: 3,
    },
    {
        id: 3,
        title: 'Phố Cổ Hội An',
        desc: 'Thương cảng sầm uất một thời, nay lung linh trong ánh đèn lồng lụa và những nếp nhà ngói âm dương rêu phong.',
        image: '/images/hoian-lanterns.png',
        rotation: -2,
    },
    {
        id: 4,
        title: 'Kinh Thành Huế',
        desc: 'Dấu ấn triều đại xưa, nơi lăng tẩm hoàng gia và nhã nhạc cung đình đưa bạn trở về những trang sử hào hùng.',
        image: '/images/hero-hue-citadel.png',
        rotation: 5,
    },
    {
        id: 5,
        title: 'Mù Cang Chải',
        desc: 'Những thửa ruộng bậc thang kỳ vĩ dệt nên tấm thảm lụa vàng ươm vắt ngang lưng trời Tây Bắc.',
        image: '/images/terraced-rice-fields.png',
        rotation: -3,
    },
];

export function StorylineDashboard() {
    const [currentStep, setCurrentStep] = useState(1);

    const handleComplete = (id: number) => {
        if (id === currentStep) {
            setCurrentStep((prev) => Math.min(prev + 1, quests.length + 1));
        }
    };

    return (
        <div className={styles.pageContainer}>
            <header className={styles.mapHeader}>
                <div className={styles.kieuKyTag}>Ký sự hành trình</div>
                <h1 className={styles.mainTitle}>
                    Di Sản <span>Việt Nam</span>
                </h1>
                <p className={styles.subTitle}>
                    Khám phá những miền di sản, văn hóa và vẻ đẹp tiềm ẩn của dải đất hình chữ S.
                    Mỗi chặng dừng chân là một câu chuyện vô giá.
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
                    Bắt đầu hành trình từ Thăng Long ngàn năm văn hiến...
                </div>
                <div className={styles.handWrittenNote} style={{ top: '60%', left: '10%' }}>
                    Cẩn thận những cơn bão biển!
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
                                                Khám phá
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
                                                Chưa khám phá
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
