import { useEffect, useState } from 'react'
import { Button, Checkbox, Empty, Spin, Tag } from 'antd'
import { ArrowLeftOutlined, CaretRightOutlined, CheckCircleFilled } from '@ant-design/icons'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { learningPlanApi, type LearningPlanItem } from '@/api/study'
import { ProblemBankSidebar } from '@/components/oj/ProblemBankSidebar'
import { useDict } from '@/hooks/useDict'
import { dictTypeData } from '@/utils/dict'

function diffClass(level: string) {
  if (level === 'Easy') return 'text-[var(--ant-color-diff-easy)]'
  if (level === 'Hard') return 'text-[var(--ant-color-diff-hard)]'
  return 'text-[var(--ant-color-diff-medium)]'
}

export function LearningPlanDetailPage() {
  useDict()
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const id = params.get('id') ?? ''
  const [loading, setLoading] = useState(true)
  const [plan, setPlan] = useState<LearningPlanItem | null>(null)
  const [showTags, setShowTags] = useState(false)

  useEffect(() => {
    if (!id) return
    void (async () => {
      setLoading(true)
      try {
        const res = await learningPlanApi.detail(id)
        setPlan(res.data)
      } finally {
        setLoading(false)
      }
    })()
  }, [id])

  const firstUnsolved = plan?.sections
    ?.flatMap((s) => s.problems)
    .find((p) => !p.solved)

  return (
    <div className="page-shell flex w-full gap-4">
      <ProblemBankSidebar active="plans" />
      <main className="min-w-0 flex-1">
        <Spin spinning={loading}>
          {!plan ? (
            <Empty description="路径不存在" />
          ) : (
            <div className="flex flex-col gap-4 xl:flex-row">
              <div className="min-w-0 flex-1">
                <div className="panel relative overflow-hidden rounded-lg">
                  <div className="relative flex items-start justify-between gap-4 px-5 py-4">
                    <Button type="text" icon={<ArrowLeftOutlined />} onClick={() => navigate('/plans')} />
                    <div className="min-w-0 flex-1 py-4 text-center">
                      <div className="muted-text text-sm">{plan.subtitle || '按章节循序练习'}</div>
                      <h1 className="mt-2 text-2xl font-semibold">{plan.title}</h1>
                      <Button
                        type="primary"
                        className="mt-5"
                        icon={<CaretRightOutlined />}
                        disabled={!firstUnsolved}
                        onClick={() =>
                          firstUnsolved && navigate(`/problems/${firstUnsolved.id}?plan_id=${id}`)
                        }
                      >
                        {plan.progress?.solved ? '继续练习' : '开始做题'}
                      </Button>
                    </div>
                    <div className="w-8 shrink-0" />
                  </div>
                </div>

                <div className="mt-4 flex justify-end">
                  <Checkbox checked={showTags} onChange={(e) => setShowTags(e.target.checked)}>
                    显示标签
                  </Checkbox>
                </div>

                <div className="panel mt-2 overflow-hidden rounded-xl">
                  {(plan.sections ?? []).map((section) => (
                    <div key={section.id}>
                      <div className="bg-[var(--ant-color-fill-alter)] px-4 py-2 text-sm font-medium">
                        {section.title}
                      </div>
                      {section.problems.map((p, idx) => (
                        <Link
                          key={p.id}
                          to={`/problems/${p.id}?plan_id=${id}`}
                          className={`flex items-center gap-3 px-4 py-3 hover:bg-[var(--ant-color-fill-secondary)] ${
                            idx % 2 ? 'bg-[var(--ant-color-fill-alter)]' : ''
                          }`}
                        >
                          {p.solved ? (
                            <CheckCircleFilled className="text-[var(--ant-color-diff-easy)]" />
                          ) : (
                            <span className="inline-block h-3.5 w-3.5 rounded-full border border-[var(--ant-color-border-secondary)]" />
                          )}
                          <span className="min-w-0 flex-1 truncate">{p.name}</span>
                          {showTags ? <Tag>{p.code}</Tag> : null}
                          <span className={`w-14 text-right text-sm ${diffClass(p.difficulty)}`}>
                            {dictTypeData('PROBLEM_DIFFICULTY', p.difficulty) || p.difficulty}
                          </span>
                        </Link>
                      ))}
                    </div>
                  ))}
                </div>
              </div>

              <aside className="w-full shrink-0 xl:w-[280px]">
                <div className="panel rounded-xl p-4">
                  <h3 className="mb-2 font-medium">概述</h3>
                  <div className="muted-text whitespace-pre-wrap text-sm">{plan.overview || '暂无概述'}</div>
                </div>
                {(plan.related ?? []).length ? (
                  <div className="panel mt-4 rounded-xl p-4">
                    <h3 className="mb-3 font-medium">相关路径</h3>
                    <div className="flex flex-col gap-3">
                      {plan.related!.map((r) => (
                        <Link key={r.id} to={`/plans/detail?id=${r.id}`} className="block hover:opacity-80">
                          <div className="text-sm font-medium">{r.title}</div>
                          <div className="muted-text text-xs">{r.subtitle || `${r.problem_count} 题`}</div>
                        </Link>
                      ))}
                    </div>
                  </div>
                ) : null}
              </aside>
            </div>
          )}
        </Spin>
      </main>
    </div>
  )
}
