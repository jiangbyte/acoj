import { defineStore } from 'pinia'
import { fetchDictTree } from '@/api/dict'

export const useDictStore = defineStore('dict', {
  state: () => ({
    /** Children indexed by type code, e.g. { "sex": [{ label, value, color }, ...] } */
    dictMap: {} as Record<string, any[]>,
    /** Raw tree data for re-indexing */
    treeData: [] as any[],
    /** Whether full tree has been loaded at least once */
    loaded: false,
  }),
  actions: {
    setDict(category: string, data: any[]) {
      this.dictMap[category] = data
    },
    getDict(category: string) {
      return this.dictMap[category] || []
    },

    /** Lazy load: skipped if already loaded */
    async loadDict() {
      if (this.loaded) return
      await this.refreshDict()
      this.loaded = true
    },

    /** Force full reload from API (call after CRUD in dict management) */
    async refreshDict() {
      const { data } = await fetchDictTree({})
      if (data) {
        this.treeData = data
        this._buildIndex(data)
      }
    },

    /** Walk tree and index children by type code */
    _buildIndex(tree: any[]) {
      for (const node of tree) {
        if (node.code && node.children?.length) {
          this.dictMap[node.code] = node.children.map((c: any) => ({
            label: c.label,
            value: c.value,
            color: c.color,
            status: c.status,
          }))
        }
        if (node.children?.length) {
          this._buildIndex(node.children)
        }
      }
    },

    /** Get items for a type code (reactively reads from dictMap) */
    getDictItems(typeCode: string): any[] {
      if (this.dictMap[typeCode]) return this.dictMap[typeCode]
      // Lazy re-index if tree is cached but index was missed
      if (this.treeData.length) {
        this._buildIndex(this.treeData)
      }
      return this.dictMap[typeCode] || []
    },

    /** Translate value → label for a dict type (回显) */
    dictLabel(typeCode: string, value: string): string {
      const items = this.getDictItems(typeCode)
      const item = items.find((i: any) => i.value === value)
      return item?.label ?? value
    },

    /** Get color for a dict type + value */
    dictColor(typeCode: string, value: string): string {
      const items = this.getDictItems(typeCode)
      const item = items.find((i: any) => i.value === value)
      return item?.color ?? 'default'
    },
  },
})
