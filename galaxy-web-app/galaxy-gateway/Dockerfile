FROM openjdk:21

# 指定路径
WORKDIR /app

# 复制jar文件到路径
COPY ./target/galaxy-gateway-1.0.0.jar /app/galaxy-gateway-1.0.0.jar

# 暴露端口（根据您的实际端口修改）
EXPOSE 93

# 启动网关服务
ENTRYPOINT ["java","-jar","galaxy-gateway-1.0.0.jar"]