'use client';

type NonLaHeroProps = {
  className?: string;
};

export default function NonLaHero({ className }: NonLaHeroProps) {
  // Conical hat Sketchfab URL parameters:
  // - autostart=1: start automatically
  // - autospin=0.25: slow elegant automatic rotation
  // - transparent=1: transparent backdrop (fits lacquer theme)
  // - ui_controls=0, ui_infos=0, ui_watermark=0, ui_settings=0, ui_help=0, ui_ar=0, ui_vr=0, ui_fullscreen=0, ui_stop=0: clean presentation
  const embedUrl = "https://sketchfab.com/models/abd86436d03344b9a40c82e127fb6252/embed" + 
    "?autostart=1" +
    "&autospin=0.25" +
    "&transparent=1" +
    "&ui_controls=0" +
    "&ui_infos=0" +
    "&ui_watermark=0" +
    "&ui_hint=0" +
    "&ui_settings=0" +
    "&ui_ar=0" +
    "&ui_vr=0" +
    "&ui_fullscreen=0" +
    "&ui_stop=0" +
    "&ui_help=0" +
    "&preload=1";

  return (
    <div className={className} style={{ width: '100%', height: '100%', position: 'relative' }}>
      <iframe
        title="Vietnamese Conical Hat (Nón Lá) 3D Model"
        src={embedUrl}
        width="100%"
        height="100%"
        frameBorder="0"
        allow="autoplay; fullscreen; xr-spatial-tracking"
        style={{ 
          border: 'none', 
          width: '100%', 
          height: '100%',
          backgroundColor: 'transparent'
        }}
      />
    </div>
  );
}
