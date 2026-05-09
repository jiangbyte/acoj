<template>
  <div class="layout-tabs flex items-center border-b px-2" v-if="app.showTabs">
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
import { useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'
import { useTabStore } from '@/store/tab'

const router = useRouter()
const app = useAppStore()
const tabStore = useTabStore()

function handleTabChange(key: string) {
  router.push(key)
}

function handleTabRemove(targetKey: string) {
  tabStore.removeTab(targetKey)
  router.push(tabStore.activeKey)
}
</script>
