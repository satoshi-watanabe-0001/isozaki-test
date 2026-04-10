/**
 * データベースヘルスチェッククラス
 *
 * <p>PostgreSQLデータベースへの接続状態を確認するヘルスチェック。
 * Readinessプローブとして使用される。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * データベース接続のReadinessヘルスチェック
 *
 * <p>データベースへの接続を確認し、接続可能であればUPを返す。</p>
 *
 * @since 1.0
 */
@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    private static final String HEALTH_CHECK_NAME = "データベース接続";

    private final DataSource dataSource;

    /**
     * DataSourceを注入してヘルスチェックを初期化する
     *
     * @param dataSource データソース
     */
    @Inject
    public DatabaseHealthCheck(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * データベースへの接続を確認する
     *
     * <p>データベースに対してバリデーションクエリを実行し、
     * 接続が有効であればUP、失敗した場合はDOWNを返す。</p>
     *
     * @return ヘルスチェック結果
     */
    @Override
    public HealthCheckResponse call() {
        try (var connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                return HealthCheckResponse.up(HEALTH_CHECK_NAME);
            }
            return HealthCheckResponse.named(HEALTH_CHECK_NAME)
                    .down()
                    .withData("エラー", "データベース接続が無効です")
                    .build();
        } catch (Exception e) {
            return HealthCheckResponse.named(HEALTH_CHECK_NAME)
                    .down()
                    .withData("エラー", e.getMessage())
                    .build();
        }
    }
}
