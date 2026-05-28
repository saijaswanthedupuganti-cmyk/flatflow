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
  icons: {
    icon: [
      { url: "/habitiq-logo.svg", type: "image/svg+xml" },
    ],
    apple: "/habitiq-logo.svg",
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
      </head>
      <body className="min-h-full flex flex-col bg-background text-foreground font-sans">
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
