'use client';

import { useEffect, useRef, useCallback } from 'react';

/* ───────────────────────────────────────────────────────
 *  DragonCanvas – slices the dragon PNG into thin vertical
 *  strips and repositions them along a sine-wave spine,
 *  creating a realistic undulating serpentine motion.
 * ─────────────────────────────────────────────────────── */

interface DragonCanvasProps {
  className?: string;
}

/* ── Tuning ── */
const SLICE_WIDTH = 4;          // px width of each vertical strip
const WAVE_AMP = 30;            // sine wave amplitude in px
const WAVE_LENGTH = 300;        // sine wavelength in px
const DRAGON_SCALE = 0.38;      // scale of the dragon image
const GLOW_COLOR = 'rgba(230, 180, 34, 0.12)';
const GLOW_RADIUS = 40;

interface DragonInstance {
  x: number;                     // current head x
  baseY: number;                 // vertical center line
  speed: number;                 // px per frame
  dir: 1 | -1;                  // 1=right, -1=left
  scale: number;
  opacity: number;
  phase: number;                // wave phase offset
  waveAmp: number;
  waveLen: number;
}

function makeDragon(
  canvasW: number, canvasH: number,
  dir: 1 | -1, yFrac: number, scale: number,
): DragonInstance {
  const imgW = 1024 * scale;     // approx image width after scale
  const startX = dir === 1 ? -imgW - 100 : canvasW + 100;
  return {
    x: startX,
    baseY: canvasH * yFrac,
    speed: 0.8 + Math.random() * 0.4,
    dir,
    scale,
    opacity: 0.22 + scale * 0.1,
    phase: Math.random() * Math.PI * 2,
    waveAmp: WAVE_AMP * scale,
    waveLen: WAVE_LENGTH * scale,
  };
}

export default function DragonCanvas({ className }: DragonCanvasProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const animRef = useRef<number>(0);
  const imgRef = useRef<HTMLImageElement | null>(null);
  const imgLoaded = useRef(false);
  const dragonsRef = useRef<DragonInstance[]>([]);

  /* ── Main draw loop ── */
  const draw = useCallback((
    ctx: CanvasRenderingContext2D,
    w: number, h: number,
    img: HTMLImageElement,
  ) => {
    ctx.clearRect(0, 0, w, h);
    const iw = img.naturalWidth;
    const ih = img.naturalHeight;

    for (const dragon of dragonsRef.current) {
      const { dir, scale, opacity, waveAmp, waveLen } = dragon;

      /* advance position */
      dragon.x += dragon.speed * dir;
      dragon.phase += 0.018;

      const drawW = iw * scale * DRAGON_SCALE;
      const drawH = ih * scale * DRAGON_SCALE;
      const sliceCount = Math.ceil(drawW / SLICE_WIDTH);

      ctx.save();
      ctx.globalAlpha = opacity;

      /* If dragon goes right-to-left, flip the image */
      const flipX = dir === -1;

      for (let i = 0; i < sliceCount; i++) {
        /* source slice in original image */
        const srcX = (i * SLICE_WIDTH) / (drawW) * iw;
        const srcW = (SLICE_WIDTH / drawW) * iw;

        /* destination x position */
        let destX: number;
        if (flipX) {
          destX = dragon.x - i * SLICE_WIDTH;
        } else {
          destX = dragon.x + i * SLICE_WIDTH;
        }

        /* sine wave offset for this slice */
        const normalizedI = i / sliceCount;
        const sineY = Math.sin(
          dragon.phase + normalizedI * Math.PI * 2 * (drawW / waveLen)
        ) * waveAmp;

        /* subtle vertical squeeze/stretch for depth illusion */
        const scaleY = 1 + Math.sin(
          dragon.phase + normalizedI * Math.PI * 4
        ) * 0.03;

        const destY = dragon.baseY + sineY - (drawH * scaleY) / 2;

        /* cull off-screen slices */
        if (destX + SLICE_WIDTH < -20 || destX > w + 20) continue;

        ctx.drawImage(
          img,
          srcX, 0, srcW, ih,                         // source
          destX, destY, SLICE_WIDTH, drawH * scaleY,  // dest
        );
      }

      /* ── golden glow trail along the spine ── */
      ctx.globalAlpha = opacity * 0.3;
      ctx.globalCompositeOperation = 'lighter';
      const glowSteps = 12;
      for (let g = 0; g < glowSteps; g++) {
        const t = g / glowSteps;
        const gx = flipX
          ? dragon.x - t * drawW
          : dragon.x + t * drawW;
        const gy = dragon.baseY + Math.sin(
          dragon.phase + t * Math.PI * 2 * (drawW / waveLen)
        ) * waveAmp;

        const grad = ctx.createRadialGradient(gx, gy, 0, gx, gy, GLOW_RADIUS * scale);
        grad.addColorStop(0, GLOW_COLOR);
        grad.addColorStop(1, 'rgba(230, 180, 34, 0)');
        ctx.fillStyle = grad;
        ctx.beginPath();
        ctx.arc(gx, gy, GLOW_RADIUS * scale, 0, Math.PI * 2);
        ctx.fill();
      }
      ctx.globalCompositeOperation = 'source-over';

      ctx.restore();

      /* ── respawn when fully off-screen ── */
      const margin = drawW + 200;
      const allOff = dir === 1
        ? dragon.x - drawW > w + margin
        : dragon.x + drawW < -margin;
      if (allOff) {
        Object.assign(dragon, makeDragon(
          w, h, dir,
          0.25 + Math.random() * 0.5,
          scale,
        ));
      }
    }
  }, []);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    /* load dragon image */
    if (!imgRef.current) {
      const img = new Image();
      img.src = '/images/navbar-dragon.png';
      img.onload = () => {
        imgLoaded.current = true;
      };
      imgRef.current = img;
    }

    const resize = () => {
      const parent = canvas.parentElement;
      if (!parent) return;
      const dpr = window.devicePixelRatio || 1;
      const w = parent.clientWidth;
      const h = parent.clientHeight;
      canvas.width = w * dpr;
      canvas.height = h * dpr;
      canvas.style.width = `${w}px`;
      canvas.style.height = `${h}px`;
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

      if (dragonsRef.current.length === 0) {
        dragonsRef.current = [
          makeDragon(w, h, 1, 0.35, 1.0),
          makeDragon(w, h, -1, 0.65, 0.75),
        ];
      }
    };

    resize();
    window.addEventListener('resize', resize);

    /* respect reduced-motion */
    const mq = window.matchMedia('(prefers-reduced-motion: reduce)');
    if (mq.matches) return;

    let running = true;
    const loop = () => {
      if (!running) return;
      const parent = canvas.parentElement;
      if (parent && imgRef.current && imgLoaded.current) {
        draw(ctx, parent.clientWidth, parent.clientHeight, imgRef.current);
      }
      animRef.current = requestAnimationFrame(loop);
    };
    animRef.current = requestAnimationFrame(loop);

    return () => {
      running = false;
      cancelAnimationFrame(animRef.current);
      window.removeEventListener('resize', resize);
    };
  }, [draw]);

  return (
    <canvas
      ref={canvasRef}
      className={className}
      aria-hidden="true"
      style={{
        position: 'absolute',
        inset: 0,
        pointerEvents: 'none',
        zIndex: 0,
        mixBlendMode: 'screen',
      }}
    />
  );
}
