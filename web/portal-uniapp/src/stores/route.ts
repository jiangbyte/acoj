import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { resourceApi } from '@/api'

export interface ResourceItem {
  id: string
  parent_id?: string | null
  code: string
  name: string
  resource_type: string
  path?: string | null
  icon?: string | null
  sort?: number
  is_visible?: boolean
  status?: string
  children?: ResourceItem[]
}

export interface ResourceModule {
  id: string
  name: string
  code: string
  client: string
  icon?: string | null
  color?: string | null
  sort: number
  resources: ResourceItem[]
}

const pathMap: Record<string, string> = {
  home: '/pages/home/index',
  usercenter: '/pages/usercenter/index',
  messages: '/pages/messages/index',
  space: '/pages/space/index',
}

export const useRouteStore = defineStore('route', () => {
  const modules = ref<ResourceModule[]>([])
  const loading = ref(false)

  const headerResources = computed(() =>
    (modules.value.find((item) => item.code === 'HEADER')?.resources ?? [])
      .filter((item) => item.status === 'ENABLED' && item.is_visible !== false)
      .filter((item) => normalizePath(item.path || item.code) !== 'space')
      .sort((a, b) => (a.sort ?? 99) - (b.sort ?? 99))
  )

  async function initRoutes() {
    loading.value = true
    try {
      modules.value = await resourceApi.current()
    } finally {
      loading.value = false
    }
  }

  function openResource(resource: ResourceItem) {
    const key = normalizePath(resource.path || resource.code)
    const url = pathMap[key]
    if (!url) {
      uni.showToast({ title: '小程序端暂未配置该入口', icon: 'none' })
      return
    }
    if (
      url.includes('/pages/home/') ||
      url.includes('/pages/messages/') ||
      url.includes('/pages/usercenter/')
    ) {
      uni.switchTab({ url })
      return
    }
    uni.navigateTo({ url })
  }

  function resetRouteStore() {
    modules.value = []
    loading.value = false
  }

  return {
    modules,
    loading,
    headerResources,
    initRoutes,
    openResource,
    resetRouteStore,
  }
})

function normalizePath(value?: string | null) {
  return (
    String(value || '')
      .split('?')[0]
      .split('/')
      .filter(Boolean)
      .pop() || ''
  )
}
