import type { DictCategory, DictFormModel, DictStatus } from './types'

export const categoryOptions: Array<{ labelKey: string; value: DictCategory }> = [
  { labelKey: 'pages.sys.dict.categories.sys', value: 'SYS' },
  { labelKey: 'pages.sys.dict.categories.biz', value: 'BIZ' },
]

export const statusOptions: Array<{ labelKey: string; value: DictStatus }> = [
  { labelKey: 'common.often.enable', value: 'ENABLED' },
  { labelKey: 'common.often.disable', value: 'DISABLED' },
]

export const colorOptions = [
  'default',
  'success',
  'info',
  'warning',
  'error',
  'primary',
  'green',
  'blue',
  'orange',
  'red',
  'purple',
]

export const categoryLabelKeyMap: Record<string, string> = Object.fromEntries(
  categoryOptions.map((item) => [item.value, item.labelKey]),
)

export const statusLabelKeyMap: Record<string, string> = Object.fromEntries(
  statusOptions.map((item) => [item.value, item.labelKey]),
)

export const statusTagTypeMap: Record<string, 'success' | 'error' | 'default'> = {
  ENABLED: 'success',
  DISABLED: 'error',
}

export function createEmptyDictForm(category: DictCategory, parentId?: string | null): DictFormModel {
  return {
    code: '',
    label: '',
    value: '',
    color: 'default',
    category,
    parent_id: parentId ?? null,
    status: 'ENABLED',
    sort: 0,
  }
}
