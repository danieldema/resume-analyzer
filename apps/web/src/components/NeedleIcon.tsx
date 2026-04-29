interface Props {
  size?: number;
  className?: string;
}

export function NeedleIcon({ size = 24, className = '' }: Props) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
      aria-hidden="true"
    >
      {/* needle body */}
      <line x1="4" y1="20" x2="17" y2="5" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
      {/* eye */}
      <ellipse cx="16" cy="6" rx="1.8" ry="1.1" stroke="currentColor" strokeWidth="1.3" fill="none" transform="rotate(-47 16 6)"/>
      {/* thread */}
      <path d="M 20 3 C 17 7 14 12 11 16 C 9 19 7 21 6 22" stroke="#3b82f6" strokeWidth="1.5" strokeLinecap="round" fill="none"/>
    </svg>
  );
}
