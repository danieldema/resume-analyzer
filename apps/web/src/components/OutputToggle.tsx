interface Props {
  value: string;
  onChange: (v: string) => void;
}

export function OutputToggle({ value, onChange }: Props) {
  return (
    <div className="flex gap-3">
      {[
        { id: 'SUGGESTIONS', label: 'Suggestions Only', sub: 'Written advice, fast' },
        { id: 'FULL_RESUME',  label: 'Full Resume',      sub: 'Rewrites & downloads PDF, ~20s' },
      ].map(({ id, label, sub }) => (
        <button
          key={id}
          type="button"
          onClick={() => onChange(id)}
          className={`flex-1 rounded-lg border-2 px-4 py-3 text-left transition-colors ${
            value === id
              ? 'border-blue-600 bg-blue-50 text-blue-900'
              : 'border-slate-200 bg-white text-slate-700 hover:border-slate-300'
          }`}
        >
          <div className="font-medium text-sm">{label}</div>
          <div className="text-xs text-slate-500 mt-0.5">{sub}</div>
        </button>
      ))}
    </div>
  );
}
