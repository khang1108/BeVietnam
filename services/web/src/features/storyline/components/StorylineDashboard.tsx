'use client';

import React, { useCallback, useEffect, useRef, useState } from 'react';
import styles from '../styles/storyline.module.css';
import { storylineApi, type QuestTask, type StorylineTaskDetail } from '@/lib/api';
import { useI18n } from '@/i18n';
import { useAuth } from '@/hooks/useAuth';

const QUEST_IMAGES = [
    '/images/hero-hue-citadel.png',
    '/images/halong-bay.png',
    '/images/hoian-lanterns.png',
    '/images/terraced-rice-fields.png',
    '/images/hero-hue-citadel.png',
];

const ROTATIONS = [-4, 3, -2, 5, -3];

const FALLBACK_TASKS: QuestTask[] = [
    { quest_id: '', task_id: '1', step_index: 1, title: 'Hồ Hoàn Kiếm', description: 'Trái tim của Thủ đô, nơi gắn liền với truyền thuyết rùa vàng trả gươm báu.', cultural_explanation: 'Hồ Hoàn Kiếm không chỉ là một danh lam thắng cảnh mà còn là biểu tượng lịch sử. Theo truyền thuyết, vua Lê Lợi đã trả thanh gươm thần cho rùa vàng tại đây sau khi đánh thắng quân Minh.', completion_requirement: 'Check-in tại Hồ Hoàn Kiếm và chụp ảnh kỷ niệm.', difficulty: 'easy', status: 'active' },
    { quest_id: '', task_id: '2', step_index: 2, title: 'Vịnh Hạ Long', description: 'Kỳ quan thiên nhiên thế giới với hàng ngàn đảo đá vôi kỳ vĩ.', cultural_explanation: 'Vịnh Hạ Long là Di sản Thiên nhiên Thế giới được UNESCO công nhận, nổi tiếng với gần 2.000 hòn đảo đá vôi và hang động kỳ bí.', completion_requirement: 'Đến thăm Vịnh Hạ Long và lưu giữ khoảnh khắc.', difficulty: 'easy', status: 'locked' },
    { quest_id: '', task_id: '3', step_index: 3, title: 'Phố Cổ Hội An', description: 'Thương cảng sầm uất một thời, nay lung linh trong ánh đèn lồng.', cultural_explanation: 'Hội An là phố cổ được bảo tồn nguyên vẹn nhất Đông Nam Á, từng là thương cảng quốc tế sầm uất từ thế kỷ XV-XIX.', completion_requirement: 'Tham quan Phố Cổ Hội An và chụp ảnh với đèn lồng.', difficulty: 'easy', status: 'locked' },
    { quest_id: '', task_id: '4', step_index: 4, title: 'Kinh Thành Huế', description: 'Dấu ấn triều đại xưa, nơi lăng tẩm hoàng gia và nhã nhạc cung đình.', cultural_explanation: 'Kinh thành Huế là quần thể di tích Cố đô, được UNESCO công nhận là Di sản Văn hóa Thế giới. Nơi đây lưu giữ dấu ấn vàng son của triều Nguyễn.', completion_requirement: 'Khám phá Kinh Thành Huế và ghi lại hình ảnh.', difficulty: 'medium', status: 'locked' },
    { quest_id: '', task_id: '5', step_index: 5, title: 'Mù Cang Chải', description: 'Những thửa ruộng bậc thang kỳ vĩ dệt nên tấm thảm lụa vàng ươm.', cultural_explanation: 'Ruộng bậc thang Mù Cang Chải là kiệt tác của người Hmông, được công nhận là Di tích Quốc gia đặc biệt. Mỗi mùa lúa chín, nơi đây biến thành biển vàng tuyệt đẹp.', completion_requirement: 'Check-in tại Mù Cang Chải.', difficulty: 'easy', status: 'locked' },
];

/** Saved check-in moment */
interface CheckInMoment {
    taskId: string;
    placeName: string;
    photoDataUrl: string;
    timestamp: string;
}

interface ActiveTaskState extends StorylineTaskDetail {
    ai_generated: boolean;
}

export function StorylineDashboard() {
    const { t } = useI18n();
    const { user } = useAuth();
    const [tasks, setTasks] = useState<QuestTask[]>(FALLBACK_TASKS);
    const [currentStep, setCurrentStep] = useState(1);
    const [loading, setLoading] = useState(true);
    const [generatingTask, setGeneratingTask] = useState(false);
    const [activeTask, setActiveTask] = useState<ActiveTaskState | null>(null);

    // Place detail card state (NEW)
    const [selectedPlace, setSelectedPlace] = useState<{ task: QuestTask; index: number } | null>(null);
    const [cameraActive, setCameraActive] = useState(false);
    const [capturedPhoto, setCapturedPhoto] = useState<string | null>(null);
    const [savedMoments, setSavedMoments] = useState<CheckInMoment[]>([]);
    const [showGallery, setShowGallery] = useState(false);
    const [savingMoment, setSavingMoment] = useState(false);
    const [cameraError, setCameraError] = useState<string | null>(null);

    const videoRef = useRef<HTMLVideoElement>(null);
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const streamRef = useRef<MediaStream | null>(null);

    const completedCount = tasks.filter((_t, i) => _t.status === 'completed' || i < currentStep - 1).length;
    const progressPercent = Math.round((completedCount / tasks.length) * 100);

    // Load saved moments dynamically based on user
    useEffect(() => {
        const userId = user?.id || 'guest';
        const storageKey = `bevietnam-checkin-moments-${userId}`;
        const saved = typeof window !== 'undefined' ? localStorage.getItem(storageKey) : null;
        if (saved) {
            try {
                // eslint-disable-next-line react-hooks/set-state-in-effect
                setSavedMoments(JSON.parse(saved));
            } catch {
                setSavedMoments([]);
            }
        } else {
            setSavedMoments([]);
        }
    }, [user]);

    const loadQuestChain = useCallback(async () => {
        const userId = user?.id || 'demo-user';
        const res = await storylineApi.getQuestChain(userId);
        if (res.data && res.data.tasks.length > 0) {
            // Read saved moments to determine completed status
            const storageKey = `bevietnam-checkin-moments-${user?.id || 'guest'}`;
            const savedStr = typeof window !== 'undefined' ? localStorage.getItem(storageKey) : null;
            const moments: CheckInMoment[] = savedStr ? JSON.parse(savedStr) : [];
            const completedTaskIds = new Set(moments.map((m) => m.taskId));

            const updatedTasks = res.data.tasks.map((task) => {
                if (completedTaskIds.has(task.task_id)) {
                    return { ...task, status: 'completed' as const };
                }
                return task;
            });

            // Find the first task that is not completed
            const firstActiveIdx = updatedTasks.findIndex((t) => t.status !== 'completed');
            const finalTasks = updatedTasks.map((task, i) => {
                if (i === firstActiveIdx) {
                    return { ...task, status: 'active' as const };
                }
                if (task.status !== 'completed') {
                    return { ...task, status: 'locked' as const };
                }
                return task;
            });

            setTasks(finalTasks);
            setCurrentStep(firstActiveIdx >= 0 ? firstActiveIdx + 1 : res.data.tasks.length + 1);
        }
        setLoading(false);
    }, [user]);

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        loadQuestChain();
    }, [loadQuestChain]);

    // Cleanup camera on unmount
    useEffect(() => {
        return () => {
            if (streamRef.current) {
                streamRef.current.getTracks().forEach((track) => track.stop());
            }
        };
    }, []);

    // Attach stream to video element once cameraActive renders the <video>
    useEffect(() => {
        if (cameraActive && videoRef.current && streamRef.current) {
            videoRef.current.srcObject = streamRef.current;
            videoRef.current.play().catch(console.error);
        }
    }, [cameraActive]);

    function unlockNextPlace(stepIndex: number) {
        setTasks((prev) =>
            prev.map((task, index) => {
                const taskStep = index + 1;

                if (taskStep === stepIndex) {
                    return { ...task, status: 'completed' as const };
                }

                if (taskStep === stepIndex + 1 && task.status === 'locked') {
                    return { ...task, status: 'active' as const };
                }

                return task;
            })
        );
        setCurrentStep(stepIndex + 1);
    }

    // Open place detail card when clicking KHÁM PHÁ
    function handleKhamPha(stepIndex: number, task: QuestTask, index: number) {
        if (stepIndex !== currentStep) return;
        setSelectedPlace({ task, index });
        setCapturedPhoto(null);
        setCameraActive(false);
        setCameraError(null);
    }

    // Start camera — get the stream first, then set cameraActive to render the <video>
    async function startCamera() {
        setCameraError(null);
        try {
            const stream = await navigator.mediaDevices.getUserMedia({
                video: { facingMode: 'environment', width: { ideal: 1280 }, height: { ideal: 720 } },
                audio: false,
            });
            streamRef.current = stream;
            // Set cameraActive so <video> element renders, then useEffect attaches stream
            setCameraActive(true);
        } catch (err) {
            console.error('Camera error:', err);
            setCameraError(t('storyline.cameraError'));
        }
    }

    // Capture photo from camera
    function capturePhoto() {
        if (!videoRef.current || !canvasRef.current) return;

        const video = videoRef.current;
        const canvas = canvasRef.current;
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;

        const ctx = canvas.getContext('2d');
        if (!ctx) return;
        ctx.drawImage(video, 0, 0);

        const dataUrl = canvas.toDataURL('image/jpeg', 0.85);
        setCapturedPhoto(dataUrl);

        // Stop camera after capture
        if (streamRef.current) {
            streamRef.current.getTracks().forEach((track) => track.stop());
            streamRef.current = null;
        }
        setCameraActive(false);
    }

    // Retake photo
    function retakePhoto() {
        setCapturedPhoto(null);
        startCamera();
    }

    // Save moment and complete the quest
    async function saveMomentAndComplete() {
        if (!selectedPlace || !capturedPhoto) return;

        setSavingMoment(true);

        const moment: CheckInMoment = {
            taskId: selectedPlace.task.task_id,
            placeName: selectedPlace.task.title,
            photoDataUrl: capturedPhoto,
            timestamp: new Date().toISOString(),
        };

        const userId = user?.id || 'demo-user';
        const storageKey = `bevietnam-checkin-moments-${user?.id || 'guest'}`;
        const updatedMoments = [...savedMoments, moment];
        setSavedMoments(updatedMoments);

        // Save to localStorage
        if (typeof window !== 'undefined') {
            localStorage.setItem(storageKey, JSON.stringify(updatedMoments));
        }

        // Unlock next place
        const stepIndex = selectedPlace.index + 1;
        unlockNextPlace(stepIndex);

        // Try to get AI task detail
        setGeneratingTask(true);
        let res: Awaited<ReturnType<typeof storylineApi.getNextTask>>;
        try {
            if (!navigator.geolocation) {
                res = await storylineApi.getNextTask({}, userId);
            } else {
                const position = await getCurrentPosition();
                res = await storylineApi.getNextTask({
                    latitude: position.coords.latitude,
                    longitude: position.coords.longitude,
                }, userId);
            }
        } catch {
            res = await storylineApi.getNextTask({}, userId);
        } finally {
            setGeneratingTask(false);
        }
        if (res.data) {
            setActiveTask({ ...res.data.task, ai_generated: res.data.ai_generated });
        }

        setSavingMoment(false);
        closePlaceCard();
    }

    // Close place card and cleanup
    function closePlaceCard() {
        if (streamRef.current) {
            streamRef.current.getTracks().forEach((track) => track.stop());
            streamRef.current = null;
        }
        setSelectedPlace(null);
        setCameraActive(false);
        setCapturedPhoto(null);
        setCameraError(null);
    }

    function handleComplete() {
        setActiveTask(null);
    }

    const diffLabel = (d: string) => t(`storyline.difficulty.${d}`) || d;

    // Get moments for a specific place
    const getMomentsForPlace = (taskId: string) =>
        savedMoments.filter((m) => m.taskId === taskId);

    return (
        <div className={styles.pageContainer}>
            {/* Header */}
            <header className={styles.mapHeader}>
                <div className={styles.kieuKyTag}>{t('storyline.tag')}</div>
                <h1 className={styles.mainTitle}>
                    {t('storyline.title')} <span>{t('storyline.titleHighlight')}</span>
                </h1>
                <p className={styles.subTitle}>{t('storyline.subtitle')}</p>
            </header>

            {/* Progress Section */}
            <div className={styles.progressSection}>
                <div className={styles.progressCard}>
                    <div className={styles.progressStats}>
                        <span className={styles.progressLabel}>
                            {t('storyline.progress')}: <strong>{completedCount}/{tasks.length}</strong> {t('storyline.explored')}
                        </span>
                        <span className={styles.progressPercent}>{progressPercent}%</span>
                    </div>
                    <div className={styles.progressBarTrack}>
                        <div
                            className={styles.progressBarFill}
                            style={{ width: `${progressPercent}%` }}
                        />
                    </div>
                    <div className={styles.progressSteps}>
                        {tasks.map((task, i) => {
                            const stepNum = i + 1;
                            const isCompleted = task.status === 'completed' || currentStep > stepNum;
                            const isActive = !isCompleted && currentStep === stepNum;
                            const dotClass = isCompleted
                                ? styles.dotCompleted
                                : isActive
                                    ? styles.dotActive
                                    : styles.dotLocked;
                            return (
                                <div key={task.task_id} className={`${styles.progressDot} ${dotClass}`}>
                                    {isCompleted ? '✓' : stepNum}
                                </div>
                            );
                        })}
                    </div>

                    {/* Saved moments gallery button */}
                    {savedMoments.length > 0 && (
                        <button
                            className={styles.galleryToggleBtn}
                            onClick={() => setShowGallery(!showGallery)}
                        >
                            📸 {t('storyline.myMoments')} ({savedMoments.length})
                        </button>
                    )}
                </div>
            </div>

            {/* Moments Gallery */}
            {showGallery && savedMoments.length > 0 && (
                <div className={styles.momentsGallery}>
                    <div className={styles.galleryHeader}>
                        <h3 className={styles.galleryTitle}>📷 {t('storyline.myMoments')}</h3>
                        <button className={styles.closeButton} onClick={() => setShowGallery(false)}>✕</button>
                    </div>
                    <div className={styles.galleryGrid}>
                        {savedMoments.map((moment, i) => (
                            <div key={i} className={styles.momentCard}>
                                <img src={moment.photoDataUrl} alt={moment.placeName} className={styles.momentPhoto} />
                                <div className={styles.momentInfo}>
                                    <span className={styles.momentPlace}>{moment.placeName}</span>
                                    <span className={styles.momentDate}>
                                        {new Date(moment.timestamp).toLocaleDateString('vi-VN', {
                                            day: '2-digit', month: '2-digit', year: 'numeric',
                                            hour: '2-digit', minute: '2-digit',
                                        })}
                                    </span>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Original Map Area */}
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
                        <p>{t('storyline.loading')}</p>
                    </div>
                ) : (
                    <div className={styles.questGrid}>
                        {tasks.map((task, index) => {
                            const stepNum = index + 1;
                            const isCompleted = task.status === 'completed' || currentStep > stepNum;
                            const isActive = !isCompleted && currentStep === stepNum;
                            const isLocked = !isCompleted && !isActive;
                            const rotation = ROTATIONS[index % ROTATIONS.length];
                            const placeMoments = getMomentsForPlace(task.task_id);

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
                                                    onClick={() => handleKhamPha(stepNum, task, index)}
                                                    disabled={generatingTask}
                                                >
                                                    {generatingTask ? t('storyline.aiGenerating') : t('storyline.explore')}
                                                </button>
                                            )}
                                            {/* Show moment count badge for completed places */}
                                            {isCompleted && placeMoments.length > 0 && (
                                                <div className={styles.momentBadge}>
                                                    📸 {placeMoments.length}
                                                </div>
                                            )}
                                        </div>
                                        {isLocked && (
                                            <div className={styles.lockedOverlay}>
                                                <span className={styles.lockedIcon}>🔒</span>
                                                <span style={{ fontFamily: 'Dancing Script', fontSize: '1.5rem', color: '#4a3219' }}>
                                                    {t('storyline.locked')}
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

            {/* ═══════════ Place Detail Card (NEW) ═══════════ */}
            {selectedPlace && (
                <div
                    className={styles.placeOverlay}
                    onClick={(e) => e.target === e.currentTarget && closePlaceCard()}
                >
                    <div className={styles.placeCard}>
                        {/* Card Header with image */}
                        <div className={styles.placeCardImageWrapper}>
                            <img
                                src={QUEST_IMAGES[selectedPlace.index % QUEST_IMAGES.length]}
                                alt={selectedPlace.task.title}
                                className={styles.placeCardImage}
                            />
                            <div className={styles.placeCardImageOverlay} />
                            <button className={styles.placeCardClose} onClick={closePlaceCard}>✕</button>
                            <div className={styles.placeCardTitleArea}>
                                <span className={styles.placeCardStep}>
                                    {t('storyline.step')} {selectedPlace.index + 1}
                                </span>
                                <h2 className={styles.placeCardTitle}>{selectedPlace.task.title}</h2>
                            </div>
                        </div>

                        {/* Card Body */}
                        <div className={styles.placeCardBody}>
                            {/* Description */}
                            <p className={styles.placeCardDesc}>{selectedPlace.task.description}</p>

                            {/* Cultural Explanation */}
                            {selectedPlace.task.cultural_explanation && (
                                <div className={styles.culturalBox}>
                                    <div className={styles.culturalLabel}>
                                        🏛️ {t('storyline.culturalKnowledge')}
                                    </div>
                                    <p className={styles.culturalText}>
                                        {selectedPlace.task.cultural_explanation}
                                    </p>
                                </div>
                            )}

                            {/* Completion Requirement */}
                            {selectedPlace.task.completion_requirement && (
                                <div className={styles.requirementBox}>
                                    <div className={styles.requirementLabel}>
                                        🎯 {t('storyline.completionRequirement')}
                                    </div>
                                    <p className={styles.requirementText}>
                                        {selectedPlace.task.completion_requirement}
                                    </p>
                                </div>
                            )}

                            {/* Difficulty badge */}
                            <div className={styles.placeCardMeta}>
                                <span className={`${styles.difficultyBadge} ${styles[`difficulty_${selectedPlace.task.difficulty}`]}`}>
                                    {diffLabel(selectedPlace.task.difficulty)}
                                </span>
                            </div>

                            {/* Camera Check-in Section */}
                            <div className={styles.checkinSection}>
                                <div className={styles.checkinHeader}>
                                    <span className={styles.checkinIcon}>📷</span>
                                    <h3 className={styles.checkinTitle}>{t('storyline.checkinTitle')}</h3>
                                </div>
                                <p className={styles.checkinDesc}>{t('storyline.checkinDesc')}</p>

                                {/* Camera view */}
                                {cameraActive && !capturedPhoto && (
                                    <div className={styles.cameraContainer}>
                                        <video
                                            ref={videoRef}
                                            className={styles.cameraVideo}
                                            autoPlay
                                            playsInline
                                            muted
                                        />
                                        <div className={styles.cameraControls}>
                                            <button className={styles.captureBtn} onClick={capturePhoto}>
                                                <span className={styles.captureBtnInner} />
                                            </button>
                                        </div>
                                    </div>
                                )}

                                {/* Captured photo preview */}
                                {capturedPhoto && (
                                    <div className={styles.capturedPreview}>
                                        <img src={capturedPhoto} alt="Captured" className={styles.capturedImage} />
                                        <div className={styles.capturedActions}>
                                            <button className={styles.retakeBtn} onClick={retakePhoto}>
                                                🔄 {t('storyline.retake')}
                                            </button>
                                        </div>
                                    </div>
                                )}

                                {/* Camera error */}
                                {cameraError && (
                                    <div className={styles.cameraErrorBox}>
                                        <span>⚠️ {cameraError}</span>
                                    </div>
                                )}

                                {/* Start camera button (when camera is not active and no photo captured) */}
                                {!cameraActive && !capturedPhoto && (
                                    <button className={styles.startCameraBtn} onClick={startCamera}>
                                        📸 {t('storyline.openCamera')}
                                    </button>
                                )}
                            </div>

                            {/* Save & Complete button */}
                            <button
                                className={`${styles.completeButton} ${!capturedPhoto ? styles.completeButtonDisabled : ''}`}
                                onClick={saveMomentAndComplete}
                                disabled={!capturedPhoto || savingMoment}
                            >
                                {savingMoment
                                    ? t('storyline.saving')
                                    : capturedPhoto
                                        ? `✓ ${t('storyline.saveAndComplete')}`
                                        : `📷 ${t('storyline.takePhotoFirst')}`
                                }
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Hidden canvas for photo capture */}
            <canvas ref={canvasRef} style={{ display: 'none' }} />

            {/* Task Detail Modal (AI generated task after completion) */}
            {activeTask && (
                <div
                    className={styles.taskOverlay}
                    onClick={(e) => e.target === e.currentTarget && setActiveTask(null)}
                >
                    <div className={styles.taskModal}>
                        <div className={styles.taskModalHeader}>
                            <div className={styles.taskBadges}>
                                <span className={`${styles.difficultyBadge} ${styles[`difficulty_${activeTask.difficulty}`]}`}>
                                    {diffLabel(activeTask.difficulty)}
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
                            <div className={styles.culturalLabel}>{t('storyline.culturalKnowledge')}</div>
                            <p className={styles.culturalText}>{activeTask.cultural_explanation}</p>
                        </div>

                        <div className={styles.requirementBox}>
                            <div className={styles.requirementLabel}>{t('storyline.completionRequirement')}</div>
                            <p className={styles.requirementText}>{activeTask.completion_requirement}</p>
                        </div>

                        <button className={styles.completeButton} onClick={handleComplete}>
                            {t('storyline.completeBtn')} ✓
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}

function getCurrentPosition(): Promise<GeolocationPosition> {
    return new Promise((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(resolve, reject, {
            enableHighAccuracy: true,
            timeout: 8000,
            maximumAge: 0,
        });
    });
}
