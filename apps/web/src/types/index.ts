export interface AnalyzeResponse {
  matchScore: number;
  strengths: string[];
  weaknesses: string[];
  skillsGap: string[];
  resumeText: string;
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}
