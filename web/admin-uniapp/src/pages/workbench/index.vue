<template>
  <Layout title="工作台">
    <view>
      <u-card v-if="moduleTabs.length > 1" :show-head="false">
        <template #body>
          <u-tabs
            :list="moduleTabs"
            :current="activeModuleIndex"
            @change="handleModuleChange"
          ></u-tabs>
        </template>
      </u-card>

      <u-card v-for="card in catalogCards" :key="card.id" :title="card.name">
        <template #body>
          <u-grid :col="3" :border="false">
            <u-grid-item
              v-for="item in card.entries"
              :key="item.id"
              @click="routeStore.openResource(item)"
            >
              <view class="entry__icon">
                <u-icon :name="iconName(item.icon)"></u-icon>
              </view>
              <text class="entry__text">{{ item.name }}</text>
            </u-grid-item>
          </u-grid>
        </template>
      </u-card>
      <u-empty
        v-if="!catalogCards.length"
        mode="list"
        text="暂无可用菜单"
      ></u-empty>
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
  routeStore.modules.map((module) => ({ name: module.name, id: module.id }))
)

const activeModuleIndex = computed(() => {
  const index = routeStore.modules.findIndex(
    (module) => module.id === routeStore.activeModuleId
  )
  return index >= 0 ? index : 0
})

const catalogCards = computed<CatalogCard[]>(() => {
  const cards: CatalogCard[] = []
  const module = routeStore.activeModule
  if (!module) {
    return cards
  }

  const visibleResources = module.resources
    .filter((item) => item.status === 'ENABLED' && item.is_visible !== false)
    .sort((a, b) => (a.sort ?? 99) - (b.sort ?? 99))
  const tree = arrayToTree(visibleResources.map((item) => ({ ...item })))
  const rootEntries: ResourceItem[] = []

  tree.forEach((node) => {
    if (node.resource_type === 'CATALOG') {
      const entries = flattenTree(node.children ?? []).filter(isMenuEntry)
      if (entries.length) {
        cards.push({
          id: node.id,
          name: node.name,
          icon: node.icon,
          entries,
        })
      }
      return
    }

    if (isMenuEntry(node)) {
      rootEntries.push(node)
    }
  })

  if (rootEntries.length) {
    cards.unshift({
      id: `${module.id}:root`,
      name: '常用功能',
      icon: module.icon,
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
  if (!icon) {
    return 'grid'
  }
  if (icon.includes('analysis')) {
    return 'home'
  }
  if (icon.includes('message')) {
    return 'chat'
  }
  if (
    icon.includes('user') ||
    icon.includes('account') ||
    icon.includes('people')
  ) {
    return 'account'
  }
  if (icon.includes('lock')) {
    return 'lock'
  }
  if (icon.includes('setting')) {
    return 'setting'
  }
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
  const module = routeStore.modules[index]
  if (module) {
    routeStore.setActiveModule(module.id)
  }
}
</script>

<style lang="scss" scoped>
.entry__icon {
  display: flex;
  justify-content: center;
}

.entry__text {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
