/**
 * UUID生成サービスクラス
 *
 * <p>UUIDv7の生成を担当するサービス。
 * タイムスタンプベースのUUIDv7を使用して時間順序を保証する。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.service;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * UUIDv7を生成するサービス
 *
 * @since 1.0
 */
@ApplicationScoped
public class UuidService {

    private final TimeBasedEpochGenerator generator;

    /**
     * UUIDv7ジェネレータを初期化する
     */
    public UuidService() {
        this.generator = Generators.timeBasedEpochGenerator();
    }

    /**
     * 新しいUUIDv7を文字列形式で生成する
     *
     * @return UUIDv7文字列
     */
    public String generateUuidV7() {
        return generator.generate().toString();
    }
}
