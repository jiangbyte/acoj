export type DictCategory = 'SYS' | 'BIZ'
export type DictStatus = 'ENABLED' | 'DISABLED'

export interface SysDict {
  id: string
  code: string
  label?: string | null
  value?: string | null
  color?: string | null
  category?: DictCategory | null
  parent_id?: string | null
  status: DictStatus
  sort: number
  created_at: string
  updated_at: string
  children?: SysDict[]
}

export interface DictFormModel {
  id?: string
  code: string
  label: string
  value: string
  color: string | null
  category: DictCategory
  parent_id: string | null
  status: DictStatus
  sort: number
}
