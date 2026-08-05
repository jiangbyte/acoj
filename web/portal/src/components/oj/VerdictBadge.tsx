import { Tag } from 'antd'
import { useDict } from '@/hooks/useDict'
import { dictTypeColor, dictTypeData } from '@/utils/dict'

const RUNNING = new Set(['QUEUED', 'JUDGING', 'PENDING'])

type Props = {
  status?: string | null
  result?: string | null
  showText?: boolean
}

/**
 * 提交状态/结果徽章：文案与颜色均来自字典
 * SUBMISSION_STATUS / SUBMISSION_RESULT（与 admin 同一套 dict 工具）。
 */
export function VerdictBadge({ status, result, showText = true }: Props) {
  useDict()
  const s = (status || '').trim()
  const r = (result || '').trim()

  const useStatus = !r || RUNNING.has(s)
  const dictCode = useStatus ? 'SUBMISSION_STATUS' : 'SUBMISSION_RESULT'
  const value = useStatus ? s || r : r
  const label = dictTypeData(dictCode, value) || value || '-'
  const color = dictTypeColor(dictCode, value) || undefined

  if (!showText) {
    return <Tag color={color} />
  }
  return <Tag color={color}>{label}</Tag>
}
