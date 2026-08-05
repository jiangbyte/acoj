import { useState } from 'react'
import {
  ArrowLeftOutlined,
  LeftOutlined,
  RightOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons'
import { Button, Tooltip } from 'antd'
import { useNavigate } from 'react-router-dom'
import { ThemeSwitch } from '@/layouts/components/header/ThemeSwitch'
import { UserCenter } from '@/layouts/components/header/UserCenter'
import { useSolveSession } from './SolveContext'
import { SolveProblemDrawer } from './SolveProblemDrawer'

export function SolveSidebar() {
  const navigate = useNavigate()
  const ctx = useSolveSession()
  const [drawerOpen, setDrawerOpen] = useState(false)

  return (
    <>
      <aside className="flex h-full w-14 shrink-0 flex-col items-center border-r border-[var(--ant-color-border)] bg-[var(--ant-color-bg-container)] py-3">
        <div className="flex shrink-0 flex-col items-center gap-2">
          <Tooltip title="返回" placement="right">
            <Button
              type="text"
              className="!h-10 !w-10 !px-0 !text-base"
              icon={<ArrowLeftOutlined />}
              aria-label="返回"
              onClick={() => navigate(ctx.backTo)}
            />
          </Tooltip>
          <Tooltip title={ctx.title} placement="right">
            <Button
              type="text"
              className="!h-10 !w-10 !px-0 !text-base"
              icon={<UnorderedListOutlined />}
              aria-label={ctx.title}
              onClick={() => setDrawerOpen(true)}
            />
          </Tooltip>
        </div>

        {ctx.mode !== 'bank' ? (
          <div className="mt-4 flex flex-1 flex-col items-center gap-2">
            <Tooltip
              title={ctx.prev ? `上一题 ${ctx.prev.label || ctx.prev.code || ''}` : '没有上一题'}
              placement="right"
            >
              <Button
                type="text"
                className="!h-10 !w-10 !px-0 !text-base"
                icon={<LeftOutlined />}
                disabled={!ctx.prev}
                aria-label="上一题"
                onClick={ctx.goPrev}
              />
            </Tooltip>
            <Tooltip
              title={ctx.next ? `下一题 ${ctx.next.label || ctx.next.code || ''}` : '没有下一题'}
              placement="right"
            >
              <Button
                type="text"
                className="!h-10 !w-10 !px-0 !text-base"
                icon={<RightOutlined />}
                disabled={!ctx.next}
                aria-label="下一题"
                onClick={ctx.goNext}
              />
            </Tooltip>
          </div>
        ) : (
          <div className="flex-1" />
        )}

        <div className="flex shrink-0 flex-col items-center gap-2">
          <ThemeSwitch placement="topLeft" />
          <UserCenter compact placement="topLeft" />
        </div>
      </aside>

      <SolveProblemDrawer open={drawerOpen} onClose={() => setDrawerOpen(false)} ctx={ctx} />
    </>
  )
}
