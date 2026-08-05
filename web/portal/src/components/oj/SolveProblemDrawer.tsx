import { CheckCircleFilled, UnorderedListOutlined } from '@ant-design/icons'
import { Drawer, Empty, Spin } from 'antd'
import type { SolveContextValue } from '@/hooks/useSolveContext'

type Props = {
  open: boolean
  onClose: () => void
  ctx: SolveContextValue
}

export function SolveProblemDrawer({ open, onClose, ctx }: Props) {
  const {
    title,
    drawerItems,
    loading,
    currentId,
    goTo,
    drawerPage,
    drawerTotal,
    drawerSize,
    drawerPaginated,
    setDrawerPage,
  } = ctx

  const offset = (drawerPage - 1) * drawerSize
  const totalPages = Math.max(1, Math.ceil(drawerTotal / drawerSize) || 1)

  const footer = drawerPaginated ? (
    <div className="flex items-center justify-between text-sm">
      <span className="text-[var(--ant-color-text-secondary)]">共 {drawerTotal} 题</span>
      <div className="flex items-center gap-2">
        <button
          type="button"
          disabled={drawerPage <= 1}
          onClick={() => setDrawerPage(drawerPage - 1)}
          className="rounded-lg px-3 py-1.5 ring-1 ring-[var(--ant-color-border)] disabled:opacity-40"
        >
          上一页
        </button>
        <span className="tabular-nums text-[var(--ant-color-text-secondary)]">
          {drawerPage} / {totalPages}
        </span>
        <button
          type="button"
          disabled={drawerPage >= totalPages}
          onClick={() => setDrawerPage(drawerPage + 1)}
          className="rounded-lg px-3 py-1.5 ring-1 ring-[var(--ant-color-border)] disabled:opacity-40"
        >
          下一页
        </button>
      </div>
    </div>
  ) : null

  return (
    <Drawer
      title={
        <span className="inline-flex items-center gap-2">
          <UnorderedListOutlined />
          {title}
        </span>
      }
      placement="left"
      width={360}
      open={open}
      onClose={onClose}
      footer={footer}
      styles={{ body: { padding: 0 } }}
    >
      <Spin spinning={loading}>
        {drawerItems.length ? (
          <div>
            {drawerItems.map((item, idx) => {
              const active = item.id === currentId
              const primary = item.label || item.code || String(offset + idx + 1)
              return (
                <button
                  key={`${item.id}-${offset + idx}`}
                  type="button"
                  onClick={() => {
                    goTo(item.id)
                    onClose()
                  }}
                  className={`flex w-full items-center gap-2 border-b border-[var(--ant-color-border)] px-4 py-3 text-left transition-colors ${
                    active
                      ? 'bg-[var(--ant-color-primary-bg)] text-[var(--ant-color-primary)]'
                      : 'hover:bg-[var(--ant-color-fill-quaternary)]'
                  }`}
                >
                  <span className="w-10 shrink-0 text-center text-sm font-medium tabular-nums">
                    {primary}
                  </span>
                  <span className="min-w-0 flex-1 truncate text-sm">
                    {item.name || item.code || item.id}
                  </span>
                  {item.solved ? (
                    <CheckCircleFilled className="shrink-0 text-[var(--ant-color-diff-easy)]" />
                  ) : null}
                </button>
              )
            })}
          </div>
        ) : (
          <div className="py-16">
            <Empty description="暂无题目" />
          </div>
        )}
      </Spin>
    </Drawer>
  )
}
