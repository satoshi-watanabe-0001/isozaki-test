/**
 * Redisヘルスチェッククラス
 *
 * <p>Redisサーバへの接続状態を確認するヘルスチェック。
 * Readinessプローブとして使用される。</p>
 *
 * @since 1.0
 */

package com.isozaki.auth.health;

import io.quarkus.redis.datasource.RedisDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Redis接続のReadinessヘルスチェック
 *
 * <p>RedisサーバへPINGコマンドを送信し、接続可能であればUPを返す。</p>
 *
 * @since 1.0
 */
@Readiness
@ApplicationScoped
public class RedisHealthCheck implements HealthCheck {

    private static final String HEALTH_CHECK_NAME = "Redis接続";

    private final RedisDataSource redisDataSource;

    /**
     * RedisDataSourceを注入してヘルスチェックを初期化する
     *
     * @param redisDataSource Redisデータソース
     */
    @Inject
    public RedisHealthCheck(RedisDataSource redisDataSource) {
        this.redisDataSource = redisDataSource;
    }

    /**
     * Redisサーバへの接続を確認する
     *
     * <p>PINGコマンドを送信し、正常応答であればUP、
     * 失敗した場合はDOWNを返す。</p>
     *
     * @return ヘルスチェック結果
     */
    @Override
    public HealthCheckResponse call() {
        try {
            var commands = redisDataSource.value(String.class, String.class);
            commands.get("health-check-ping");
            return HealthCheckResponse.up(HEALTH_CHECK_NAME);
        } catch (Exception e) {
            return HealthCheckResponse.named(HEALTH_CHECK_NAME)
                    .down()
                    .withData("エラー", e.getMessage())
                    .build();
        }
    }
}
