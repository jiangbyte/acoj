<script setup lang="ts">
import { onMounted } from 'vue'
import { useAppStore } from './stores/app'

const appStore = useAppStore()

onMounted(() => {
  appStore.recordVisit()
})

const stats = [
  { label: 'Users', value: '1,248' },
  { label: 'Problems', value: '356' },
  { label: 'Submissions', value: '24.8k' },
]
</script>

<template>
  <n-config-provider>
    <n-message-provider>
      <n-layout class="min-h-screen bg-#f6f8fb">
        <n-layout-header bordered class="h-16 flex items-center justify-between px-6">
          <div class="flex items-center gap-3">
            <div class="brand-mark">A</div>
            <div>
              <div class="text-16px font-700 text-#1f2937">ACOJ Admin</div>
              <div class="text-12px text-#6b7280">Management Console</div>
            </div>
          </div>

          <n-space align="center">
            <n-tag type="success" round>{{ appStore.layoutMode }}</n-tag>
            <n-button secondary @click="appStore.toggleCollapsed">Toggle layout</n-button>
          </n-space>
        </n-layout-header>

        <n-layout has-sider class="min-h-[calc(100vh-64px)]">
          <n-layout-sider
            bordered
            collapse-mode="width"
            :collapsed="appStore.collapsed"
            :collapsed-width="64"
            :width="220"
            class="bg-white"
          >
            <div class="p-4">
              <n-menu
                :collapsed="appStore.collapsed"
                :collapsed-width="64"
                :options="[
                  { label: 'Dashboard', key: 'dashboard' },
                  { label: 'Problems', key: 'problems' },
                  { label: 'Users', key: 'users' },
                  { label: 'Settings', key: 'settings' },
                ]"
                default-value="dashboard"
              />
            </div>
          </n-layout-sider>

          <n-layout-content class="p-6">
            <div class="mx-auto max-w-1180px">
              <div class="mb-6 flex flex-wrap items-end justify-between gap-4">
                <div>
                  <h1 class="m-0 text-28px font-700 text-#111827">Dashboard</h1>
                  <p class="mt-2 text-#6b7280">Base admin tooling is ready for feature work.</p>
                </div>
                <n-tag type="info">Persisted visits: {{ appStore.visitCount }}</n-tag>
              </div>

              <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
                <n-card v-for="item in stats" :key="item.label" size="small">
                  <div class="text-13px text-#6b7280">{{ item.label }}</div>
                  <div class="mt-2 text-26px font-700 text-#111827">{{ item.value }}</div>
                </n-card>
              </div>

              <n-card class="mt-4" title="Integration Status">
                <n-space vertical>
                  <n-alert type="success" title="Naive UI">
                    Components are resolved on demand.
                  </n-alert>
                  <n-alert type="success" title="UnoCSS">Utility classes are active.</n-alert>
                  <n-alert type="success" title="Pinia">Store state is persisted locally.</n-alert>
                  <n-alert type="success" title="Axios">
                    Shared request instance is available.
                  </n-alert>
                </n-space>
              </n-card>
            </div>
          </n-layout-content>
        </n-layout>
      </n-layout>
    </n-message-provider>
  </n-config-provider>
</template>
