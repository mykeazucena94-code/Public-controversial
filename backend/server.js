import express from 'express';
const app = express();
app.use(express.json({ limit: '12mb' }));
app.get('/', (_req, res) => res.json({ name: 'Screen AI backend', status: 'ok' }));
app.get('/health', (_req, res) => res.json({ ok: true }));
app.post('/api/analyze', async (req, res) => {
  try {
    const key = process.env.OPENAI_API_KEY;
    if (!key) return res.status(503).json({ error: 'OPENAI_API_KEY is not configured in Render environment variables.' });
    const imageBase64 = req.body?.imageBase64;
    if (typeof imageBase64 !== 'string' || imageBase64.length < 100) return res.status(400).json({ error: 'A valid screenshot imageBase64 is required.' });
    if (imageBase64.length > 10_000_000) return res.status(413).json({ error: 'Screenshot is too large.' });
    const upstream = await fetch('https://api.openai.com/v1/responses', {
      method: 'POST', headers: { 'Authorization': `Bearer ${key}`, 'Content-Type': 'application/json' },
      body: JSON.stringify({ model: process.env.OPENAI_MODEL || 'gpt-4.1-mini', input: [{ role: 'user', content: [
        { type: 'input_text', text: 'You are Screen AI. Carefully read the visible screen. If it contains a question, answer it directly first, then briefly explain. If text is unclear, say so rather than inventing it. Respect privacy and do not infer hidden information. If there is no clear question, summarize what is visible and ask what the user wants to know.' },
        { type: 'input_image', image_url: `data:image/jpeg;base64,${imageBase64}`, detail: 'high' }
      ] }] })
    });
    const data = await upstream.json();
    if (!upstream.ok) return res.status(upstream.status).json({ error: data?.error?.message || 'AI provider request failed.' });
    const answer = (data.output || []).flatMap(item => item.content || []).filter(c => c.type === 'output_text').map(c => c.text).join('\n').trim();
    return res.json({ answer: answer || 'I could not extract an answer from this screen. Try again with the relevant content visible.' });
  } catch (err) { console.error(err); return res.status(500).json({ error: 'Unexpected backend error.' }); }
});
const port = process.env.PORT || 3000;
app.listen(port, '0.0.0.0', () => console.log(`Screen AI backend listening on ${port}`));
