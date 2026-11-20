<script lang="ts" setup>
// Technology stack data
const frontendTechStack = [
  { technology: 'Vue 3', purpose: 'Frontend Framework', description: 'Progressive JavaScript framework' },
  { technology: 'TypeScript', purpose: 'Language', description: 'Type-safe JavaScript superset' },
  { technology: 'Naive UI', purpose: 'UI Component Library', description: 'Vue 3 component library' },
  { technology: 'Vite', purpose: 'Build Tool', description: 'Next-generation frontend tooling' },
  { technology: 'Pinia', purpose: 'State Management', description: 'Vue state management library' },
]

const backendTechStack = [
  { technology: 'Redis', purpose: 'Cache', description: 'In-memory data store' },
  { technology: 'MySQL', purpose: 'Database', description: 'Relational database' },
  { technology: 'Spring Boot', purpose: 'Backend Framework', description: 'Java web framework' },
  { technology: 'Spring Cloud', purpose: 'Microservices Framework', description: 'Microservices framework for Spring Boot' },
  { technology: 'Spring Cloud Alibaba', purpose: 'Microservices Framework', description: 'Spring Cloud extensions' },
  { technology: 'Nacos', purpose: 'Service Discovery', description: 'Service Discovery' },
  { technology: 'RabbitMQ', purpose: 'Message Queue', description: 'Message Queue' },
  { technology: 'Docker', purpose: 'Containerization', description: 'Container platform' },
]
</script>

<template>
  <div class="flex flex-col h-full w-full">
    <n-card size="small" class="flex-1 pb-11">
      <n-space vertical>
        <n-text>About</n-text>
        <n-space vertical>
          <n-text depth="1">
            ACOJ online judge system.
          </n-text>
          <n-space>
            <n-button
              tag="a"
              href="https://github.com/jiangbyte/acoj"
              target="_blank"
              type="primary"
              ghost
            >
              GitHub
            </n-button>

            <n-button
              tag="a"
              href="https://gitee.com/jiangbyte/acoj"
              type="error"
              target="_blank"
              ghost
            >
              Gitee
            </n-button>
          </n-space>
        </n-space>
        <n-text>Technology Stack</n-text>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- Frontend technology stack -->
          <n-card title="Frontend" size="small">
            <n-table :bordered="false" :single-line="false">
              <thead>
                <tr>
                  <th>Technology</th>
                  <th>Purpose</th>
                  <th>Description</th>
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

          <!-- Backend technology stack -->
          <n-card title="Backend" size="small">
            <n-table :bordered="false" :single-line="false">
              <thead>
                <tr>
                  <th>Technology</th>
                  <th>Purpose</th>
                  <th>Description</th>
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
