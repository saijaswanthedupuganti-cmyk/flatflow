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
        // Apply security headers to all routes
        source: "/(.*)",
        headers: securityHeaders,
      },
    ];
  },
};

export default nextConfig;
