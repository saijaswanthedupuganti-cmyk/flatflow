import { MetadataRoute } from 'next'

export default function robots(): MetadataRoute.Robots {
  return {
    rules: {
      userAgent: '*',
      allow: '/',
      disallow: ['/dashboard/', '/onboarding/', '/join/', '/api/'],
    },
    sitemap: 'https://habitiq.app/sitemap.xml',
  }
}
