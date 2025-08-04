# 使用 Node.js 22 作为基础镜像
FROM node:22-alpine

# 设置工作目录
WORKDIR /app

# # 复制构建好的项目文件
# COPY ./.output /app/.output
# COPY ecosystem.config.cjs /app/ecosystem.config.cjs

# # 全局安装 PM2
# # RUN npm install -g pm2 --registry https://registry.npmmirror.com

# # 暴露端口（Nuxt.js 默认使用 3000 端口）
# EXPOSE 3000

# # 使用 PM2 启动应用
# # CMD ["pm2", "start", "/app/ecosystem.config.cjs"]

# # CMD ["pm2", "start", "/app/ecosystem.config.cjs", "--no-daemon"]

# # 使用node
# CMD ["node", "/app/.output/server/index.mjs"]

# docker build -t registry.cn-beijing.aliyuncs.com/charlie_images/zero-nuxt:latest .
# docker push registry.cn-beijing.aliyuncs.com/charlie_images/zero-nuxt:latest
# docker run -d -p 80:3000 --restart always zero-online-judge-nuxt:latest
# cp -r ./dist /app/vue
# node /app/nuxt/server/index.mjs
# node /app/vue/index.html