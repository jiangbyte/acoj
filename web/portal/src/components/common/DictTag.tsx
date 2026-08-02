import { Tag } from 'antd'
import { dictTypeColor, dictTypeData } from '@/utils/dict'

type Props = {
  dictCode: string
  value?: string | number | null
}

export function DictTag({ dictCode, value }: Props) {
  const label = dictTypeData(dictCode, value)
  if (!label) {
    return <span>-</span>
  }
  const color = dictTypeColor(dictCode, value)
  return <Tag color={color || undefined}>{label}</Tag>
}
