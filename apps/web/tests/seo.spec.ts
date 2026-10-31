import { test, expect } from '@playwright/test'

test('public brand, canonical, icons and structured data agree', async ({ request }) => {
  const response = await request.get('/')
  expect(response.status()).toBe(200)
  const html = await response.text()
  expect(html).toContain('<title>Habitiq — Find Rooms, Flatmates &amp; Manage Your Flat</title>')
  expect(html).toMatch(/rel="canonical" href="https:\/\/habitiq\.app\/?"/)
  expect(html.match(/rel="manifest"/g)).toHaveLength(1)
  expect(html.match(/rel="apple-touch-icon"/g)).toHaveLength(1)
  expect(html).toContain('What is Habitiq?')
  expect(html).toContain('Recording a settlement does not transfer money.')
  expect(html).toContain('href="/delete-account"')
  const blocks = [...html.matchAll(/<script type="application\/ld\+json">([\s\S]*?)<\/script>/g)]
  const nodes = blocks.flatMap(match => JSON.parse(match[1]))
  const website = nodes.find(node => node['@type'] === 'WebSite')
  expect(website).toMatchObject({ name: 'Habitiq', alternateName: 'Habitiq.app', url: 'https://habitiq.app' })
  expect(response.headers()['x-robots-tag'] || '').not.toContain('noindex')
  const icon = await request.get('/api/pwa-icon/192')
  expect(icon.status()).toBe(200)
  expect(icon.headers()['content-type']).toContain('image/png')
})

test('crawler rules expose brand assets and honest public sitemap', async ({ request }) => {
  const robots = await (await request.get('/robots.txt')).text()
  expect(robots).toContain('Allow: /api/pwa-icon/')
  expect(robots).toContain('Sitemap: https://habitiq.app/sitemap.xml')
  expect(robots).not.toContain('Disallow: /dashboard')
  const sitemap = await (await request.get('/sitemap.xml')).text()
  expect(sitemap.match(/<loc>/g)).toHaveLength(10)
  expect(sitemap).not.toContain('<lastmod>')
  expect(sitemap).not.toMatch(/<loc>[^<]*(dashboard|login|onboarding|join)/)
})

test('private app and authentication routes explicitly opt out of indexing', async ({ request }) => {
  for (const path of ['/dashboard', '/dashboard/tasks', '/onboarding', '/login', '/join']) {
    const response = await request.get(path, { maxRedirects: 0 })
    expect(response.headers()['x-robots-tag'], path).toBe('noindex, nofollow')
  }
})

test('public pages have their own canonical and sharing URLs', async ({ request }) => {
  for (const path of ['/discover', '/flat-manager', '/voice', '/about', '/privacy-and-safety', '/privacy', '/terms', '/safety', '/delete-account']) {
    const response = await request.get(path)
    expect(response.status(), path).toBe(200)
    const html = await response.text()
    expect(html, path).toContain(`rel="canonical" href="https://habitiq.app${path}"`)
    expect(html, path).toContain(`property="og:url" content="https://habitiq.app${path}"`)
    expect(html.match(/<title>/g), path).toHaveLength(1)
    expect(response.headers()['x-robots-tag'] || '', path).not.toContain('noindex')
  }
})

for (const width of [390, 1280]) {
  test(`visible product answers remain readable at ${width}px`, async ({ page }) => {
    await page.setViewportSize({ width, height: 900 })
    await page.goto('/', { waitUntil: 'domcontentloaded' })
    const answers = page.locator('#questions')
    await answers.scrollIntoViewIfNeeded()
    await expect(answers.getByRole('heading', { level: 3 })).toHaveCount(7)
    await expect(answers.getByRole('link', { name: 'account deletion page' })).toBeVisible()
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
    // Capture the viewport: a full-height element capture waits for unrelated
    // animated sections above this one to settle and can time out on desktop.
    await page.screenshot({ path: `test-results/seo-answers-${width}.png` })
  })
}
