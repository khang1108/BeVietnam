'use client';
/* eslint-disable react-hooks/set-state-in-effect */

import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useI18n } from '@/i18n';
import { mockTasks, Task } from '@/lib/mockTasks';
import { questionPoolApi, SelectedQuestion } from '@/lib/api';
import styles from './page.module.css';

const numbersVi = ['Một', 'Hai', 'Ba', 'Tư', 'Năm'];
const numbersEn = ['One', 'Two', 'Three', 'Four', 'Five'];
const USER_ID = 'demo-user';
const CHALLENGE_COUNT = 5;
const MATCH_CANDIDATE_COUNT = 10;

type LocationStatus = 'idle' | 'locating' | 'ready' | 'denied' | 'unavailable' | 'fallback';

interface Coordinates {
  latitude: number;
  longitude: number;
}

const MAP_NODE_SLOTS = [
  { x: '44%', y: '18%' },
  { x: '58%', y: '52%' },
  { x: '61%', y: '56%' },
  { x: '47%', y: '24%' },
  { x: '53%', y: '48%' },
];

const PATH_POINTS = [
  { x: 44, y: 18 },
  { x: 58, y: 52 },
  { x: 61, y: 56 },
  { x: 47, y: 24 },
  { x: 53, y: 48 },
];

const CATEGORY_IMAGES: Record<string, string> = {
  architecture: '/images/citadel-thanglong.png',
  art: '/images/event-art.png',
  festival: '/images/event-hue.png',
  food: '/images/banhmi-phuong.png',
  history: '/images/hero-hue-citadel.png',
  nature: '/images/halong-bay.png',
  religion: '/images/one-pillar-pagoda.png',
  tradition: '/images/water-puppet.png',
};

function safeParseStringArray(value: string | null): string[] {
  if (!value) return [];
  try {
    const parsed = JSON.parse(value);
    return Array.isArray(parsed) ? parsed.filter((item): item is string => typeof item === 'string') : [];
  } catch {
    return [];
  }
}

function shuffle<T>(items: T[]): T[] {
  const copy = [...items];
  for (let i = copy.length - 1; i > 0; i -= 1) {
    const j = Math.floor(Math.random() * (i + 1));
    [copy[i], copy[j]] = [copy[j], copy[i]];
  }
  return copy;
}

function normalizeDifficulty(value: string): Task['difficulty'] {
  const normalized = value.toLowerCase();
  if (normalized === 'medium') return 'MEDIUM';
  if (normalized === 'hard') return 'HARD';
  return 'EASY';
}

function getImageForQuestion(selected: SelectedQuestion, index: number): string {
  const question = selected.question;
  const categories = question.categories.map((category) => category.toLowerCase());
  const categoryImage = categories.map((category) => CATEGORY_IMAGES[category]).find(Boolean);
  if (categoryImage) return categoryImage;

  const place = (question.place_name || '').toLowerCase();
  if (place.includes('huế') || place.includes('hue')) return '/images/hero-hue-citadel.png';
  if (place.includes('hội an') || place.includes('hoi an')) return '/images/hoian-lanterns.png';
  if (place.includes('hạ long') || place.includes('ha long')) return '/images/halong-bay.png';

  return mockTasks[index % mockTasks.length]?.image || '/images/hero-hue-citadel.png';
}

function completionRequirementVi(requiredMedia: string): string {
  if (requiredMedia === 'note') return 'Ghi lại câu trả lời hoặc quan sát của bạn trên ứng dụng';
  if (requiredMedia === 'quiz_answer') return 'Trả lời câu hỏi thử thách trên ứng dụng';
  return 'Chụp ảnh minh chứng và gửi lên ứng dụng';
}

function completionRequirementEn(requiredMedia: string): string {
  if (requiredMedia === 'note') return 'Record your answer or observation in the app';
  if (requiredMedia === 'quiz_answer') return 'Answer the challenge question in the app';
  return 'Take a proof photo and upload it';
}

function mapSelectedQuestionToTask(selected: SelectedQuestion, index: number): Task {
  const question = selected.question;
  const placeName = question.place_name || 'Việt Nam';

  return {
    id: question.question_id,
    title_vi: question.title,
    title_en: question.title,
    description_vi: question.question_text,
    description_en: question.question_text,
    culturalExplanation_vi: question.cultural_explanation,
    culturalExplanation_en: question.cultural_explanation,
    completionRequirement_vi: completionRequirementVi(question.required_media),
    completionRequirement_en: completionRequirementEn(question.required_media),
    difficulty: normalizeDifficulty(question.difficulty),
    image: getImageForQuestion(selected, index),
    location_vi: placeName,
    location_en: placeName,
  };
}

function firstAvailableTaskId(tasks: Task[], completedIds: string[]): string {
  const firstIncomplete = tasks.find((task) => !completedIds.includes(task.id));
  return firstIncomplete?.id || tasks[0]?.id || '';
}

function movedEnough(previous: Coordinates | null, next: Coordinates): boolean {
  if (!previous) return true;
  return (
    Math.abs(previous.latitude - next.latitude) > 0.001 ||
    Math.abs(previous.longitude - next.longitude) > 0.001
  );
}

export default function StorylinePage() {
  const { t, locale } = useI18n();
  const fallbackTasks = useMemo(() => mockTasks.slice(0, CHALLENGE_COUNT), []);
  const [tasks, setTasks] = useState<Task[]>(fallbackTasks);
  const [completedTaskIds, setCompletedTaskIds] = useState<string[]>([]);
  const [activeTaskId, setActiveTaskId] = useState<string>(fallbackTasks[0]?.id || '');
  const [isClient, setIsClient] = useState(false);
  const [loadingTasks, setLoadingTasks] = useState(true);
  const [locationStatus, setLocationStatus] = useState<LocationStatus>('idle');
  const [locationLabel, setLocationLabel] = useState('');
  const completedTaskIdsRef = useRef<string[]>([]);
  const lastLoadedCoordsRef = useRef<Coordinates | null>(null);

  useEffect(() => {
    setIsClient(true);
    const savedCompleted = safeParseStringArray(localStorage.getItem('bevietnam-completed-tasks'));
    const savedActive = localStorage.getItem('bevietnam-active-task');

    completedTaskIdsRef.current = savedCompleted;
    setCompletedTaskIds(savedCompleted);
    setActiveTaskId(savedActive || firstAvailableTaskId(fallbackTasks, savedCompleted));
  }, [fallbackTasks]);

  useEffect(() => {
    completedTaskIdsRef.current = completedTaskIds;
  }, [completedTaskIds]);

  const applyFallbackTasks = useCallback((
    completedIds = completedTaskIdsRef.current,
    status: LocationStatus = 'fallback',
  ) => {
    const nextTasks = shuffle(fallbackTasks).slice(0, CHALLENGE_COUNT);
    setTasks(nextTasks);
    setActiveTaskId(firstAvailableTaskId(nextTasks, completedIds));
    setLocationStatus(status);
    setLoadingTasks(false);
  }, [fallbackTasks]);

  const loadLocationTasks = useCallback(async (coords: Coordinates) => {
    setLoadingTasks(true);
    const res = await questionPoolApi.select({
      user_id: USER_ID,
      latitude: coords.latitude,
      longitude: coords.longitude,
      completed_question_ids: completedTaskIdsRef.current,
      limit: MATCH_CANDIDATE_COUNT,
    });

    if (!res.data || res.data.selected.length === 0) {
      applyFallbackTasks();
      return;
    }

    const randomizedTasks = shuffle(res.data.selected)
      .slice(0, CHALLENGE_COUNT)
      .map(mapSelectedQuestionToTask);

    setTasks(randomizedTasks);
    setActiveTaskId(firstAvailableTaskId(randomizedTasks, completedTaskIdsRef.current));
    setLocationLabel(
      res.data.context.formatted_address ||
      res.data.context.place_name ||
      `${coords.latitude.toFixed(4)}, ${coords.longitude.toFixed(4)}`
    );
    setLocationStatus(res.data.fallback ? 'fallback' : 'ready');
    setLoadingTasks(false);
  }, [applyFallbackTasks]);

  useEffect(() => {
    if (!isClient) return undefined;

    if (!('geolocation' in navigator)) {
      applyFallbackTasks(completedTaskIdsRef.current, 'unavailable');
      return undefined;
    }

    setLocationStatus('locating');
    const watchId = navigator.geolocation.watchPosition(
      (position) => {
        const nextCoords = {
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
        };

        if (!movedEnough(lastLoadedCoordsRef.current, nextCoords)) return;
        lastLoadedCoordsRef.current = nextCoords;
        setLocationStatus('ready');
        void loadLocationTasks(nextCoords);
      },
      (error) => {
        const nextStatus = error.code === error.PERMISSION_DENIED ? 'denied' : 'unavailable';
        applyFallbackTasks(completedTaskIdsRef.current, nextStatus);
      },
      {
        enableHighAccuracy: true,
        maximumAge: 60_000,
        timeout: 12_000,
      }
    );

    return () => navigator.geolocation.clearWatch(watchId);
  }, [isClient, loadLocationTasks, applyFallbackTasks]);

  const isTaskLocked = (id: string): boolean => {
    const idx = tasks.findIndex(task => task.id === id);
    if (idx < 0) return true;
    if (idx === 0) return false;
    const previousTask = tasks[idx - 1];
    return !completedTaskIds.includes(previousTask.id);
  };

  const handleSelectPage = (id: string) => {
    if (isTaskLocked(id)) return;
    setActiveTaskId(id);
    localStorage.setItem('bevietnam-active-task', id);
  };

  const handleCompleteTask = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    const updated = completedTaskIds.includes(id) ? completedTaskIds : [...completedTaskIds, id];
    setCompletedTaskIds(updated);
    localStorage.setItem('bevietnam-completed-tasks', JSON.stringify(updated));

    const idx = tasks.findIndex(task => task.id === id);
    if (idx < 0) return;
    if (idx < tasks.length - 1) {
      const nextId = tasks[idx + 1].id;
      setActiveTaskId(nextId);
      localStorage.setItem('bevietnam-active-task', nextId);
    }
  };

  const handleReset = () => {
    setCompletedTaskIds([]);
    completedTaskIdsRef.current = [];
    setActiveTaskId(tasks[0]?.id || '');
    localStorage.removeItem('bevietnam-completed-tasks');
    localStorage.removeItem('bevietnam-active-task');
  };

  const getDifficultyLabel = (diff: Task['difficulty']) => {
    if (diff === 'EASY') return t('storyline.easy');
    if (diff === 'MEDIUM') return t('storyline.medium');
    return t('storyline.hard');
  };

  const getLocationStatusLabel = () => {
    if (locationStatus === 'locating') return locale === 'vi' ? 'Đang định vị...' : 'Locating...';
    if (locationStatus === 'ready') return locationLabel || (locale === 'vi' ? 'Đã nhận vị trí' : 'Location ready');
    if (locationStatus === 'denied') return locale === 'vi' ? 'Vị trí chưa được cấp quyền' : 'Location permission denied';
    if (locationStatus === 'unavailable') return locale === 'vi' ? 'Không lấy được vị trí' : 'Location unavailable';
    if (locationStatus === 'fallback') return locale === 'vi' ? 'Đang dùng bộ thử thách dự phòng' : 'Using fallback challenges';
    return '';
  };

  const activeTask = tasks.find(task => task.id === activeTaskId) || tasks[0] || fallbackTasks[0];
  const activeIndex = Math.max(tasks.findIndex(task => task.id === activeTask.id), 0);
  const activeChapterNumString = (locale === 'vi' ? numbersVi[activeIndex] : numbersEn[activeIndex]) || String(activeIndex + 1);
  const activeTaskCompleted = completedTaskIds.includes(activeTask.id);
  const mapNodes = tasks.map((task, index) => {
    const slot = MAP_NODE_SLOTS[index % MAP_NODE_SLOTS.length];
    return {
      id: task.id,
      x: slot.x,
      y: slot.y,
      name_vi: task.location_vi,
      name_en: task.location_en,
    };
  });
  const completedVisibleCount = tasks.filter(task => completedTaskIds.includes(task.id)).length;
  const locationStatusLabel = getLocationStatusLabel();

  let drawPathD = '';
  if (completedVisibleCount > 0) {
    drawPathD = `M ${PATH_POINTS[0].x} ${PATH_POINTS[0].y}`;
    for (let i = 1; i <= completedVisibleCount && i < PATH_POINTS.length; i += 1) {
      drawPathD += ` L ${PATH_POINTS[i].x} ${PATH_POINTS[i].y}`;
    }
  }

  if (!isClient || loadingTasks) {
    return (
      <div className={styles.container}>
        <div className={styles.bgContainer}>
          <div className={styles.bgOverlay}></div>
        </div>
        <div style={{ textAlign: 'center', padding: '200px 50px', color: '#e2ddd5' }}>
          {t('common.loading')}
        </div>
      </div>
    );
  }

  return (
    <div className={`${styles.container} ${locale === 'en' ? 'en-locale' : ''}`}>
      <div className={styles.bgContainer}>
        <div className={styles.bgOverlay}></div>
      </div>

      <div className={styles.socialLinks}>
        <a className={styles.socialItem}>FB</a>
        <a className={styles.socialItem}>TW</a>
        <a className={styles.socialItem}>IG</a>
        <a className={styles.socialItem}>YT</a>
      </div>

      <div className={styles.mainLayout}>
        <div className={styles.leftColumn}>
          <div className={styles.tagline}>{t('storyline.kieuKyTag')}</div>
          <h1 className={styles.heroTitle}>
            {t('storyline.mainTitlePrefix')}
            <span className={styles.brushedText}>{t('storyline.mainTitleSuffix')}</span>
          </h1>
          <p className={styles.description}>
            {t('storyline.subTitle')}
          </p>
          {locationStatusLabel && (
            <div className={styles.locationStatus}>
              {locationStatusLabel}
            </div>
          )}
        </div>

        <div className={styles.rightColumn}>
          <div className={styles.mapContainer}>
            <svg viewBox="0 0 100 100" className={styles.mapSvg}>
              <path
                d="M 44,15 C 33,18 36,23 45,26 C 50,28 49,33 46,38 C 42,43 36,47 37,53 C 38,59 48,65 52,70 C 54,73 54,77 52,82 C 49,87 40,94 42,98 C 43,100 48,102 46,105 C 44,108 35,112 36,115"
                className={styles.mapOutline}
              />

              {drawPathD && (
                <path
                  d={drawPathD}
                  className={styles.activePath}
                />
              )}
            </svg>

            {mapNodes.map((coord) => {
              const isNodeCompleted = completedTaskIds.includes(coord.id);
              const isNodeActive = activeTaskId === coord.id;
              const isNodeLocked = isTaskLocked(coord.id);

              let nodeClass = styles.nodeLocked;
              if (isNodeCompleted) nodeClass = styles.nodeCompleted;
              else if (isNodeActive) nodeClass = styles.nodeActive;
              else if (!isNodeLocked) nodeClass = styles.nodeActive;

              return (
                <div
                  key={coord.id}
                  className={`${styles.mapNode} ${nodeClass}`}
                  style={{ left: coord.x, top: coord.y }}
                  onClick={() => handleSelectPage(coord.id)}
                >
                  <div className={styles.mapNodeInner}></div>
                  <span className={styles.nodeLabel} style={{ transform: 'translateY(-22px)' }}>
                    {locale === 'vi' ? coord.name_vi : coord.name_en}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      <div className={styles.bottomSection}>
        <div className={styles.glassCard}>
          <div className={styles.cardHeader}>
            <div className={styles.cardCategory}>
              {t('storyline.chapter')} {activeChapterNumString} • {locale === 'vi' ? activeTask.location_vi : activeTask.location_en}
            </div>
            <span className={`${styles.taskDifficulty} ${activeTask.difficulty === 'EASY' ? styles.diffEasy : activeTask.difficulty === 'MEDIUM' ? styles.diffMedium : styles.diffHard}`}>
              {getDifficultyLabel(activeTask.difficulty)}
            </span>
          </div>
          <h3 className={styles.cardTitle}>
            {locale === 'vi' ? activeTask.title_vi : activeTask.title_en}
          </h3>
          <p className={styles.cardText}>
            {locale === 'vi' ? activeTask.description_vi : activeTask.description_en}
          </p>

          <div className={styles.requirementBox}>
            <div className={styles.reqLabel}>{t('storyline.requirement')}</div>
            <div className={styles.reqText}>
              {locale === 'vi' ? activeTask.completionRequirement_vi : activeTask.completionRequirement_en}
            </div>
          </div>
        </div>

        <div className={styles.glassCard}>
          <div className={styles.cardHeader}>
            <div className={styles.cardCategory}>
              {locale === 'vi' ? 'BỐI CẢNH VĂN HÓA' : 'CULTURAL CONTEXT'}
            </div>
          </div>
          <div className={styles.culturalContextCard}>
            &quot;{locale === 'vi' ? activeTask.culturalExplanation_vi : activeTask.culturalExplanation_en}&quot;
          </div>
        </div>

        <div className={styles.glassCard}>
          <div className={styles.cardHeader}>
            <div className={styles.cardCategory}>
              {locale === 'vi' ? 'TIẾN TRÌNH HÀNH TRÌNH' : 'JOURNEY PROGRESS'}
            </div>
          </div>

          <div className={styles.checklist}>
            {tasks.map((task, idx) => {
              const isCompleted = completedTaskIds.includes(task.id);
              const isActive = activeTaskId === task.id;

              let itemClass = '';
              if (isCompleted) itemClass = styles.checkItemCompleted;
              else if (isActive) itemClass = styles.checkItemActive;

              return (
                <div
                  key={task.id}
                  className={`${styles.checkItem} ${itemClass}`}
                  onClick={() => handleSelectPage(task.id)}
                >
                  <span className={styles.checkMarker}>
                    {isCompleted ? '✓' : idx + 1}
                  </span>
                  <span>
                    {locale === 'vi' ? task.title_vi : task.title_en}
                  </span>
                </div>
              );
            })}
          </div>

          <div className={styles.actionContainer}>
            {activeTaskCompleted ? (
              <button className={`${styles.completeBtn} ${styles.done}`} disabled>
                {t('storyline.completed')}
              </button>
            ) : (
              <button
                className={styles.completeBtn}
                onClick={(e) => handleCompleteTask(activeTask.id, e)}
              >
                {t('storyline.completeTask')}
              </button>
            )}

            <button className={styles.resetLink} onClick={handleReset}>
              {t('storyline.reset')}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
