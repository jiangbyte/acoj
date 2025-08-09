
## 使用更小的 JRE 基础镜像
FROM eclipse-temurin:21-jre

# 创建应用目录
WORKDIR /app

# 复制 JAR 文件 (确保先执行了构建)
COPY target/app-core-1.0.0.jar app.jar

# 使用 JSON 格式的 CMD 以避免信号处理问题
CMD ["java", \
    "-Xms128m", \
    "-Xmx128m", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]

EXPOSE 29093

# docker build -t registry.cn-beijing.aliyuncs.com/charlie_images/app-core:1.0.0 .
# docker push registry.cn-beijing.aliyuncs.com/charlie_images/app-core:1.0.0

# docker build -t registry.cn-beijing.aliyuncs.com/charlie_images/zero-online:latest .

# docker push registry.cn-beijing.aliyuncs.com/charlie_images/zero-online:latest

# 暴露端口28093
# docker run -d --restart always --name zero-online -p 29084:29084 registry.cn-beijing.aliyuncs.com/charlie_images/zero-online:latest
