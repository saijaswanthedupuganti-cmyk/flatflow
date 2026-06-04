import { defineConfig, devices } from '@playwright/test'

/**
 * Habitiq — Playwright configuration
 *
 * Tests run against mock mode (no Firebase keys).
 * The app auto-detects missing keys and uses seeded local data.
 * Login via the "Mock Admin" / "Mock Member" buttons.
 */
export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,

  reporter: [
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
    ['list'],
  ],

  use: {
    // Local dev server
    baseURL: 'http://localhost:3000',

    // Capture on failure
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',

    // Reasonable timeouts
    actionTimeout: 10_000,
    navigationTimeout: 15_000,
  },

  projects: [
    // Desktop
    {
      name: 'chromium-desktop',
      use: { ...devices['Desktop Chrome'] },
    },
    // Mobile (tests @mobile tagged tests only)
    {
      name: 'mobile-chrome',
      use: { ...devices['Pixel 5'] },
      grep: /@mobile/,
    },
  ],

  // Start Next.js dev server before tests, stop after
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
  },
})
