'use client';

/**
 * NgoMonHero — a stylised gold wireframe of Huế's Ngọ Môn gate.
 *
 * Procedural low-poly geometry (tiered ramparts + hip roofs) drawn as gold
 * edge lines on a transparent canvas. Slowly rotates, tilts with scroll, and
 * drifts toward the pointer. Honors prefers-reduced-motion, caps pixel ratio,
 * and disposes all GPU resources on unmount.
 */

import { useEffect, useRef } from 'react';
import * as THREE from 'three';

const GOLD = 0xc69a3f;
const GOLD_SOFT = 0xd6b873;
const MAX_PIXEL_RATIO = 1.5;

type NgoMonHeroProps = {
  className?: string;
};

/** A box drawn as gold edge lines. */
function edgeBox(
  w: number,
  h: number,
  d: number,
  y: number,
  color = GOLD,
  opacity = 0.9,
): THREE.LineSegments {
  const geo = new THREE.EdgesGeometry(new THREE.BoxGeometry(w, h, d));
  const mat = new THREE.LineBasicMaterial({ color, transparent: true, opacity });
  const lines = new THREE.LineSegments(geo, mat);
  lines.position.y = y;
  return lines;
}

/** A flattened 4-sided pyramid (hip roof) as gold edge lines. */
function edgeRoof(
  radius: number,
  height: number,
  y: number,
  color = GOLD,
  opacity = 0.9,
): THREE.LineSegments {
  const cone = new THREE.ConeGeometry(radius, height, 4);
  const geo = new THREE.EdgesGeometry(cone);
  const mat = new THREE.LineBasicMaterial({ color, transparent: true, opacity });
  const lines = new THREE.LineSegments(geo, mat);
  lines.position.y = y;
  lines.rotation.y = Math.PI / 4; // square the hip roof to the base
  return lines;
}

function buildGate(): THREE.Group {
  const gate = new THREE.Group();

  // Three stacked ramparts (wide -> narrow) = the gate's tiered mass.
  gate.add(edgeBox(6.0, 1.7, 2.4, 0.85));
  gate.add(edgeBox(4.6, 1.1, 1.8, 2.1, GOLD_SOFT));
  gate.add(edgeBox(3.1, 0.95, 1.35, 3.15, GOLD_SOFT));

  // Hip roofs over the upper tiers (the Five-Phoenix pavilion silhouette).
  gate.add(edgeRoof(3.7, 1.0, 2.05));
  gate.add(edgeRoof(2.7, 0.9, 3.0));
  gate.add(edgeRoof(2.0, 1.0, 3.95, GOLD));

  // Three arch openings suggested as upright frames in the base.
  for (let i = -1; i <= 1; i++) {
    const arch = edgeBox(0.9, 1.2, 0.1, 0.7, GOLD, 0.55);
    arch.position.x = i * 1.7;
    arch.position.z = 1.25;
    gate.add(arch);
  }

  gate.position.y = -1.6; // recenter the tall form in view
  return gate;
}

export default function NgoMonHero({ className }: NgoMonHeroProps) {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    const host = canvas?.parentElement;
    if (!canvas || !host) return;

    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(38, 1, 0.1, 100);
    camera.position.set(0, 0, 11);

    const renderer = new THREE.WebGLRenderer({
      canvas,
      alpha: true,
      antialias: true,
      powerPreference: 'low-power',
    });
    renderer.outputColorSpace = THREE.SRGBColorSpace;
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, MAX_PIXEL_RATIO));

    const gate = buildGate();
    scene.add(gate);

    let frameId = 0;
    let pointerX = 0;
    let pointerY = 0;
    let scrollTilt = 0;

    const resize = () => {
      const w = Math.max(host.clientWidth, 1);
      const h = Math.max(host.clientHeight, 1);
      renderer.setSize(w, h, false);
      camera.aspect = w / h;
      camera.updateProjectionMatrix();
    };

    const onPointer = (e: PointerEvent) => {
      const rect = host.getBoundingClientRect();
      pointerX = ((e.clientX - rect.left) / rect.width - 0.5) * 2;
      pointerY = ((e.clientY - rect.top) / rect.height - 0.5) * 2;
    };

    const onScroll = () => {
      scrollTilt = Math.min(window.scrollY / 900, 1);
    };

    const clock = new THREE.Clock();
    const render = () => {
      const t = clock.getElapsedTime();
      // Slow ceremonial spin + gentle breathing tilt.
      const baseSpin = reduceMotion ? 0.5 : t * 0.12;
      gate.rotation.y = baseSpin + pointerX * 0.35;
      gate.rotation.x = 0.12 + scrollTilt * 0.5 + pointerY * 0.12;
      gate.position.y = -1.6 + Math.sin(t * 0.6) * 0.06;
      renderer.render(scene, camera);
      if (!reduceMotion) frameId = window.requestAnimationFrame(render);
    };

    const observer = new ResizeObserver(resize);
    observer.observe(host);
    resize();

    window.addEventListener('pointermove', onPointer);
    window.addEventListener('scroll', onScroll, { passive: true });

    if (reduceMotion) {
      render(); // single static frame
    } else {
      frameId = window.requestAnimationFrame(render);
    }

    return () => {
      observer.disconnect();
      window.cancelAnimationFrame(frameId);
      window.removeEventListener('pointermove', onPointer);
      window.removeEventListener('scroll', onScroll);
      scene.traverse((obj) => {
        const ls = obj as THREE.LineSegments;
        ls.geometry?.dispose();
        const mat = ls.material as THREE.Material | THREE.Material[] | undefined;
        if (Array.isArray(mat)) mat.forEach((m) => m.dispose());
        else mat?.dispose();
      });
      renderer.dispose();
    };
  }, []);

  return <canvas ref={canvasRef} className={className} aria-hidden="true" />;
}
