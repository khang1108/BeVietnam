'use client';

type NonLaHeroProps = {
  className?: string;
};

export default function NonLaHero({ className }: NonLaHeroProps) {
  // Vietnamese Nón Lá Sketchfab model. Embed params:
  // autostart=1, autospin=0.25 (slow elegant rotation), transparent=1 (fits lacquer theme),
  // ui_*=0 to strip Sketchfab chrome for a clean hero presentation.
  const embedUrl =
    'https://sketchfab.com/models/abd86436d03344b9a40c82e127fb6252/embed' +
    '?autostart=1' +
    '&autospin=0.25' +
    '&transparent=1' +
    '&ui_controls=0' +
    '&ui_infos=0' +
    '&ui_watermark=0' +
    '&ui_hint=0' +
    '&ui_settings=0' +
    '&ui_ar=0' +
    '&ui_vr=0' +
    '&ui_fullscreen=0' +
    '&ui_stop=0' +
    '&ui_help=0' +
    '&preload=1';

  return (
    <div className={className} style={{ width: '100%', height: '100%', position: 'relative' }}>
      <iframe
        title="Vietnamese Conical Hat (Nón Lá) 3D Model"
        src={embedUrl}
        allow="autoplay; fullscreen; xr-spatial-tracking"
        style={{
          border: 'none',
          width: '100%',
          height: '100%',
          backgroundColor: 'transparent',
        }}
      />
    </div>
  );
}
