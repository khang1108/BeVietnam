'use client';

type NonLaHeroProps = {
  className?: string;
};

/**
 * Auto-spinning nón lá as a pre-baked transparent VP9-alpha WebM loop (no WebGL).
 * Model: "Vietnamese _ Non La" by NNgan, CC-BY-4.0.
 * Regenerate: scripts/bake_model.sh <gltf> non-la
 */
export default function NonLaHero({ className }: NonLaHeroProps) {
  return (
    <video
      className={className}
      autoPlay
      loop
      muted
      playsInline
      poster="/models/non-la.png"
      aria-hidden="true"
      style={{ width: '100%', height: '100%', objectFit: 'contain', display: 'block' }}
    >
      <source src="/models/non-la.webm" type="video/webm" />
    </video>
  );
}
