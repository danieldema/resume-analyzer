import { useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { TaylorAvatar } from '../components/TaylorAvatar';

export function Landing() {
  useEffect(() => { document.title = 'Resume Tailor'; }, []);
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-50 to-white flex flex-col items-center justify-center px-4">
      <div className="max-w-lg w-full text-center">
        <div className="flex justify-center mb-6">
          <TaylorAvatar expression="neutral" />
        </div>

        <h1 className="text-4xl font-bold text-slate-900 mb-3">
          Land the interview.<br />
          <span className="text-blue-600">Not just any interview.</span>
        </h1>

        <p className="text-slate-600 text-lg mb-8">
          Upload your resume and paste a job description. Taylor analyzes the gap,
          you confirm what you actually have, and we generate tailored suggestions
          or a full rewritten resume — no account required.
        </p>

        <button
          onClick={() => navigate('/target')}
          className="bg-blue-600 hover:bg-blue-700 text-white font-semibold px-8 py-3 rounded-lg text-lg transition-colors w-full sm:w-auto"
        >
          Tailor My Resume
        </button>

        <p className="text-slate-400 text-sm mt-4">
          Free. No sign-up. Your data is never stored.
        </p>
      </div>

      <footer className="mt-16 text-slate-400 text-sm">
        <Link to="/privacy" className="hover:text-slate-600 transition-colors">Privacy Policy</Link>
      </footer>
    </div>
  );
}
