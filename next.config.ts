import type { NextConfig } from "next";

const securityHeaders = [
  // Prevents clickjacking — blocks this site from being iframed by other origins
  { key: "X-Frame-Options", value: "DENY" },
  // Prevents MIME-type sniffing attacks
  { key: "X-Content-Type-Options", value: "nosniff" },
  // Limits referrer info sent to third-party sites
  { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
  // Enforces HTTPS for 1 year (only active when served over HTTPS)
  { key: "Strict-Transport-Security", value: "max-age=31536000; includeSubDomains" },
  // Restricts access to browser features not needed by this app
  { key: "Permissions-Policy", value: "camera=(), microphone=(), geolocation=(), payment=()" },
  // Prevents XSS via DOM — stops browsers from executing injected scripts
  { key: "X-XSS-Protection", value: "1; mode=block" },
];

const nextConfig: NextConfig = {
  async headers() {
    return [
      {
        source: "/(.*)",
        headers: securityHeaders,
      },
    ];
  },

  // Proxy Firebase auth requests through our own domain.
  // This keeps the entire OAuth flow on one origin, fixing iOS Safari ITP
  // and Android Chrome cross-site storage blocks.
  // Previously done via netlify.toml — moving here so it works on any platform.
  async rewrites() {
    return [
      {
        source: "/__/auth/:path*",
        destination: "https://garbage-f79f7.firebaseapp.com/__/auth/:path*",
      },
      {
        source: "/__/firebase/:path*",
        destination: "https://garbage-f79f7.firebaseapp.com/__/firebase/:path*",
      },
    ];
  },
};

export default nextConfig;
