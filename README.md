# Resume Tailor

A full-stack web app that analyzes your resume against a job description, surfaces the skills gap, and generates either tailored suggestions or a fully rewritten resume as a downloadable PDF.

**Live:** [resume-tailor.net](https://resume-tailor.net): no account required.

---

## How It Works

1. **Upload** your resume (PDF) and paste a job description.
2. **Review** the AI-generated skills gap analysis. Check off skills you actually have, so nothing gets fabricated.
3. **Generate** either written suggestions or a complete rewritten resume as a downloadable PDF.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 19, TypeScript, Tailwind CSS, Vite |
| Backend | Spring Boot 3.5 (Java 21) |
| AI | Google Gemini API (`gemini-1.5-flash`) |
| PDF pipeline | Apache POI (DOCX generation) → LibreOffice headless (PDF conversion) |
| PDF extraction | Apache PDFBox |
| Rate limiting | Bucket4j (10 requests/hour per IP, in-memory) |
| Streaming | Server-Sent Events (SSE) via Spring WebFlux + `ReadableStream` on the client |
| Deployment | Railway (API, Dockerized) + Vercel (frontend) |

---

## Project Structure

```
resume-analyzer/
├── apps/
│   ├── api/                   Spring Boot backend
│   │   └── src/main/java/com/resumetailor/api/
│   │       ├── analyze/       PDF upload + skills gap endpoint
│   │       ├── generate/      Suggestions + PDF generation endpoint
│   │       ├── chat/          Streaming chat endpoint
│   │       ├── gemini/        Gemini API client
│   │       ├── pdf/           PDFBox text extraction
│   │       ├── docx/          POI + LibreOffice PDF pipeline
│   │       ├── ratelimit/     Bucket4j per-IP rate limiter
│   │       ├── config/        CORS, WebClient, interceptor config
│   │       └── exception/     Global error handler
│   └── web/                   React + Vite frontend
│       └── src/
│           ├── pages/         Landing, Target, Skills, Results, Privacy
│           ├── components/    ChatPanel, OutputToggle, ErrorMessage, NeedleIcon
│           ├── lib/           Axios client, error utilities
│           └── types/         Shared TypeScript interfaces
├── Dockerfile                 Multi-stage build (Maven → JRE + LibreOffice)
└── railway.json               Railway deployment config
```
