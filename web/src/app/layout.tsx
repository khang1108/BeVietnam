import type { Metadata, Viewport } from "next";
import "@/styles/globals.css";
import ClientProviders from "./providers";
import Navbar from "@/components/layout/Navbar";
import Footer from "@/components/layout/Footer";

export const metadata: Metadata = {
  title: {
    default: "BeVietnam - Smart Tourism System",
    template: "%s | BeVietnam",
  },
  description:
    "Khám phá Việt Nam thông minh - Discover Vietnam with cultural depth. Smart Tourism System powered by AI.",
  keywords: [
    "Vietnam",
    "tourism",
    "travel",
    "du lịch",
    "Việt Nam",
    "BeVietnam",
    "smart tourism",
  ],
  authors: [{ name: "BeVietnam Team - HCMUS" }],
  openGraph: {
    type: "website",
    locale: "vi_VN",
    alternateLocale: "en_US",
    siteName: "BeVietnam",
    title: "BeVietnam - Smart Tourism System",
    description: "Khám phá Việt Nam thông minh với chiều sâu văn hóa",
  },
  manifest: "/manifest.json",
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  maximumScale: 5,
  themeColor: [
    { media: "(prefers-color-scheme: light)", color: "#ffffff" },
    { media: "(prefers-color-scheme: dark)", color: "#0d1117" },
  ],
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi" data-theme="light" suppressHydrationWarning>
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link
          rel="preconnect"
          href="https://fonts.gstatic.com"
          crossOrigin="anonymous"
        />
      </head>
      <body>
        <ClientProviders>
          <Navbar />
          <main className="page-wrapper">{children}</main>
          <Footer />
        </ClientProviders>
      </body>
    </html>
  );
}
