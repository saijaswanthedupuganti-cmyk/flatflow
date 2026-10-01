const CACHE_NAME = 'habitiq-v1'

self.addEventListener('install', () => {
  self.skipWaiting()
})

self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))
    )
  )
  self.clients.claim()
})

self.addEventListener('fetch', event => {
  if (event.request.method !== 'GET') return
  // Skip cross-origin requests (Firebase, Google APIs, CDN)
  if (!event.request.url.startsWith(self.location.origin)) return

  event.respondWith(
    fetch(event.request)
      .then(response => {
        if (response.ok) {
          const clone = response.clone()
          caches.open(CACHE_NAME).then(cache => cache.put(event.request, clone))
        }
        return response
      })
      .catch(() =>
        caches.match(event.request).then(cached => {
          if (cached) return cached
          // Offline fallback page for document navigations
          if (event.request.destination === 'document') {
            return new Response(
              `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Habitiq — Offline</title>
  <style>
    body {
      font-family: -apple-system, sans-serif;
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      margin: 0;
      background: #0a0a0a;
      color: #fff;
      flex-direction: column;
      gap: 12px;
      text-align: center;
      padding: 24px;
      box-sizing: border-box;
    }
    .logo {
      width: 72px;
      height: 72px;
      background: linear-gradient(135deg, #7c3aed, #4338ca);
      border-radius: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 32px;
      font-weight: 800;
      margin-bottom: 8px;
    }
    h1 { font-size: 22px; margin: 0; }
    p { color: #888; margin: 0; font-size: 15px; max-width: 280px; }
  </style>
</head>
<body>
  <div class="logo">H</div>
  <h1>You're offline</h1>
  <p>Open Habitiq when you're back online to see your tasks and duties.</p>
</body>
</html>`,
              { headers: { 'Content-Type': 'text/html' } }
            )
          }
        })
      )
  )
})
