import { useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { NeedleIcon } from '../components/NeedleIcon';

export function Landing() {
  useEffect(() => { document.title = 'Resume Tailor'; }, []);
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-white flex flex-col">
      <main className="flex-1 flex items-center justify-center px-6 py-16">
        <div className="max-w-xl w-full">
          <div className="flex items-center gap-2.5 mb-10">
            <div className="bg-blue-600 rounded-lg p-2">
              <NeedleIcon size={22} className="text-white" />
            </div>
            <span className="font-semibold text-slate-800 tracking-tight">Resume Tailor</span>
          </div>

          <h1 className="text-5xl font-bold text-slate-900 leading-tight mb-5">
            Land the interview,<br />
            <span className="text-blue-600">not just any interview.</span>
          </h1>

          <p className="text-slate-500 text-lg leading-relaxed mb-8">
            Upload your resume and a job description. Get a skills gap analysis,
            confirm what you have, and download a tailored resume or written suggestions.
            No account needed.
          </p>

          <button
            onClick={() => navigate('/target')}
            className="bg-blue-600 hover:bg-blue-700 text-white font-semibold px-6 py-3 rounded-lg transition-colors"
          >
            Get Started
          </button>

          <p className="text-slate-400 text-sm mt-3">Free. No sign-up required.</p>

          <div className="mt-12 pt-8 border-t border-slate-100 grid grid-cols-3 gap-6">
            <div>
              <div className="font-medium text-slate-800 text-sm mb-1">Skills gap</div>
              <div className="text-slate-500 text-xs leading-relaxed">See exactly what the role needs that your resume does not show.</div>
            </div>
            <div>
              <div className="font-medium text-slate-800 text-sm mb-1">Suggestions</div>
              <div className="text-slate-500 text-xs leading-relaxed">Actionable rewrites and additions tailored to the role.</div>
            </div>
            <div>
              <div className="font-medium text-slate-800 text-sm mb-1">Full resume PDF</div>
              <div className="text-slate-500 text-xs leading-relaxed">Download a fully rewritten resume ready to send.</div>
            </div>
          </div>
        </div>
      </main>

      <footer className="px-6 py-4 border-t border-slate-100">
        <div className="max-w-xl mx-auto">
          <Link to="/privacy" className="text-slate-400 text-xs hover:text-slate-600 transition-colors">
            Privacy Policy
          </Link>
        </div>
      </footer>
    </div>
  );
}
