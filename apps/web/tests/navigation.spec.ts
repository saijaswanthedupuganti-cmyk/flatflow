import { test, expect } from '@playwright/test'
import { loginAsMember } from './helpers'

/**
 * Navigation tests
 * Covers: desktop sidebar, mobile bottom nav, multi-flat switcher.
 * Mobile tests are tagged @mobile and run in Pixel 5 viewport.
 */

const SECTIONS = [
  { name: 'Dashboard', path: '/dashboard' },
  { name: 'Insights',  path: '/dashboard/insights'  },
  { name: 'Expenses',  path: '/dashboard/expenses'  },
  { name: 'Swaps',     path: '/dashboard/swaps'     },
  { name: 'Members',   path: '/dashboard/members'   },
  { name: 'About',     path: '/dashboard/about'     },
]

test.describe('Desktop sidebar navigation', () => {
  test.use({ viewport: { width: 1280, height: 800 } })

  test.beforeEach(async ({ page }) => {
    await loginAsMember(page)
  })

  for (const section of SECTIONS) {
    test(`navigates to ${section.name}`, async ({ page }) => {
      await page.getByRole('link', { name: new RegExp(`^${section.name}$`, 'i') }).click()
      await expect(page).toHaveURL(new RegExp(`${section.path.replace('/', '\\/')}(?:[/?#]|$)`))
      await expect(page.locator('main')).toBeVisible()
    })
  }

  test('sidebar shows Habitiq branding', async ({ page }) => {
    await expect(page.getByRole('img', { name: 'Habitiq' })).toBeVisible()
  })

  test('sidebar shows flat name', async ({ page }) => {
    await expect(page.getByRole('button', { name: 'Bachelor Pad', exact: true })).toBeVisible()
  })
})

test.describe('Mobile bottom nav @mobile', () => {
  test.use({ viewport: { width: 390, height: 844 } })

  test.beforeEach(async ({ page }) => {
    await loginAsMember(page)
  })

  test('bottom nav is visible on mobile', async ({ page }) => {
    const nav = page.getByRole('navigation', { name: 'Primary' })
    await expect(nav).toBeVisible()
    await expect(nav.getByRole('link')).toHaveCount(4)
  })

  test('can navigate to Manage from bottom nav @mobile', async ({ page }) => {
    await page.getByRole('navigation', { name: 'Primary' }).getByRole('link', { name: 'Manage' }).click()
    await expect(page).toHaveURL(/\/dashboard\/tasks/)
    await expect(page.locator('main')).toBeVisible()
  })
})

test.describe('Swap navigation', () => {
  test('opens the swaps page from the sidebar', async ({ page }) => {
    await loginAsMember(page)
    await page.getByRole('link', { name: /^swaps$/i }).click()
    await expect(page).toHaveURL(/\/dashboard\/swaps/)
    await expect(page.getByRole('heading', { name: /swap/i })).toBeVisible()
  })
})
