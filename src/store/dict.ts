import { defineStore } from 'pinia'

export const useDictStore = defineStore('dict', {
  state: () => ({
    dictMap: {} as Record<string, any[]>,
  }),
  actions: {
    setDict(category: string, data: any[]) {
      this.dictMap[category] = data
    },
    getDict(category: string) {
      return this.dictMap[category] || []
    },
  },
})
