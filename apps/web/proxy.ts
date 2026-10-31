import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

const CANONICAL_HOST = 'habitiq.app'

export function proxy(request: NextRequest) {
  const host = request.headers.get('host') ?? ''

  // In local dev there's no canonical host to enforce
  if (!host || host.includes('localhost') || host.includes('127.0.0.1')) {
    return NextResponse.next()
  }

  // The exact preview deployment must be reviewable without becoming an
  // indexable duplicate. Production aliases still redirect to the canonical host.
  if (process.env.VERCEL_ENV === 'preview' && host === process.env.VERCEL_URL) {
    const response = NextResponse.next()
    response.headers.set('X-Robots-Tag', 'noindex, nofollow')
    return response
  }

  // Netlify deploy previews / branch deploys (e.g. deploy-preview-12--flatsflow.netlify.app)
  // stay on their own host so the owner can review a change before it reaches habitiq.app.
  if (/--flatsflow\.netlify\.app$/.test(host)) {
    return NextResponse.next()
  }

  // Redirect every other non-canonical domain (production aliases, old Netlify
  // domain, any alias) to habitiq.app with a permanent 301.
  if (host !== CANONICAL_HOST) {
    const url = new URL(request.url)
    url.host = CANONICAL_HOST
    url.protocol = 'https:'
    return NextResponse.redirect(url.toString(), { status: 301 })
  }

  return NextResponse.next()
}

export const config = {
  // Run on every route except Next.js internals and static assets
  matcher: ['/((?!_next/static|_next/image|favicon\\.ico|api/pwa-icon).*)'],
}
