'use client';

type HoiAnHouseProps = {
  className?: string;
};

/**
 * Auto-spinning Tự Đức tomb as a pre-baked transparent VP9-alpha WebM loop (no WebGL).
 * Model: "Tu Duc's Tomb - Stele Building (CyArk Dataset)" by Vasilis Haroupas, CC-BY-4.0.
 * Regenerate: scripts/bake_model.sh <gltf> tu-ducs-tomb
 */
export default function HoiAnHouse({ className }: HoiAnHouseProps) {
  return (
    <video
      className={className}
      autoPlay
      loop
      muted
      playsInline
      poster="/models/tu-ducs-tomb.png"
      aria-hidden="true"
      style={{ width: '100%', height: '100%', objectFit: 'contain', display: 'block' }}
    >
      <source src="/models/tu-ducs-tomb.webm" type="video/webm" />
    </video>
  );
}
