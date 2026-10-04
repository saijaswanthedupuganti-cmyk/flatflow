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
    const bg = await page.locator('[data-glass]').first().evaluate(el => getComputedStyle(el).backgroundColor)
    return bg.startsWith('rgba') ? parseFloat(bg.split(',')[3]) : 1
  }, { timeout: 5000 }).toBeGreaterThanOrEqual(0.9)
})

test.describe('navigation', () => {
  test('navbar shows products, privacy, about and download', async ({ page }) => {
    await page.goto('/')
    const nav = page.getByRole('navigation', { name: 'Main' })
    await expect(nav.getByRole('button', { name: 'Products' })).toBeVisible()
    await nav.getByRole('button', { name: 'Products' }).click()
    await expect(nav.getByRole('link', { name: /Flat manager/ })).toBeVisible()
    await expect(nav.getByRole('link', { name: /Discover/ })).toBeVisible()
    await expect(nav.getByRole('link', { name: /Voice assistant/ })).toBeVisible()
    await expect(nav.getByRole('link', { name: 'Privacy' })).toBeVisible()
    await expect(nav.getByRole('link', { name: 'About' })).toBeVisible()
  })

  test('mobile menu opens and closes @mobile', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 800 })
    await page.goto('/')
    await page.getByRole('button', { name: 'Open menu' }).click()
    const sheet = page.getByRole('dialog', { name: 'Menu' })
    await expect(sheet.getByRole('link', { name: /Discover/ })).toBeVisible()
    await page.keyboard.press('Escape')
    await expect(sheet).toHaveCount(0)
  })

  test('footer has legal links and contact', async ({ page }) => {
    await page.goto('/')
    const footer = page.getByRole('contentinfo')
    for (const name of ['Privacy & safety', 'Privacy policy', 'Terms', 'Safety', 'About']) {
      await expect(footer.getByRole('link', { name, exact: true })).toBeVisible()
    }
    await expect(footer.getByRole('link', { name: 'hello@habitiq.app' })).toHaveAttribute('href', 'mailto:hello@habitiq.app')
  })
})

test('home phone mockup is labelled and never wider than the viewport @mobile', async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 800 })
  await page.goto('/')
  const phone = page.getByRole('img', { name: /Oddroof home screen/ })
  await expect(phone).toBeVisible()
  const box = await phone.boundingBox()
  expect(box!.width).toBeLessThanOrEqual(375 - 32)
})

test.describe('hero', () => {
  test('headline, download with meta, and see-how link', async ({ page }) => {
    await page.goto('/')
    await expect(page.getByRole('heading', { level: 1, name: 'Your flat, sorted.' })).toBeVisible()
    await expect(page.locator('#hero [data-download]')).toBeVisible()
    await expect(page.locator('#hero [data-download-meta]')).toBeVisible()
    await expect(page.getByRole('link', { name: 'See how it works' })).toHaveAttribute('href', '#how-it-works')
  })

  test('QR shows on desktop only', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 })
    await page.goto('/')
    await expect(page.locator('#hero [data-download-qr]')).toBeVisible()
    await page.setViewportSize({ width: 375, height: 800 })
    await expect(page.locator('#hero [data-download-qr]')).toBeHidden()
  })
})

test('problem section names the WhatsApp chaos and resolves it', async ({ page }) => {
  await page.goto('/')
  const sec = page.locator('#problem')
  await sec.scrollIntoViewIfNeeded()
  await expect(sec.getByRole('heading', { name: /WhatsApp group is doing too much/ })).toBeVisible()
  await expect(sec.getByText('Who bought milk?')).toBeAttached()
  await expect(sec.getByText('One place for the whole flat')).toBeVisible()
})

test.describe('pinned story', () => {
  test('four steps in order', async ({ page }) => {
    await page.goto('/')
    const steps = page.locator('#how-it-works [data-story-step] h3')
    await expect(steps).toHaveText(['Tasks rotate fairly', 'Expenses split themselves', 'Bills and month close', 'Away mode'])
  })

  test('phone screen follows the step on desktop', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 })
    await page.goto('/')
    await page.locator('#how-it-works [data-story-step]').nth(1).scrollIntoViewIfNeeded()
    await expect(page.locator('#how-it-works [data-active-screen]')).toHaveAttribute('data-active-screen', '1', { timeout: 4000 })
  })
})
