'use client';

type HoiAnHouseProps = {
  className?: string;
};

/**
 * Auto-spinning Tự Đức tomb as a pre-baked transparent animated WebP (no WebGL).
 * Model: "Tu Duc's Tomb - Stele Building (CyArk Dataset)" by Vasilis Haroupas, CC-BY-4.0.
 * Regenerate: scripts/bake_model.sh <gltf> tu-duc-tomb
 */
export default function HoiAnHouse({ className }: HoiAnHouseProps) {
  return (
    // eslint-disable-next-line @next/next/no-img-element
    <img
      className={className}
      src="/models/tu-duc-tomb.webp"
      alt=""
      aria-hidden="true"
      style={{ width: '100%', height: '100%', objectFit: 'contain', display: 'block' }}
    />
  );
}
