/**
 * Playwright設定ファイル
 *
 * Frontend統合テスト用の設定。
 * PC・Android・iPhoneの3デバイスで動作確認を行う。
 *
 * @since 1.0
 */
import { defineConfig, devices } from "@playwright/test";

/** フロントエンドのベースURL */
const BASE_URL: string = process.env.FRONTEND_URL || "http://localhost:3000";

export default defineConfig({
  testDir: "./tests",
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ["list"],
    ["html", { outputFolder: "reports/html", open: "never" }],
    ["junit", { outputFile: "reports/junit-report.xml" }],
  ],
  outputDir: "test-results",
  use: {
    baseURL: BASE_URL,
    trace: "on-first-retry",
    screenshot: "only-on-failure",
  },
  projects: [
    {
      name: "PC（Desktop Chrome）",
      use: { ...devices["Desktop Chrome"] },
    },
    {
      name: "Android（Pixel 7）",
      use: { ...devices["Pixel 7"] },
    },
    {
      name: "iPhone（iPhone 14）",
      use: { ...devices["iPhone 14"] },
    },
  ],
});
