import { MetadataRoute } from 'next'

export default function robots(): MetadataRoute.Robots {
  return {
    rules: [
      {
        userAgent: '*',
        // Brand icons must be crawlable. App pages use X-Robots-Tag noindex;
        // blocking their crawl would prevent crawlers from seeing that directive.
        allow: ['/', '/api/pwa-icon/'],
        disallow: ['/api/', '/__/'],
      },
    ],
    sitemap: 'https://habitiq.app/sitemap.xml',
    host: 'https://habitiq.app',
  }
}
