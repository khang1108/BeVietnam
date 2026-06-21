'use client';

type NonLaHeroProps = {
  className?: string;
};

export default function NonLaHero({ className }: NonLaHeroProps) {
  return (
    <div className={className} style={{ width: '100%', height: '100%', position: 'relative' }} aria-hidden="true">
      <svg
        viewBox="0 0 720 720"
        role="img"
        style={{
          width: '100%',
          height: '100%',
          display: 'block',
          filter: 'drop-shadow(0 34px 72px rgba(0, 0, 0, 0.42))',
        }}
      >
        <defs>
          <radialGradient id="nonLaSurface" cx="50%" cy="43%" r="58%">
            <stop offset="0%" stopColor="#f7efd5" stopOpacity="0.95" />
            <stop offset="52%" stopColor="#d8c895" stopOpacity="0.82" />
            <stop offset="100%" stopColor="#8f7643" stopOpacity="0.46" />
          </radialGradient>
          <linearGradient id="rim" x1="18%" y1="34%" x2="82%" y2="72%">
            <stop offset="0%" stopColor="#fff6d8" stopOpacity="0.78" />
            <stop offset="100%" stopColor="#6a4b21" stopOpacity="0.56" />
          </linearGradient>
          <radialGradient id="shadow" cx="50%" cy="50%" r="50%">
            <stop offset="0%" stopColor="#000000" stopOpacity="0.28" />
            <stop offset="100%" stopColor="#000000" stopOpacity="0" />
          </radialGradient>
        </defs>

        <ellipse cx="360" cy="518" rx="236" ry="56" fill="url(#shadow)" />
        <path
          d="M122 462 C178 256 274 150 360 130 C446 150 542 256 598 462 C486 512 234 512 122 462 Z"
          fill="url(#nonLaSurface)"
          stroke="url(#rim)"
          strokeWidth="5"
        />
        <path
          d="M144 450 C244 485 476 485 576 450"
          fill="none"
          stroke="#f6e9bd"
          strokeOpacity="0.54"
          strokeWidth="4"
        />
        <path
          d="M360 132 C340 235 312 345 174 456"
          fill="none"
          stroke="#6f552a"
          strokeOpacity="0.18"
          strokeWidth="3"
        />
        <path
          d="M360 132 C380 235 408 345 546 456"
          fill="none"
          stroke="#6f552a"
          strokeOpacity="0.18"
          strokeWidth="3"
        />
        <path
          d="M360 132 C352 248 348 350 292 486"
          fill="none"
          stroke="#fff2c9"
          strokeOpacity="0.16"
          strokeWidth="2"
        />
        <path
          d="M360 132 C368 248 372 350 428 486"
          fill="none"
          stroke="#fff2c9"
          strokeOpacity="0.16"
          strokeWidth="2"
        />
        {[190, 236, 282, 328, 374, 420].map((y, index) => (
          <ellipse
            key={y}
            cx="360"
            cy={y}
            rx={34 + index * 38}
            ry={10 + index * 9}
            fill="none"
            stroke="#6f552a"
            strokeOpacity={0.16 + index * 0.025}
            strokeWidth="2"
          />
        ))}
        <circle cx="360" cy="132" r="17" fill="#9e2b25" stroke="#f5ddb2" strokeWidth="5" />
        <path
          d="M488 314 C514 340 534 370 548 406"
          fill="none"
          stroke="#9e2b25"
          strokeLinecap="round"
          strokeWidth="8"
        />
        <path
          d="M506 300 C524 326 538 354 546 382"
          fill="none"
          stroke="#c69a3f"
          strokeLinecap="round"
          strokeWidth="4"
        />
      </svg>
    </div>
  );
}
