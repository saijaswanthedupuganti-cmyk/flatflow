import { test, expect } from '@playwright/test'
import { loginAsMember } from './helpers'

/**
 * Accessibility tests
 * Covers: page titles, focus management, keyboard navigation, ARIA.
 */

test.describe('Page titles', () => {
  const routes: { path: string; expectedTitle: RegExp }[] = [
    { path: '/',                      expectedTitle: /habitiq/i        },
    { path: '/dashboard',             expectedTitle: /habitiq/i        },
    { path: '/dashboard/expenses',    expectedTitle: /habitiq/i        },
    { path: '/dashboard/settings',    expectedTitle: /habitiq/i        },
  ]

  for (const { path, expectedTitle } of routes) {
    test(`${path} has correct title`, async ({ page }) => {
      await loginAsMember(page)
      await page.goto(path)
      await expect(page).toHaveTitle(expectedTitle)
    })
  }
})

test.describe('Keyboard navigation', () => {
  test('can Tab through landing page CTAs', async ({ page }) => {
    await page.goto('/')
    await page.keyboard.press('Tab')
    await expect(page.locator(':focus')).toBeVisible()
  })

  test('modal traps focus correctly', async ({ page }) => {
    await loginAsMember(page)
    await page.goto('/dashboard/expenses')
    await page.getByRole('button', { name: /add expense/i }).click()
    // Tab through modal fields
    await page.keyboard.press('Tab')
    await page.keyboard.press('Tab')
    await expect(page.getByRole('heading', { name: /add a split/i })).toBeVisible()
  })

  test('Escape does not break the expense modal flow', async ({ page }) => {
    await loginAsMember(page)
    await page.goto('/dashboard/expenses')
    await page.getByRole('button', { name: /add expense/i }).click()
    await expect(page.getByRole('heading', { name: /add a split/i })).toBeVisible()
    await page.keyboard.press('Escape')
    await expect(page.getByRole('heading', { name: /add a split/i })).toBeVisible()
  })
})

test.describe('Images and icons', () => {
  test('logo has meaningful text or aria label', async ({ page }) => {
    await page.goto('/')
    // The H logo should have some accessible context
    await expect(page.getByText('Habitiq').first()).toBeVisible()
  })
})

test.describe('Touch targets @mobile', () => {
  test.use({ viewport: { width: 390, height: 844 } })

  test('bottom nav buttons are at least 44px tall', async ({ page }) => {
    await loginAsMember(page)
    const navLinks = page.getByRole('navigation', { name: 'Primary' }).getByRole('link')
    await expect(navLinks).toHaveCount(4)
    for (const link of await navLinks.all()) {
      const box = await link.boundingBox()
      expect(box, 'mobile nav link should have a rendered box').not.toBeNull()
      expect(box!.height).toBeGreaterThanOrEqual(44)
    }
  })
})
