interface Props {
  size?: number;
  className?: string;
}

export function NeedleIcon({ size = 24, className = '' }: Props) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 32 32"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
      aria-hidden="true"
    >
      <rect x="3" y="6" width="14" height="4" rx="1.5" fill="white"/>
      <rect x="6" y="10" width="8" height="11" fill="white"/>
      <rect x="3" y="21" width="14" height="4" rx="1.5" fill="white"/>
      <path d="M 22 9 Q 22 7 24 7 Q 26 7 26 9 L 25.5 22 L 24 27 L 22.5 22 Z" fill="white"/>
      <ellipse cx="24" cy="10" rx="1.2" ry="0.9" fill="#2563eb"/>
    </svg>
  );
}
