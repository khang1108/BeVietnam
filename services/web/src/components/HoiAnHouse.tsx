'use client';

type HoiAnHouseProps = {
  className?: string;
};

export default function HoiAnHouse({ className }: HoiAnHouseProps) {
  return (
    <div className={className} style={{ width: '100%', height: '100%', position: 'relative' }} aria-hidden="true">
      <svg
        viewBox="0 0 760 520"
        role="img"
        style={{
          width: '100%',
          height: '100%',
          display: 'block',
          filter: 'drop-shadow(0 28px 56px rgba(0, 0, 0, 0.36))',
        }}
      >
        <defs>
          <linearGradient id="stone" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="#d8c795" />
            <stop offset="58%" stopColor="#8f7040" />
            <stop offset="100%" stopColor="#49311b" />
          </linearGradient>
          <linearGradient id="roof" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="#c69a3f" />
            <stop offset="100%" stopColor="#7d271d" />
          </linearGradient>
          <radialGradient id="glow" cx="50%" cy="48%" r="60%">
            <stop offset="0%" stopColor="#c69a3f" stopOpacity="0.28" />
            <stop offset="100%" stopColor="#c69a3f" stopOpacity="0" />
          </radialGradient>
        </defs>

        <ellipse cx="380" cy="424" rx="278" ry="52" fill="#000" opacity="0.24" />
        <ellipse cx="380" cy="244" rx="300" ry="210" fill="url(#glow)" />

        <path d="M205 210 L380 88 L555 210 Z" fill="url(#roof)" stroke="#f3d18b" strokeWidth="6" />
        <path d="M246 210 H514 V382 H246 Z" fill="url(#stone)" stroke="#f3d18b" strokeWidth="5" />
        <path d="M302 382 V262 C302 224 330 198 380 198 C430 198 458 224 458 262 V382 Z" fill="#25180f" opacity="0.88" />
        <path d="M326 382 V270 C326 242 344 226 380 226 C416 226 434 242 434 270 V382 Z" fill="#3a2415" stroke="#c69a3f" strokeWidth="4" />

        <path d="M196 212 C248 230 512 230 564 212" fill="none" stroke="#f5ddb2" strokeWidth="6" strokeLinecap="round" />
        <path d="M264 172 H496" stroke="#6a221a" strokeWidth="9" strokeLinecap="round" />
        <path d="M286 142 H474" stroke="#f3d18b" strokeOpacity="0.65" strokeWidth="4" strokeLinecap="round" />

        <g opacity="0.72">
          <circle cx="214" cy="340" r="18" fill="#9e2b25" stroke="#f5ddb2" strokeWidth="4" />
          <circle cx="546" cy="340" r="18" fill="#9e2b25" stroke="#f5ddb2" strokeWidth="4" />
          <path d="M214 358 V400" stroke="#c69a3f" strokeWidth="5" strokeLinecap="round" />
          <path d="M546 358 V400" stroke="#c69a3f" strokeWidth="5" strokeLinecap="round" />
        </g>

        {[278, 329, 431, 482].map((x) => (
          <path key={x} d={`M${x} 230 V376`} stroke="#f5ddb2" strokeOpacity="0.34" strokeWidth="4" />
        ))}
        {[244, 280, 316, 352].map((y) => (
          <path key={y} d={`M262 ${y} H498`} stroke="#25180f" strokeOpacity="0.28" strokeWidth="2" />
        ))}
      </svg>
    </div>
  );
}
