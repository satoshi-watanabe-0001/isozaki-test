/**
 * 認証サービスクラス
 *
 * <p>ログイン認証のビジネスロジックを担当するサービス。
 * メールアドレスとパスワードによるユーザ認証を行い、
 * 認証成功時にRedisセッションを作成する。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.service;

import com.isozaki.auth.dto.LoginRequest;
import com.isozaki.auth.dto.LoginResponse;
import com.isozaki.auth.entity.UserEntity;
import com.isozaki.auth.exception.AuthenticationException;
import com.isozaki.auth.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;

/**
 * ログイン認証ロジックを提供するサービス
 *
 * <p>ユーザのメールアドレスとパスワードをDBのデータと突合し、
 * 認証に成功した場合はRedisにセッションを保存する。</p>
 *
 * @since 1.0
 */
@ApplicationScoped
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final SessionService sessionService;

    /**
     * 依存するサービスを注入して認証サービスを初期化する
     *
     * @param userRepository  ユーザリポジトリ
     * @param passwordService パスワードサービス
     * @param sessionService  セッション管理サービス
     */
    @Inject
    public AuthService(UserRepository userRepository,
                       PasswordService passwordService,
                       SessionService sessionService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.sessionService = sessionService;
    }

    /**
     * メールアドレスとパスワードでユーザを認証する
     *
     * <p>DBからメールアドレスでユーザを検索し、パスワードのハッシュ値を検証する。
     * 認証に成功した場合、Redisにセッションを作成してレスポンスを返す。</p>
     *
     * @param request ログインリクエスト（メールアドレスとパスワードを含む）
     * @return ログイン成功時のレスポンス（セッションID、ユーザID、ユーザ名を含む）
     * @throws AuthenticationException メールアドレスが未登録またはパスワードが不一致の場合
     */
    public LoginResponse login(LoginRequest request) {
        Optional<UserEntity> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            throw new AuthenticationException("メールアドレスまたはパスワードが正しくありません");
        }

        UserEntity user = userOptional.get();

        if (!passwordService.verifyPassword(request.getPassword(), user.passwordHash)) {
            throw new AuthenticationException("メールアドレスまたはパスワードが正しくありません");
        }

        String sessionId = sessionService.createSession(user.userId);
        return new LoginResponse(sessionId, user.userId, user.username);
    }
}
