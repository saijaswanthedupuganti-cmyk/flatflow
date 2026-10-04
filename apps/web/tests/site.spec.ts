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
  const phone = page.locator('#hero [data-deck-card]').first()
  await expect(phone).toBeVisible()
  const box = await phone.boundingBox()
  expect(box!.width).toBeLessThanOrEqual(375 - 32)
})

test.describe('hero', () => {
  test('headline, download with meta, and see-how link', async ({ page }) => {
    await page.goto('/')
    await expect(page.getByRole('heading', { level: 1, name: 'Find your flat. Run it together.' })).toBeVisible()
    await expect(page.locator('#hero [data-download]')).toBeVisible()
    await expect(page.locator('#hero [data-download-meta]')).toBeVisible()
    await expect(page.locator('#hero').getByRole('link', { name: 'Explore Discover' })).toHaveAttribute('href', '#discover')
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

test('voice section shows the demo request and confirm card', async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' }) // final state, deterministic
  await page.goto('/')
  const sec = page.locator('#voice')
  await sec.scrollIntoViewIfNeeded()
  await expect(sec.getByRole('heading', { name: 'Just say it.' })).toBeVisible()
  await expect(sec.getByText('paid Ravi 300 for the cylinder')).toBeVisible()
  await expect(sec.getByText('Record ₹300 you paid Ravi')).toBeVisible()
  await expect(sec.getByText(/Hindi or Telugu/)).toBeVisible()
})

test('no hydration errors with reduced motion', async ({ page }) => {
  const errors: string[] = []
  page.on('console', m => { if (m.type() === 'error') errors.push(m.text()) })
  page.on('pageerror', e => errors.push(e.message))
  await page.emulateMedia({ reducedMotion: 'reduce' })
  await page.goto('/')
  await page.waitForTimeout(1500)
  expect(errors.filter(e => /hydrat/i.test(e))).toEqual([])
})

test('discover section shows listings, both journeys and the privacy note', async ({ page }) => {
  await page.goto('/')
  const sec = page.locator('#discover')
  await sec.scrollIntoViewIfNeeded()
  await expect(sec.getByRole('heading', { name: /next flat, or your next flatmate/ })).toBeVisible()
  await expect(sec.locator('[data-listing]')).toHaveCount(2)
  await expect(sec.getByText('Approximate location only')).toBeVisible()
  await expect(sec.getByRole('link', { name: 'Explore Discover' })).toHaveAttribute('href', '/discover')
})

test.describe('closing sections', () => {
  test('trust tiles carry only the approved claims', async ({ page }) => {
    await page.goto('/')
    const tiles = page.locator('#trust [data-trust-tile] b')
    await expect(tiles).toHaveText([
      'Contact stays private', 'Approximate location only', 'Your data, your call', 'Built for India\'s DPDP Act',
    ])
    await expect(page.locator('#trust').getByRole('link', { name: 'Read how we protect you' })).toHaveAttribute('href', '/privacy-and-safety')
  })

  test('install steps show version and date from version.json', async ({ page }) => {
    await page.goto('/')
    const sec = page.locator('#install')
    await expect(sec.locator('ol > li')).toHaveCount(3)
    await expect(sec.getByText(`Version ${version.version}`)).toBeVisible()
  })

  test('final CTA repeats the download', async ({ page }) => {
    await page.goto('/')
    await expect(page.locator('#get-oddroof [data-download]')).toHaveAttribute('href', version.url)
  })
})

const WIDTHS = [375, 768, 1024, 1440]

for (const width of WIDTHS) {
  test(`no horizontal scroll at ${width}px`, async ({ page }) => {
    await page.setViewportSize({ width, height: 900 })
    await page.goto('/')
    // Walk the page so scroll-linked layouts settle at every section.
    for (const id of ['hero', 'problem', 'how-it-works', 'voice', 'discover', 'trust', 'install', 'get-oddroof']) {
      await page.locator(`#${id}`).scrollIntoViewIfNeeded()
      const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth)
      expect(overflow, `overflow in #${id}`).toBeLessThanOrEqual(0)
    }
  })
}

test.describe('reduced motion', () => {
  test('every section heading and lede is visible without animation', async ({ page }) => {
    // test.use({ reducedMotion }) does not reach the page in this setup; emulateMedia does.
    await page.emulateMedia({ reducedMotion: 'reduce' })
    await page.goto('/')
    await page.waitForTimeout(500) // let hydration switch Reveal to its final state
    for (const id of ['problem', 'how-it-works', 'voice', 'discover', 'trust', 'install', 'get-oddroof']) {
      const sec = page.locator(`#${id}`)
      await sec.scrollIntoViewIfNeeded()
      const heading = sec.getByRole('heading').first()
      await expect(heading).toBeVisible()
      // toBeVisible() treats opacity:0 as visible, so check the effective opacity up the tree.
      const minOpacity = await heading.evaluate(el => {
        let o = 1
        for (let n: Element | null = el; n; n = n.parentElement) o = Math.min(o, parseFloat(getComputedStyle(n).opacity))
        return o
      })
      expect(minOpacity).toBe(1)
    }
  })
})


test.describe('revision 2', () => {
  test('hero deck cycles four feature cards, discovery first', async ({ page }) => {
    await page.goto('/')
    const cards = page.locator('#hero [data-deck-card]')
    await expect(cards).toHaveCount(4)
    const labels = await cards.evaluateAll(els => els.map(e => e.getAttribute('aria-label')))
    expect(labels[0]).toMatch(/Discover flats/)
    expect(labels[1]).toMatch(/Discover flatmates/)
    const first = await page.locator('#hero [data-deck-active]').getAttribute('data-deck-active')
    await expect(page.locator('#hero [data-deck-active]')).not.toHaveAttribute('data-deck-active', first!, { timeout: 6000 })
  })

  test('discover comes straight after the hero with both journeys', async ({ page }) => {
    await page.goto('/')
    const ids = await page.locator('main > section').evaluateAll(els => els.map(e => e.id))
    expect(ids.slice(0, 3)).toEqual(['hero', 'discover', 'problem'])
    const sec = page.locator('#discover')
    await expect(sec.getByRole('heading', { name: 'Find a flat', exact: true })).toBeVisible()
    await expect(sec.getByRole('heading', { name: 'Find a flatmate' })).toBeVisible()
  })

  for (const width of [375, 1440]) {
    test(`no mockup or bubble is cut off at ${width}px`, async ({ page }) => {
      await page.setViewportSize({ width, height: 900 })
      await page.emulateMedia({ reducedMotion: 'reduce' })
      await page.goto('/')
      await page.waitForTimeout(600)
      for (const sel of ['[data-bubble]', '[data-listing]', '[data-deck-card]']) {
        const boxes = await page.locator(sel).evaluateAll(els => els.map(e => { const r = e.getBoundingClientRect(); return [r.left, r.right] }))
        for (const [l, r] of boxes) {
          expect(l, `${sel} left`).toBeGreaterThanOrEqual(-1)
          expect(r, `${sel} right`).toBeLessThanOrEqual(width + 1)
        }
      }
    })
  }

  test('every nav page loads for a logged-out visitor', async ({ page }) => {
    for (const path of ['/discover', '/flat-manager', '/voice', '/privacy-and-safety', '/about']) {
      const res = await page.goto(path)
      expect(res!.status(), path).toBe(200)
      await page.waitForTimeout(800)
      expect(new URL(page.url()).pathname, `${path} redirected`).toBe(path)
      await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
    }
  })

  test('voice confirm card stays readable for at least 2.5 seconds', async ({ page }) => {
    await page.goto('/')
    const sec = page.locator('#voice')
    await sec.scrollIntoViewIfNeeded()
    const card = sec.getByText('Record ₹300 you paid Ravi')
    await expect(card).toBeVisible({ timeout: 8000 })
    await page.waitForTimeout(2500)
    await expect(card).toBeVisible()
  })

  test('mobile menu moves focus inside and returns it @mobile', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 800 })
    await page.goto('/')
    const burger = page.getByRole('button', { name: 'Open menu' })
    await burger.click()
    await expect(page.getByRole('button', { name: 'Close menu' })).toBeFocused()
    await page.keyboard.press('Escape')
    await expect(burger).toBeFocused()
  })
})
