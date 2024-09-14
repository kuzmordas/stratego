import type { Metadata } from "next";
import { Inter } from "next/font/google";

import { Root } from "@/components/Root/Root";

import '@telegram-apps/telegram-ui/dist/styles.css';
// import 'normalize.css/normalize.css';
import "./globals.css";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "Stratego Game"
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        <Root>{children}</Root>
      </body>
    </html>
  );
}
