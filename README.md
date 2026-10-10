# Controversy Archive

A responsive full-stack starter app for exploring contested public-interest topics while keeping source trails, evidence labels, and uncertainty visible.

## Features
- Search across topic titles, summaries, categories, research questions, facts, and claims
- Filter by category and evidence status
- Topic detail view with documented facts, claims under review, multiple perspectives, sources, and limitations
- Create new research files through the UI
- Express API and JSON-backed persistence
- Seed archive entries for science, technology, history, and society
- Responsive layout for phones and desktop

## Run locally
Requires Node.js 18+.

```bash
npm install
npm start
```

Open `http://localhost:3000`.

## API
- `GET /api/health`
- `GET /api/topics?q=climate&category=Science&status=All`
- `GET /api/topics/:id`
- `POST /api/topics` with JSON `{ "title": "...", "category": "Science", "summary": "...", "question": "..." }`

Data is stored in `data.json`, created automatically on first launch. Back up this file. For production, replace JSON storage with a database (PostgreSQL/Supabase), add authentication and role-based access, request validation, rate limiting, audit history, and automated backups.

## Important evidence policy
The seeded topics are starter research leads and source directories, not automated fact-check verdicts. Always open source links and verify the specific source, date, methodology, and context. Keep allegations distinct from documented facts. Do not infer guilt or motive from association alone. New topics are marked `Needs review`.

## Deploy
This project can be deployed to a Node-compatible host such as Render or Railway. Set the build/install command to `npm install` and start command to `npm start`. Note: the JSON file may not persist across some hosts' redeploys or ephemeral filesystems; use a persistent disk or managed database before relying on it for long-term archives.
