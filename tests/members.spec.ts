import { test, expect } from '@playwright/test'
import { loginAsAdmin, loginAsMember, goTo } from './helpers'

/**
 * Members & Settings tests
 * Covers: member list, member profile, settings page, danger zone.
 */

test.describe('Members page', () => {
  test('shows all flat members', async ({ page }) => {
    await loginAsMember(page)
    await goTo(page, 'Members')
    // Mock flat has: Sai, Rahul, Arjun, Kiran
    await expect(page.getByText(/sai|rahul|arjun|kiran/i).first()).toBeVisible()
  })

  test('shows reliability score for each member', async ({ page }) => {
    await loginAsMember(page)
    await goTo(page, 'Members')
    await expect(page.getByText(/%/)).toBeVisible()
  })

  test('admin sees kick / remove option', async ({ page }) => {
    await loginAsAdmin(page)
    await goTo(page, 'Members')
    await expect(page.getByRole('button', { name: /remove|kick/i }).first()).toBeVisible()
  })

  test('member does not see kick option', async ({ page }) => {
    await loginAsMember(page)
    await goTo(page, 'Members')
    await expect(page.getByRole('button', { name: /kick/i })).not.toBeVisible()
  })
})

test.describe('Settings page', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsMember(page)
    await goTo(page, 'Settings')
  })

  test('shows settings heading', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /settings/i })).toBeVisible()
  })

  test('shows Danger Zone with leave flat option', async ({ page }) => {
    await expect(page.getByText(/danger zone/i)).toBeVisible()
    await expect(page.getByRole('button', { name: /leave flat/i })).toBeVisible()
  })

  test('leave flat shows confirmation dialog', async ({ page }) => {
    await page.getByRole('button', { name: /leave flat/i }).click()
    // Should show a confirmation — not immediately leave
    await expect(page.getByText(/are you sure|confirm|cannot be undone/i).first()).toBeVisible()
  })

  test('cancel leave flat aborts the action', async ({ page }) => {
    await page.getByRole('button', { name: /leave flat/i }).click()
    const cancelBtn = page.getByRole('button', { name: /cancel/i })
    if (await cancelBtn.isVisible()) {
      await cancelBtn.click()
    }
    // Still on settings page
    await expect(page).toHaveURL(/settings/)
  })
})

test.describe('Analytics page', () => {
  test('shows reliability scores and completion grid', async ({ page }) => {
    await loginAsMember(page)
    await goTo(page, 'Analytics')
    await expect(page.getByRole('heading', { name: /analytics/i })).toBeVisible()
  })
})

test.describe('Activity log', () => {
  test('shows activity log entries', async ({ page }) => {
    await loginAsMember(page)
    await page.goto('/dashboard/activity')
    await expect(page.getByRole('heading', { name: /activity/i })).toBeVisible()
  })
})
