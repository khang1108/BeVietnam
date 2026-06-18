'use client';

type HoiAnHouseProps = {
  className?: string;
};

export default function HoiAnHouse({ className }: HoiAnHouseProps) {
  // Model Nghia trang An Bang from Sketchfab: https://sketchfab.com/3d-models/nghia-trang-an-bang-aa21ecfc7a3f42e899437f603949f345
  const embedUrl = "https://sketchfab.com/models/aa21ecfc7a3f42e899437f603949f345/embed" + 
    "?autostart=1" +
    "&autospin=0.15" +
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
        title="Nghĩa trang An Bằng"
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
