import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore, useRouteStore } from '@/store'
import { menuToItems } from './menuHelper'

export function useMenu() {
  const route = useRoute()
  const router = useRouter()
  const app = useAppStore()
  const routeStore = useRouteStore()

  const openKeys = ref<string[]>([])

  const menuItems = computed(() => menuToItems(routeStore.menus))

  // Expand parent submenu when navigating to a child page
  watch(
    () => route.path,
    path => {
      if (app.collapsed) {
        openKeys.value = []
        return
      }

      const name = route.name as string | undefined
      const sidebarItems = menuItems.value

      // Strategy 1: Direct visible child match
      for (const item of sidebarItems) {
        if (item.children?.length && item.children.some((c: any) => c.key === path)) {
          openKeys.value = [item.key]
          return
        }
      }

      // Strategy 2: Path prefix matching
      for (const item of sidebarItems) {
        if (path.startsWith(item.key + '/') && path !== item.key) {
          openKeys.value = [item.key]
          return
        }
      }

      // Strategy 3: Resource tree ancestry via route.name (resource code)
      if (name && routeStore.menus?.length) {
        const idMap = new Map<string, any>()
        const codeMap = new Map<string, any>()
        function walk(nodes: any[]) {
          for (const n of nodes) {
            if (n.id) idMap.set(n.id, n)
            if (n.code) codeMap.set(n.code, n)
            if (n.children) walk(n.children)
          }
        }
        walk(routeStore.menus)

        const sidebarKeySet = new Set(sidebarItems.map((i: any) => i.key))
        const resource = codeMap.get(name)
        if (resource) {
          let currentId = resource.parent_id
          while (currentId) {
            const parent = idMap.get(currentId)
            if (!parent) break
            if (sidebarKeySet.has(parent.route_path)) {
              openKeys.value = [parent.route_path]
              return
            }
            currentId = parent.parent_id
          }
        }
      }

      openKeys.value = []
    }
  )

  // 收起侧边栏时清空 openKeys
  watch(
    () => app.collapsed,
    collapsed => {
      if (collapsed) openKeys.value = []
    }
  )

  function handleMenuClick({ key }: { key: string }) {
    router.push(key)
  }

  function handleOpenChange(keys: string[]) {
    if (keys.length > 1) {
      const added = keys.filter(k => !openKeys.value.includes(k))
      openKeys.value = added.length ? [added[0]] : keys.slice(-1)
    } else {
      openKeys.value = keys
    }
  }

  return {
    openKeys,
    menuItems,
    handleMenuClick,
    handleOpenChange,
  }
}
