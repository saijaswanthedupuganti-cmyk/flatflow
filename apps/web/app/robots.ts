import { MetadataRoute } from 'next'

export default function robots(): MetadataRoute.Robots {
  return {
    rules: [
      {
        userAgent: '*',
        allow: '/',
        // Block authenticated / app routes and internal API from being indexed
        disallow: ['/dashboard/', '/onboarding/', '/join/', '/api/'],
      },
    ],
    sitemap: 'https://habitiq.app/sitemap.xml',
    host: 'https://habitiq.app',
  }
}
