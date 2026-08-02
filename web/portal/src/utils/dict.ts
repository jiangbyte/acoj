const DICT_TREE_STORAGE_KEY = 'hei:portal:dict-tree'

let dictTree: any[] = []
let refreshDictPromise: Promise<void> | null = null

export function syncDictTree() {
  dictTree = readStoredDictTree()
  return dictTree
}

export function isDictLoaded() {
  return dictTree.length > 0 || syncDictTree().length > 0
}

export async function refreshDict() {
  if (refreshDictPromise) {
    return refreshDictPromise
  }

  refreshDictPromise = (async () => {
    try {
      const dictApi = await import('@/api/sys/dict')
      const response = await dictApi.tree()
      setDictTree(response.data ?? [])
    } finally {
      refreshDictPromise = null
    }
  })()

  return refreshDictPromise
}

/** Portal 字典公开可读：先同步本地缓存，再拉取最新（无需登录）。 */
export async function ensureDict() {
  syncDictTree()
  try {
    await refreshDict()
  } catch {
    // keep cached tree if network fails
  }
}

export function clearDict() {
  dictTree = []
  localStorage.removeItem(DICT_TREE_STORAGE_KEY)
}

export function dictDataAll() {
  return dictTree
}

export function dictTypeList(dictCode: string, tree = dictDataAll()) {
  return findDictRoot(tree, dictCode)?.children ?? []
}

export function dictList(dictCode: any, tree = dictDataAll()) {
  return dictTypeList(dictCode, tree)
    .filter(isEnabledDict)
    .map((item: any) => ({
      label: getDictLabel(item),
      value: getDictValue(item),
    }))
}

export function dictTypeData(
  dictCode: string,
  value?: string | number | null,
  tree = dictDataAll(),
) {
  const dict = findDictItem(dictCode, value, tree)
  return dict ? getDictLabel(dict) : ''
}

export function dictTypeColor(
  dictCode: string,
  value?: string | number | null,
  tree = dictDataAll(),
) {
  const dict = findDictItem(dictCode, value, tree)
  return dict?.color || ''
}

export function getDictValue(item: any) {
  return item.value || item.code
}

export function getDictLabel(item: any) {
  return item.label || item.code
}

export function isEnabledDict(item: any) {
  return item.status === undefined || item.status === null || item.status === 'ENABLED'
}

function findDictItem(dictCode: string, value?: string | number | null, tree = dictDataAll()) {
  if (value === undefined || value === null || value === '') {
    return undefined
  }
  const normalizedValue = String(value)
  return dictTypeList(dictCode, tree).find((item: any) => getDictValue(item) === normalizedValue)
}

function findDictRoot(tree: any[], dictCode: string) {
  return tree.find((item) => item.code === dictCode)
}

function setDictTree(tree: any[]) {
  dictTree = Array.isArray(tree) ? tree : []
  localStorage.setItem(DICT_TREE_STORAGE_KEY, JSON.stringify(dictTree))
}

function readStoredDictTree() {
  const raw = localStorage.getItem(DICT_TREE_STORAGE_KEY)
  if (!raw) {
    return []
  }
  try {
    const tree = JSON.parse(raw)
    return Array.isArray(tree) ? tree : []
  } catch {
    localStorage.removeItem(DICT_TREE_STORAGE_KEY)
    return []
  }
}

syncDictTree()
