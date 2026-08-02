import { Tag } from 'antd'

const ACCEPTED = new Set(['AC', 'ACCEPTED'])
const RUNNING = new Set(['QUEUED', 'JUDGING', 'PENDING'])
const SYSTEM_ERRORS = new Set(['SE', 'IE', 'FAILED'])

const resultLabels: Record<string, string> = {
  AC: '通过',
  ACCEPTED: '通过',
  WA: '答案错误',
  TLE: '超时',
  MLE: '超内存',
  RE: '运行错误',
  CE: '编译错误',
  OLE: '输出超限',
  SE: '系统错误',
  IE: '内部错误',
  FAILED: '判题失败',
}

function verdictColor(status?: string | null, result?: string | null): string | undefined {
  const s = status || ''
  const r = result || ''
  if (ACCEPTED.has(r)) {
    return 'success'
  }
  if (RUNNING.has(s)) {
    return 'processing'
  }
  if (r || SYSTEM_ERRORS.has(s)) {
    return 'error'
  }
  return 'default'
}

function verdictText(status?: string | null, result?: string | null) {
  const s = status || ''
  const r = result || ''
  if (RUNNING.has(s)) {
    return s === 'QUEUED' ? '排队中' : '判题中'
  }
  if (resultLabels[r]) {
    return resultLabels[r]
  }
  if (SYSTEM_ERRORS.has(s)) {
    return resultLabels[s] ?? s
  }
  return s || '-'
}

type Props = {
  status?: string | null
  result?: string | null
  showText?: boolean
}

export function VerdictBadge({ status, result, showText = true }: Props) {
  const color = verdictColor(status, result)
  const label = verdictText(status, result)
  if (!showText) {
    return <Tag color={color} />
  }
  return <Tag color={color}>{label}</Tag>
}
