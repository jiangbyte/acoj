import { useDictStore } from '@/store'

/**
 * 字典工具
 *
 * 在模板中直接使用:
 *   {{ dictLabel('sex', record.sex) }}
 *
 * 或通过全局 $dict:
 *   {{ $dict.label('sex', record.sex) }}
 *
 * 注: 首次调用前需要先执行 useDictStore().loadDict()（DictSelect 已自动触发）
 */

/** 根据字典 typeCode 和 value 获取显示标签（回显） */
export function dictLabel(typeCode: string, value: string): string {
  return useDictStore().dictLabel(typeCode, value)
}

/** 根据字典 typeCode 和 value 获取颜色 */
export function dictColor(typeCode: string, value: string): string {
  return useDictStore().dictColor(typeCode, value)
}

/** 获取某个字典类型下的所有子项列表（用于 DictSelect 等） */
export function getDictItems(typeCode: string): any[] {
  return useDictStore().getDictItems(typeCode)
}
