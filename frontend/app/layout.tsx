import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Create Next App",
  description: "Generated by create next app",
};

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
      <body className="font-d2coding">
      {/* Header */}
      <header className="sticky top-0 z-50 bg-white shadow-md">
        <div className="container mx-auto px-4 py-4 flex justify-between items-center">
          {/* 로고 */}
          <h1 className="text-2xl font-bold text-green-600">Toolgether</h1>

          {/* 네비게이션 */}
          <nav className="space-x-4">
            <a href="#" className="text-gray-700 hover:text-green-600">
              둘러보기
            </a>
            <a href="#" className="text-gray-700 hover:text-green-600">
              로그인
            </a>
            <a href="#" className="text-gray-700 hover:text-green-600">
              회원가입
            </a>
          </nav>
        </div>
      </header>
        {children}
      {/* Footer */}
      <footer className="bg-gray-800 text-white py-6">
        <div className="container mx-auto px-4 flex flex-col md:flex-row justify-between items-center">
          {/* 푸터 텍스트 */}
          <p className="text-sm">&copy; 2025 Toolgether. 모든 권리 보유.</p>

          {/* 소셜 미디어 링크 */}
          <div className="space-x-4 mt-4 md:mt-0">
            <a
                href="#"
                className="text-gray-400 hover:text-white transition-colors"
                aria-label="Facebook"
            >
              Facebook
            </a>
            <a
                href="#"
                className="text-gray-400 hover:text-white transition-colors"
                aria-label="Twitter"
            >
              Twitter
            </a>
            <a
                href="#"
                className="text-gray-400 hover:text-white transition-colors"
                aria-label="Instagram"
            >
              Instagram
            </a>
          </div>
        </div>
      </footer>
      </body>
    </html>
  );
}
