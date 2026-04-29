import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { TaylorAvatar } from '../components/TaylorAvatar';
import { ErrorMessage } from '../components/ErrorMessage';
import api from '../lib/api';
import { extractErrorMessage } from '../lib/utils';
import type { AnalyzeResponse, Expression } from '../types';

const schema = z.object({
  file: z
    .instanceof(FileList)
    .refine(f => f.length > 0, 'Please select a file')
    .refine(f => f[0]?.type === 'application/pdf', 'Must be a PDF')
    .refine(f => (f[0]?.size ?? 0) <= 10 * 1024 * 1024, 'Must be under 10 MB'),
  jobDescription: z
    .string()
    .min(100, 'Please paste the full job description (at least 100 characters)'),
});

type FormValues = z.infer<typeof schema>;

export function Target() {
  useEffect(() => { document.title = 'Resume Tailor — Upload'; }, []);
  const navigate = useNavigate();
  const [expression, setExpression] = useState<Expression>('neutral');
  const [error, setError] = useState<string | null>(null);

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data: FormValues) => {
    setError(null);
    setExpression('thinking');
    const formData = new FormData();
    formData.append('file', data.file[0]);
    formData.append('jobDescription', data.jobDescription);

    try {
      const res = await api.post<AnalyzeResponse>('/analyze', formData);
      const analysis = res.data;

      if (analysis.skillsGap.length === 0) {
        navigate('/results', {
          state: {
            matchScore: analysis.matchScore,
            strengths: analysis.strengths,
            weaknesses: analysis.weaknesses,
            suggestions: null,
            pdfUrl: null,
            resumeText: analysis.resumeText,
            jobDescription: data.jobDescription,
            zeroGap: true,
            messages: [],
          },
        });
      } else {
        navigate('/skills', {
          state: {
            skillsGap: analysis.skillsGap,
            matchScore: analysis.matchScore,
            strengths: analysis.strengths,
            weaknesses: analysis.weaknesses,
            resumeText: analysis.resumeText,
            jobDescription: data.jobDescription,
          },
        });
      }
    } catch (err) {
      setError(extractErrorMessage(err));
      setExpression('neutral');
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex items-start justify-center px-4 py-12">
      <div className="max-w-xl w-full">
        <div className="flex items-center gap-4 mb-8">
          <TaylorAvatar expression={expression} />
          <div>
            <h1 className="text-2xl font-bold text-slate-900">Let's get started</h1>
            <p className="text-slate-600 text-sm mt-1">
              {expression === 'thinking' ? 'Analyzing your resume…' : 'Upload your resume and paste the job description.'}
            </p>
          </div>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6 bg-white rounded-xl border border-slate-200 p-6">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Resume (PDF)</label>
            <input
              type="file"
              accept="application/pdf"
              {...register('file')}
              className="block w-full text-sm text-slate-600 file:mr-3 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-medium file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100 cursor-pointer"
            />
            {errors.file && <p className="text-red-600 text-xs mt-1">{errors.file.message as string}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Job Description</label>
            <textarea
              rows={8}
              placeholder="Paste the full job description here…"
              {...register('jobDescription')}
              className="block w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
            />
            {errors.jobDescription && <p className="text-red-600 text-xs mt-1">{errors.jobDescription.message}</p>}
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-lg transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {isSubmitting ? 'Analyzing…' : 'Analyze Resume'}
          </button>

          <ErrorMessage message={error} />
        </form>
      </div>
    </div>
  );
}
