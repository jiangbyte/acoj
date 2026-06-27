export const dictStorageKey = 'dictTreeData'

export function setStoredDictTree(tree: any[]) {
  localStorage.setItem(dictStorageKey, JSON.stringify(tree))
}

export function getStoredDictTree() {
  const raw = localStorage.getItem(dictStorageKey)
  if (!raw) {
    return []
  }

  try {
    const data = JSON.parse(raw)
    return Array.isArray(data) ? data : []
  } catch {
    localStorage.removeItem(dictStorageKey)
    return []
  }
}

export function clearStoredDictTree() {
  localStorage.removeItem(dictStorageKey)
}

export async function refreshDict() {
  const { dictApi } = await import('@/api')
  const response = await dictApi.tree()
  const tree = response.data ?? []
  setStoredDictTree(tree)
  return tree
}

export function dictDataAll() {
  return getStoredDictTree()
}

export function dictTypeList(dictCode: string) {
  return findDictRoot(dictDataAll(), dictCode)?.children ?? []
}

export function dictList(dictCode: any) {
  return dictTypeList(dictCode)
    .filter(isEnabledDict)
    .map((item) => ({
      label: getDictLabel(item),
      value: getDictValue(item),
    }))
}

export function dictTypeData(dictCode: string, value?: string | number | null) {
  const dict = findDictItem(dictCode, value)
  return dict ? getDictLabel(dict) : ''
}

export function dictTypeColor(dictCode: string, value?: string | number | null) {
  const dict = findDictItem(dictCode, value)
  return dict?.color || ''
}

export function translateDictTree(dictCode: string, value?: string | number | null) {
  const root = findDictRoot(dictDataAll(), dictCode)
  const dict = findNodeByValue(root, value)
  return dict ? getDictLabel(dict) : ''
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

function findDictItem(dictCode: string, value?: string | number | null) {
  if (value === undefined || value === null || value === '') {
    return undefined
  }
  const normalizedValue = String(value)
  return dictTypeList(dictCode).find((item) => getDictValue(item) === normalizedValue)
}

function findDictRoot(tree: any[], dictCode: string) {
  return tree.find((item) => item.code === dictCode)
}

function findNodeByValue(node: any, value?: string | number | null): any {
  if (!node || value === undefined || value === null || value === '') {
    return undefined
  }

  if (getDictValue(node) === String(value)) {
    return node
  }

  for (const child of node.children ?? []) {
    const result = findNodeByValue(child, value)
    if (result) {
      return result
    }
  }

  return undefined
}
