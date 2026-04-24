# SAMビルド用Makefile
#
# sam build 実行時に呼び出され、Lambda用アーティファクトを生成する。
# package-lambda.json を使用することで、ローカル開発専用の express を除外し、
# Lambda実行に必要な依存（@aws-sdk/client-s3, sharp）のみを含むパッケージを生成する。
#
# @since 1.4

build-ImageProcessorFunction:
	mkdir -p $(ARTIFACTS_DIR)/src
	cp src/imageProcessor.js src/lambdaHandler.js $(ARTIFACTS_DIR)/src/
	cp package-lambda.json $(ARTIFACTS_DIR)/package.json
	cd $(ARTIFACTS_DIR) && npm install --omit=dev
