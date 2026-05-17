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

  // Find ancestor keys in the menu tree for a given path.
  // Returns an array of keys from root to the matching item (inclusive),
  // so all intermediate submenus stay open.
  function findAncestorPath(items: any[], path: string): string[] | null {
    for (const item of items) {
      if (item.key === path) {
        return item.children?.length ? [item.key] : []
      }
      if (item.children) {
        const found = findAncestorPath(item.children, path)
        if (found !== null) {
          return [item.key, ...found]
        }
      }
    }
    return null
  }

  function isSubmenuKey(items: any[], key: string): boolean {
    for (const item of items) {
      if (item.key === key) return !!item.children?.length
      if (item.children && isSubmenuKey(item.children, key)) return true
    }
    return false
  }

  // Expand parent submenu when navigating to a child page
  watch(
    () => route.path,
    path => {
      if (app.collapsed) {
        openKeys.value = []
        return
      }

      const ancestorPath = findAncestorPath(menuItems.value, path)
      if (ancestorPath) {
        openKeys.value = ancestorPath
        return
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
    // Don't navigate for submenu items (directories with children)
    if (isSubmenuKey(menuItems.value, key)) return
    router.push(key)
  }

  function handleOpenChange(keys: string[]) {
    openKeys.value = keys
  }

  return {
    openKeys,
    menuItems,
    handleMenuClick,
    handleOpenChange,
  }
}
