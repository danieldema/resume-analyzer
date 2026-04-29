import type { Expression } from '../types';

const FACES: Record<Expression, { emoji: string; bg: string }> = {
  neutral:     { emoji: '🤖', bg: 'bg-slate-100' },
  thinking:    { emoji: '🤔', bg: 'bg-yellow-50' },
  encouraging: { emoji: '😊', bg: 'bg-green-50'  },
  concerned:   { emoji: '😟', bg: 'bg-orange-50' },
  celebrating: { emoji: '🎉', bg: 'bg-blue-50'   },
};

export function TaylorAvatar({ expression }: { expression: Expression }) {
  const { emoji, bg } = FACES[expression];
  return (
    <div className={`w-20 h-20 rounded-full ${bg} border-2 border-slate-200 flex items-center justify-center text-4xl select-none`}>
      {emoji}
    </div>
  );
}
