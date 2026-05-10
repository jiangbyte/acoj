<template>
  <div class="layout-tabs" v-if="app.showTabs">
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
          <span>
            <component :is="t.icon ? resolveIcon(t.icon) : null" v-if="t.icon" />
            {{ t.title }}
          </span>
        </template>
      </ATabPane>

      <template #tabBarExtraContent>
        <ADropdown v-model:open="menuOpen" placement="bottomRight" trigger="click">
          <AButton size="small" type="text">
            <DownOutlined />
          </AButton>
          <template #overlay>
            <AMenu @click="handleMenuClick">
              <AMenuItem key="refresh" :disabled="tabStore.tabs.length === 0">
                <ReloadOutlined /> 刷新当前页面
              </AMenuItem>
              <AMenuItem key="closeCurrent" :disabled="tabStore.tabs.length <= 1">
                <CloseOutlined /> 关闭当前页面
              </AMenuItem>
              <AMenuItem key="closeOthers" :disabled="tabStore.tabs.length <= 1">
                <MinusOutlined /> 关闭其他页面
              </AMenuItem>
              <AMenuDivider />
              <AMenuItem key="closeAll">
                <DeleteOutlined /> 关闭全部页面
              </AMenuItem>
            </AMenu>
          </template>
        </ADropdown>
      </template>
    </ATabs>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore, useTabStore } from '@/store'
import * as Icons from '@ant-design/icons-vue'
import { resolveIcon } from '@/utils'

const { DownOutlined, ReloadOutlined, CloseOutlined, MinusOutlined, DeleteOutlined } = Icons

const HOME_PATH = import.meta.env.VITE_HOME_PATH as string || '/dashboard'

const route = useRoute()
const router = useRouter()
const app = useAppStore()
const tabStore = useTabStore()

const menuOpen = ref(false)

// Track route changes to add/switch tabs
watch(() => route.path, (path) => {
  const title = (route.meta?.title as string) || ''
  const icon = (route.meta?.icon as string) || undefined
  const affix = (route.meta?.affix as boolean) || false
  const visible = (route.meta?.visible as boolean) ?? true
  if (title && path !== HOME_PATH) {
    // is_visible === 'NO' 的路由不添加到 tab
    if (!visible) return
    tabStore.addTab({ title, icon, path, key: path, closable: !affix, affix })
  } else if (path === HOME_PATH) {
    tabStore.activeKey = HOME_PATH
    // 首页 tab 同步 icon
    const homeTab = tabStore.tabs[0]
    if (homeTab && icon) {
      homeTab.icon = icon
    }
  }
}, { immediate: true })

function handleTabChange(key: string) {
  router.push(key)
}

function handleTabRemove(targetKey: string) {
  tabStore.removeTab(targetKey)
  router.push(tabStore.activeKey)
}

function handleMenuClick({ key }: { key: string }) {
  menuOpen.value = false
  switch (key) {
    case 'refresh':
      app.reloadPage()
      break
    case 'closeCurrent':
      tabStore.removeTab(tabStore.activeKey)
      router.push(tabStore.activeKey)
      break
    case 'closeOthers': {
      const activeKey = tabStore.activeKey
      tabStore.tabs.forEach((t) => {
        if (t.key !== activeKey && t.closable) {
          tabStore.removeTab(t.key)
        }
      })
      break
    }
    case 'closeAll':
      tabStore.closeAll()
      router.push(HOME_PATH)
      break
  }
}
</script>

<style scoped>
.layout-tabs {
  display: flex;
  align-items: center;
}
.layout-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 0;
}
</style>
