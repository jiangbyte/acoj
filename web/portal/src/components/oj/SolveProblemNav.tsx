import { LeftOutlined, RightOutlined } from '@ant-design/icons'
import { Button, Tooltip } from 'antd'
import { useOptionalSolveSession } from './SolveContext'

type Props = {
  compact?: boolean
}

/** 题单 / 路径 / 竞赛下的上下题切换（题库模式不展示） */
export function SolveProblemNav({ compact = false }: Props) {
  const ctx = useOptionalSolveSession()
  if (!ctx || ctx.mode === 'bank') return null

  const { prev, next, goPrev, goNext } = ctx

  const prevTitle = prev
    ? `上一题 ${prev.label || prev.code || ''} ${prev.name || ''}`.trim()
    : '没有上一题'
  const nextTitle = next
    ? `下一题 ${next.label || next.code || ''} ${next.name || ''}`.trim()
    : '没有下一题'

  if (compact) {
    return (
      <div className="flex items-center gap-1">
        <Tooltip title={prevTitle}>
          <Button
            type="text"
            size="small"
            disabled={!prev}
            icon={<LeftOutlined />}
            onClick={goPrev}
            aria-label="上一题"
          />
        </Tooltip>
        <Tooltip title={nextTitle}>
          <Button
            type="text"
            size="small"
            disabled={!next}
            icon={<RightOutlined />}
            onClick={goNext}
            aria-label="下一题"
          />
        </Tooltip>
      </div>
    )
  }

  return (
    <div className="mb-2 flex items-center gap-2">
      <Button size="small" disabled={!prev} icon={<LeftOutlined />} onClick={goPrev}>
        上一题
      </Button>
      <Button size="small" disabled={!next} onClick={goNext}>
        下一题
        <RightOutlined />
      </Button>
    </div>
  )
}
