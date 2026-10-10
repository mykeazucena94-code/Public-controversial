# Screen AI — Android starter

A starter project for an Android screen assistant with a double-tap floating bubble, user-approved MediaProjection screen capture, and a Render-hosted AI backend.

## Important
- This is a starter project, not a finished production app. Android may show a screen-capture consent prompt each session; this is expected.
- The Android app must point to your deployed Render backend. In `app/build.gradle.kts`, replace `https://YOUR-RENDER-SERVICE.onrender.com` in `BACKEND_URL` with your real Render service URL, then commit the change.
- The backend needs an OpenAI API key configured as the Render environment variable `OPENAI_API_KEY`. API usage may cost money. Never place the key in Android code or GitHub.
- The app sends the captured screen image to your backend and then to the AI provider. Do not capture passwords, financial details, private messages, or other sensitive screens.

## Deploy backend to Render
1. Create a new Web Service from this GitHub repository.
2. Set Root Directory to `backend` (or use the included `render.yaml` blueprint).
3. Build command: `npm install`; Start command: `npm start`.
4. Add `OPENAI_API_KEY` in Render Environment → Environment Variables.
5. Deploy and test `/health`.

## Build APK using GitHub Actions
1. Upload this project to the root of a GitHub repository.
2. Open the repository's Actions tab and run **Build Screen AI APK** (or push a change to main).
3. Open the successful workflow run, scroll to Artifacts, and download `screen-ai-debug-apk`.
4. Extract the ZIP on Android and install the APK. Android may ask you to allow installation from that browser/files app.

## Configure backend URL
Update `BACKEND_URL` in `app/build.gradle.kts` to your actual Render URL before building the APK.
