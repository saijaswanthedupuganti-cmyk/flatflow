// Audit-only preview. Does not change the production configuration.
process.env.NEXT_PUBLIC_FIREBASE_API_KEY = 'YOUR_API_KEY';
const next = require('next');
const { createServer } = require('node:http');
const app = next({ dev: true, dir: 'C:/garbage', hostname: '127.0.0.1', port: 3017,
  conf: { distDir: 'design-plans/2026-09-25-audit/.next-audit' } });
const handle = app.getRequestHandler();
app.prepare().then(() => createServer(handle).listen(3017, '127.0.0.1', () => console.log('Audit mock preview: http://127.0.0.1:3017')));
