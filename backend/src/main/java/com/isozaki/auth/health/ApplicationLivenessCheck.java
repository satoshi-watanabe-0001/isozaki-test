/**
 * アプリケーションLivenessチェッククラス
 *
 * <p>アプリケーションが正常に稼働しているかを確認するLivenessプローブ。</p>
 *
 * @since 1.0
 */

package com.isozaki.auth.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

/**
 * アプリケーションのLivenessヘルスチェック
 *
 * <p>アプリケーションが応答可能であることを確認する。
 * Kubernetesなどのオーケストレーターで使用される。</p>
 *
 * @since 1.0
 */
@Liveness
@ApplicationScoped
public class ApplicationLivenessCheck implements HealthCheck {

    private static final String HEALTH_CHECK_NAME = "アプリケーション稼働状態";

    /**
     * アプリケーションの稼働状態を確認する
     *
     * <p>アプリケーションが正常に動作していればUPを返す。</p>
     *
     * @return ヘルスチェック結果
     */
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up(HEALTH_CHECK_NAME);
    }
}
