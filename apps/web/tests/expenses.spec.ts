import { test, expect } from '@playwright/test'
import { dismissDueBillsModal, loginAsAdmin, loginAsMember, goTo } from './helpers'

/**
 * Expenses + Bills tests
 * Covers: expense list, add expense, balance view, recurring bills,
 *         bill generation, month-end flow.
 */

test.describe('Expenses page — loads', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsMember(page)
    await goTo(page, 'Expenses')
  })

  test('shows Expenses Hub heading', async ({ page }) => {
    await expect(page.getByRole('heading', { name: 'Expenses Hub' })).toBeVisible()
  })

  test('shows Daily Splits and Monthly Bills tabs', async ({ page }) => {
    await expect(page.getByRole('button', { name: /^daily splits$/i })).toBeVisible()
    await expect(page.getByRole('button', { name: /^monthly bills$/i })).toBeVisible()
  })

  test('shows current cycle balances', async ({ page }) => {
    await expect(page.getByText('Current Cycle Status')).toBeVisible()
    await expect(page.getByText('You Need To Pay')).toBeVisible()
  })

  test('shows expense history', async ({ page }) => {
    await expect(page.getByRole('heading', { name: 'Expense History' })).toBeVisible()
    await expect(page.getByText('Groceries run', { exact: true })).toBeVisible()
  })
})

test.describe('Add expense', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsMember(page)
    await goTo(page, 'Expenses')
  })

  test('opens Add Expense modal', async ({ page }) => {
    await page.getByRole('button', { name: /add expense/i }).click()
    await expect(page.getByRole('heading', { name: 'Add a Split' })).toBeVisible()
  })

  test('modal has required fields', async ({ page }) => {
    await page.getByRole('button', { name: /add expense/i }).click()
    await expect(page.getByPlaceholder(/dinner, groceries, cab/i)).toBeVisible()
    await expect(page.getByPlaceholder(/^0$/)).toBeVisible()
  })

  test('closes modal with its close control', async ({ page }) => {
    await page.getByRole('button', { name: /add expense/i }).click()
    await page.getByRole('button', { name: /close add split/i }).click()
    await expect(page.getByRole('heading', { name: 'Add a Split' })).not.toBeVisible()
  })

  test('adds a new expense end-to-end', async ({ page }) => {
    await page.getByRole('button', { name: /add expense/i }).click()
    const splitModal = page.getByRole('heading', { name: 'Add a Split' }).locator('..').locator('..')
    await splitModal.getByRole('button', { name: /groceries/i, exact: true }).click()
    await page.getByPlaceholder(/^0$/).fill('500')
    await page.getByRole('button', { name: /^add split$/i }).click()
    await expect(page.getByRole('heading', { name: 'Add a Split' })).not.toBeVisible()
    await expect(page.getByText('Groceries', { exact: true })).toBeVisible()
  })
})

test.describe('Monthly Bills tab', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page)
    await goTo(page, 'Expenses')
    await dismissDueBillsModal(page)
    await page.getByRole('button', { name: /monthly bills/i }).click()
  })

  test('shows monthly bill summary', async ({ page }) => {
    await expect(page.getByText('Total Bills', { exact: true })).toBeVisible()
    await expect(page.getByText('Your Share', { exact: true })).toBeVisible()
    await expect(page.getByRole('paragraph').filter({ hasText: /^Next Due$/ })).toBeVisible()
  })

  test('shows resident breakdown entry point', async ({ page }) => {
    await expect(page.getByRole('button', { name: /view resident breakdown/i })).toBeVisible()
  })

  test('shows existing mock bills', async ({ page }) => {
    await expect(page.getByText(/room rent|wifi|electricity/i).first()).toBeVisible()
  })

  test('admin sees Add Bill button', async ({ page }) => {
    await expect(page.getByRole('button', { name: /add monthly bill/i })).toBeVisible()
  })

  test('opens Add Monthly Bill modal', async ({ page }) => {
    await page.getByRole('button', { name: /add monthly bill/i }).click()
    await expect(page.getByRole('heading', { name: /^add monthly bill$/i })).toBeVisible()
  })

  test('Add Monthly Bill modal has amount type options', async ({ page }) => {
    await page.getByRole('button', { name: /add monthly bill/i }).click()
    await expect(page.getByRole('button', { name: /fixed.*same amount every month/i })).toBeVisible()
    await expect(page.getByRole('button', { name: /variable.*confirm each month/i })).toBeVisible()
  })

  test('Add Monthly Bill modal has split and collector controls', async ({ page }) => {
    await page.getByRole('button', { name: /add monthly bill/i }).click()
    await expect(page.getByText('Split Among', { exact: true })).toBeVisible()
    await expect(page.getByText('Collector', { exact: true })).toBeVisible()
  })

  test('does not offer Quick Setup when mock bills already exist', async ({ page }) => {
    await expect(page.getByRole('button', { name: /quick setup/i })).not.toBeVisible()
  })
})

test.describe('Bill generation', () => {
  test('admin can generate a due fixed bill', async ({ page }) => {
    await loginAsAdmin(page)
    await goTo(page, 'Expenses')
    const heading = page.getByRole('heading', { name: /^generate .* bills$/i })
    await expect(heading).toBeVisible()
    await page.getByRole('button', { name: /^generate \(\d+\)$/i }).click()
    await expect(heading).not.toBeVisible()
  })
})

test.describe('Month-end settlement — admin only', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page)
    await goTo(page, 'Expenses')
    await dismissDueBillsModal(page)
  })

  test('shows Close Cycle button', async ({ page }) => {
    await expect(page.getByRole('button', { name: /close cycle/i })).toBeVisible()
  })

  test('opens cycle close modal with 3-step flow', async ({ page }) => {
    await page.getByRole('button', { name: /close cycle/i }).click()
    await expect(page.getByRole('heading', { name: /^close /i })).toBeVisible()
    await expect(page.getByText('Summary', { exact: true })).toBeVisible()
    await expect(page.getByText('Settlements', { exact: true })).toBeVisible()
    await expect(page.getByText('Confirm', { exact: true })).toBeVisible()
  })
})
