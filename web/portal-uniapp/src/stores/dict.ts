import { defineStore } from 'pinia'
import { ref, shallowRef } from 'vue'
import { dictApi } from '@/api'
import { fallbackDicts, type OptionItem } from '@/config/dict'

export const dictTreeState = shallowRef<any[]>([])

function syncDictTreeState(tree: any[]) {
  dictTreeState.value = Array.isArray(tree) ? tree : []
}

export const useDictStore = defineStore('dict', () => {
  const tree = ref<any[]>([])
  const loaded = ref(false)
  const loading = ref(false)

  async function refreshDict() {
    if (loading.value) {
      return
    }
    loading.value = true
    try {
      tree.value = await dictApi.tree()
      syncDictTreeState(tree.value)
      loaded.value = true
    } finally {
      loading.value = false
    }
  }

  function options(dictCode?: string): OptionItem[] {
    if (!dictCode) {
      return []
    }
    const root = tree.value.find((item) => item.code === dictCode)
    const remote = (root?.children ?? [])
      .filter(
        (item: any) => item.status === undefined || item.status === 'ENABLED'
      )
      .map((item: any) => ({
        label: item.label || item.code,
        value: item.value || item.code,
      }))
    return remote.length ? remote : (fallbackDicts[dictCode] ?? [])
  }

  function label(dictCode: string | undefined, value: unknown) {
    if (!dictCode || value === null || value === undefined || value === '') {
      return ''
    }
    const item = options(dictCode).find(
      (option) => String(option.value) === String(value)
    )
    return item?.label ?? String(value)
  }

  function clearDict() {
    tree.value = []
    syncDictTreeState([])
    loaded.value = false
    loading.value = false
  }

  return {
    tree,
    loaded,
    loading,
    refreshDict,
    options,
    label,
    clearDict,
  }
})
