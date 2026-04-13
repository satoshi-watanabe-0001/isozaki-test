"use client";

import { useState, useCallback } from "react";

/**
 * ヘルスチェックレスポンスの型定義
 */
interface HealthCheckResponse {
  status: string;
  checks: HealthCheck[];
}

interface HealthCheck {
  name: string;
  status: string;
  data?: Record<string, string>;
}

/**
 * ヘルスチェック結果の表示状態
 */
interface HealthState {
  loading: boolean;
  error: string | null;
  data: HealthCheckResponse | null;
  fetchedAt: string | null;
}

const BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";
const HEALTH_ENDPOINT = `${BACKEND_URL}/q/health`;

/**
 * テストページ
 *
 * backendサービスのヘルスチェック状況を表示する。
 * /q/health エンドポイントを呼び出し、各チェックの状態を一覧表示する。
 */
export default function TestPage() {
  const [health, setHealth] = useState<HealthState>({
    loading: false,
    error: null,
    data: null,
    fetchedAt: null,
  });

  const fetchHealth = useCallback(async () => {
    setHealth((prev) => ({ ...prev, loading: true, error: null }));
    try {
      const res = await fetch(HEALTH_ENDPOINT, {
        cache: "no-store",
      });
      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      }
      const data: HealthCheckResponse = await res.json();
      setHealth({
        loading: false,
        error: null,
        data,
        fetchedAt: new Date().toLocaleString("ja-JP"),
      });
    } catch (err) {
      setHealth({
        loading: false,
        error: err instanceof Error ? err.message : "不明なエラーが発生しました",
        data: null,
        fetchedAt: new Date().toLocaleString("ja-JP"),
      });
    }
  }, []);

  return (
    <main className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">
          テストページ
        </h1>
        <p className="text-gray-600 mb-8">
          Backendサービスのヘルスチェック状況を確認できます。
        </p>

        <div className="bg-white shadow rounded-lg p-6">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-semibold text-gray-800">
              ヘルスチェック
            </h2>
            <button
              onClick={fetchHealth}
              disabled={health.loading}
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {health.loading ? "取得中..." : "ヘルスチェック実行"}
            </button>
          </div>

          <p className="text-sm text-gray-500 mb-4">
            対象: <code className="bg-gray-100 px-1 py-0.5 rounded text-sm">{HEALTH_ENDPOINT}</code>
          </p>

          {health.fetchedAt && (
            <p className="text-sm text-gray-400 mb-4">
              最終取得: {health.fetchedAt}
            </p>
          )}

          {health.error && (
            <div className="rounded-md bg-red-50 p-4 mb-4">
              <div className="flex">
                <div className="flex-shrink-0">
                  <span className="text-red-400 text-xl">✗</span>
                </div>
                <div className="ml-3">
                  <h3 className="text-sm font-medium text-red-800">
                    接続エラー
                  </h3>
                  <p className="mt-1 text-sm text-red-700">{health.error}</p>
                </div>
              </div>
            </div>
          )}

          {health.data && (
            <div>
              <div
                className={`rounded-md p-4 mb-6 ${
                  health.data.status === "UP"
                    ? "bg-green-50"
                    : "bg-red-50"
                }`}
              >
                <div className="flex items-center">
                  <span
                    className={`text-xl mr-2 ${
                      health.data.status === "UP"
                        ? "text-green-500"
                        : "text-red-500"
                    }`}
                  >
                    {health.data.status === "UP" ? "●" : "●"}
                  </span>
                  <span
                    className={`text-lg font-semibold ${
                      health.data.status === "UP"
                        ? "text-green-800"
                        : "text-red-800"
                    }`}
                  >
                    全体ステータス: {health.data.status}
                  </span>
                </div>
              </div>

              <div className="space-y-3">
                {health.data.checks.map((check, index) => (
                  <div
                    key={index}
                    className="border border-gray-200 rounded-lg p-4"
                  >
                    <div className="flex items-center justify-between">
                      <span className="font-medium text-gray-900">
                        {check.name}
                      </span>
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          check.status === "UP"
                            ? "bg-green-100 text-green-800"
                            : "bg-red-100 text-red-800"
                        }`}
                      >
                        {check.status}
                      </span>
                    </div>
                    {check.data && Object.keys(check.data).length > 0 && (
                      <div className="mt-2 text-sm text-gray-500">
                        {Object.entries(check.data).map(([key, value]) => (
                          <div key={key}>
                            <span className="font-medium">{key}:</span> {value}
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {!health.data && !health.error && !health.loading && (
            <div className="text-center py-8 text-gray-400">
              <p>「ヘルスチェック実行」ボタンを押してください</p>
            </div>
          )}
        </div>
      </div>
    </main>
  );
}
