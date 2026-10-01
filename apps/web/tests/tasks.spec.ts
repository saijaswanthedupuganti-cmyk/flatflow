import { test, expect } from '@playwright/test'
import { loginAsAdmin, loginAsMember, goTo } from './helpers'

/**
 * Task management tests
 * Covers: task list, mark done, overdue display, admin controls.
 */

test.describe('Task list — member view', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsMember(page)
  })

  test('shows assigned tasks', async ({ page }) => {
    await expect(page.getByText('Garbage Duty', { exact: true }).first()).toBeVisible()
    await expect(page.getByText('Kitchen Cleaning', { exact: true }).first()).toBeVisible()
  })

  test('assigned tasks show due dates', async ({ page }) => {
    await expect(page.getByText(/^Due:/).first()).toBeVisible()
  })

  test('member does not see Create Task button', async ({ page }) => {
    await goTo(page, 'Tasks')
    await expect(page.getByRole('button', { name: /create task/i })).not.toBeVisible()
  })
})

test.describe('Task list — admin view', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page)
  })

  test('admin sees all tasks in Org View', async ({ page }) => {
    await page.getByRole('button', { name: /org view/i }).click()
    await expect(page.getByRole('heading', { name: 'Garbage Duty', exact: true })).toBeVisible()
    await expect(page.getByRole('heading', { name: /bathroom cleaning/i })).toBeVisible()
  })

  test('admin can access Tasks management page', async ({ page }) => {
    await goTo(page, 'Tasks')
    await expect(page.getByRole('heading', { name: /tasks/i })).toBeVisible()
  })

  test('admin sees New Task button', async ({ page }) => {
    await goTo(page, 'Tasks')
    await expect(page.getByRole('button', { name: /^new task$/i })).toBeVisible()
  })
})

test.describe('Mark task complete', () => {
  test('member can mark their assigned task as done', async ({ page }) => {
    await loginAsMember(page)
    await page.getByRole('button', { name: /^done$/i }).first().click()
    await expect(page.getByText(/when did you do this/i)).toBeVisible()
    await page.getByRole('button', { name: /^confirm$/i }).click()
    await expect(page.getByText(/when did you do this/i)).not.toBeVisible()
  })
})
