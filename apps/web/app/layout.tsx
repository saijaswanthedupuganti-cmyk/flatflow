import type { Metadata, Viewport } from "next";
import { Inter, Geist_Mono } from "next/font/google";
import "./globals.css";

const inter = Inter({
  variable: "--font-inter",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  metadataBase: new URL("https://habitiq.app"),
  title: "Oddroof — Your flat, sorted",
  description: "Oddroof keeps a shared flat running: fair task rotation, shared expenses and bills, away mode, a voice assistant, and safe flat and flatmate discovery. Android app, free.",
  manifest: "/manifest.json",
  keywords: ["shared flat app", "flatmate app India", "chore rotation app", "split expenses with flatmates", "PG expense manager", "find a flatmate", "find a flat India"],
  authors: [{ name: "Oddroof" }],
  creator: "Oddroof",
  publisher: "Oddroof",
  robots: { index: true, follow: true, googleBot: { index: true, follow: true } },
  icons: {
    icon: [
      { url: "/api/pwa-icon/32", sizes: "32x32", type: "image/png" },
      { url: "/api/pwa-icon/192", sizes: "192x192", type: "image/png" },
    ],
    apple: "/api/pwa-icon/180",
  },
  openGraph: {
    title: "Oddroof — Your flat, sorted",
    description: "Fair task rotation, shared expenses, bills, a voice assistant and safe flatmate discovery for shared flats in India.",
    siteName: "Oddroof",
    type: "website",
    locale: "en_IN",
    images: [{ url: "/opengraph-image", width: 1200, height: 630, alt: "Oddroof — Your flat, sorted" }],
  },
  twitter: {
    card: "summary_large_image",
    title: "Oddroof — Your flat, sorted",
    description: "Fair task rotation, shared expenses, bills, a voice assistant and safe flatmate discovery for shared flats in India.",
    images: ["/opengraph-image"],
  },
  // No site-wide canonical: each public page sets its own alternates.canonical.
};

export const viewport: Viewport = {
  themeColor: "#1C2A40",
};

const SITE_URL = "https://habitiq.app";

const jsonLd = [
  {
    "@context": "https://schema.org",
    "@type": "Organization",
    "@id": `${SITE_URL}/#organization`,
    name: "Oddroof",
    url: SITE_URL,
    logo: `${SITE_URL}/api/pwa-icon/512`,
    email: "hello@habitiq.app",
    contactPoint: { "@type": "ContactPoint", contactType: "customer support", email: "hello@habitiq.app" },
  },
  {
    "@context": "https://schema.org",
    "@type": "MobileApplication",
    name: "Oddroof",
    url: SITE_URL,
    image: `${SITE_URL}/opengraph-image`,
    operatingSystem: "Android",
    applicationCategory: "LifestyleApplication",
    description: "Shared flat app: fair task rotation, shared expenses and bills, away mode, a voice assistant, and flat and flatmate discovery.",
    offers: { "@type": "Offer", price: "0", priceCurrency: "INR" },
    countryOfOrigin: "IN",
    publisher: { "@id": `${SITE_URL}/#organization` },
  },
];

import AuthProvider from "@/components/AuthProvider";
import ServiceWorkerRegistration from "@/components/ServiceWorkerRegistration";
import PWAInstallPrompt from "@/components/PWAInstallPrompt";
import { PWAProvider } from '@/contexts/PWAContext';

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="en"
      className={`${inter.variable} ${geistMono.variable} h-full antialiased`}
      suppressHydrationWarning
    >
      <head>
        {/* Apply saved theme before React hydrates to prevent flash */}
        <script
          dangerouslySetInnerHTML={{
            __html: `
              try {
                const t = localStorage.getItem('habitiq-theme');
                if (t === 'dark') document.documentElement.classList.add('dark');
              } catch(e) {}
            `,
          }}
        />
        <link rel="manifest" href="/manifest.json" />
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
        />
        <meta name="mobile-web-app-capable" content="yes" />
        <meta name="apple-mobile-web-app-capable" content="yes" />
        <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
        <meta name="apple-mobile-web-app-title" content="Oddroof" />
        <link rel="apple-touch-icon" href="/api/pwa-icon/180" />
      </head>
      <body className="min-h-full flex flex-col bg-background text-foreground font-sans">
        <PWAProvider>
          <AuthProvider>{children}</AuthProvider>
          <ServiceWorkerRegistration />
          <PWAInstallPrompt />
        </PWAProvider>
      </body>
    </html>
  );
}
