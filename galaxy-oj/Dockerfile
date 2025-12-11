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


#FROM openjdk:21
#FROM docker.io/openjdk:21
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/openjdk:21
#FROM  docker.1ms.run/astita/openjdk21-alpine:latest

WORKDIR /app

RUN mkdir -p /app/logs

COPY ./target/galaxy-oj-1.0.0.jar /app/galaxy-oj-1.0.0.jar

EXPOSE 89

## 最稳定的配置
#ENTRYPOINT ["java", \
#    "-Xmx10G", \
#    "-Xms10G", \
#    "-XX:MaxMetaspaceSize=512M", \
#    "-XX:+UseG1GC", \
#    "-XX:MaxGCPauseMillis=150", \
#    "-XX:ParallelGCThreads=2", \
#    "-XX:ConcGCThreads=2", \
#    "-XX:+UseContainerSupport", \
#    "-XX:+DisableExplicitGC", \
#    "-XX:+HeapDumpOnOutOfMemoryError", \
#    "-Xlog:gc*:file=/app/logs/gc.log:time:filecount=3,filesize=20M", \
#    "-Djava.security.egd=file:/dev/./urandom", \
#    "-Dfile.encoding=UTF-8", \
#    "-jar", "galaxy-oj-1.0.0.jar"]




#ENTRYPOINT ["java", \
#    "-Xmx6G", \
#    "-Xms6G", \
#    "-XX:MaxMetaspaceSize=512M", \
#    "-XX:+UseG1GC", \
#    "-XX:MaxGCPauseMillis=100", \
#    "-XX:ParallelGCThreads=4", \
#    "-XX:ConcGCThreads=2", \
#    "-XX:+UseContainerSupport", \
#    "-XX:+DisableExplicitGC", \
#    "-XX:+HeapDumpOnOutOfMemoryError", \
#    "-Xlog:gc*:file=/app/logs/gc.log:time:filecount=3,filesize=20M", \
#    "-Djava.security.egd=file:/dev/./urandom", \
#    "-Dfile.encoding=UTF-8", \
#    "-Dserver.tomcat.max-threads=200", \
#    "-Dserver.tomcat.accept-count=100", \
#    "-jar", "galaxy-oj-1.0.0.jar"]




#ENTRYPOINT ["java", \
#    "-Xmx4G", \
#    "-Xms4G", \
#    "-XX:MaxMetaspaceSize=256M", \
#    "-XX:+UseZGC", \
#    "-XX:ConcGCThreads=1", \
#    "-XX:MaxGCPauseMillis=100", \
#    "-XX:InitiatingHeapOccupancyPercent=60", \
#     "-XX:G1ConcRefinementThreads=1", \
#  "-XX:G1RSetUpdatingPauseTimePercent=10", \
#    "-XX:ParallelGCThreads=2", \
#    "-XX:G1HeapRegionSize=4m", \
#    "-XX:G1HeapWastePercent=10",          \
#    "-XX:ConcGCThreads=1", \
#    "-XX:+UseContainerSupport", \
#    "-XX:G1PeriodicGCInterval=0", \
#     "-XX:-G1PeriodicGCInvokesConcurrent", \
#    "-XX:+DisableExplicitGC", \
#    "-XX:+HeapDumpOnOutOfMemoryError", \
#    "-Xlog:gc*:file=/app/logs/gc.log:time:filecount=3,filesize=10M", \
#    "-Djava.security.egd=file:/dev/./urandom", \
#    "-Dfile.encoding=UTF-8", \
#    "-Dio.undertow.no-host-header=true", \
#    "-Djava.util.concurrent.ForkJoinPool.common.parallelism=1", \
#    "-Dorg.jboss.threads.enqueue-batching=true", \
#    "-Dio.netty.leakDetectionLevel=disabled", \
# "-Dundertow.worker-threads=40", \
#    "-Dundertow.worker-keep-alive=60000", \
#    "-jar", "galaxy-oj-1.0.0.jar"]



#ENTRYPOINT ["java", \
#    "-Xmx8G", \
#    "-Xms8G", \
#    "-XX:MaxMetaspaceSize=512M", \
#    "-XX:+UseZGC", \
#    "-XX:ConcGCThreads=1", \
#    "-XX:ParallelGCThreads=2", \
#    "-XX:+ZProactive", \
#    "-XX:+UseContainerSupport", \
#    "-XX:+DisableExplicitGC", \
#    "-XX:+HeapDumpOnOutOfMemoryError", \
#    "-Xlog:gc*:file=/app/logs/gc.log:time:filecount=3,filesize=10M", \
#    "-Djava.security.egd=file:/dev/./urandom", \
#    "-Dfile.encoding=UTF-8", \
#    "-Dio.undertow.no-host-header=true", \
#    "-Djava.util.concurrent.ForkJoinPool.common.parallelism=2", \
#    "-Dorg.jboss.threads.enqueue-batching=true", \
#    "-Dio.netty.leakDetectionLevel=disabled", \
#    "-jar", "galaxy-oj-1.0.0.jar"]


ENTRYPOINT ["java", \
    "-Xmx8G", \
    "-Xms8G", \
    "-XX:MaxMetaspaceSize=512M", \
    "-XX:+UseZGC", \
    "-XX:ConcGCThreads=1", \
    "-XX:ParallelGCThreads=2", \
    "-XX:+ZProactive", \
    "-XX:+UseContainerSupport", \
    "-XX:+DisableExplicitGC", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-Xlog:gc*:file=/app/logs/gc.log:time:filecount=3,filesize=10M", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dfile.encoding=UTF-8", \
    "-Dio.undertow.no-host-header=true", \
    "-Djava.util.concurrent.ForkJoinPool.common.parallelism=2", \
    "-Dorg.jboss.threads.enqueue-batching=true", \
    "-Dio.netty.leakDetectionLevel=disabled", \
    "-jar", "galaxy-oj-1.0.0.jar"]