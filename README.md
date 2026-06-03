# Resume Tailor

A full-stack web app that analyzes your resume against a job description, surfaces the skills gap, and generates either tailored suggestions or a fully rewritten resume as a downloadable PDF.

**Live:** [resume-tailor.net](https://resume-tailor.net) — no account required.

---

## How It Works

1. **Upload** your resume (PDF) and paste a job description.
2. **Review** the AI-generated skills gap analysis. Check off skills you actually have — so nothing gets fabricated.
3. **Generate** either written suggestions or a complete rewritten resume as a downloadable PDF.
4. **Ask follow-up questions** via the built-in streaming chatbot, with your resume and the job description already in context.

The application is completely stateless. No data is stored anywhere — everything lives in the browser session.

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

## Architecture

```
Browser (React SPA)
  │
  ├── POST /api/analyze    → AnalyzeService → PDFBox + Gemini → skills gap JSON
  ├── POST /api/generate   → GenerateService → Gemini → POI → LibreOffice → PDF bytes
  └── POST /api/chat       → ChatService → Gemini streaming → SSE Flux<String>
```

The frontend is a static SPA hosted on Vercel. The API is a Spring Boot JAR containerized in Docker and deployed on Railway. They share no state — every request is self-contained.

**PDF generation pipeline:**
Gemini returns plain text → Apache POI writes it into a `.docx` in memory → LibreOffice converts the `.docx` to `.pdf` in a subprocess → bytes are streamed back in the HTTP response. Temp files are cleaned up in a `finally` block regardless of outcome.

**Streaming chat:**
The `/api/chat` endpoint returns a `Flux<ServerSentEvent<String>>` from Spring WebFlux. The frontend reads it with the `ReadableStream` API, appending tokens to the UI as they arrive, then commits the full response to message history on the terminal `event: done`.

---

## Running Locally

### Prerequisites

- Java 21
- Node 20
- A [Google Gemini API key](https://aistudio.google.com/app/apikey) (free tier)
- LibreOffice installed (only needed for PDF generation; skip with `SKIP_PDF=true`)

### Backend

```bash
cd apps/api
```

Create `src/main/resources/application-local.properties`:

```properties
gemini.api-key=YOUR_GEMINI_API_KEY
gemini.model=gemini-1.5-flash
allowed.origins=http://localhost:5173
pdf.skip=true
```

```bash
./mvnw spring-boot:run
# API available at http://localhost:8080
```

### Frontend

```bash
cd apps/web
npm install
npm run dev
# App available at http://localhost:5173
```

The Vite dev server proxies `/api/*` requests to `localhost:8080` automatically.

### Running Tests

```bash
cd apps/api
./mvnw verify
```

---

## Deployment

The backend is packaged as a multi-stage Docker image. Stage 1 builds the JAR with Maven; stage 2 runs it on a JRE base image with LibreOffice installed.

```bash
docker build -t resume-tailor .
docker run -p 8080:8080 \
  -e GEMINI_API_KEY=your_key \
  -e ALLOWED_ORIGINS=https://your-frontend.com \
  resume-tailor
```

Railway reads `railway.json` for the health check path (`/api/health`). All other configuration is set via environment variables in the Railway dashboard.

---

## API Reference

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/health` | Health check |
| `POST` | `/api/analyze` | Upload PDF + job description → skills gap analysis |
| `POST` | `/api/generate` | Generate suggestions or full resume PDF |
| `POST` | `/api/chat` | Stream a chat reply (SSE) |

### POST /api/analyze

`multipart/form-data` — `file` (PDF, max 10 MB) + `jobDescription` (string)

```json
{
  "matchScore": 72,
  "strengths": ["Strong Java background", "..."],
  "weaknesses": ["No cloud experience shown", "..."],
  "skillsGap": ["Kubernetes", "Terraform", "..."],
  "resumeText": "..."
}
```

### POST /api/generate

```json
{
  "resumeText": "...",
  "jobDescription": "...",
  "confirmedSkills": ["Kubernetes", "Terraform"],
  "outputMode": "SUGGESTIONS"
}
```

`outputMode: "SUGGESTIONS"` → JSON `{ "suggestions": "..." }`  
`outputMode: "FULL_RESUME"` → `application/pdf` binary

### POST /api/chat

```json
{
  "resumeText": "...",
  "jobDescription": "...",
  "messages": [
    { "role": "user", "content": "How should I reframe my cloud experience?" }
  ]
}
```

Returns `text/event-stream`. Each `data:` line is a token; `event: done` signals end of stream.

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
