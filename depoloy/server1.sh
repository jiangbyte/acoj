docker stop galaxy_oj_gateway
docker rm galaxy_oj_gateway
docker rmi registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_gateway:1.0.0
docker run -d -p 93:93 --name galaxy_oj_gateway --pull always registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_gateway:1.0.0



docker stop galaxy_oj_01
docker rm galaxy_oj_01
docker rmi registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj:1.0.0
docker run -d --name galaxy_oj_01 --pull always registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj:1.0.0
docker stop galaxy_oj_02
docker rm galaxy_oj_02
docker run -d --name galaxy_oj_02 --pull always registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj:1.0.0
docker stop galaxy_oj_03
docker rm galaxy_oj_03
docker run -d --name galaxy_oj_03 --pull always registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj:1.0.0




docker stop galaxy_oj_admin
docker rm galaxy_oj_admin
docker rmi registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_admin:1.0.0
docker run -d -p 81:81 --name galaxy_oj_admin --pull always registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_admin:1.0.0



docker stop galaxy_oj_pc
docker rm galaxy_oj_pc
docker rmi registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_pc:1.0.0
docker run -d -p 80:80 --name galaxy_oj_pc --pull always registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_pc:1.0.0