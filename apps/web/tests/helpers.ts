import { type Page, expect } from '@playwright/test'

/**
 * Shared helpers used across all Habitiq test suites.
 *
 * MOCK MODE
 * The app runs in mock mode when Firebase keys are absent.
 * The landing page opens the sign-in panel, which exposes the mock accounts.
 * All tests use one of these two entry points.
 */

// ── Login helpers ────────────────────────────────────────────────────────────

/**
 * Log in as the mock admin (Sai, uid: u1).
 * Has full access: create/delete tasks, manage members, generate bills.
 */
export async function loginAsAdmin(page: Page) {
  await page.goto('/', { waitUntil: 'domcontentloaded' })
  await page.waitForLoadState('networkidle')
  await openAuthPanel(page)
  await page.getByRole('button', { name: /mock admin/i }).click()
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 10_000 })
  await expect(page.locator('main')).toBeVisible()
}

/**
 * Log in as the mock member (Rahul, uid: u2).
 * Member access: mark tasks done, log expenses, request swaps.
 */
export async function loginAsMember(page: Page) {
  await page.goto('/', { waitUntil: 'domcontentloaded' })
  await page.waitForLoadState('networkidle')
  await openAuthPanel(page)
  await page.getByRole('button', { name: /mock member/i }).click()
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 10_000 })
  await expect(page.locator('main')).toBeVisible()
}

async function openAuthPanel(page: Page) {
  const desktopSignIn = page.getByRole('button', { name: /^sign in$/i })
  if (await desktopSignIn.isVisible()) {
    await desktopSignIn.click()
    return
  }

  await page.getByRole('button', { name: /open menu/i }).click()
  await page.getByRole('button', { name: /^continue$/i }).click()
}

// ── Navigation helpers ───────────────────────────────────────────────────────

/** Go to a dashboard section by name. Works on both mobile and desktop. */
export async function goTo(page: Page, section: 'Dashboard' | 'Expenses' | 'Swaps' | 'Tasks' | 'Members' | 'Settings' | 'Analytics' | 'Calendar') {
  const href: Record<string, string> = {
    Dashboard: '/dashboard',
    Expenses:  '/dashboard/expenses',
    Swaps:     '/dashboard/swaps',
    Tasks:     '/dashboard/tasks',
    Members:   '/dashboard/members',
    Settings:  '/dashboard/settings',
    Analytics: '/dashboard/analytics',
    Calendar:  '/dashboard/calendar',
  }
  const path = href[section]
  await page.goto(path, { waitUntil: 'domcontentloaded' })
  await expect(page).toHaveURL(new RegExp(`${path.replace('/', '\\/')}(?:[/?#]|$)`))
  await expect(page.locator('main')).toBeVisible()
}

/** Close the due-bills modal that intentionally auto-opens for mock admins. */
export async function dismissDueBillsModal(page: Page) {
  const heading = page.getByRole('heading', { name: /^generate .* bills$/i })
  if (await heading.isVisible()) {
    await page.getByRole('button', { name: /^cancel$/i }).click()
    await expect(heading).not.toBeVisible()
  }
}

// ── Wait helpers ─────────────────────────────────────────────────────────────

/** Wait for a toast / notification to appear */
export async function waitForToast(page: Page, text: string | RegExp) {
  await expect(page.getByText(text)).toBeVisible({ timeout: 5_000 })
}

/** Wait for a modal to open by its title */
export async function waitForModal(page: Page, title: string | RegExp) {
  await expect(page.getByRole('dialog').filter({ hasText: title })).toBeVisible({ timeout: 5_000 })
}
