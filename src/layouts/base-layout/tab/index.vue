<template>
  <div class="layout-tabs flex items-center px-2" v-if="app.showTabs">
    <ATabs
      type="editable-card"
      :activeKey="tabStore.activeKey"
      hideAdd
      size="small"
      @change="handleTabChange"
      @edit="handleTabRemove"
      style="flex: 1"
    >
      <ATabPane v-for="t in tabStore.tabs" :key="t.key" :closable="t.closable">
        <template #tab>
          <span>{{ t.title }}</span>
        </template>
      </ATabPane>
    </ATabs>
  </div>
</template>

<script setup lang="ts">
import { watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'
import { useTabStore } from '@/store/tab'

const route = useRoute()
const router = useRouter()
const app = useAppStore()
const tabStore = useTabStore()

// Track route changes to add/switch tabs
watch(() => route.path, (path) => {
  const title = (route.meta?.title as string) || ''
  if (title && path !== '/dashboard') {
    tabStore.addTab({ title, path, key: path, closable: true })
  } else if (path === '/dashboard') {
    tabStore.activeKey = '/dashboard'
  }
})

function handleTabChange(key: string) {
  router.push(key)
}

function handleTabRemove(targetKey: string) {
  tabStore.removeTab(targetKey)
  router.push(tabStore.activeKey)
}
</script>
