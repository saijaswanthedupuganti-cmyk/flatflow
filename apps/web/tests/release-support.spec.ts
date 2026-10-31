import { test, expect } from '@playwright/test'

for (const width of [320, 390, 1280]) {
  test(`public deletion and privacy remain accessible at ${width}px @mobile`, async ({ page }) => {
    await page.setViewportSize({ width, height: 900 })
    await page.goto('/delete-account')
    await expect(page.getByRole('heading', { name: 'Delete your Habitiq account', exact: true })).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Request deletion here' })).toBeVisible()
    await expect(page.getByText('These records are not presented as fully anonymised.', { exact: false })).toBeVisible()
    await expect(page.locator('a[href^="mailto:hello@habitiq.app"]').first()).toBeVisible()
    const login = page.getByRole('button', { name: 'Sign in with email', exact: true })
    if (await login.count()) {
      await expect(page.getByLabel('Email address', { exact: true })).toBeVisible()
      await expect(page.getByLabel('Password', { exact: true })).toHaveAttribute('type', 'password')
      await page.route('**/identitytoolkit.googleapis.com/**', route => route.fulfill({ status: 400, contentType: 'application/json', body: JSON.stringify({ error: { message: 'INVALID_LOGIN_CREDENTIALS' } }) }))
      await page.getByLabel('Email address', { exact: true }).fill('invalid@example.com')
      await page.getByLabel('Password', { exact: true }).fill('invalid-test-password')
      await login.click()
      await expect(page.locator('#online-request').getByRole('alert')).toContainText('Sign-in did not finish')
      await expect(login).toBeEnabled()
    } else {
      await expect(page.getByText('Online account deletion is unavailable in this preview.', { exact: false })).toBeVisible()
    }
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
    await page.screenshot({ path: `test-results/deletion-${width}.png`, fullPage: true })
    await page.goto('/privacy')
    await expect(page.getByRole('heading', { level: 1 })).toContainText(/privacy/i)
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
  })
}
