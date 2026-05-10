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
      for (const item of menuItems.value) {
        if (item.children?.length && item.children.some((c: any) => c.key === path)) {
          openKeys.value = [item.key]
          return
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
