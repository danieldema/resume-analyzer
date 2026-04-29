import { useEffect } from 'react';
import { Link } from 'react-router-dom';

export function Privacy() {
  useEffect(() => { document.title = 'Resume Tailor — Privacy'; }, []);

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-12">
      <div className="max-w-2xl mx-auto">
        <Link to="/" className="text-blue-600 hover:text-blue-800 text-sm mb-6 inline-block">
          ← Back to Resume Tailor
        </Link>

        <h1 className="text-3xl font-bold text-slate-900 mb-8">Privacy Policy</h1>

        <div className="bg-white rounded-xl border border-slate-200 p-8 space-y-6 text-slate-700">
          <section>
            <h2 className="font-semibold text-slate-900 mb-2">What data is sent</h2>
            <p className="text-sm leading-relaxed">
              Your resume text and job description are sent to Google Gemini's API for analysis and content generation.
              This is necessary for the service to function. We do not store your resume or job description on our servers —
              the data is processed in memory and discarded after each request.
            </p>
          </section>

          <section>
            <h2 className="font-semibold text-slate-900 mb-2">Session lifetime</h2>
            <p className="text-sm leading-relaxed">
              Your session ends when you close or refresh the page. No data is saved between visits.
              There are no accounts, no local storage of your resume, and no history of past analyses.
            </p>
          </section>

          <section>
            <h2 className="font-semibold text-slate-900 mb-2">Tracking and cookies</h2>
            <p className="text-sm leading-relaxed">
              Resume Tailor does not use tracking cookies, analytics, or advertising scripts.
              No personal information is collected or sold.
            </p>
          </section>

          <section>
            <h2 className="font-semibold text-slate-900 mb-2">Google Gemini</h2>
            <p className="text-sm leading-relaxed">
              Content you submit is processed by Google Gemini's API, subject to{' '}
              <a
                href="https://ai.google.dev/terms"
                target="_blank"
                rel="noopener noreferrer"
                className="text-blue-600 hover:underline"
              >
                Google's AI terms of service
              </a>.
            </p>
          </section>

          <section>
            <h2 className="font-semibold text-slate-900 mb-2">Contact</h2>
            <p className="text-sm leading-relaxed">
              For questions or concerns, email{' '}
              <a href="mailto:danieldema42@gmail.com" className="text-blue-600 hover:underline">
                danieldema42@gmail.com
              </a>.
            </p>
          </section>
        </div>
      </div>
    </div>
  );
}
