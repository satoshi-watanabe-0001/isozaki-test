/**
 * セッション管理サービスクラス
 *
 * <p>Redisを使用したユーザセッションの保存・取得・削除を担当するサービス。
 * セッションIDをキーとしてユーザIDを保存し、ログインユーザの識別に使用する。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.service;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

/**
 * Redisベースのセッション管理サービス
 *
 * <p>ログイン成功時にセッションを作成し、Redisに保存する。
 * セッションの有効期限はデフォルトで1800秒（30分）。</p>
 *
 * @since 1.0
 */
@ApplicationScoped
public class SessionService {

    private static final String SESSION_PREFIX = "session:";
    private static final long SESSION_TTL_SECONDS = 1800L;

    private final ValueCommands<String, String> valueCommands;

    /**
     * RedisDataSourceを注入してセッションサービスを初期化する
     *
     * @param redisDataSource Redisデータソース
     */
    public SessionService(RedisDataSource redisDataSource) {
        this.valueCommands = redisDataSource.value(String.class, String.class);
    }

    /**
     * 新しいセッションを作成してRedisに保存する
     *
     * <p>セッションIDとしてUUIDを生成し、ユーザIDを値としてRedisに保存する。
     * セッションには有効期限（30分）が設定される。</p>
     *
     * @param userId セッションに紐づけるユーザID（null不可）
     * @return 生成されたセッションID
     */
    public String createSession(String userId) {
        String sessionId = UUID.randomUUID().toString();
        String key = SESSION_PREFIX + sessionId;
        valueCommands.setex(key, SESSION_TTL_SECONDS, userId);
        return sessionId;
    }

    /**
     * セッションIDからユーザIDを取得する
     *
     * @param sessionId 検索対象のセッションID（null不可）
     * @return ユーザIDが存在する場合はその値、存在しない場合はnull
     */
    public String getUserIdBySession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        return valueCommands.get(key);
    }

    /**
     * セッションを削除する
     *
     * @param sessionId 削除対象のセッションID（null不可）
     */
    public void invalidateSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        valueCommands.getdel(key);
    }
}
