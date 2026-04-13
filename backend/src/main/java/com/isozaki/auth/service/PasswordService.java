/**
 * パスワードサービスクラス
 *
 * <p>パスワードのハッシュ化と検証を担当するサービス。
 * bcryptアルゴリズムを使用してセキュアなハッシュ化を行う。</p>
 *
 * @since 1.0
 */

package com.isozaki.auth.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * bcryptを使用したパスワードのハッシュ化・検証サービス
 *
 * @since 1.0
 */
@ApplicationScoped
public class PasswordService {

    private static final int BCRYPT_COST = 12;

    /**
     * 平文パスワードをbcryptでハッシュ化する
     *
     * @param plainPassword ハッシュ化対象の平文パスワード（null不可）
     * @return bcryptハッシュ文字列
     */
    public String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, plainPassword.toCharArray());
    }

    /**
     * 平文パスワードとハッシュ値を検証する
     *
     * @param plainPassword 検証対象の平文パスワード（null不可）
     * @param hashedPassword 比較対象のbcryptハッシュ値（null不可）
     * @return パスワードが一致する場合はtrue
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
        return result.verified;
    }
}
