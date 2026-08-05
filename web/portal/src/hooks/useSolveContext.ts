import { useCallback, useEffect, useMemo, useState } from 'react'
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { contestApi, learningPlanApi, problemApi, problemListApi } from '@/api'

export type SolveMode = 'bank' | 'list' | 'plan' | 'contest'

export type SolveItem = {
  id: string
  code?: string | null
  name?: string | null
  label?: string | null
  solved?: boolean
}

export type SolveContextValue = {
  mode: SolveMode
  title: string
  backTo: string
  currentId: string
  /** 当前可用于导航的题目（题库=当前页；题单/路径/竞赛=全量） */
  items: SolveItem[]
  /** 抽屉当前页展示的题目 */
  drawerItems: SolveItem[]
  loading: boolean
  prev: SolveItem | null
  next: SolveItem | null
  /** 抽屉分页（题库服务端；题单/路径客户端；竞赛不分页） */
  drawerPage: number
  drawerTotal: number
  drawerSize: number
  drawerPaginated: boolean
  setDrawerPage: (page: number) => void
  reload: () => void
  goTo: (problemId: string) => void
  goPrev: () => void
  goNext: () => void
  preserveSearch: string
}

const CONTEST_PROBLEM_PATTERN = /^\/contests\/([^/]+)\/problems\/([^/]+)/
/** 抽屉每页条数（小于题库总量时才能翻页；与题库列表默认页大小可不同） */
const DRAWER_PAGE_SIZE = 10

function buildPath(
  mode: SolveMode,
  problemId: string,
  opts: {
    contestId?: string
    listId?: string
    planId?: string
    search?: string
  },
) {
  if (mode === 'contest' && opts.contestId) {
    return `/contests/${opts.contestId}/problems/${problemId}`
  }
  const params = new URLSearchParams()
  if (opts.listId) params.set('list_id', opts.listId)
  if (opts.planId) params.set('plan_id', opts.planId)
  if (opts.search) {
    const incoming = new URLSearchParams(opts.search.startsWith('?') ? opts.search.slice(1) : opts.search)
    incoming.forEach((v, k) => {
      if (k === 'list_id' || k === 'plan_id') return
      if (!params.has(k)) params.set(k, v)
    })
  }
  const qs = params.toString()
  return qs ? `/problems/${problemId}?${qs}` : `/problems/${problemId}`
}

export function useSolveContext(currentProblemId: string): SolveContextValue {
  const navigate = useNavigate()
  const { pathname, search } = useLocation()
  const [searchParams] = useSearchParams()

  const contestMatch = pathname.match(CONTEST_PROBLEM_PATTERN)
  const contestId = contestMatch?.[1]
  const listId = searchParams.get('list_id') || undefined
  const planId = searchParams.get('plan_id') || undefined

  const mode: SolveMode = contestId ? 'contest' : listId ? 'list' : planId ? 'plan' : 'bank'
  const drawerPaginated = mode !== 'contest'

  const title =
    mode === 'contest' ? '竞赛题目' : mode === 'list' ? '题单题目' : mode === 'plan' ? '路径题目' : '题库'

  const backTo = contestId
    ? `/contests/${contestId}`
    : listId
      ? `/lists/detail?id=${listId}`
      : planId
        ? `/plans/detail?id=${planId}`
        : '/problems'

  const [items, setItems] = useState<SolveItem[]>([])
  const [loading, setLoading] = useState(false)
  const [drawerPage, setDrawerPage] = useState(1)
  const [bankTotal, setBankTotal] = useState(0)
  const drawerSize = DRAWER_PAGE_SIZE

  const load = useCallback(async () => {
    if (!currentProblemId && mode !== 'bank') return
    setLoading(true)
    try {
      if (mode === 'contest' && contestId) {
        const res = await contestApi.contestProblems(contestId)
        setItems(
          (res.data ?? []).map((p: any) => ({
            id: p.problem_id,
            code: p.problem_code,
            name: p.problem_name,
            label: p.label,
          })),
        )
        setBankTotal(0)
        return
      }
      if (mode === 'list' && listId) {
        const res = await problemListApi.detail(listId)
        setItems(
          (res.data?.problems ?? []).map((p: any) => ({
            id: p.id,
            code: p.code,
            name: p.name,
            solved: p.solved,
          })),
        )
        setBankTotal(0)
        return
      }
      if (mode === 'plan' && planId) {
        const res = await learningPlanApi.detail(planId)
        const flat =
          res.data?.sections?.flatMap((s: any) =>
            s.problems.map((p: any) => ({
              id: p.id,
              code: p.code,
              name: p.name,
              solved: p.solved,
            })),
          ) ?? []
        setItems(flat)
        setBankTotal(0)
        return
      }
      const res = await problemApi.problemPage({ current: drawerPage, size: drawerSize })
      setItems(
        (res.data?.records ?? []).map((p: any) => ({
          id: p.id,
          code: p.code,
          name: p.name,
          solved: p.solved,
        })),
      )
      setBankTotal(res.data?.total ?? 0)
    } catch {
      setItems([])
      setBankTotal(0)
    } finally {
      setLoading(false)
    }
    // 题库翻页才重新请求；题单/路径用本地切片，不把 drawerPage 放进依赖
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mode, contestId, listId, planId, drawerSize, currentProblemId, mode === 'bank' ? drawerPage : 0])

  useEffect(() => {
    void load()
  }, [load])

  // 切换入口时重置页码，再由下方 effect 对齐到当前题
  useEffect(() => {
    setDrawerPage(1)
  }, [mode, listId, planId, contestId])

  // 题单 / 路径：抽屉页跟随当前题，上下题切换后仍落在对应页
  useEffect(() => {
    if (mode !== 'list' && mode !== 'plan') return
    const idx = items.findIndex((item) => item.id === currentProblemId)
    if (idx < 0) return
    const target = Math.floor(idx / drawerSize) + 1
    setDrawerPage((prev) => (prev === target ? prev : target))
  }, [mode, items, currentProblemId, drawerSize])

  const drawerTotal = mode === 'bank' ? bankTotal : items.length

  const drawerItems = useMemo(() => {
    if (mode === 'bank' || mode === 'contest') return items
    const start = (drawerPage - 1) * drawerSize
    return items.slice(start, start + drawerSize)
  }, [mode, items, drawerPage, drawerSize])

  const index = useMemo(
    () => items.findIndex((item) => item.id === currentProblemId),
    [items, currentProblemId],
  )
  const prev = index > 0 ? items[index - 1] : null
  const next = index >= 0 && index < items.length - 1 ? items[index + 1] : null

  const goTo = useCallback(
    (problemId: string) => {
      navigate(
        buildPath(mode, problemId, {
          contestId,
          listId,
          planId,
          search: mode === 'bank' ? search : undefined,
        }),
      )
    },
    [navigate, mode, contestId, listId, planId, search],
  )

  const goPrev = useCallback(() => {
    if (prev) goTo(prev.id)
  }, [prev, goTo])

  const goNext = useCallback(() => {
    if (next) goTo(next.id)
  }, [next, goTo])

  const handleSetDrawerPage = useCallback(
    (page: number) => {
      setDrawerPage(page)
      // 题库翻页会触发重新请求；题单/路径只切本地切片
    },
    [],
  )

  return {
    mode,
    title,
    backTo,
    currentId: currentProblemId,
    items,
    drawerItems,
    loading,
    prev,
    next,
    drawerPage,
    drawerTotal,
    drawerSize,
    drawerPaginated,
    setDrawerPage: handleSetDrawerPage,
    reload: () => void load(),
    goTo,
    goPrev,
    goNext,
    preserveSearch: search,
  }
}
