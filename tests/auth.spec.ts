import { test, expect } from '@playwright/test'

/**
 * Auth tests
 * Covers: landing page, mock login flows, redirect behaviour.
 * All tests use mock mode — no Firebase account required.
 */

test.describe('Landing page', () => {
  test('renders hero with Habitiq branding', async ({ page }) => {
    await page.goto('/')
    await expect(page.getByRole('heading', { name: /habitiq/i }).first()).toBeVisible()
    await expect(page.getByText(/smart living/i)).toBeVisible()
  })

  test('shows sign-in form', async ({ page }) => {
    await page.goto('/')
    await expect(page.getByPlaceholder(/email/i).first()).toBeVisible()
  })
})

test.describe('Mock login — Admin', () => {
  test('logs in as mock admin and lands on dashboard', async ({ page }) => {
    await page.goto('/')
    await page.getByRole('button', { name: /mock admin/i }).click()
    await expect(page).toHaveURL(/\/dashboard/)
    // Admin sees their name and role
    await expect(page.getByText(/admin/i).first()).toBeVisible()
  })

  test('admin sees Org View toggle on dashboard', async ({ page }) => {
    await page.goto('/')
    await page.getByRole('button', { name: /mock admin/i }).click()
    await expect(page.getByRole('button', { name: /org view/i })).toBeVisible()
  })
})

test.describe('Mock login — Member', () => {
  test('logs in as mock member and lands on dashboard', async ({ page }) => {
    await page.goto('/')
    await page.getByRole('button', { name: /mock member/i }).click()
    await expect(page).toHaveURL(/\/dashboard/)
  })

  test('member does NOT see Org View toggle', async ({ page }) => {
    await page.goto('/')
    await page.getByRole('button', { name: /mock member/i }).click()
    await expect(page.getByRole('button', { name: /org view/i })).not.toBeVisible()
  })
})

test.describe('Auth redirect', () => {
  test('unauthenticated user visiting /dashboard is redirected to /', async ({ page }) => {
    await page.goto('/dashboard')
    // Should land on login/landing, not dashboard
    await expect(page).not.toHaveURL(/\/dashboard$/)
  })
})
