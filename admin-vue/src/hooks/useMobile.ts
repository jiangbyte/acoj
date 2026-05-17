import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

export function useMobile(defaultWidth = 640) {
  const isMobile = ref(false)

  onMounted(() => {
    const mql = window.matchMedia('(max-width: 767px)')
    isMobile.value = mql.matches
    const handler = (e: MediaQueryListEvent) => {
      isMobile.value = e.matches
    }
    mql.addEventListener('change', handler)
    onBeforeUnmount(() => mql.removeEventListener('change', handler))
  })

  const drawerWidth = computed(() => (isMobile.value ? '100%' : defaultWidth))

  return { isMobile, drawerWidth }
}
