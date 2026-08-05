import { useEffect, useMemo, useState } from 'react'
import { Empty, Spin } from 'antd'
import { Link } from 'react-router-dom'
import { learningPlanApi, type LearningPlanItem } from '@/api/study'
import { ProblemBankSidebar } from '@/components/oj/ProblemBankSidebar'

export function LearningPlanListPage() {
  const [loading, setLoading] = useState(true)
  const [plans, setPlans] = useState<LearningPlanItem[]>([])

  useEffect(() => {
    void (async () => {
      try {
        const res = await learningPlanApi.page({ current: 1, size: 50 })
        setPlans(res.data?.records ?? [])
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  const featured = useMemo(() => plans.filter((p) => p.category === 'FEATURED'), [plans])
  const interview = useMemo(() => plans.filter((p) => p.category === 'INTERVIEW'), [plans])

  return (
    <div className="page-shell flex w-full gap-4">
      <ProblemBankSidebar active="plans" />
      <main className="min-w-0 flex-1">
        <div className="mb-6">
          <h1 className="text-2xl font-semibold">练习路径</h1>
          <p className="muted-text mt-1 text-sm">按知识点与章节循序练习，巩固课程内容</p>
        </div>
        <Spin spinning={loading}>
          <section className="mb-8">
            <h2 className="mb-3 text-sm font-medium text-[var(--ant-color-text-secondary)]">推荐路径</h2>
            {featured.length ? (
              <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
                {featured.map((plan) => (
                  <Link
                    key={plan.id}
                    to={`/plans/detail?id=${plan.id}`}
                    className="panel block rounded-xl border-t-4 border-t-[var(--ant-color-primary)] p-4 transition-all hover:-translate-y-0.5 hover:shadow-[0_8px_20px_rgba(22,119,255,0.12)]"
                  >
                    <div className="text-base font-semibold">{plan.title}</div>
                    <div className="muted-text mt-2 line-clamp-2 text-sm">{plan.subtitle || plan.overview}</div>
                    <div className="mt-3 text-xs font-medium text-[var(--ant-color-primary)]">
                      {plan.problem_count} 题
                    </div>
                  </Link>
                ))}
              </div>
            ) : (
              <Empty description="暂无推荐路径" />
            )}
          </section>

          <section>
            <h2 className="mb-3 text-sm font-medium text-[var(--ant-color-text-secondary)]">进阶专题</h2>
            {interview.length ? (
              <div className="grid gap-3 md:grid-cols-2">
                {interview.map((plan) => (
                  <Link
                    key={plan.id}
                    to={`/plans/detail?id=${plan.id}`}
                    className="panel flex items-center gap-4 rounded-xl px-4 py-3 transition-all hover:-translate-y-0.5 hover:shadow-[0_6px_16px_rgba(22,119,255,0.1)]"
                  >
                    <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-[var(--ant-color-primary-bg)] text-xs font-semibold text-[var(--ant-color-primary)]">
                      专题
                    </div>
                    <div className="min-w-0 flex-1">
                      <div className="truncate font-medium">{plan.title}</div>
                      <div className="muted-text mt-0.5 truncate text-sm">
                        {plan.subtitle || `${plan.problem_count} 题`}
                      </div>
                    </div>
                  </Link>
                ))}
              </div>
            ) : (
              <Empty description="暂无进阶专题" />
            )}
          </section>
        </Spin>
      </main>
    </div>
  )
}
