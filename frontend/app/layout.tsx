import type { Metadata } from "next";
import "./globals.css";
import { AuthProvider } from '@/app/lib/auth-context';
import Header from '@/components/Header';
import Script from 'next/script';
import {FacebookIcon, InstagramIcon, TwitterIcon} from "lucide-react";
import AnimatedMain from '@/components/AnimatedMain';

export const metadata: Metadata = {
  title: "Toolgether",
  description: "소유에서 공유로, 지속 가능한 라이프 스타일",
};
export const dynamic = 'force-dynamic';

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
    <head>
        <link
            href="http://cdn.jsdelivr.net/gh/joungkyun/font-d2coding/d2coding.css"
            rel="stylesheet"
            type="text/css"
        />
    </head>
      <body className="font-d2coding" suppressHydrationWarning={true}>
      <AuthProvider>

        <Header />
          <AnimatedMain>{children}</AnimatedMain>
        <Script
            src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"
            strategy="beforeInteractive"
        />
      </AuthProvider>

      {/* Footer */}
      <footer className="bg-gradient-to-r from-green-700 to-green-900 text-white py-8">
        <div className="container mx-auto px-4 flex flex-col md:flex-row justify-between items-center">
          {/* 푸터 텍스트 */}
          <p className="text-sm mb-4 md:mb-0">&copy; 2025 Toolgether. All rights reserved.</p>

          {/* 소셜 미디어 링크 */}
          <div className="flex space-x-6">
            <a
                href="#"
                className="text-green-200 hover:text-white transition-colors"
                aria-label="Facebook"
            >
              <FacebookIcon className="w-6 h-6" />
            </a>
            <a
                href="#"
                className="text-green-200 hover:text-white transition-colors"
                aria-label="Twitter"
            >
              <TwitterIcon className="w-6 h-6" />
            </a>
            <a
                href="#"
                className="text-green-200 hover:text-white transition-colors"
                aria-label="Instagram"
            >
              <InstagramIcon className="w-6 h-6" />
            </a>
          </div>
        </div>
      </footer>
      </body>
    </html>
  );
}
