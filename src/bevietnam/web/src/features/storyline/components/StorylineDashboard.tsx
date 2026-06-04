'use client';

import React, { useState, useEffect } from 'react';
import styles from '../styles/storyline.module.css';
import { storylineApi, QuestTask, StorylineTaskDetail } from '@/lib/api';

const QUEST_IMAGES = [
    '/images/hero-hue-citadel.png',
    '/images/halong-bay.png',
    '/images/hoian-lanterns.png',
    '/images/terraced-rice-fields.png',
    '/images/hero-hue-citadel.png',
];

const ROTATIONS = [-4, 3, -2, 5, -3];

const FALLBACK_TASKS: QuestTask[] = [
    { quest_id: '', task_id: '1', step_index: 1, title: 'Hồ Hoàn Kiếm', description: 'Trái tim của Thủ đô, nơi gắn liền với truyền thuyết rùa vàng trả gươm báu.', cultural_explanation: '', completion_requirement: '', difficulty: 'easy', status: 'active' },
    { quest_id: '', task_id: '2', step_index: 2, title: 'Vịnh Hạ Long', description: 'Kỳ quan thiên nhiên thế giới với hàng ngàn đảo đá vôi kỳ vĩ.', cultural_explanation: '', completion_requirement: '', difficulty: 'easy', status: 'locked' },
    { quest_id: '', task_id: '3', step_index: 3, title: 'Phố Cổ Hội An', description: 'Thương cảng sầm uất một thời, nay lung linh trong ánh đèn lồng.', cultural_explanation: '', completion_requirement: '', difficulty: 'easy', status: 'locked' },
    { quest_id: '', task_id: '4', step_index: 4, title: 'Kinh Thành Huế', description: 'Dấu ấn triều đại xưa, nơi lăng tẩm hoàng gia và nhã nhạc cung đình.', cultural_explanation: '', completion_requirement: '', difficulty: 'medium', status: 'locked' },
    { quest_id: '', task_id: '5', step_index: 5, title: 'Mù Cang Chải', description: 'Những thửa ruộng bậc thang kỳ vĩ dệt nên tấm thảm lụa vàng ươm.', cultural_explanation: '', completion_requirement: '', difficulty: 'easy', status: 'locked' },
];

const DIFFICULTY_LABELS: Record<string, string> = {
    easy: 'Dễ',
    medium: 'Trung bình',
    hard: 'Khó',
};

interface ActiveTaskState extends StorylineTaskDetail {
    ai_generated: boolean;
}

export function StorylineDashboard() {
    const [tasks, setTasks] = useState<QuestTask[]>(FALLBACK_TASKS);
    const [currentStep, setCurrentStep] = useState(1);
    const [loading, setLoading] = useState(true);
    const [generatingTask, setGeneratingTask] = useState(false);
    const [activeTask, setActiveTask] = useState<ActiveTaskState | null>(null);

    useEffect(() => {
        loadQuestChain();
    }, []);

    async function loadQuestChain() {
        setLoading(true);
        const res = await storylineApi.getQuestChain();
        if (res.data && res.data.tasks.length > 0) {
            setTasks(res.data.tasks);
            const activeIdx = res.data.tasks.findIndex((t) => t.status === 'active');
            setCurrentStep(activeIdx >= 0 ? activeIdx + 1 : 1);
        }
        setLoading(false);
    }

    async function handleKhamPha(stepIndex: number) {
        if (stepIndex !== currentStep) return;
        setGeneratingTask(true);
        const res = await storylineApi.getNextTask();
        setGeneratingTask(false);
        if (res.data) {
            setActiveTask({ ...res.data.task, ai_generated: res.data.ai_generated });
        }
    }

    function handleComplete() {
        setActiveTask(null);
        setTasks((prev) =>
            prev.map((t, i) => {
                if (i === currentStep - 1) return { ...t, status: 'completed' as const };
                if (i === currentStep) return { ...t, status: 'active' as const };
                return t;
            })
        );
        setCurrentStep((prev) => prev + 1);
    }

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
                <div className={styles.footprint} style={{ top: '20%', left: '30%', transform: 'rotate(45deg)' }}>🐾</div>
                <div className={styles.footprint} style={{ top: '25%', left: '35%', transform: 'rotate(50deg)' }}>🐾</div>
                <div className={styles.footprint} style={{ top: '60%', right: '25%', transform: 'rotate(-30deg)' }}>🐾</div>
                <div className={styles.footprint} style={{ top: '65%', right: '30%', transform: 'rotate(-25deg)' }}>🐾</div>
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

                {loading ? (
                    <div className={styles.loadingState}>
                        <div className={styles.loadingSpinner} />
                        <p>Đang tải hành trình...</p>
                    </div>
                ) : (
                    <div className={styles.questGrid}>
                        {tasks.map((task, index) => {
                            const stepNum = index + 1;
                            const isCompleted = task.status === 'completed' || currentStep > stepNum;
                            const isActive = !isCompleted && currentStep === stepNum;
                            const isLocked = !isCompleted && !isActive;
                            const rotation = ROTATIONS[index % ROTATIONS.length];

                            return (
                                <div key={task.task_id} className={styles.questRow}>
                                    <div
                                        className={`${styles.polaroidCard} ${isActive ? styles.activeCard : ''}`}
                                        style={{ transform: `rotate(${rotation}deg)` }}
                                    >
                                        <img
                                            src={QUEST_IMAGES[index % QUEST_IMAGES.length]}
                                            alt={task.title}
                                            className={styles.polaroidImage}
                                        />
                                        <div className={styles.polaroidContent}>
                                            <h2 className={styles.questTitle}>{task.title}</h2>
                                            {isActive && (
                                                <button
                                                    className={styles.actionButton}
                                                    onClick={() => handleKhamPha(stepNum)}
                                                    disabled={generatingTask}
                                                >
                                                    {generatingTask ? 'AI đang tạo...' : 'Khám phá'}
                                                </button>
                                            )}
                                        </div>
                                        {isLocked && (
                                            <div className={styles.lockedOverlay}>
                                                <span className={styles.lockedIcon}>🔒</span>
                                                <span style={{ fontFamily: 'Dancing Script', fontSize: '1.5rem', color: '#4a3219' }}>
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
                )}
            </div>

            {activeTask && (
                <div
                    className={styles.taskOverlay}
                    onClick={(e) => e.target === e.currentTarget && setActiveTask(null)}
                >
                    <div className={styles.taskModal}>
                        <div className={styles.taskModalHeader}>
                            <div className={styles.taskBadges}>
                                <span className={`${styles.difficultyBadge} ${styles[`difficulty_${activeTask.difficulty}`]}`}>
                                    {DIFFICULTY_LABELS[activeTask.difficulty] ?? activeTask.difficulty}
                                </span>
                                {activeTask.ai_generated ? (
                                    <span className={styles.aiBadge}>✦ AI Generated</span>
                                ) : (
                                    <span className={styles.mockBadge}>Demo</span>
                                )}
                            </div>
                            <button className={styles.closeButton} onClick={() => setActiveTask(null)}>✕</button>
                        </div>

                        <h2 className={styles.taskModalTitle}>{activeTask.title}</h2>
                        <p className={styles.taskModalDesc}>{activeTask.description}</p>

                        <div className={styles.culturalBox}>
                            <div className={styles.culturalLabel}>Kiến thức văn hóa</div>
                            <p className={styles.culturalText}>{activeTask.cultural_explanation}</p>
                        </div>

                        <div className={styles.requirementBox}>
                            <div className={styles.requirementLabel}>Điều kiện hoàn thành</div>
                            <p className={styles.requirementText}>{activeTask.completion_requirement}</p>
                        </div>

                        <button className={styles.completeButton} onClick={handleComplete}>
                            Hoàn thành ✓
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}
