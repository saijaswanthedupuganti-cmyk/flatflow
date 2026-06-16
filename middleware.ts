import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

const CANONICAL_HOST = 'habitiq.app'

export function middleware(request: NextRequest) {
  const host = request.headers.get('host') ?? ''

  // In local dev there's no canonical host to enforce
  if (!host || host.includes('localhost') || host.includes('127.0.0.1')) {
    return NextResponse.next()
  }

  // Redirect every non-canonical domain (Vercel preview URLs, old Netlify
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
