# syntax=docker/dockerfile:1.4
# 多阶段构建：CI / Compose 均可从仓库根目录直接打包，无需预先 mvn package。
# 构建上下文必须是仓库根目录：
#   docker build -f service-system/Dockerfile -t acoj/service-system:local .

FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

# 1. 先拷贝 POM，利用层缓存加速依赖下载
COPY pom.xml .
COPY service-system/pom.xml service-system/

# 2. 再拷贝源码并按 docker profile 打包（写入 spring.profiles.active=docker）
COPY service-system/src service-system/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl service-system -am package -DskipTests -Pdocker

FROM bellsoft/liberica-openjdk-rocky:21.0.5
WORKDIR /app

RUN mkdir -p /app/logs

COPY --from=builder /build/service-system/target/service-system-1.0.0.jar /app/service-system-1.0.0.jar

EXPOSE 89

ENTRYPOINT ["java", \
    "-Xms1280m", \
    "-Xmx1280m", \
    "-XX:MaxMetaspaceSize=256M", \
    "-XX:MaxDirectMemorySize=160M", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=200", \
    "-XX:ParallelGCThreads=2", \
    "-XX:ConcGCThreads=1", \
    "-XX:+UseContainerSupport", \
    "-XX:+DisableExplicitGC", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-XX:HeapDumpPath=/app/logs/heap.hprof", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dfile.encoding=UTF-8", \
    "-Dio.undertow.no-host-header=true", \
    "-Djava.util.concurrent.ForkJoinPool.common.parallelism=2", \
    "-Dorg.jboss.threads.enqueue-batching=true", \
    "-Dio.netty.leakDetectionLevel=disabled", \
    "-jar", "service-system-1.0.0.jar"]
