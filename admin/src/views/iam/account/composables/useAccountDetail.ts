/** Author: Charlie */

import { wireBool } from '@/utils/wire'

/** 账户详情中的布尔字段展示文案。 */
export function accountBoolLabel(value: unknown, defaultValue = false): string {
  return wireBool(value, defaultValue) ? '是' : '否'
}
