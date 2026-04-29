import { useEffect, useRef, useState } from 'react';
import ReactMarkdown from 'react-markdown';
import { Send } from 'lucide-react';
import type { ChatMessage, Expression } from '../types';

interface Props {
  resumeText: string;
  jobDescription: string;
  initialMessages?: ChatMessage[];
  taylorExpression: Expression;
  setTaylorExpression: (e: Expression) => void;
}

function MessageBubble({ msg }: { msg: ChatMessage }) {
  return (
    <div className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'} mb-3`}>
      <div className={`max-w-[80%] rounded-lg px-4 py-2 text-sm ${
        msg.role === 'user'
          ? 'bg-blue-600 text-white'
          : 'bg-slate-100 text-slate-900'
      }`}>
        {msg.role === 'user'
          ? <p>{msg.content}</p>
          : <div className="prose prose-sm"><ReactMarkdown>{msg.content}</ReactMarkdown></div>
        }
      </div>
    </div>
  );
}

export function ChatPanel({ resumeText, jobDescription, initialMessages = [], taylorExpression: _expr, setTaylorExpression }: Props) {
  const [messages, setMessages] = useState<ChatMessage[]>(initialMessages);
  const [input, setInput] = useState('');
  const [streaming, setStreaming] = useState(false);
  const [currentStream, setCurrentStream] = useState('');
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, currentStream]);

  const sendMessage = async () => {
    if (!input.trim() || streaming) return;
    const userText = input.trim();
    setInput('');

    const userMsg: ChatMessage = { role: 'user', content: userText };
    const updatedMessages = [...messages, userMsg];
    setMessages(updatedMessages);
    setStreaming(true);
    setCurrentStream('');
    setTaylorExpression('thinking');

    try {
      const apiBase = import.meta.env.VITE_API_URL ?? '';
      const response = await fetch(`${apiBase}/api/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ resumeText, jobDescription, messages: updatedMessages }),
      });

      if (!response.ok || !response.body) throw new Error('Stream request failed');

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let fullText = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        const chunk = decoder.decode(value, { stream: true });
        for (const line of chunk.split('\n')) {
          if (line.startsWith('data:')) {
            const token = line.slice(5).trim();
            if (token) {
              fullText += token;
              setCurrentStream(fullText);
            }
          }
          if (line.startsWith('event: done')) {
            setMessages(prev => [...prev, { role: 'assistant', content: fullText }]);
            setCurrentStream('');
          }
        }
      }
    } catch {
      setMessages(prev => [...prev, { role: 'assistant', content: 'Something went wrong. Please try again.' }]);
    } finally {
      setStreaming(false);
      setTaylorExpression('neutral');
    }
  };

  return (
    <div className="mt-8 border border-slate-200 rounded-xl overflow-hidden">
      <div className="bg-slate-50 px-4 py-3 border-b border-slate-200">
        <h3 className="font-semibold text-slate-800 text-sm">Chat with Taylor</h3>
        <p className="text-xs text-slate-500 mt-0.5">Ask follow-up questions about your resume. Session ends when you close the page.</p>
      </div>

      <div className="h-80 overflow-y-auto p-4">
        {messages.length === 0 && (
          <p className="text-slate-400 text-sm text-center mt-8">
            Ask Taylor anything about your resume or the job description.
          </p>
        )}
        {messages.map((msg, i) => <MessageBubble key={i} msg={msg} />)}

        {streaming && currentStream && (
          <div className="flex justify-start mb-3">
            <div className="max-w-[80%] rounded-lg px-4 py-2 bg-slate-100 text-sm">
              <div className="prose prose-sm"><ReactMarkdown>{currentStream}</ReactMarkdown></div>
              <span className="inline-block animate-pulse ml-1">▋</span>
            </div>
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      <div className="border-t border-slate-200 p-3 flex gap-2">
        <textarea
          rows={1}
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => {
            if (e.key === 'Enter' && !e.shiftKey) {
              e.preventDefault();
              sendMessage();
            }
          }}
          placeholder="Ask a question… (Enter to send, Shift+Enter for newline)"
          disabled={streaming}
          className="flex-1 resize-none rounded-lg border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
        />
        <button
          onClick={sendMessage}
          disabled={streaming || !input.trim()}
          className="rounded-lg bg-blue-600 text-white px-3 py-2 hover:bg-blue-700 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
        >
          <Send size={16} />
        </button>
      </div>
    </div>
  );
}
