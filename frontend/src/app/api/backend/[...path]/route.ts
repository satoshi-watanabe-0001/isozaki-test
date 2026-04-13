import { NextRequest, NextResponse } from "next/server";

/**
 * 動的レンダリングを強制（プロキシはキャッシュ不要）
 */
export const dynamic = "force-dynamic";

/**
 * バックエンドAPIプロキシ用のURLを構築する
 *
 * <p>パスセグメント配列とクエリパラメータから
 * バックエンドサービスの完全なURLを生成する。</p>
 *
 * @param pathSegments - プロキシ対象のパスセグメント配列
 * @param searchParams - 転送するクエリパラメータ
 * @returns バックエンドAPIの完全なURL文字列
 */
function buildBackendUrl(
  pathSegments: string[],
  searchParams: URLSearchParams
): string {
  const baseUrl =
    process.env.BACKEND_INTERNAL_URL || "http://localhost:8080";
  const path = pathSegments.join("/");
  const query = searchParams.toString();
  return query ? `${baseUrl}/${path}?${query}` : `${baseUrl}/${path}`;
}

/**
 * バックエンドAPIへリクエストをプロキシする
 *
 * <p>Next.jsのRoute Handlerとしてリクエストを受け取り、
 * バックエンドサービスへ転送してレスポンスを返却する。
 * rewritesではなくRoute Handlerを使用することで、
 * standalone出力モードでも実行時に環境変数が正しく解決される。</p>
 *
 * @param request - クライアントからのリクエスト
 * @param pathSegments - プロキシ対象のパスセグメント配列
 * @returns バックエンドからのレスポンス、または接続エラー時は502レスポンス
 */
async function proxyRequest(
  request: NextRequest,
  pathSegments: string[]
): Promise<NextResponse> {
  const url = buildBackendUrl(pathSegments, request.nextUrl.searchParams);

  const headers = new Headers(request.headers);
  headers.delete("host");

  const init: RequestInit = {
    method: request.method,
    headers,
    cache: "no-store",
  };

  if (request.method !== "GET" && request.method !== "HEAD") {
    init.body = await request.arrayBuffer();
  }

  try {
    const response = await fetch(url, init);
    const responseHeaders = new Headers(response.headers);
    responseHeaders.delete("transfer-encoding");

    return new NextResponse(response.body, {
      status: response.status,
      statusText: response.statusText,
      headers: responseHeaders,
    });
  } catch (error) {
    console.error("Backend proxy error:", error);
    return NextResponse.json(
      {
        data: null,
        message: "バックエンドサービスへの接続に失敗しました",
        status: "error",
      },
      { status: 502 }
    );
  }
}

/**
 * GETリクエストのプロキシハンドラー
 */
export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
): Promise<NextResponse> {
  const { path } = await params;
  return proxyRequest(request, path);
}

/**
 * POSTリクエストのプロキシハンドラー
 */
export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
): Promise<NextResponse> {
  const { path } = await params;
  return proxyRequest(request, path);
}

/**
 * PUTリクエストのプロキシハンドラー
 */
export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
): Promise<NextResponse> {
  const { path } = await params;
  return proxyRequest(request, path);
}

/**
 * DELETEリクエストのプロキシハンドラー
 */
export async function DELETE(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
): Promise<NextResponse> {
  const { path } = await params;
  return proxyRequest(request, path);
}

/**
 * PATCHリクエストのプロキシハンドラー
 */
export async function PATCH(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
): Promise<NextResponse> {
  const { path } = await params;
  return proxyRequest(request, path);
}
