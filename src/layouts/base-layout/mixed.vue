<template>
  <div class="h-screen">
    <ALayout class="h-full">
      <!-- First level: narrow module dock -->
      <ALayoutSider :width="64" theme="dark" class="layout-sider-mixed-first">
        <div class="flex flex-col items-center py-2">
          <div class="w-10 h-10 bg-primary-dynamic rounded flex items-center justify-center text-white font-bold text-sm mb-4 cursor-pointer" @click="goHome">
            H
          </div>
          <ATooltip v-for="mod in modules" :key="mod.key" :title="mod.label" placement="right">
            <div
              class="w-10 h-10 flex items-center justify-center rounded cursor-pointer mb-1 text-white/70 hover:text-white hover:bg-white/10"
              :class="{ 'text-white bg-white/10': activeModule === mod.key }"
              @click="switchModule(mod)"
            >
              <component :is="mod.icon" />
            </div>
          </ATooltip>
        </div>
      </ALayoutSider>

      <!-- Second level: module sub-menus -->
      <ALayoutSider
        v-model:collapsed="app.collapsed"
        :width="200"
        :collapsed-width="0"
        :theme="siderTheme"
        :style="{ overflow: 'auto' }"
      >
        <div class="h-16 flex items-center px-4 font-medium border-b" :style="{ borderColor: 'var(--border-color)' }">
          {{ activeModuleLabel }}
        </div>
        <AMenu
          mode="inline"
          :selectedKeys="[route.path]"
          :openKeys="openKeys"
          :theme="siderTheme"
          :items="currentMenus"
          @click="handleMenuClick"
        />
      </ALayoutSider>

      <!-- Main content -->
      <ALayout>
        <Header />
        <Breadcrumb />
        <Tab />
        <ALayoutContent class="layout-content layout-content-bg p-4">
          <router-view />
          <FooterBar v-if="app.showFooter" />
        </ALayoutContent>
      </ALayout>
    </ALayout>
    <ThemeDrawer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'
import { useRouteStore } from '@/store/route'
import Header from './header/index.vue'
import Breadcrumb from './breadcrumb/index.vue'
import Tab from './tab/index.vue'
import FooterBar from './components/FooterBar.vue'
import ThemeDrawer from './components/ThemeDrawer.vue'
import { menuToItems } from './sider/menuHelper'
import {
  AppstoreOutlined,
  TeamOutlined,
  SettingOutlined,
  FileOutlined,
  SafetyOutlined,
  BellOutlined,
  KeyOutlined,
  ProfileOutlined,
  ToolOutlined,
  DatabaseOutlined,
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const app = useAppStore()
const routeStore = useRouteStore()

const openKeys = ref<string[]>([])
const activeModule = ref<string>('')

const siderTheme = computed(() => app.theme === 'light' ? 'light' : 'dark')

const iconMap: Record<string, any> = {
  AppstoreOutlined,
  TeamOutlined,
  SettingOutlined,
  FileOutlined,
  SafetyOutlined,
  BellOutlined,
  KeyOutlined,
  ProfileOutlined,
  ToolOutlined,
  DatabaseOutlined,
}

const modules = computed(() => {
  return routeStore.menus.map((m: any) => ({
    key: m.route_path || m.code,
    label: m.name,
    icon: m.icon ? (iconMap[m.icon] || AppstoreOutlined) : AppstoreOutlined,
    children: m.children || [],
  }))
})

const activeModuleLabel = computed(() => {
  const mod = modules.value.find((m) => m.key === activeModule.value)
  return mod?.label || ''
})

const currentMenus = computed(() => {
  const mod = modules.value.find((m) => m.key === activeModule.value)
  if (!mod) return []
  if (mod.children.length) {
    return menuToItems(mod.children)
  }
  return [{
    key: mod.key,
    label: mod.label,
    icon: mod.icon,
  }]
})

watch(() => route.path, (path) => {
  const segments = path.split('/').filter(Boolean)
  openKeys.value = segments.slice(0, -1).map((_, i) => '/' + segments.slice(0, i + 1).join('/'))

  // Auto-select module based on current route
  const matchedModule = modules.value.find((m) => path.startsWith(m.key))
  if (matchedModule) {
    activeModule.value = matchedModule.key
  }
}, { immediate: true })

function goHome() {
  router.push('/dashboard')
}

function findFirstRoute(items: any[]): string | undefined {
  for (const item of items) {
    const route = item.route_path || item.path
    if (route) return route
    if (item.children?.length) {
      const found = findFirstRoute(item.children)
      if (found) return found
    }
  }
}

function switchModule(mod: { key: string; label: string; children: any[] }) {
  activeModule.value = mod.key
  const route = mod.children?.length ? findFirstRoute(mod.children) : mod.key
  if (route) router.push(route)
}

function handleMenuClick({ key }: { key: string }) {
  router.push(key)
}
</script>

<style scoped>
.layout-sider-mixed-first {
  overflow: hidden !important;
  border-right: 1px solid #303030;
}
.layout-sider-mixed-first :deep(.ant-layout-sider-trigger) {
  display: none !important;
}

.layout-sider-sub {
  overflow: hidden !important;
}
.layout-sider-sub :deep(.ant-layout-sider-trigger) {
  display: none !important;
}

.layout-sider-menu-scroll {
  height: calc(100vh - 64px);
  overflow-y: auto;
  overflow-x: hidden;
}
</style>
