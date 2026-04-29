import { useEffect, useState } from 'react';
import { useLocation, useNavigate, Navigate } from 'react-router-dom';
import { OutputToggle } from '../components/OutputToggle';
import { ErrorMessage } from '../components/ErrorMessage';
import api from '../lib/api';
import { extractErrorMessage } from '../lib/utils';

interface SkillsState {
  skillsGap: string[];
  matchScore: number;
  strengths: string[];
  weaknesses: string[];
  resumeText: string;
  jobDescription: string;
}

export function Skills() {
  useEffect(() => { document.title = 'Resume Tailor — Skills Check'; }, []);
  const location = useLocation();
  const navigate = useNavigate();
  const state = location.state as SkillsState | null;

  const [checkedSkills, setCheckedSkills] = useState<Set<string>>(new Set());
  const [outputMode, setOutputMode] = useState('SUGGESTIONS');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!state) return <Navigate to="/target" replace />;

  const toggleSkill = (skill: string) => {
    setCheckedSkills(prev => {
      const next = new Set(prev);
      next.has(skill) ? next.delete(skill) : next.add(skill);
      return next;
    });
  };

  const handleGenerate = async () => {
    setError(null);
    setLoading(true);

    const req = {
      resumeText: state.resumeText,
      jobDescription: state.jobDescription,
      confirmedSkills: [...checkedSkills],
      outputMode,
    };

    try {
      if (outputMode === 'FULL_RESUME') {
        const res = await api.post('/generate', req, { responseType: 'blob' });
        const pdfUrl = URL.createObjectURL(res.data as Blob);
        navigate('/results', { state: { ...state, pdfUrl, pdfAvailable: true, messages: [] } });
      } else {
        const res = await api.post<{ suggestions: string }>('/generate', req);
        navigate('/results', {
          state: {
            ...state,
            suggestions: res.data.suggestions,
            pdfUrl: null,
            pdfAvailable: false,
            messages: [],
          },
        });
      }
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const loadingLabel = loading
    ? (outputMode === 'FULL_RESUME' ? 'Generating resume… (~20 seconds)' : 'Generating…')
    : 'Generate';

  return (
    <div className="min-h-screen bg-slate-50 flex items-start justify-center px-4 py-12">
      <div className="max-w-xl w-full">
        <h1 className="text-2xl font-bold text-slate-900 mb-1">Skills Check</h1>
        <p className="text-slate-500 text-sm mb-8">
          We found {state.skillsGap.length} skill{state.skillsGap.length !== 1 ? 's' : ''} in the
          job description that weren't evident on your resume. Check the ones you actually have.
        </p>

        <div className="bg-white rounded-xl border border-slate-200 p-6 space-y-6">
          <div className="space-y-2">
            {state.skillsGap.map(skill => (
              <label key={skill} className="flex items-center gap-3 cursor-pointer group">
                <input
                  type="checkbox"
                  checked={checkedSkills.has(skill)}
                  onChange={() => toggleSkill(skill)}
                  className="w-4 h-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                />
                <span className="text-sm text-slate-700 group-hover:text-slate-900">{skill}</span>
              </label>
            ))}
          </div>

          <div>
            <p className="text-sm font-medium text-slate-700 mb-3">Output format:</p>
            <OutputToggle value={outputMode} onChange={setOutputMode} />
          </div>

          <button
            onClick={handleGenerate}
            disabled={loading}
            className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-lg transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {loadingLabel}
          </button>

          <ErrorMessage message={error} />
        </div>
      </div>
    </div>
  );
}
