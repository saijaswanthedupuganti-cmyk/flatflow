import type { Metadata } from "next";
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
  title: "Habitiq — Flatmate Expense Tracker & Shared Living App",
  description: "Split bills, track shared expenses, and manage flatmate duties automatically. The free Splitwise alternative built for Indian flats, PGs, and co-living spaces. No arguments. No forgotten tasks.",
  manifest: "/manifest.json",
  keywords: [
    "flatmate expense tracker",
    "shared living app",
    "bill splitting app",
    "roommate expense tracker",
    "PG expense manager",
    "splitwise alternative India",
    "shared flat management",
    "duty rotation app",
    "co-living app",
    "house share app",
    "flatmate bill splitter",
    "shared apartment app",
    "student accommodation app",
    "flat chores app",
    "expense split app India",
  ],
  authors: [{ name: "Habitiq" }],
  creator: "Habitiq",
  publisher: "Habitiq",
  robots: {
    index: true,
    follow: true,
    googleBot: { index: true, follow: true },
  },
  icons: {
    icon: [
      { url: "/api/pwa-icon/32", sizes: "32x32", type: "image/png" },
      { url: "/api/pwa-icon/192", sizes: "192x192", type: "image/png" },
    ],
    apple: "/api/pwa-icon/180",
  },
  openGraph: {
    title: "Habitiq — Flatmate Expense Tracker & Shared Living App",
    description: "Split bills, track shared expenses, and manage flatmate duties automatically. Free Splitwise alternative for Indian flats, PGs, and co-living spaces.",
    siteName: "Habitiq",
    type: "website",
    url: "https://habitiq.app",
    images: [{ url: "https://habitiq.app/api/pwa-icon/512", width: 512, height: 512, alt: "Habitiq App Icon" }],
  },
  twitter: {
    card: "summary_large_image",
    title: "Habitiq — Flatmate Expense Tracker & Shared Living App",
    description: "Split bills, track shared expenses, and manage flatmate duties automatically. Free Splitwise alternative for Indian flats, PGs, and co-living spaces.",
    images: ["https://habitiq.app/api/pwa-icon/512"],
  },
  alternates: {
    canonical: "https://habitiq.app",
  },
};

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
          dangerouslySetInnerHTML={{
            __html: JSON.stringify({
              "@context": "https://schema.org",
              "@type": "WebApplication",
              "name": "Habitiq",
              "url": "https://habitiq.app",
              "description": "Split bills, track shared expenses, and manage flatmate duties automatically. Free Splitwise alternative for Indian flats, PGs, and co-living spaces.",
              "applicationCategory": "FinanceApplication",
              "operatingSystem": "Web, Android, iOS",
              "offers": { "@type": "Offer", "price": "0", "priceCurrency": "INR" },
              "featureList": [
                "Flatmate expense splitting",
                "Bill tracking and splitting",
                "Duty rotation management",
                "Real-time sync",
                "Settlement tracking",
                "Monthly bill management"
              ],
              "audience": {
                "@type": "Audience",
                "audienceType": "Flatmates, PG residents, co-living spaces, students, roommates"
              },
              "inLanguage": "en",
              "countryOfOrigin": "IN",
            })
          }}
        />
        <meta name="theme-color" content="#4f46e5" />
        <meta name="mobile-web-app-capable" content="yes" />
        <meta name="apple-mobile-web-app-capable" content="yes" />
        <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
        <meta name="apple-mobile-web-app-title" content="Habitiq" />
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
