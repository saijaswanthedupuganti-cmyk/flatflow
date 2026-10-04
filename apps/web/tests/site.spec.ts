import { test, expect } from '@playwright/test'
import version from '../public/downloads/version.json'

test.describe('site shell', () => {
  test('home renders the Oddroof site root', async ({ page }) => {
    await page.goto('/')
    await expect(page.locator('.or-site')).toBeVisible()
    await expect(page).toHaveTitle(/Oddroof/)
  })

  test('PWA install prompt never renders on site routes', async ({ page }) => {
    // Make the browser look install-eligible so the prompt WOULD show without the guard.
    await page.addInitScript(() => {
      window.addEventListener('load', () => {
        const e = new Event('beforeinstallprompt') as Event & { prompt?: () => Promise<void>; userChoice?: Promise<unknown> }
        e.prompt = async () => {}
        e.userChoice = Promise.resolve({ outcome: 'dismissed' })
        window.dispatchEvent(e)
      })
    })
    await page.goto('/')
    await page.waitForTimeout(2500) // prompt appears after 1.5 s when eligible
    await expect(page.getByRole('button', { name: /install/i })).toHaveCount(0)
  })
})

test.describe('download', () => {
  test('every download button points at version.json url', async ({ page }) => {
    await page.goto('/')
    const links = page.locator('[data-download]')
    expect(await links.count()).toBeGreaterThan(0)
    for (const href of await links.evaluateAll(els => els.map(e => e.getAttribute('href')))) {
      expect(href).toBe(version.url)
    }
  })

  test('meta line shows version, size and Android floor', async ({ page }) => {
    await page.goto('/')
    await expect(page.locator('[data-download-meta]').first())
      .toHaveText(`Android ${version.minAndroid}+ · ${version.sizeMb} MB · free · v${version.version}`)
  })
})

test('glass falls back to a solid surface without backdrop-filter', async ({ page }) => {
  await page.addInitScript(() => {
    const orig = CSS.supports.bind(CSS)
    // @ts-expect-error test override
    CSS.supports = (a: string, b?: string) => (String(a).includes('backdrop-filter') ? false : orig(a, b))
  })
  await page.goto('/')
  // The fallback class is applied after hydration, so poll rather than read once.
  await expect.poll(async () => {
    const bg = await page.locator('[data-glass="canvas"]').first().evaluate(el => getComputedStyle(el).backgroundColor)
    return bg.startsWith('rgba') ? parseFloat(bg.split(',')[3]) : 1
  }, { timeout: 5000 }).toBeGreaterThanOrEqual(0.9)
})
