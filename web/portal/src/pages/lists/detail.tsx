import { useEffect, useMemo, useState, type MouseEvent } from 'react'
import { Button, Empty, Input, Modal, Progress, Spin, message } from 'antd'
import { CaretRightOutlined, CheckCircleFilled, DeleteOutlined, PlusOutlined } from '@ant-design/icons'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { problemListApi, type ProblemListItem } from '@/api/study'
import { ProblemBankSidebar } from '@/components/oj/ProblemBankSidebar'
import { ProblemSelector, type ProblemOption } from '@/components/selector/ProblemSelector'
import { useDict } from '@/hooks/useDict'
import { dictTypeData } from '@/utils/dict'
import { useAuthStore } from '@/stores/auth'

function diffClass(level: string) {
  if (level === 'Easy') return 'text-[var(--ant-color-diff-easy)]'
  if (level === 'Hard') return 'text-[var(--ant-color-diff-hard)]'
  return 'text-[var(--ant-color-diff-medium)]'
}

export function ProblemListDetailPage() {
  useDict()
  const navigate = useNavigate()
  const isLogin = useAuthStore((s) => s.isLogin)
  const [params] = useSearchParams()
  const id = params.get('id') ?? ''
  const [loading, setLoading] = useState(true)
  const [detail, setDetail] = useState<ProblemListItem | null>(null)
  const [keyword, setKeyword] = useState('')
  const [pickerOpen, setPickerOpen] = useState(false)
  const [adding, setAdding] = useState(false)

  async function load() {
    if (!id) return
    setLoading(true)
    try {
      const res = await problemListApi.detail(id)
      setDetail(res.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [id])

  const problems = useMemo(() => {
    const rows = detail?.problems ?? []
    const q = keyword.trim().toLowerCase()
    if (!q) return rows
    return rows.filter((p) => p.name.toLowerCase().includes(q) || p.code.toLowerCase().includes(q))
  }, [detail, keyword])

  const progress = detail?.progress
  const pct = progress?.total ? Math.round((progress.solved / progress.total) * 100) : 0
  const firstUnsolved = problems.find((p) => !p.solved)
  const isPersonal = Boolean(isLogin() && detail?.kind === 'PERSONAL')
  const isFavorites = Boolean(detail?.is_system)
  const canDeleteList = isPersonal && !isFavorites

  async function onDelete() {
    if (!detail || isFavorites) return
    Modal.confirm({
      title: '删除题单',
      content: `确定删除「${detail.title}」吗？题目本身不会被删除。`,
      okText: '删除',
      okButtonProps: { danger: true },
      onOk: async () => {
        await problemListApi.remove([detail.id])
        message.success('已删除')
        navigate('/problems')
      },
    })
  }

  async function onRemoveItem(problemId: string, e: MouseEvent) {
    e.preventDefault()
    e.stopPropagation()
    if (!detail) return
    await problemListApi.removeItem(detail.id, problemId)
    message.success(isFavorites ? '已取消收藏' : '已移出题单')
    await load()
  }

  const existingProblemOptions = useMemo<ProblemOption[]>(
    () =>
      (detail?.problems ?? []).map((p) => ({
        id: String(p.id),
        code: p.code || '',
        name: p.name || '',
      })),
    [detail],
  )

  async function onConfirmAddProblems(problems: ProblemOption[]) {
    if (!detail) return
    const existing = new Set(existingProblemOptions.map((p) => p.id))
    const toAdd = problems.filter((p) => !existing.has(p.id))
    if (!toAdd.length) {
      message.info(isFavorites ? '所选题目均已收藏' : '所选题目均已在题单中')
      return
    }
    setAdding(true)
    try {
      for (const p of toAdd) {
        if (isFavorites) {
          await problemListApi.addFavorite(p.id)
        } else {
          await problemListApi.addItem(detail.id, p.id)
        }
      }
      message.success(isFavorites ? `已收藏 ${toAdd.length} 题` : `已添加 ${toAdd.length} 题`)
      await load()
    } finally {
      setAdding(false)
    }
  }

  return (
    <div className="page-shell flex w-full gap-4">
      <ProblemBankSidebar active="lists" />
      <main className="min-w-0 flex-1">
        <Spin spinning={loading}>
          {!detail ? (
            <Empty description="题单不存在" />
          ) : (
            <div className="grid gap-4 xl:grid-cols-[320px_minmax(0,1fr)]">
              <div className="panel rounded-xl p-5">
                <div className="text-xl font-semibold">{detail.title}</div>
                <div className="muted-text mt-1 text-sm">
                  {detail.kind === 'OFFICIAL'
                    ? '官方题单'
                    : isFavorites
                      ? '个人收藏'
                      : '个人题单'}{' '}
                  · {detail.problem_count} 题
                </div>
                <Button
                  type="primary"
                  block
                  className="mt-4"
                  icon={<CaretRightOutlined />}
                  disabled={!firstUnsolved}
                  onClick={() =>
                    firstUnsolved && navigate(`/problems/${firstUnsolved.id}?list_id=${id}`)
                  }
                >
                  开始做题
                </Button>
                {canDeleteList ? (
                  <Button
                    block
                    danger
                    className="mt-2"
                    icon={<DeleteOutlined />}
                    onClick={() => void onDelete()}
                  >
                    删除题单
                  </Button>
                ) : null}
                <div className="mt-6">
                  <div className="mb-2 text-sm font-medium">进度</div>
                  <div className="flex items-center gap-4">
                    <Progress type="circle" percent={pct} size={96} format={() => `${progress?.solved ?? 0}/${progress?.total ?? 0}`} />
                    <div className="space-y-1 text-sm">
                      <div className="text-[var(--ant-color-diff-easy)]">
                        简单 {progress?.easy_solved ?? 0}/{progress?.easy_total ?? 0}
                      </div>
                      <div className="text-[var(--ant-color-diff-medium)]">
                        中等 {progress?.medium_solved ?? 0}/{progress?.medium_total ?? 0}
                      </div>
                      <div className="text-[var(--ant-color-diff-hard)]">
                        困难 {progress?.hard_solved ?? 0}/{progress?.hard_total ?? 0}
                      </div>
                    </div>
                  </div>
                  <div className="muted-text mt-2 text-xs">尝试中 {progress?.attempted ?? 0}</div>
                </div>
              </div>

              <div className="panel overflow-hidden rounded-xl">
                <div className="flex items-center gap-2 border-b border-[var(--ant-color-border)] px-4 py-3">
                  <Input
                    allowClear
                    placeholder="搜索题目"
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    className="max-w-md"
                  />
                  {isPersonal ? (
                    <Button
                      icon={<PlusOutlined />}
                      loading={adding}
                      onClick={() => setPickerOpen(true)}
                    >
                      {isFavorites ? '添加收藏' : '添加题目'}
                    </Button>
                  ) : null}
                </div>
                {problems.length ? (
                  problems.map((p, index) => (
                    <div
                      key={p.id}
                      className={`grid grid-cols-[36px_minmax(0,1fr)_88px_64px_36px] items-center gap-2 px-4 py-3.5 hover:bg-[var(--ant-color-fill-secondary)] ${
                        index % 2 ? 'bg-[var(--ant-color-fill-alter)]' : ''
                      }`}
                    >
                      <Link to={`/problems/${p.id}?list_id=${id}`} className="flex justify-center">
                        {p.solved ? (
                          <CheckCircleFilled className="text-[var(--ant-color-diff-easy)]" />
                        ) : (
                          <span className="inline-block h-3.5 w-3.5 rounded-full border border-[var(--ant-color-border-secondary)]" />
                        )}
                      </Link>
                      <Link to={`/problems/${p.id}?list_id=${id}`} className="min-w-0 truncate font-medium">
                        {p.code}. {p.name}
                      </Link>
                      <span className="text-right text-sm tabular-nums text-[var(--ant-color-text-secondary)]">
                        {Number(p.ac_rate || 0).toFixed(1)}%
                      </span>
                      <span className={`text-right text-sm ${diffClass(p.difficulty)}`}>
                        {dictTypeData('PROBLEM_DIFFICULTY', p.difficulty) || p.difficulty}
                      </span>
                      {isPersonal ? (
                        <button
                          type="button"
                          className="muted-text hover:text-[var(--ant-color-error)]"
                          title={isFavorites ? '取消收藏' : '移出题单'}
                          onClick={(e) => void onRemoveItem(p.id, e)}
                        >
                          <DeleteOutlined />
                        </button>
                      ) : (
                        <span />
                      )}
                    </div>
                  ))
                ) : (
                  <div className="py-16">
                    <Empty description={isFavorites ? '还没有收藏题目' : '暂无题目'} />
                  </div>
                )}
              </div>
            </div>
          )}
        </Spin>
      </main>

      <ProblemSelector
        open={pickerOpen}
        onClose={() => setPickerOpen(false)}
        mode="multiple"
        title={isFavorites ? '选择收藏题目' : '选择题目加入题单'}
        onConfirm={(problems) => void onConfirmAddProblems(problems)}
      />
    </div>
  )
}
