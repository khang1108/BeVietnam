'use client';

type NonLaHeroProps = {
  className?: string;
};

/**
 * Auto-spinning nón lá as a pre-baked transparent animated WebP (no WebGL).
 * Model: "Vietnamese _ Non La" by NNgan, CC-BY-4.0.
 * Regenerate: scripts/bake_model.sh <gltf> non-la
 */
export default function NonLaHero({ className }: NonLaHeroProps) {
  return (
    // eslint-disable-next-line @next/next/no-img-element
    <img
      className={className}
      src="/models/non-la.webp"
      alt=""
      aria-hidden="true"
      style={{ width: '100%', height: '100%', objectFit: 'contain', display: 'block' }}
    />
  );
}
