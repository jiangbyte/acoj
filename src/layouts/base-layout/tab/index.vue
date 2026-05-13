<template>
  <div
    v-if="app.showTabs"
    class="tab-bar bg-[var(--container-bg)] border-b border-[var(--border-color)]"
  >
    <ATabs
      type="editable-card"
      :active-key="tabStore.activeKey"
      hide-add
      size="small"
      class="tab-container"
      @change="handleTabChange"
      @edit="handleTabRemove"
    >
      <ATabPane
        v-for="t in tabStore.tabs"
        :key="t.key"
        :closable="t.closable"
        class="tab-pane-item"
      >
        <template #tab>
          <span class="tab-label">
            <component :is="t.icon ? resolveIcon(t.icon) : null" v-if="t.icon" />
            {{ t.title }}
          </span>
        </template>
      </ATabPane>

      <template #rightExtra>
        <ADropdown v-model:open="menuOpen" placement="bottomRight" trigger="click">
          <AButton size="small" type="text" class="tab-more-btn">
            <DownOutlined />
          </AButton>
          <template #overlay>
            <AMenu @click="handleMenuClick">
              <AMenuItem key="refresh" :disabled="tabStore.tabs.length === 0">
                <ReloadOutlined />
                刷新当前页面
              </AMenuItem>
              <AMenuItem key="closeCurrent" :disabled="tabStore.tabs.length <= 1">
                <CloseOutlined />
                关闭当前页面
              </AMenuItem>
              <AMenuItem key="closeOthers" :disabled="tabStore.tabs.length <= 1">
                <MinusOutlined />
                关闭其他页面
              </AMenuItem>
              <AMenuDivider />
              <AMenuItem key="closeAll">
                <DeleteOutlined />
                关闭全部页面
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

const HOME_PATH = (import.meta.env.VITE_HOME_PATH as string) || '/dashboard'

const route = useRoute()
const router = useRouter()
const app = useAppStore()
const tabStore = useTabStore()

const menuOpen = ref(false)

// Clean up persisted tabs whose routes are no longer visible (e.g. after route changes)
function cleanupInvisibleTabs() {
  for (const t of [...tabStore.tabs]) {
    if (t.affix) continue
    const resolved = router.resolve(t.path)
    if (resolved.meta?.visible === false) {
      tabStore.removeTab(t.key)
    }
  }
}

// Track route changes to add/switch tabs
watch(
  () => route.path,
  path => {
    // Remove any persisted tabs for invisible routes
    cleanupInvisibleTabs()

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
      const homeTab = tabStore.tabs[0]
      if (homeTab) {
        if (title) homeTab.title = title
        if (icon) homeTab.icon = icon
      }
    }
  },
  { immediate: true }
)

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
      tabStore.tabs.forEach(t => {
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
.tab-bar {
  display: flex;
  align-items: center;
  height: 36px;
  min-height: 36px;
  overflow: hidden;
}
.tab-bar :deep(.ant-tabs) {
  height: 36px;
  overflow: hidden;
}
.tab-bar :deep(.ant-tabs-nav) {
  height: 36px !important;
  min-height: 36px !important;
  margin-bottom: 0 !important;
  overflow: hidden;
}
.tab-bar :deep(.ant-tabs-nav::before) {
  display: none !important;
}
.tab-bar :deep(.ant-tabs-nav-wrap) {
  height: 36px;
  overflow: hidden;
}
.tab-bar :deep(.ant-tabs-nav-list) {
  height: 36px;
}
.tab-bar :deep(.ant-tabs-nav .ant-tabs-nav-operations) {
  display: none !important;
}
.tab-container {
  flex: 1;
  min-width: 0;
}
.tab-bar :deep(.ant-tabs-tab) {
  height: 36px !important;
  line-height: 36px !important;
  padding: 0 12px !important;
  display: inline-flex !important;
  align-items: center !important;
  transition: none !important;
}
.tab-bar :deep(.ant-tabs-tab .ant-tabs-tab-remove) {
  display: inline-flex;
  align-items: center;
  line-height: 1;
}
.tab-bar :deep(.ant-tabs-extra-content) {
  height: 36px;
  display: inline-flex;
  align-items: center;
}
.tab-more-btn {
  height: 36px !important;
  width: 36px !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  border-radius: 0 !important;
}
.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  line-height: 1;
}
</style>
