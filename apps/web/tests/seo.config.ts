import { defineConfig } from '@playwright/test'

// Read-only deployed-site checks; no local Next server or customer login required.
export default defineConfig({
  testDir: '.',
  testMatch: 'seo.spec.ts',
  workers: 1,
  timeout: 60_000,
  reporter: 'list',
  use: {
    baseURL: process.env.SEO_TEST_URL || 'https://habitiq.app',
    contextOptions: { reducedMotion: 'reduce' },
  },
})
