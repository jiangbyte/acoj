import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useAppStore = defineStore(
  'app',
  () => {
    const collapsed = ref(false)
    const visitCount = ref(0)

    const layoutMode = computed(() => (collapsed.value ? 'Compact' : 'Comfortable'))

    function toggleCollapsed() {
      collapsed.value = !collapsed.value
    }

    function recordVisit() {
      visitCount.value += 1
    }

    return {
      collapsed,
      visitCount,
      layoutMode,
      toggleCollapsed,
      recordVisit,
    }
  },
  {
    persist: true,
  },
)
