# 需先 mvn package，再构建运行时镜像：
#   docker build -f app/admin/Dockerfile -t registry.cn-beijing.aliyuncs.com/czbyte/hei-boot:latest .
# Compose 使用 registry.cn-beijing.aliyuncs.com/czbyte/hei-boot:${HEI_BOOT_VERSION:-latest}，不包含 build。
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/eclipse-temurin:21-jre-jammy
LABEL maintainer="Charlie"

ENV TZ=Asia/Shanghai \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    SERVER_PORT=8000 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --create-home --uid 10001 hei \
    && mkdir -p /app/logs /app/storage \
    && chown -R hei:hei /app

COPY app/admin/target/admin.jar /app/app.jar
USER hei

EXPOSE ${SERVER_PORT}

ENTRYPOINT ["sh", "-c", "exec java -Djava.security.egd=file:/dev/./urandom -Dserver.port=${SERVER_PORT} ${JAVA_OPTS} -jar /app/app.jar"]
