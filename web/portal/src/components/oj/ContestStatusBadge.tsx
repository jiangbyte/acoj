import { Tag } from 'antd'
import { useDict } from '@/hooks/useDict'
import { dictTypeColor, dictTypeData } from '@/utils/dict'

type Props = {
  status?: string | null
}

export function ContestStatusBadge({ status }: Props) {
  useDict()
  const value = status ?? ''
  const label = dictTypeData('CONTEST_LIFECYCLE_STATUS', value) || value || '-'
  const color = dictTypeColor('CONTEST_LIFECYCLE_STATUS', value) || undefined
  return <Tag color={color}>{label}</Tag>
}
