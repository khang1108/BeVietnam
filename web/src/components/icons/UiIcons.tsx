import type { SVGProps } from 'react';

const baseProps = {
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 1.8,
  strokeLinecap: 'round' as const,
  strokeLinejoin: 'round' as const,
};

type IconProps = SVGProps<SVGSVGElement>;

export function IconFeed(props: IconProps) {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" {...baseProps} {...props}>
      <rect x="3" y="4" width="18" height="16" rx="2" />
      <line x1="7" y1="8" x2="17" y2="8" />
      <line x1="7" y1="12" x2="17" y2="12" />
      <line x1="7" y1="16" x2="13" y2="16" />
    </svg>
  );
}

export function IconExplore(props: IconProps) {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" {...baseProps} {...props}>
      <circle cx="12" cy="12" r="9" />
      <path d="M9 9l6-2-2 6-6 2 2-6z" />
      <circle cx="12" cy="12" r="1" fill="currentColor" stroke="none" />
    </svg>
  );
}

export function IconCalendar(props: IconProps) {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" {...baseProps} {...props}>
      <rect x="3" y="4" width="18" height="17" rx="2" />
      <line x1="7" y1="2.5" x2="7" y2="6.5" />
      <line x1="17" y1="2.5" x2="17" y2="6.5" />
      <line x1="3" y1="9" x2="21" y2="9" />
      <rect x="7" y="12" width="3" height="3" rx="0.6" />
      <rect x="12" y="12" width="3" height="3" rx="0.6" />
      <rect x="7" y="16" width="3" height="3" rx="0.6" />
      <rect x="12" y="16" width="3" height="3" rx="0.6" />
    </svg>
  );
}

export function IconSparkle(props: IconProps) {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" {...baseProps} {...props}>
      <path d="M12 3l1.8 4.4L18 9l-4.2 1.6L12 15l-1.8-4.4L6 9l4.2-1.6L12 3z" />
      <path d="M18.5 14.5l.8 2 2 .8-2 .8-.8 2-.8-2-2-.8 2-.8.8-2z" />
    </svg>
  );
}

export function IconFood(props: IconProps) {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" {...baseProps} {...props}>
      <path d="M4 12h16" />
      <path d="M5 12c0 4 3.1 7 7 7s7-3 7-7" />
      <path d="M8 4c0 1.2 1.2 1.8 1.2 3" />
      <path d="M12 4c0 1.2 1.2 1.8 1.2 3" />
      <path d="M16 4c0 1.2 1.2 1.8 1.2 3" />
    </svg>
  );
}

export function IconLocation(props: IconProps) {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" {...baseProps} {...props}>
      <path d="M12 21s7-6.2 7-11a7 7 0 1 0-14 0c0 4.8 7 11 7 11z" />
      <circle cx="12" cy="10" r="2.5" />
    </svg>
  );
}

export function IconGlobe(props: IconProps) {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" {...baseProps} {...props}>
      <circle cx="12" cy="12" r="9" />
      <path d="M3 12h18" />
      <path d="M12 3c3.5 3.6 3.5 14.4 0 18" />
      <path d="M12 3c-3.5 3.6-3.5 14.4 0 18" />
    </svg>
  );
}

export function IconMail(props: IconProps) {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" {...baseProps} {...props}>
      <rect x="3" y="5" width="18" height="14" rx="2" />
      <path d="M4 7l8 6 8-6" />
    </svg>
  );
}

export function IconLink(props: IconProps) {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" {...baseProps} {...props}>
      <path d="M10 13a4 4 0 0 1 0-6l2-2a4 4 0 1 1 6 6l-1 1" />
      <path d="M14 11a4 4 0 0 1 0 6l-2 2a4 4 0 1 1-6-6l1-1" />
    </svg>
  );
}

export function IconMountain(props: IconProps) {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" {...baseProps} {...props}>
      <path d="M3 18l6-10 4 6 2-3 6 7" />
      <path d="M9 8l2 3" />
      <path d="M15 11l2 3" />
    </svg>
  );
}

export function IconFlagVn(props: IconProps) {
  return (
    <svg viewBox="0 0 28 20" aria-hidden="true" focusable="false" {...props}>
      <rect width="28" height="20" rx="2" fill="#da251d" />
      <path
        d="M14 3.5l1.7 4.5 4.8.4-3.7 3 1.2 4.7-4-2.4-4 2.4 1.2-4.7-3.7-3 4.8-.4L14 3.5z"
        fill="#ffdd00"
      />
    </svg>
  );
}

export function IconRoute(props: IconProps) {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" {...baseProps} {...props}>
      <circle cx="6" cy="19" r="3" />
      <circle cx="18" cy="5" r="3" />
      <path d="M9 19h4.5a3.5 3.5 0 0 0 0-7h-3a3.5 3.5 0 0 1 0-7H15" />
    </svg>
  );
}

