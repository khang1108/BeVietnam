'use client';
/* eslint-disable react-hooks/set-state-in-effect, @next/next/no-img-element */

import React, { useState, useEffect } from 'react';
import { useI18n } from '@/i18n';
import { mockTasks, Task } from '@/lib/mockTasks';
import styles from './page.module.css';

const numerals = ["I", "II", "III", "IV", "V"];
const numbersVi = ["Một", "Hai", "Ba", "Tư", "Năm"];
const numbersEn = ["One", "Two", "Three", "Four", "Five"];

export default function StorylinePage() {
  const { t, locale } = useI18n();
  const [completedTaskIds, setCompletedTaskIds] = useState<string[]>([]);
  const [activeTaskId, setActiveTaskId] = useState<string>("1");
  const [isClient, setIsClient] = useState(false);

  // Initialize state from localStorage
  useEffect(() => {
    setIsClient(true);
    const savedCompleted = localStorage.getItem('bevietnam-completed-tasks');
    const savedActive = localStorage.getItem('bevietnam-active-task');

    if (savedCompleted) {
      setCompletedTaskIds(JSON.parse(savedCompleted));
    }
    if (savedActive) {
      setActiveTaskId(savedActive);
    }
  }, []);

  const isTaskLocked = (id: string): boolean => {
    const idx = mockTasks.findIndex(t => t.id === id);
    if (idx === 0) return false;
    const prevTask = mockTasks[idx - 1];
    return !completedTaskIds.includes(prevTask.id);
  };

  const handleSelectPage = (id: string) => {
    if (isTaskLocked(id)) return;
    setActiveTaskId(id);
    localStorage.setItem('bevietnam-active-task', id);
  };

  const handleCompleteTask = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    const updated = [...completedTaskIds];
    if (!updated.includes(id)) {
      updated.push(id);
      setCompletedTaskIds(updated);
      localStorage.setItem('bevietnam-completed-tasks', JSON.stringify(updated));
    }

    // Unlock and activate next task
    const idx = mockTasks.findIndex(t => t.id === id);
    if (idx < mockTasks.length - 1) {
      const nextId = mockTasks[idx + 1].id;
      setActiveTaskId(nextId);
      localStorage.setItem('bevietnam-active-task', nextId);
    }
  };

  const handleReset = () => {
    setCompletedTaskIds([]);
    setActiveTaskId("1");
    localStorage.removeItem('bevietnam-completed-tasks');
    localStorage.removeItem('bevietnam-active-task');
  };

  const getDifficultyLabel = (diff: Task['difficulty']) => {
    if (diff === 'EASY') return t('storyline.easy');
    if (diff === 'MEDIUM') return t('storyline.medium');
    return t('storyline.hard');
  };

  if (!isClient) {
    return (
      <div className={styles.container}>
        <div className={styles.headerPanel}>
          <h1>{t('storyline.title')}</h1>
          <p>{t('storyline.subtitle')}</p>
        </div>
        <div style={{ textAlign: 'center', padding: '100px 50px', color: '#e2ddd5' }}>
          {t('common.loading')}
        </div>
      </div>
    );
  }

  const completionPercentage = (completedTaskIds.length / mockTasks.length) * 100;

  return (
    <div className={`${styles.container} ${locale === 'en' ? 'en-locale' : ''}`}>
      <div className={styles.headerPanel}>
        <h1>{t('storyline.title')}</h1>
        <p>{t('storyline.subtitle')}</p>
      </div>

      <div className={styles.controls}>
        <button className={styles.resetBtn} onClick={handleReset}>
          {t('storyline.reset')}
        </button>
        
        <div className={styles.progressBarContainer}>
          <div 
            className={styles.progressBarFill} 
            style={{ width: `${Math.max(completionPercentage, 8)}%` }}
          ></div>
        </div>
      </div>

      <div className={styles.storybook}>
        {mockTasks.map((task, index) => {
          const isCompleted = completedTaskIds.includes(task.id);
          const isActive = activeTaskId === task.id;
          const isLocked = isTaskLocked(task.id);
          const chapterNumString = locale === 'vi' ? numbersVi[index] : numbersEn[index];

          const diffClass = task.difficulty === 'EASY' 
            ? styles.diffEasy 
            : task.difficulty === 'MEDIUM' 
              ? styles.diffMedium 
              : styles.diffHard;

          return (
            <div 
              key={task.id}
              className={`${styles.storybookPage} ${isActive ? styles.active : ''} ${isLocked ? styles.locked : ''} ${isCompleted ? styles.completed : ''}`}
              onClick={() => handleSelectPage(task.id)}
            >
              {isActive ? (
                /* Expanded Page Content */
                <div className={styles.pageWrapper}>
                  <div className={styles.openContent}>
                    <div className={styles.pageHeader}>
                      <div>
                        <div className={styles.chapterNum}>
                          {t('storyline.chapter')} {chapterNumString} • {locale === 'vi' ? task.location_vi : task.location_en}
                        </div>
                        <h2 className={styles.taskTitle}>
                          {locale === 'vi' ? task.title_vi : task.title_en}
                        </h2>
                      </div>
                      <span className={`${styles.taskDifficulty} ${diffClass}`}>
                        {getDifficultyLabel(task.difficulty)}
                      </span>
                    </div>

                    <div className={styles.taskDetailsGrid}>
                      <div className={styles.detailsLeft}>
                        <p className={styles.taskDesc}>
                          {locale === 'vi' ? task.description_vi : task.description_en}
                        </p>
                        
                        <div className={styles.culturalContext}>
                          <p className={styles.culturalText}>
                            {locale === 'vi' ? task.culturalExplanation_vi : task.culturalExplanation_en}
                          </p>
                        </div>

                        <div className={styles.requirementBox}>
                          <div className={styles.reqLabel}>{t('storyline.requirement')}</div>
                          <div className={styles.reqText}>
                            {locale === 'vi' ? task.completionRequirement_vi : task.completionRequirement_en}
                          </div>
                        </div>
                      </div>

                      <div className={styles.detailsRight}>
                        <div className={styles.polaroidFrame}>
                          <img 
                            className={styles.polaroidImage} 
                            src={task.image} 
                            alt={locale === 'vi' ? task.title_vi : task.title_en}
                          />
                          <div className={styles.polaroidCaption}>
                            {locale === 'vi' ? task.title_vi : task.title_en} - {locale === 'vi' ? task.location_vi : task.location_en}
                          </div>
                        </div>

                        <div className={styles.actionContainer}>
                          {isCompleted ? (
                            <button className={`${styles.completeBtn} ${styles.done}`} disabled>
                              {t('storyline.completed')}
                            </button>
                          ) : (
                            <button 
                              className={styles.completeBtn} 
                              onClick={(e) => handleCompleteTask(task.id, e)}
                            >
                              {t('storyline.completeTask')}
                            </button>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              ) : (
                /* Collapsed Page/Fold Header */
                <div className={styles.pageWrapper}>
                  <div className={styles.closedHeader}>
                    <span className={styles.pageDot}>
                      {isCompleted ? '✓' : numerals[index]}
                    </span>
                    <span>
                      {t('storyline.chapter')} {chapterNumString}: {locale === 'vi' ? task.title_vi : task.title_en}
                    </span>
                    {isLocked ? (
                      <span className={styles.lockStamp}>{t('storyline.locked')}</span>
                    ) : isCompleted ? (
                      <span className={styles.closedIndicator} style={{ color: '#2e7d32' }}>
                        {t('storyline.done')}
                      </span>
                    ) : (
                      <span className={styles.closedIndicator}>
                        {t('storyline.doing')}
                      </span>
                    )}
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
