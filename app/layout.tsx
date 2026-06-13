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
  title: "Habitiq | Smart Living Management",
  description: "The smart shared living management platform — automated duty rotation, real-time sync, and fair accountability for flats, PGs, and co-living spaces.",
  manifest: "/manifest.json",
  icons: {
    icon: [
      { url: "/api/pwa-icon/32", sizes: "32x32", type: "image/png" },
      { url: "/api/pwa-icon/192", sizes: "192x192", type: "image/png" },
    ],
    apple: "/api/pwa-icon/180",
  },
  openGraph: {
    title: "Habitiq — Smart Living Management",
    description: "Automated duty rotation for flats, PGs, and co-living spaces. No arguments. No forgotten tasks. Just fair, transparent shared living.",
    siteName: "Habitiq",
    type: "website",
    url: "https://garbage-liart.vercel.app",
  },
  twitter: {
    card: "summary_large_image",
    title: "Habitiq — Smart Living Management",
    description: "Automated duty rotation for flats, PGs, and co-living spaces. No arguments. No forgotten tasks. Just fair, transparent shared living.",
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
