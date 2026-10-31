import type { Metadata } from 'next'

/** Keep each public page's search and sharing identity tied to its own URL. */
export function publicPageMetadata(input: {
  title: string | { absolute: string }
  description: string
  alternates: { canonical: string }
}): Metadata {
  const title = typeof input.title === 'string' ? `${input.title} · Habitiq` : input.title.absolute
  const url = new URL(input.alternates.canonical, 'https://habitiq.app').toString()
  return {
    ...input,
    openGraph: {
      title, description: input.description, url,
      siteName: 'Habitiq', type: 'website', locale: 'en_IN',
      images: [{ url: 'https://habitiq.app/opengraph-image', width: 1200, height: 630, alt: 'Habitiq — Your flat, sorted' }],
    },
    twitter: {
      card: 'summary_large_image', title, description: input.description,
      images: ['https://habitiq.app/opengraph-image'],
    },
  }
}
