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

  test('shows reliability scores', async ({ page }) => {
    await loginAsMember(page)
    await goTo(page, 'Members')
    await expect(page.getByText('Avg Score', { exact: true })).toBeVisible()
    await expect(page.getByText('95 score', { exact: true })).toBeVisible()
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
    await expect(page.getByRole('button', { name: /^leave$/i })).toBeVisible()
  })

  test('leave flat shows confirmation dialog', async ({ page }) => {
    await page.getByRole('button', { name: /^leave$/i }).click()
    await expect(page.getByRole('heading', { name: /leave bachelor pad/i })).toBeVisible()
    await expect(page.getByRole('button', { name: /^leave flat$/i })).toBeVisible()
  })

  test('cancel leave flat aborts the action', async ({ page }) => {
    await page.getByRole('button', { name: /^leave$/i }).click()
    await page.getByRole('button', { name: /^cancel$/i }).click()
    await expect(page.getByRole('heading', { name: /leave bachelor pad/i })).not.toBeVisible()
    await expect(page.getByRole('button', { name: /^leave$/i })).toBeVisible()
    await expect(page).toHaveURL(/settings/)
  })
})

test.describe('Insights page', () => {
  test('shows completion calendar and per-person summary', async ({ page }) => {
    await loginAsMember(page)
    await goTo(page, 'Analytics')
    await expect(page).toHaveURL(/\/dashboard\/insights/)
    await expect(page.getByRole('heading', { name: 'Insights' })).toBeVisible()
    await expect(page.getByRole('heading', { name: /this month.*per person/i })).toBeVisible()
  })
})

test.describe('Activity log', () => {
  test('shows activity log entries', async ({ page }) => {
    await loginAsMember(page)
    await page.goto('/dashboard/activity')
    await expect(page.getByRole('heading', { name: /activity/i })).toBeVisible()
  })
})
