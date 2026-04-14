/**
 * ルートレイアウト
 *
 * アプリケーション全体の共通レイアウトを定義する。
 * AuthProviderで認証状態を管理し、共通ヘッダーを全ページに表示する。
 *
 * @since 1.0
 */
import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/contexts/AuthContext";
import Header from "@/components/Header";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "isozaki-test Frontend",
  description: "isozaki-test フロントエンドサービス",
};

/**
 * ルートレイアウトコンポーネント
 *
 * 全ページ共通のHTML構造を提供する。
 * AuthProviderで認証状態を管理し、Headerコンポーネントを共通表示する。
 *
 * @param props - 子コンポーネントを含むProps
 * @returns ルートレイアウトのJSX要素
 */
export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="ja"
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="min-h-full flex flex-col">
        <AuthProvider>
          <Header />
          {children}
        </AuthProvider>
      </body>
    </html>
  );
}
