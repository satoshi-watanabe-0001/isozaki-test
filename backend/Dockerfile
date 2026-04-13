## Stage 1: ビルドステージ
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle gradle.properties ./
COPY config ./config
COPY src ./src
RUN chmod +x ./gradlew && ./gradlew build -Dquarkus.package.jar.type=uber-jar -x test --no-daemon

## Stage 2: 実行ステージ
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/build/*-runner.jar /app/application.jar
USER appuser
EXPOSE 8080
ENV JAVA_OPTS="-Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/application.jar"]
