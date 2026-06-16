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
  title: "Habitiq — Shared Flat Management App | Chore Rotation & Bill Splitting",
  description: "The smarter way to manage a shared flat. Automatic chore rotation, fair bill splitting, swap requests, and reliability scores — for flatmates, roommates, and housemates worldwide. No app download needed. Free.",
  manifest: "/manifest.json",
  keywords: [
    // Core — global
    "shared flat management app",
    "flatmate management app",
    "roommate app",
    "house share app",
    "shared house app",
    "co-living app",
    "shared accommodation app",
    "flatmate app",
    // Chore / duty rotation
    "chore rotation app",
    "duty rotation app",
    "roommate chore app",
    "flatmate chores app",
    "chore splitting app",
    "household task manager",
    // Bill splitting
    "bill splitting app",
    "flatmate expense tracker",
    "roommate expense tracker",
    "shared expense app",
    "splitwise alternative",
    "splitwise alternative India",
    "house share bill splitter",
    // Region — India
    "shared flat management app India",
    "PG expense manager",
    "flatmate app India",
    "chore rotation app India",
    "shared expense app India",
    // Region — UK / Australia / global
    "share house app Australia",
    "flatmate bill splitter UK",
    "roommate management app UK",
    "house share management app",
    "student accommodation app",
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
    title: "Habitiq — Shared Flat Management App | Chore Rotation & Bill Splitting",
    description: "The smarter way to manage a shared flat. Automatic chore rotation, fair bill splitting, swap requests, and reliability scores — for flatmates and roommates worldwide. Free.",
    siteName: "Habitiq",
    type: "website",
    url: "https://habitiq.app",
    images: [{ url: "https://habitiq.app/api/pwa-icon/512", width: 512, height: 512, alt: "Habitiq App Icon" }],
  },
  twitter: {
    card: "summary_large_image",
    title: "Habitiq — Shared Flat Management App | Chore Rotation & Bill Splitting",
    description: "The smarter way to manage a shared flat. Automatic chore rotation, fair bill splitting, swap requests, and reliability scores — for flatmates and roommates worldwide. Free.",
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
              "description": "The smarter way to manage a shared flat. Automatic chore rotation, fair bill splitting, swap requests, and reliability scores — for flatmates, roommates, and housemates worldwide.",
              "applicationCategory": "LifestyleApplication",
              "operatingSystem": "Web, Android, iOS",
              "offers": { "@type": "Offer", "price": "0", "priceCurrency": "USD" },
              "featureList": [
                "Automatic chore rotation",
                "Duty rotation management",
                "Flatmate expense splitting",
                "Bill tracking and splitting",
                "Swap request system",
                "Reliability scoring",
                "Real-time sync",
                "Settlement tracking",
                "Monthly bill management",
                "Out-of-station task auto-skip"
              ],
              "audience": {
                "@type": "Audience",
                "audienceType": "Flatmates, roommates, housemates, PG residents, co-living spaces, students, house shares"
              },
              "inLanguage": "en",
              "countryOfOrigin": "IN",
            })
          }}
        />
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{
            __html: JSON.stringify({
              "@context": "https://schema.org",
              "@type": "FAQPage",
              "mainEntity": [
                { "@type": "Question", "name": "Is Habitiq free?", "acceptedAnswer": { "@type": "Answer", "text": "Yes, Habitiq is completely free. No credit card required. All features — duty rotation, bill splitting, expense tracking, and settlements — are available at no cost during our trial phase." } },
                { "@type": "Question", "name": "Is Habitiq a Splitwise alternative for India?", "acceptedAnswer": { "@type": "Answer", "text": "Yes. While Splitwise only handles money and expense splitting, Habitiq is a complete shared living management platform. It combines expense splitting with automated duty rotation, monthly fixed bill tracking, and settlement management — purpose-built for Indian flats, PGs, and co-living spaces." } },
                { "@type": "Question", "name": "How do I split bills and expenses with my flatmates?", "acceptedAnswer": { "@type": "Answer", "text": "Add an expense, choose who paid, and select how to split it — equally, by percentage, or custom amounts per person. Habitiq automatically calculates who owes what and tracks all balances in real time. When someone settles, it's recorded instantly and the balance updates." } },
                { "@type": "Question", "name": "How is Habitiq different from Splitwise?", "acceptedAnswer": { "@type": "Answer", "text": "Splitwise only tracks money. Habitiq tracks money AND manages your flat operations. On top of expense splitting and settlements, Habitiq gives you automated daily task rotation, monthly fixed bill management with per-person share calculation, a swap request system, and a full activity log — all in one app." } },
                { "@type": "Question", "name": "What is duty rotation and how does it work?", "acceptedAnswer": { "@type": "Answer", "text": "Duty rotation automatically assigns daily household tasks — cooking, cleaning, trash, maid supervision — to flatmates on a fair rotating schedule. The system rotates automatically each cycle, tracks who completed what, and allows swap requests if someone needs to trade a day." } },
                { "@type": "Question", "name": "Does Habitiq work for PG accommodations and co-living spaces?", "acceptedAnswer": { "@type": "Answer", "text": "Yes. Habitiq works for any shared living setup — flats, PGs, hostels, student accommodations, and co-living spaces. Up to 8 residents can share a single flat. Each person gets their own view of duties, bills, and balances." } },
                { "@type": "Question", "name": "Where is Habitiq available?", "acceptedAnswer": { "@type": "Answer", "text": "Habitiq is available worldwide. It is used in India (Bengaluru, Hyderabad, Pune, Mumbai, Delhi, Chennai), the UK (London, Manchester), Australia (Sydney, Melbourne, Brisbane), North America (New York, Toronto, Vancouver), Southeast Asia (Singapore, Kuala Lumpur), and beyond. Any shared flat, house share, PG, or co-living setup can use Habitiq." } },
                { "@type": "Question", "name": "Does Habitiq work for shared houses and roommates outside India?", "acceptedAnswer": { "@type": "Answer", "text": "Yes. Habitiq works for any shared living arrangement worldwide — flatmates in the UK, roommates in the USA, housemates in Australia, or co-living residents anywhere. The chore rotation, bill splitting, and swap request features work the same way regardless of location or currency context." } },
                { "@type": "Question", "name": "Do I need to download an app to use Habitiq?", "acceptedAnswer": { "@type": "Answer", "text": "No. Habitiq works directly in your phone's browser. You can also add it to your home screen for a native app experience (it's a Progressive Web App). No App Store or Play Store download needed — your flatmates can join instantly from any device." } },
                { "@type": "Question", "name": "How do I manage daily task management for my flat?", "acceptedAnswer": { "@type": "Answer", "text": "Habitiq's task management system lets you define any household task, assign it to a rotation queue, and let the system handle the rest. Tasks rotate automatically, completions are logged with timestamps, and admins can edit dates retroactively." } },
                { "@type": "Question", "name": "Can Habitiq track monthly bills like rent, electricity, and WiFi?", "acceptedAnswer": { "@type": "Answer", "text": "Yes. Habitiq has a dedicated Fixed Bills system. Add recurring monthly bills — rent, electricity, WiFi, gas, maid salary — and set how they split among flatmates. The app tracks payment status, shows each person's share, generates settlement splits automatically, and keeps a payment history." } }
              ]
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
