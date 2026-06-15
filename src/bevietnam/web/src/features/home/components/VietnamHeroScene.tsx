'use client';

import { useEffect, useRef } from 'react';
import * as THREE from 'three';
import styles from '@/features/home/styles/home.module.css';

const CAMERA_FOV = 38;
const CAMERA_NEAR = 0.1;
const CAMERA_FAR = 100;
const MAX_PIXEL_RATIO = 2;
const LINE_WHITE = 0xe8ecf4;
const GLOW_GOLD = 0xf7cc4f;

function glowMat(color: number = LINE_WHITE, intensity = 0.7, opacity = 0.92) {
  return new THREE.MeshStandardMaterial({
    color, emissive: color, emissiveIntensity: intensity,
    roughness: 0.15, metalness: 0.85, transparent: true, opacity,
  });
}

function curveTube(pts: [number, number, number][], r = 0.012, color?: number) {
  const vecs = pts.map(([x, y, z]) => new THREE.Vector3(x, y, z));
  const curve = new THREE.CatmullRomCurve3(vecs);
  return new THREE.Mesh(new THREE.TubeGeometry(curve, pts.length * 14, r, 6, false), glowMat(color));
}

function lineTube(x1: number, y1: number, x2: number, y2: number, r = 0.009, color?: number) {
  const s = new THREE.Vector3(x1, y1, 0);
  const e = new THREE.Vector3(x2, y2, 0);
  const dir = new THREE.Vector3().subVectors(e, s);
  const len = dir.length();
  const geo = new THREE.CylinderGeometry(r, r, len, 6);
  const mesh = new THREE.Mesh(geo, glowMat(color));
  mesh.position.copy(s).add(e).multiplyScalar(0.5);
  mesh.quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), dir.normalize());
  return mesh;
}

function concentricDot(x: number, y: number, sc = 1) {
  const g = new THREE.Group();
  const m = glowMat(LINE_WHITE, 0.9);
  const dot = new THREE.Mesh(new THREE.SphereGeometry(0.018 * sc, 8, 6), m);
  dot.position.set(x, y, 0.01);
  g.add(dot);
  [0.045, 0.08].forEach(r => {
    const ring = new THREE.Mesh(new THREE.TorusGeometry(r * sc, 0.007 * sc, 6, 24), m);
    ring.position.set(x, y, 0.01);
    ring.rotation.x = Math.PI / 2;
    g.add(ring);
  });
  return g;
}

/** Dong Son style Chim Lac — geometric line-art with parallel wing segments */
function createChimLac(): { group: THREE.Group; upperWings: THREE.Mesh[]; lowerWings: THREE.Mesh[] } {
  const group = new THREE.Group();
  const uw: THREE.Mesh[] = [];
  const lw: THREE.Mesh[] = [];

  /* Body outline — upper curve */
  group.add(curveTube([
    [-0.7, 0, 0], [-0.3, 0.06, 0], [0, 0.12, 0],
    [0.3, 0.16, 0], [0.5, 0.22, 0],
    [0.6, 0.38, 0], [0.64, 0.58, 0],
    [0.7, 0.72, 0], [0.82, 0.8, 0],
  ], 0.014));

  /* Body outline — lower curve */
  group.add(curveTube([
    [-0.7, 0, 0], [-0.35, -0.05, 0], [0, -0.07, 0],
    [0.2, -0.04, 0], [0.4, 0.04, 0], [0.5, 0.14, 0],
  ], 0.012));

  /* Beak — very long, pointed (key feature) */
  group.add(curveTube([
    [0.82, 0.8, 0], [1.1, 0.84, 0], [1.5, 0.87, 0], [1.9, 0.88, 0], [2.2, 0.86, 0],
  ], 0.01));
  group.add(curveTube([
    [0.82, 0.76, 0], [1.1, 0.79, 0], [1.5, 0.83, 0], [1.9, 0.85, 0], [2.15, 0.855, 0],
  ], 0.007));

  /* Concentric dots on neck/head (Dong Son pattern) */
  group.add(concentricDot(0.86, 0.78, 0.85));
  group.add(concentricDot(0.7, 0.65, 0.65));
  group.add(concentricDot(0.63, 0.5, 0.55));
  group.add(concentricDot(0.6, 0.38, 0.5));

  /* Upper wing — parallel line segments fanning upward-left */
  for (let i = 0; i < 8; i++) {
    const t = i / 7;
    const bx = -0.05 + t * 0.35;
    const by = 0.1 + t * 0.04;
    const ex = bx - 0.85 - t * 0.25;
    const ey = by + 0.5 + t * 0.18;
    const m = lineTube(bx, by, ex, ey, 0.008);
    uw.push(m);
    group.add(m);
  }

  /* Lower wing — parallel line segments fanning downward-left */
  for (let i = 0; i < 5; i++) {
    const t = i / 4;
    const bx = -0.05 + t * 0.25;
    const by = -0.02 - t * 0.02;
    const ex = bx - 0.55 - t * 0.18;
    const ey = by - 0.35 - t * 0.12;
    const m = lineTube(bx, by, ex, ey, 0.007);
    lw.push(m);
    group.add(m);
  }

  /* Tail lines */
  for (let i = 0; i < 4; i++) {
    const t = i / 3;
    group.add(lineTube(
      -0.55 - t * 0.08, 0.02 - t * 0.02,
      -1.05 - t * 0.18, 0.12 + t * 0.1,
      0.007,
    ));
  }

  /* Body hatching lines */
  for (let i = 0; i < 5; i++) {
    const t = i / 4;
    const y = -0.03 + t * 0.13;
    group.add(lineTube(-0.15 + t * 0.08, y, 0.25 + t * 0.04, y + 0.01, 0.005));
  }

  /* Legs */
  group.add(curveTube([[0.12, -0.07, 0], [0.16, -0.22, 0], [0.18, -0.38, 0]], 0.007));
  group.add(lineTube(0.18, -0.38, 0.28, -0.41, 0.005));
  group.add(lineTube(0.18, -0.38, 0.12, -0.43, 0.005));
  group.add(curveTube([[0.3, -0.05, 0], [0.32, -0.2, 0], [0.34, -0.35, 0]], 0.007));
  group.add(lineTube(0.34, -0.35, 0.42, -0.38, 0.005));
  group.add(lineTube(0.34, -0.35, 0.28, -0.4, 0.005));

  group.scale.setScalar(2.0);
  group.position.set(-0.3, -0.1, 0);

  return { group, upperWings: uw, lowerWings: lw };
}

function createParticles(count: number) {
  const pos = new Float32Array(count * 3);
  for (let i = 0; i < count; i++) {
    const a = Math.random() * Math.PI * 2;
    const r = 2.5 + Math.random() * 2.5;
    pos[i * 3] = Math.cos(a) * r;
    pos[i * 3 + 1] = (Math.random() - 0.5) * 3;
    pos[i * 3 + 2] = Math.sin(a) * r * 0.4 - 0.5;
  }
  const geo = new THREE.BufferGeometry();
  geo.setAttribute('position', new THREE.BufferAttribute(pos, 3));
  return new THREE.Points(geo, new THREE.PointsMaterial({
    color: LINE_WHITE, size: 0.035, transparent: true, opacity: 0.35, sizeAttenuation: true,
  }));
}

function createDataOverlays() {
  const g = new THREE.Group();
  const m = glowMat(LINE_WHITE, 0.3, 0.12);
  [{ x: 3.2, y: 1.4, r: 0.28 }, { x: -3.8, y: -0.6, r: 0.22 }, { x: 2.8, y: -1.3, r: 0.2 }].forEach(({ x, y, r }) => {
    const ring = new THREE.Mesh(new THREE.TorusGeometry(r, 0.004, 6, 32), m);
    ring.position.set(x, y, -1);
    g.add(ring);
    const arc = new THREE.Mesh(new THREE.TorusGeometry(r * 0.75, 0.006, 6, 20, Math.PI * 1.3), glowMat(GLOW_GOLD, 0.4, 0.1));
    arc.position.set(x, y, -1);
    arc.rotation.z = Math.random() * Math.PI;
    g.add(arc);
  });
  return g;
}

function disposeScene(scene: THREE.Scene) {
  scene.traverse((obj) => {
    const m = obj as THREE.Mesh;
    m.geometry?.dispose();
    if (Array.isArray(m.material)) m.material.forEach(mt => mt.dispose());
    else (m.material as THREE.Material)?.dispose();
  });
}

export function VietnamHeroScene() {
  const rootRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const root = rootRef.current;
    if (!root) return;

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(CAMERA_FOV, 1, CAMERA_NEAR, CAMERA_FAR);
    const renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true, preserveDrawingBuffer: true });
    const pointer = { x: 0, y: 0 };

    renderer.outputColorSpace = THREE.SRGBColorSpace;
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, MAX_PIXEL_RATIO));
    root.appendChild(renderer.domElement);

    camera.position.set(0, 0.5, 6.5);
    camera.lookAt(0, 0.3, 0);

    scene.add(new THREE.AmbientLight(0x4a5568, 0.4));
    const sun = new THREE.DirectionalLight(0xe8eaf0, 1.6);
    sun.position.set(2, 3, 5);
    scene.add(sun);

    const glow = new THREE.PointLight(LINE_WHITE, 2.2, 8, 1.5);
    glow.position.set(0, 1, 1.5);
    scene.add(glow);

    const accent = new THREE.PointLight(GLOW_GOLD, 0.6, 6, 2);
    accent.position.set(-1, 0, 2);
    scene.add(accent);

    const { group: chimLac, upperWings, lowerWings } = createChimLac();
    const particles = createParticles(45);
    const overlays = createDataOverlays();
    scene.add(chimLac, particles, overlays);

    /* Store original wing positions for animation */
    const uwData = upperWings.map(m => ({ mesh: m, oy: m.position.y, oz: m.rotation.z }));
    const lwData = lowerWings.map(m => ({ mesh: m, oy: m.position.y, oz: m.rotation.z }));

    const resize = () => {
      const w = root.clientWidth;
      const h = root.clientHeight;
      renderer.setSize(w, h, false);
      camera.aspect = w / h;
      camera.updateProjectionMatrix();
    };

    const onPointer = (e: PointerEvent) => {
      pointer.x = (e.clientX / window.innerWidth - 0.5) * 2;
      pointer.y = (e.clientY / window.innerHeight - 0.5) * 2;
    };

    let fid = 0;
    const animate = () => {
      const t = performance.now() * 0.001;

      /* Gentle float */
      chimLac.position.y = -0.1 + Math.sin(t * 0.8) * 0.09;
      chimLac.rotation.z = Math.sin(t * 0.6) * 0.025;

      /* Wing flutter — each line oscillates with phase offset */
      uwData.forEach(({ mesh, oy, oz }, i) => {
        const phase = i * 0.25;
        mesh.position.y = oy + Math.sin(t * 2.2 + phase) * 0.008;
        mesh.rotation.z = oz + Math.sin(t * 2.2 + phase) * 0.02;
      });
      lwData.forEach(({ mesh, oy, oz }, i) => {
        const phase = i * 0.3;
        mesh.position.y = oy + Math.sin(t * 2.0 + phase) * 0.006;
        mesh.rotation.z = oz + Math.sin(t * 2.0 + phase) * 0.015;
      });

      particles.rotation.y = t * 0.04;
      overlays.rotation.z = t * 0.015;
      glow.intensity = 2.2 + Math.sin(t * 1.8) * 0.5;

      /* Pointer parallax */
      chimLac.rotation.y = Math.sin(t * 0.4) * 0.04 + pointer.x * 0.1;
      chimLac.rotation.x = pointer.y * 0.04;

      renderer.render(scene, camera);
      fid = requestAnimationFrame(animate);
    };

    resize();
    animate();
    window.addEventListener('resize', resize);
    window.addEventListener('pointermove', onPointer);

    return () => {
      cancelAnimationFrame(fid);
      window.removeEventListener('resize', resize);
      window.removeEventListener('pointermove', onPointer);
      renderer.dispose();
      disposeScene(scene);
      root.removeChild(renderer.domElement);
    };
  }, []);

  return <div ref={rootRef} className={styles.heroScene} aria-hidden="true" />;
}
