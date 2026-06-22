'use client';
/* eslint-disable react-hooks/set-state-in-effect, @next/next/no-img-element */

import React, { useState, useEffect, useRef } from 'react';
import { useI18n } from '@/i18n';
import { questionPoolApi, storylineApi, type QuestionPoolItem, type VerifyCaptureResponse } from '@/lib/api';
import styles from './path.module.css';

const STORAGE_KEY = 'bevietnam-completed-questions';
// Winding amplitude for the zig-zag trail (px).
const SWING = 92;

function loadCompleted(): string[] {
  if (typeof window === 'undefined') return [];
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
  } catch {
    return [];
  }
}

function readFileAsDataUrl(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = () => reject(reader.error);
    reader.readAsDataURL(file);
  });
}

export default function StorylinePage() {
  const { t } = useI18n();
  const [questions, setQuestions] = useState<QuestionPoolItem[]>([]);
  const [completedIds, setCompletedIds] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [isClient, setIsClient] = useState(false);

  const [selected, setSelected] = useState<number | null>(null);
  const [preview, setPreview] = useState<string | null>(null);
  const [verifying, setVerifying] = useState(false);
  const [result, setResult] = useState<VerifyCaptureResponse | null>(null);
  const cameraRef = useRef<HTMLInputElement>(null);
  const uploadRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    setIsClient(true);
    setCompletedIds(loadCompleted());
    (async () => {
      const res = await questionPoolApi.list();
      if (res.data && res.data.items.length > 0) {
        setQuestions(res.data.items);
      } else if (res.error) {
        setError(true);
      }
      setLoading(false);
    })();
  }, []);

  const activeIndex = questions.findIndex((q) => !completedIds.includes(q.question_id));
  const currentActive = activeIndex === -1 ? questions.length : activeIndex;
  const completedCount = completedIds.filter((id) => questions.some((q) => q.question_id === id)).length;
  const percent = questions.length ? Math.round((completedCount / questions.length) * 100) : 0;

  const stateOf = (index: number): 'done' | 'active' | 'locked' => {
    if (completedIds.includes(questions[index].question_id)) return 'done';
    if (index === currentActive) return 'active';
    return 'locked';
  };

  function openNode(index: number) {
    if (stateOf(index) === 'locked') return;
    setSelected(index);
    setPreview(null);
    setResult(null);
    setVerifying(false);
  }

  function closeSheet() {
    setSelected(null);
    setPreview(null);
    setResult(null);
    setVerifying(false);
  }

  async function onPickFile(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    setResult(null);
    setPreview(await readFileAsDataUrl(file));
  }

  async function submitCapture() {
    if (selected === null || !preview) return;
    const q = questions[selected];
    setVerifying(true);
    setResult(null);

    const res = await storylineApi.verifyCapture({
      user_id: 'demo-user',
      task: {
        question_id: q.question_id,
        title: q.title,
        question_text: q.question_text,
        cultural_explanation: q.cultural_explanation,
        place_name: q.place_name,
        difficulty: q.difficulty,
        required_media: q.required_media,
        categories: q.categories,
      },
      capture: {
        media_url: preview,
        place_id: q.place_name || q.question_id,
        note: '',
      },
    });
    setVerifying(false);

    if (res.error || !res.data) {
      setResult({ approved: false, status: 'error', reason: t('storyline.verifyError'), confidence: 0 });
      return;
    }
    setResult(res.data);

    if (res.data.approved) {
      const next = [...completedIds, q.question_id];
      setCompletedIds(next);
      localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
    }
  }

  const diffBadgeClass = (d: string) =>
    d === 'easy' ? styles.badgeEasy : d === 'hard' ? styles.badgeHard : styles.badgeMedium;

  // ── Header (shared across states) ──
  const header = (
    <>
      <header className={styles.intro}>
        <div className={styles.introEyebrow}>{t('storyline.tag')}</div>
        <h1 className={styles.introTitle}>
          {t('storyline.title')} <span>{t('storyline.titleHighlight')}</span>
        </h1>
        <p className={styles.introSub}>{t('storyline.subtitle')}</p>
      </header>

      {isClient && questions.length > 0 && (
        <div className={styles.topBar}>
          <div className={styles.crest}>⛩</div>
          <div className={styles.barMeta}>
            <div className={styles.barTitle}>{t('storyline.progress')}</div>
            <div className={styles.barTrack}>
              <div className={styles.barFill} style={{ width: `${Math.max(percent, 4)}%` }} />
            </div>
          </div>
          <div className={styles.barCount}>{completedCount}/{questions.length}</div>
        </div>
      )}
    </>
  );

  if (!isClient || loading) {
    return (
      <main className={styles.page}>
        {header}
        <div className={styles.skeletonTrail}>
          {Array.from({ length: 5 }).map((_, i) => (
            <div
              key={i}
              className={styles.skeletonNode}
              style={{ transform: `translateX(${Math.round(Math.sin(i * 0.8) * SWING)}px)` }}
            />
          ))}
        </div>
      </main>
    );
  }

  if (error || questions.length === 0) {
    return (
      <main className={styles.page}>
        {header}
        <div className={styles.center}>
          <p>{error ? t('storyline.loadError') : t('storyline.emptyPool')}</p>
        </div>
      </main>
    );
  }

  const sel = selected !== null ? questions[selected] : null;

  return (
    <main className={styles.page}>
      {header}

      <ol className={styles.trail}>
        {questions.map((q, index) => {
          const state = stateOf(index);
          const offset = Math.round(Math.sin(index * 0.8) * SWING);
          return (
            <li key={q.question_id} style={{ transform: `translateX(${offset}px)` }}>
              <button
                className={`${styles.node} ${styles[state]}`}
                style={{ transform: 'none' }}
                onClick={() => openNode(index)}
                disabled={state === 'locked'}
                aria-label={`${q.title}${state === 'locked' ? ` — ${t('storyline.locked')}` : ''}`}
              >
                {state === 'active' && <span className={styles.startPill}>{t('storyline.start')}</span>}
                <span className={styles.disc}>
                  {state === 'done' ? '✓' : state === 'locked' ? '🔒' : index + 1}
                </span>
                <span className={styles.nodeLabel}>{q.title}</span>
              </button>
            </li>
          );
        })}
      </ol>

      {sel && (
        <div className={styles.overlay} onClick={(e) => e.target === e.currentTarget && closeSheet()}>
          <div className={styles.sheet}>
            <button className={styles.close} onClick={closeSheet} aria-label={t('common.close')}>✕</button>
            <div className={styles.sheetHandle} />

            {sel.place_name && <div className={styles.sheetEyebrow}>{sel.place_name}</div>}
            <h2 className={styles.sheetTitle}>{sel.title}</h2>

            <div className={styles.metaRow}>
              <span className={`${styles.badge} ${diffBadgeClass(sel.difficulty)}`}>
                {t(`storyline.${sel.difficulty}`) || sel.difficulty}
              </span>
              <span className={styles.badge}>⏱ {sel.estimated_duration_minutes}′</span>
              {sel.categories.slice(0, 2).map((c) => (
                <span key={c} className={styles.badge}>{c}</span>
              ))}
            </div>

            <p className={styles.question}>{sel.question_text}</p>

            {sel.cultural_explanation && (
              <div className={styles.cultural}>
                <div className={styles.culturalLabel}>{t('storyline.culturalKnowledge')}</div>
                <p className={styles.culturalText}>{sel.cultural_explanation}</p>
              </div>
            )}

            <section className={styles.capture}>
              <div className={styles.captureHead}>{t('storyline.checkinTitle')}</div>
              <p className={styles.captureHint}>{t('storyline.checkinDesc')}</p>

              <input
                ref={cameraRef}
                type="file"
                accept="image/*"
                capture="environment"
                className={styles.hiddenInput}
                onChange={onPickFile}
              />
              <input
                ref={uploadRef}
                type="file"
                accept="image/*"
                className={styles.hiddenInput}
                onChange={onPickFile}
              />

              {preview && (
                <div className={styles.preview}>
                  <img src={preview} alt={sel.title} className={styles.previewImg} />
                  <button className={styles.retake} onClick={() => uploadRef.current?.click()}>
                    {t('storyline.retake')}
                  </button>
                </div>
              )}

              <div className={styles.btnRow}>
                {!preview ? (
                  <>
                    <button className={`${styles.btn} ${styles.btnPrimary}`} onClick={() => cameraRef.current?.click()}>
                      {t('storyline.capture')}
                    </button>
                    <button className={`${styles.btn} ${styles.btnGhost}`} onClick={() => uploadRef.current?.click()}>
                      {t('storyline.upload')}
                    </button>
                  </>
                ) : (
                  <button
                    className={`${styles.btn} ${styles.btnPrimary}`}
                    onClick={submitCapture}
                    disabled={verifying || result?.approved}
                  >
                    {verifying
                      ? <><span className={styles.spinner} />{t('storyline.verifying')}</>
                      : result?.approved
                        ? `✓ ${t('storyline.completed')}`
                        : t('storyline.submitProof')}
                  </button>
                )}
              </div>

              {result && (
                <div
                  className={`${styles.result} ${
                    result.approved
                      ? styles.resultApproved
                      : result.status === 'needs_review'
                        ? styles.resultReview
                        : styles.resultRejected
                  }`}
                >
                  <div className={styles.resultTitle}>
                    {result.approved
                      ? t('storyline.proofApproved')
                      : result.status === 'needs_review'
                        ? t('storyline.proofReview')
                        : t('storyline.proofRejected')}
                  </div>
                  {result.reason && <span>{result.reason} </span>}
                  {result.confidence > 0 && (
                    <span className={styles.confidence}>({Math.round(result.confidence * 100)}%)</span>
                  )}
                </div>
              )}

              {result?.approved && (
                <div className={styles.btnRow}>
                  <button className={`${styles.btn} ${styles.btnGhost}`} onClick={closeSheet}>
                    {t('storyline.continue')}
                  </button>
                </div>
              )}
            </section>
          </div>
        </div>
      )}
    </main>
  );
}
