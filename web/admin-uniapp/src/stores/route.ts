import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { resourceApi } from '@/api'
import {
  resourceCodeMap,
  resourceConfigs,
  type ResourceKey,
} from '@/config/resource'
import { arrayToTree } from '@/utils/tree'

export interface ResourceItem {
  id: string
  parent_id?: string | null
  code: string
  name: string
  resource_type: string
  module_id?: string | null
  module_id_name?: string | null
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

export const useRouteStore = defineStore('route', () => {
  const modules = ref<ResourceModule[]>([])
  const activeModuleId = ref<string>('')
  const loading = ref(false)

  const activeModule = computed(
    () =>
      modules.value.find((item) => item.id === activeModuleId.value) ??
      modules.value[0]
  )

  const activeMenus = computed(() => {
    const resources = (activeModule.value?.resources ?? [])
      .filter((item) => item.status === 'ENABLED' && item.is_visible !== false)
      .sort((a, b) => (a.sort ?? 99) - (b.sort ?? 99))
    return arrayToTree(resources)
  })

  const availableResourceKeys = computed(() => {
    const keys = new Set<ResourceKey>()
    modules.value.forEach((module) => {
      module.resources.forEach((resource) => {
        const key = resolveResourceKey(resource)
        if (key) {
          keys.add(key)
        }
      })
    })
    return keys
  })

  async function initRoutes() {
    loading.value = true
    try {
      modules.value = await resourceApi.current()
      if (
        !activeModuleId.value ||
        !modules.value.some((item) => item.id === activeModuleId.value)
      ) {
        activeModuleId.value = modules.value[0]?.id ?? ''
      }
    } finally {
      loading.value = false
    }
  }

  function setActiveModule(id: string) {
    activeModuleId.value = id
  }

  function resolveResourceKey(resource: ResourceItem): ResourceKey | null {
    const code = String(resource.code ?? '').replace(/-/g, '_')
    const fromCode = resourceCodeMap[code]
    if (fromCode) {
      return fromCode
    }
    const normalizedPath = String(resource.path ?? '')
      .split('/')
      .filter(Boolean)
      .pop()
    return normalizedPath ? (resourceCodeMap[normalizedPath] ?? null) : null
  }

  function openResource(resource: ResourceItem) {
    const key = resolveResourceKey(resource)
    if (!key || !resourceConfigs[key]) {
      uni.showToast({ title: '小程序端暂未配置该菜单', icon: 'none' })
      return
    }
    uni.navigateTo({ url: `/pages/resource/list/index?resource=${key}` })
  }

  function resetRouteStore() {
    modules.value = []
    activeModuleId.value = ''
    loading.value = false
  }

  return {
    modules,
    activeModuleId,
    loading,
    activeModule,
    activeMenus,
    availableResourceKeys,
    initRoutes,
    setActiveModule,
    openResource,
    resolveResourceKey,
    resetRouteStore,
  }
})
