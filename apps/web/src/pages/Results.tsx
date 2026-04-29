import { useEffect, useState } from 'react';
import { useLocation, Navigate } from 'react-router-dom';
import ReactMarkdown from 'react-markdown';
import { Download } from 'lucide-react';
import { TaylorAvatar } from '../components/TaylorAvatar';
import { ChatPanel } from '../components/ChatPanel';
import type { ChatMessage, Expression } from '../types';

interface ResultsState {
  matchScore: number;
  strengths: string[];
  weaknesses: string[];
  suggestions: string | null;
  pdfUrl: string | null;
  pdfAvailable?: boolean;
  resumeText: string;
  jobDescription: string;
  zeroGap?: boolean;
  messages: ChatMessage[];
}

function scoreToExpression(score: number): Expression {
  if (score >= 70) return 'encouraging';
  if (score < 40) return 'concerned';
  return 'neutral';
}

export function Results() {
  useEffect(() => { document.title = 'Resume Tailor — Your Results'; }, []);
  const location = useLocation();
  const state = location.state as ResultsState | null;
  const [expression, setExpression] = useState<Expression>('celebrating');

  useEffect(() => {
    if (!state) return;
    const t = setTimeout(() => setExpression(scoreToExpression(state.matchScore)), 2000);
    return () => clearTimeout(t);
  }, []);

  useEffect(() => {
    return () => { if (state?.pdfUrl) URL.revokeObjectURL(state.pdfUrl); };
  }, []);

  if (!state) return <Navigate to="/target" replace />;

  const handleDownload = () => {
    const a = document.createElement('a');
    a.href = state.pdfUrl!;
    a.download = 'tailored-resume.pdf';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  };

  const scoreColor =
    state.matchScore >= 70 ? 'text-green-600' :
    state.matchScore < 40  ? 'text-red-500' :
    'text-yellow-600';

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-12">
      <div className="max-w-2xl mx-auto">
        <div className="flex items-center gap-4 mb-8">
          <TaylorAvatar expression={expression} />
          <div>
            <h1 className="text-2xl font-bold text-slate-900">Your Results</h1>
            <p className="text-slate-600 text-sm mt-1">
              {expression === 'celebrating' ? "Here's how you stack up!" : "Here's your analysis."}
            </p>
          </div>
        </div>

        <div className="bg-white rounded-xl border border-slate-200 p-6 mb-6">
          <div className="flex items-baseline gap-2 mb-6">
            <span className={`text-5xl font-bold ${scoreColor}`}>{state.matchScore}</span>
            <span className="text-slate-500 text-lg">/100 match score</span>
          </div>

          {state.zeroGap && (
            <div className="bg-green-50 border border-green-200 text-green-800 rounded-lg px-4 py-3 text-sm mb-6">
              Your resume already covers the key skills in this job description.
            </div>
          )}

          {state.strengths.length > 0 && (
            <div className="mb-4">
              <h3 className="font-semibold text-slate-800 mb-2 text-sm uppercase tracking-wide">Strengths</h3>
              <ul className="space-y-1">
                {state.strengths.map((s, i) => (
                  <li key={i} className="flex items-start gap-2 text-sm text-slate-700">
                    <span className="text-green-500 mt-0.5">✓</span> {s}
                  </li>
                ))}
              </ul>
            </div>
          )}

          {state.weaknesses.length > 0 && (
            <div className="mb-4">
              <h3 className="font-semibold text-slate-800 mb-2 text-sm uppercase tracking-wide">Areas to Strengthen</h3>
              <ul className="space-y-1">
                {state.weaknesses.map((w, i) => (
                  <li key={i} className="flex items-start gap-2 text-sm text-slate-700">
                    <span className="text-orange-400 mt-0.5">→</span> {w}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>

        {state.suggestions && (
          <div className="bg-white rounded-xl border border-slate-200 p-6 mb-6">
            <h3 className="font-semibold text-slate-800 mb-4">Tailoring Suggestions</h3>
            <div className="prose prose-sm max-w-none text-slate-700">
              <ReactMarkdown>{state.suggestions ?? ''}</ReactMarkdown>
            </div>
          </div>
        )}

        {state.pdfAvailable && state.pdfUrl && (
          <div className="bg-white rounded-xl border border-slate-200 p-6 mb-6 flex items-center justify-between">
            <div>
              <h3 className="font-semibold text-slate-800">Your Tailored Resume</h3>
              <p className="text-sm text-slate-500 mt-0.5">Ready to download as PDF</p>
            </div>
            <button
              onClick={handleDownload}
              className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white font-medium px-4 py-2 rounded-lg transition-colors text-sm"
            >
              <Download size={16} /> Download PDF
            </button>
          </div>
        )}

        <ChatPanel
          resumeText={state.resumeText}
          jobDescription={state.jobDescription}
          initialMessages={state.messages}
          taylorExpression={expression}
          setTaylorExpression={setExpression}
        />
      </div>
    </div>
  );
}
