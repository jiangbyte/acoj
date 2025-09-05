<script lang="ts" setup>
// 技术栈数据
const frontendTechStack = [
  { technology: 'Vue 3', purpose: '前端框架', description: '渐进式JavaScript框架' },
  { technology: 'TypeScript', purpose: '编程语言', description: '类型安全的JavaScript超集' },
  { technology: 'Naive UI', purpose: 'UI组件库', description: 'Vue 3组件库' },
  { technology: 'Vite', purpose: '构建工具', description: '下一代前端工具链' },
  { technology: 'Pinia', purpose: '状态管理', description: 'Vue状态管理库' },
]

const backendTechStack = [
  { technology: 'Redis', purpose: '缓存', description: '内存数据存储' },
  { technology: 'MySQL', purpose: '数据库', description: '关系型数据库' },
  { technology: 'Spring Boot', purpose: '后端框架', description: 'Java Web开发框架' },
  { technology: 'Spring Cloud', purpose: '微服务框架', description: 'Spring Boot的微服务框架' },
  { technology: 'Spring Cloud Alibaba', purpose: '微服务框架', description: 'Spring Cloud的扩展' },
  { technology: 'Nacos', purpose: '服务注册与发现', description: '服务注册与发现' },
  { technology: 'RabbitMQ', purpose: '消息队列', description: '消息队列' },
  { technology: 'Docker', purpose: '容器化', description: '容器化平台' },
]
</script>

<template>
  <div class="flex flex-col h-full w-full">
    <n-card size="small" class="flex-1 pb-11">
      <n-space vertical>
        <n-text>关于</n-text>
        <n-space vertical>
          <n-text depth="1">
            Astro Code OJ 在线评测系统。
          </n-text>
          <n-space>
            <n-button
              tag="a"
              href="https://github.com/jiangbyte/astro-code-oj"
              target="_blank"
              type="primary"
              ghost
            >
              GitHub
            </n-button>

            <n-button
              tag="a"
              href="https://gitee.com/jiangbyte/astro-code-oj"
              type="error"
              target="_blank"
              ghost
            >
              Gitee
            </n-button>
          </n-space>
        </n-space>
        <n-text>技术栈</n-text>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- 前端技术栈 -->
          <n-card title="前端技术" size="small">
            <n-table :bordered="false" :single-line="false">
              <thead>
                <tr>
                  <th>技术</th>
                  <th>用途</th>
                  <th>描述</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(tech, index) in frontendTechStack" :key="`front-${index}`">
                  <td>
                    <n-tag type="info" size="small">
                      {{ tech.technology }}
                    </n-tag>
                  </td>
                  <td>{{ tech.purpose }}</td>
                  <td>{{ tech.description }}</td>
                </tr>
              </tbody>
            </n-table>
          </n-card>

          <!-- 后端技术栈 -->
          <n-card title="后端技术" size="small">
            <n-table :bordered="false" :single-line="false">
              <thead>
                <tr>
                  <th>技术</th>
                  <th>用途</th>
                  <th>描述</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(tech, index) in backendTechStack" :key="`back-${index}`">
                  <td>
                    <n-tag type="success" size="small">
                      {{ tech.technology }}
                    </n-tag>
                  </td>
                  <td>{{ tech.purpose }}</td>
                  <td>{{ tech.description }}</td>
                </tr>
              </tbody>
            </n-table>
          </n-card>
        </div>
      </n-space>
    </n-card>
  </div>
</template>

<style scoped>

</style>
