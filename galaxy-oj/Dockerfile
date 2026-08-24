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
#FROM bellsoft/liberica-openjdk-rocky:21
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/openjdk:21
#FROM docker.xuanyuan.run/bellsoft/liberica-openjdk-alpine:21.0.8-cds
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


# 4 vCPU / 8 GiB 同机：Nacos / MySQL / MinIO / Redis / 判题（相似度未启用）
# galaxy-oj 进程约 2.0~2.4G（堆 1.5G + 元空间/直接内存/线程栈）
ENTRYPOINT ["java", \
    "-Xms1536m", \
    "-Xmx1536m", \
    "-XX:MaxMetaspaceSize=256M", \
    "-XX:MaxDirectMemorySize=192M", \
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
    "-jar", "galaxy-oj-1.0.0.jar"]