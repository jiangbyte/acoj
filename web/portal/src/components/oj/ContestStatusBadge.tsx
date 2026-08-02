import { Tag } from 'antd'

const statusMeta: Record<string, { color: string; label: string }> = {
  SCHEDULED: { color: 'blue', label: '未开始' },
  RUNNING: { color: 'green', label: '进行中' },
  ENDED: { color: 'default', label: '已结束' },
  LOCKED: { color: 'orange', label: '已锁定' },
}

type Props = {
  status?: string | null
}

export function ContestStatusBadge({ status }: Props) {
  const meta = statusMeta[status ?? ''] ?? { color: 'default', label: status || '-' }
  return <Tag color={meta.color}>{meta.label}</Tag>
}
