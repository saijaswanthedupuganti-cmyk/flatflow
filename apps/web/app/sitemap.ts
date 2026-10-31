import { MetadataRoute } from 'next'

const BASE = 'https://habitiq.app'

const PAGES: { path: string; changeFrequency: 'weekly' | 'monthly'; priority: number }[] = [
  { path: '/', changeFrequency: 'weekly', priority: 1 },
  { path: '/discover', changeFrequency: 'weekly', priority: 0.8 },
  { path: '/flat-manager', changeFrequency: 'weekly', priority: 0.8 },
  { path: '/voice', changeFrequency: 'monthly', priority: 0.7 },
  { path: '/privacy-and-safety', changeFrequency: 'monthly', priority: 0.6 },
  { path: '/about', changeFrequency: 'monthly', priority: 0.5 },
  { path: '/privacy', changeFrequency: 'monthly', priority: 0.3 },
  { path: '/terms', changeFrequency: 'monthly', priority: 0.3 },
  { path: '/safety', changeFrequency: 'monthly', priority: 0.3 },
  { path: '/delete-account', changeFrequency: 'monthly', priority: 0.3 },
]

export default function sitemap(): MetadataRoute.Sitemap {
  // Omit lastModified until actual per-page content dates are maintained.
  // A rebuild does not mean every public page changed.
  return PAGES.map(({ path, changeFrequency, priority }) => ({
    url: path === '/' ? BASE : `${BASE}${path}`,
    changeFrequency,
    priority,
  }))
}
