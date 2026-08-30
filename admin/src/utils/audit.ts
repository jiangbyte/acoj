/** Author: Charlie — 操作审计字段展示（文案走字典/落库字段，不做本地映射表） */

import { accountTypeLabel } from '@/constants/account'
import { dictTypeData } from '@/utils/dict'

const AUDIT_ACTION_TYPE_DICT = 'AUDIT_ACTION_TYPE'

export function auditActionTypeLabel(type?: string | null) {
  if (!type) return '-'
  return dictTypeData(AUDIT_ACTION_TYPE_DICT, String(type)) || String(type)
}

export function auditModuleLabel(row: {
  module_label?: string | null
  module?: string | null
}) {
  return row.module_label || row.module || '-'
}

export function auditActionName(row: {
  action_name?: string | null
  action?: string | null
}) {
  return row.action_name || row.action || '-'
}

export function auditOperatorName(row: {
  operator_name?: string | null
  account_id?: string | null
  account_type?: string | null
}) {
  const nickname = String(row.operator_name ?? '').trim()
  const accountId = String(row.account_id ?? '').trim()
  const typeLabel = row.account_type ? accountTypeLabel(String(row.account_type).toUpperCase()) : ''

  const parts: string[] = []
  if (nickname) {
    parts.push(nickname)
  }
  if (accountId && accountId !== nickname) {
    parts.push(accountId)
  }
  const main = parts.length ? parts.join(' / ') : '-'
  return typeLabel ? `${main}（${typeLabel}）` : main
}

export function auditDurationText(ms?: number | string | null) {
  if (ms === undefined || ms === null || ms === '') return '-'
  const n = Number(ms)
  if (!Number.isFinite(n)) return '-'
  return `${n} ms`
}
