#FROM openjdk:21
#
## 指定路径
#WORKDIR /app
#
## 复制jar文件到路径
#COPY ./target/galaxy-oj-1.0.0.jar /app/galaxy-oj-1.0.0.jar
#
## 暴露端口（根据您的实际端口修改）
#EXPOSE 89
#
## 启动网关服务
#ENTRYPOINT ["java","-jar","galaxy-oj-1.0.0.jar"]


FROM openjdk:21

WORKDIR /app

RUN mkdir -p /app/logs

COPY ./target/galaxy-oj-1.0.0.jar /app/galaxy-oj-1.0.0.jar

EXPOSE 89

# 最稳定的配置
ENTRYPOINT ["java", \
    "-Xmx12G", \
    "-Xms12G", \
    "-XX:MaxMetaspaceSize=512M", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=150", \
    "-XX:ParallelGCThreads=2", \
    "-XX:ConcGCThreads=1", \
    "-XX:+UseContainerSupport", \
    "-XX:+DisableExplicitGC", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-Xlog:gc*:file=/app/logs/gc.log:time:filecount=3,filesize=20M", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dfile.encoding=UTF-8", \
    "-jar", "galaxy-oj-1.0.0.jar"]