<template>
  <Layout title="工作台">
    <view class="flex flex-col">
      <view v-if="moduleTabs.length > 1" class="bg-white">
        <u-tabs
          :list="moduleTabs"
          :current="activeModuleIndex"
          @change="handleModuleChange"
        />
      </view>

      <view
        v-for="card in catalogCards"
        :key="card.id"
        class="mx-4 mt-3 bg-white rounded-lg"
      >
        <text class="block px-4 py-3 text-base font-bold text-gray-900">
          {{ card.name }}
        </text>
        <u-grid :col="3" :border="false">
          <u-grid-item
            v-for="item in card.entries"
            :key="item.id"
            @click="routeStore.openResource(item)"
          >
            <view class="flex flex-col items-center gap-1 py-3">
              <u-icon :name="iconName(item.icon)" />
              <text class="text-xs text-gray-700 truncate">{{ item.name }}</text>
            </view>
          </u-grid-item>
        </u-grid>
      </view>

      <view
        v-if="!catalogCards.length"
        class="empty-state"
      >
        <u-empty mode="list" text="暂无可用菜单" />
      </view>
    </view>
  </Layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import Layout from '@/layouts/index.vue'
import { useAuthStore } from '@/stores/auth'
import { useDictStore } from '@/stores/dict'
import { type ResourceItem, useRouteStore } from '@/stores/route'
import { arrayToTree, flattenTree } from '@/utils/tree'

interface CatalogCard {
  id: string
  name: string
  icon?: string | null
  entries: ResourceItem[]
}

const authStore = useAuthStore()
const routeStore = useRouteStore()
const dictStore = useDictStore()

const moduleTabs = computed(() =>
  routeStore.modules.map((mod) => ({ name: mod.name }))
)

const activeModuleIndex = computed(() => {
  const idx = routeStore.modules.findIndex(
    (m) => m.id === routeStore.activeModuleId
  )
  return idx >= 0 ? idx : 0
})

const catalogCards = computed<CatalogCard[]>(() => {
  const cards: CatalogCard[] = []
  const mod = routeStore.activeModule
  if (!mod) return cards

  const visibleResources: ResourceItem[] = (mod.resources ?? [])
    .filter((item) => item.status === 'ENABLED' && item.is_visible !== false)
    .sort((a, b) => (a.sort ?? 99) - (b.sort ?? 99))
    .map((item) => ({ ...item }))
  const tree = arrayToTree<ResourceItem>(visibleResources)
  const rootEntries: ResourceItem[] = []

  tree.forEach((node: any) => {
    if (node.resource_type === 'CATALOG') {
      const entries = flattenTree<ResourceItem>(
        (node.children ?? []) as ResourceItem[]
      ).filter(isMenuEntry)
      if (entries.length) {
        cards.push({ id: node.id, name: node.name, icon: node.icon, entries })
      }
      return
    }
    if (isMenuEntry(node)) {
      rootEntries.push(node)
    }
  })

  if (rootEntries.length) {
    cards.unshift({
      id: `${mod.id}:root`,
      name: '常用功能',
      icon: mod.icon,
      entries: rootEntries,
    })
  }

  return cards
})

onShow(async () => {
  if (!authStore.isLogin) {
    uni.reLaunch({ url: '/pages/auth/login/login' })
    return
  }
  await bootstrap()
})

onPullDownRefresh(async () => {
  await refreshMenus()
  uni.stopPullDownRefresh()
})

async function bootstrap() {
  if (!dictStore.loaded) {
    await dictStore.refreshDict()
  }
  if (!routeStore.modules.length) {
    await routeStore.initRoutes()
  }
}

async function refreshMenus() {
  await routeStore.initRoutes()
}

function iconName(icon?: string | null) {
  if (!icon) return 'grid'
  if (icon.includes('analysis')) return 'home'
  if (icon.includes('message')) return 'chat'
  if (icon.includes('user') || icon.includes('account') || icon.includes('people')) return 'account'
  if (icon.includes('lock')) return 'lock'
  if (icon.includes('setting')) return 'setting'
  return 'grid'
}

function isMenuEntry(resource: ResourceItem) {
  return (
    ['MENU', 'PAGE'].includes(resource.resource_type) &&
    Boolean(routeStore.resolveResourceKey(resource))
  )
}

function handleModuleChange(event: any) {
  const index = typeof event === 'number' ? event : event.index
  const mod = routeStore.modules[index]
  if (mod) {
    routeStore.setActiveModule(mod.id)
  }
}
</script>

<style lang="scss" scoped>
.empty-state {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
