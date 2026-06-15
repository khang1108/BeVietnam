'use client';

import { useEffect, useRef } from 'react';
import * as THREE from 'three';

type DashboardCardEffectProps = {
  className: string;
};

const MAX_PIXEL_RATIO = 1.5;

function disposeScene(scene: THREE.Scene) {
  scene.traverse((object) => {
    const mesh = object as THREE.Mesh;
    mesh.geometry?.dispose();

    const material = mesh.material as THREE.Material | THREE.Material[] | undefined;
    if (Array.isArray(material)) {
      material.forEach((item) => item.dispose());
    } else {
      material?.dispose();
    }
  });
}

export default function DashboardCardEffect({ className }: DashboardCardEffectProps) {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    const card = canvas?.parentElement;
    if (!canvas || !card) return;

    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (reduceMotion) return;

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(34, 1, 0.1, 20);
    const renderer = new THREE.WebGLRenderer({
      canvas,
      alpha: true,
      antialias: true,
      powerPreference: 'low-power',
    });

    renderer.outputColorSpace = THREE.SRGBColorSpace;
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, MAX_PIXEL_RATIO));
    camera.position.set(0, 0, 5);

    let frameId = 0;
    let isRunning = false;
    let targetX = 0;
    let targetY = 0;
    let currentX = 0;
    let currentY = 0;
    let targetOpacity = 0;
    let currentOpacity = 0;

    const resize = () => {
      const width = Math.max(card.clientWidth, 1);
      const height = Math.max(card.clientHeight, 1);
      renderer.setSize(width, height, false);
      camera.aspect = width / height;
      camera.updateProjectionMatrix();
      renderer.render(scene, camera);
    };

    const run = () => {
      isRunning = true;
      currentX += (targetX - currentX) * 0.12;
      currentY += (targetY - currentY) * 0.12;
      currentOpacity += (targetOpacity - currentOpacity) * 0.1;

      renderer.render(scene, camera);

      const stillMoving =
        Math.abs(targetX - currentX) > 0.001 ||
        Math.abs(targetY - currentY) > 0.001 ||
        Math.abs(targetOpacity - currentOpacity) > 0.01;

      if (stillMoving || targetOpacity > 0) {
        frameId = window.requestAnimationFrame(run);
      } else {
        isRunning = false;
      }
    };

    const requestRun = () => {
      if (!isRunning) {
        frameId = window.requestAnimationFrame(run);
      }
    };

    const setPointer = (event: PointerEvent) => {
      const rect = card.getBoundingClientRect();
      const localX = event.clientX - rect.left;
      const localY = event.clientY - rect.top;
      const normalX = (localX / rect.width - 0.5) * 2;
      const normalY = (localY / rect.height - 0.5) * 2;

      targetX = normalX;
      targetY = normalY;
      targetOpacity = 1;

      card.style.setProperty('--card-rotate-y', `${(normalX * 7).toFixed(2)}deg`);
      card.style.setProperty('--card-rotate-x', `${(-normalY * 6).toFixed(2)}deg`);
      card.style.setProperty('--card-glow-x', `${localX.toFixed(0)}px`);
      card.style.setProperty('--card-glow-y', `${localY.toFixed(0)}px`);
      card.style.setProperty('--card-effect-opacity', '1');
      requestRun();
    };

    const reset = () => {
      targetX = 0;
      targetY = 0;
      targetOpacity = 0;
      card.style.setProperty('--card-rotate-x', '0deg');
      card.style.setProperty('--card-rotate-y', '0deg');
      card.style.setProperty('--card-effect-opacity', '0');
      card.style.removeProperty('--card-glow-x');
      card.style.removeProperty('--card-glow-y');
      requestRun();
    };

    const onFocus = () => {
      targetOpacity = 1;
      card.style.setProperty('--card-effect-opacity', '1');
      requestRun();
    };

    const observer = new ResizeObserver(resize);
    observer.observe(card);
    resize();

    card.addEventListener('pointerenter', setPointer);
    card.addEventListener('pointermove', setPointer);
    card.addEventListener('pointerleave', reset);
    card.addEventListener('focusin', onFocus);
    card.addEventListener('focusout', reset);

    return () => {
      observer.disconnect();
      window.cancelAnimationFrame(frameId);
      card.removeEventListener('pointerenter', setPointer);
      card.removeEventListener('pointermove', setPointer);
      card.removeEventListener('pointerleave', reset);
      card.removeEventListener('focusin', onFocus);
      card.removeEventListener('focusout', reset);
      disposeScene(scene);
      renderer.dispose();
    };
  }, []);

  return <canvas ref={canvasRef} className={className} aria-hidden="true" />;
}
