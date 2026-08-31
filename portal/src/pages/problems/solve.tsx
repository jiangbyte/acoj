/** Author: Charlie */

import { useCallback, useEffect, useMemo, useRef, useState, type MouseEvent } from 'react'
import {
  Button,
  Collapse,
  Empty,
  Flex,
  Input,
  Modal,
  Popover,
  Select,
  Space,
  Spin,
  Table,
  Tag,
  Typography,
  message,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import {
  ArrowLeftOutlined,
  CaretRightOutlined,
  CheckCircleFilled,
  ClockCircleOutlined,
  DatabaseOutlined,
  PlusOutlined,
  SendOutlined,
  TagsOutlined,
} from '@ant-design/icons'
import { Link, useLocation, useParams } from 'react-router-dom'
import { ojMetaApi, ojProblemApi, ojSubmissionApi } from '@/api'
import { CodeBlock } from '@/components/editor/CodeBlock'
import { CodeEditor } from '@/components/editor/CodeEditor'
import { MdPreview } from '@/components/editor/MdPreview'
import { useAuthStore } from '@/stores/auth'
import {
  difficultyColor,
  difficultyLabel,
  draftStorageKey,
  editorThemeStorageKey,
  formatMemory,
  formatTimeMs,
  isJudgingStatus,
  languageStorageKey,
  mapOjLanguageToMonaco,
  ojLanguageLabel,
  type EditorThemeMode,
} from '@/utils'
import { formatDateTime } from '@/utils/time'

type LeftTab = 'description' | 'submissions'
type BottomTab = 'cases' | 'test-result'
type SubmissionView = 'list' | 'detail'
type SubmissionRow = Record<string, any>
type TestCase = { input: string; output?: string }

const POLL_MS = 2000
const MAX_TEST_CASES = 5

const submissionStatusFilterOptions = [
  { label: '所有状态', value: '' },
  { label: 'AC', value: 'AC' },
  { label: 'WA', value: 'WA' },
  { label: 'TLE', value: 'TLE' },
  { label: 'MLE', value: 'MLE' },
  { label: 'OLE', value: 'OLE' },
  { label: 'RE', value: 'RE' },
  { label: 'CE', value: 'CE' },
  { label: 'SE', value: 'SE' },
  { label: 'PENDING', value: 'PENDING' },
  { label: 'JUDGING', value: 'JUDGING' },
]

const editorThemeOptions: { label: string; value: EditorThemeMode }[] = [
  { label: '跟随系统', value: 'auto' },
  { label: '浅色', value: 'vs' },
  { label: '深色', value: 'vs-dark' },
]

function sameSubmissionSnapshot(a: SubmissionRow | null | undefined, b: SubmissionRow | null | undefined) {
  if (a === b) return true
  if (!a || !b) return false
  return (
    a.id === b.id
    && a.status === b.status
    && a.score === b.score
    && a.time_ms === b.time_ms
    && a.memory_bytes === b.memory_bytes
    && a.judge_message === b.judge_message
    && a.compile_output === b.compile_output
  )
}

function samplesToCases(samples: unknown): TestCase[] {
  if (!Array.isArray(samples) || !samples.length) {
    return [{ input: '' }]
  }
  return samples.slice(0, MAX_TEST_CASES).map((sample: any) => ({
    input: String(sample?.input ?? ''),
    output: sample?.output == null ? '' : String(sample.output),
  }))
}

export function ProblemSolvePage() {
  const { id = '' } = useParams()
  const location = useLocation()
  const loggedIn = useAuthStore((s) => s.isLogin())
  const loginRedirect = `/auth/login?redirect=${encodeURIComponent(`${location.pathname}${location.search}`)}`

  const [loading, setLoading] = useState(true)
  const [problem, setProblem] = useState<any>(null)
  const [clusterLanguages, setClusterLanguages] = useState<string[]>([])
  const [language, setLanguage] = useState('')
  const [sourceCode, setSourceCode] = useState('')
  const [editorTheme, setEditorTheme] = useState<EditorThemeMode>(() => {
    const saved = localStorage.getItem(editorThemeStorageKey()) as EditorThemeMode | null
    return saved === 'vs' || saved === 'vs-dark' || saved === 'auto' ? saved : 'auto'
  })
  const [leftTab, setLeftTab] = useState<LeftTab>('description')
  const [bottomTab, setBottomTab] = useState<BottomTab>('cases')
  const [submissionView, setSubmissionView] = useState<SubmissionView>('list')
  const [codeExpanded, setCodeExpanded] = useState(false)
  const [noteDraft, setNoteDraft] = useState('')
  const [noteSaving, setNoteSaving] = useState(false)
  const [quickNote, setQuickNote] = useState<{ id: string; note: string } | null>(null)
  const [leftWidth, setLeftWidth] = useState(44)
  const [bottomHeight, setBottomHeight] = useState(240)
  const [submitting, setSubmitting] = useState(false)
  const [testing, setTesting] = useState(false)
  const [activeSubmission, setActiveSubmission] = useState<SubmissionRow | null>(null)
  const [submissionRows, setSubmissionRows] = useState<SubmissionRow[]>([])
  const [submissionTotal, setSubmissionTotal] = useState(0)
  const [submissionPage, setSubmissionPage] = useState({ current: 1, size: 20 })
  const [submissionStatusFilter, setSubmissionStatusFilter] = useState('')
  const [submissionLanguageFilter, setSubmissionLanguageFilter] = useState('')
  const [submissionBootstrapping, setSubmissionBootstrapping] = useState(false)
  const [selectedSubmissionId, setSelectedSubmissionId] = useState<string | null>(null)
  const submissionPageRef = useRef(submissionPage)
  submissionPageRef.current = submissionPage
  const submissionFiltersRef = useRef({ status: '', language: '' })
  submissionFiltersRef.current = {
    status: submissionStatusFilter,
    language: submissionLanguageFilter,
  }
  const [testCases, setTestCases] = useState<TestCase[]>([{ input: '' }])
  const [activeCaseIndex, setActiveCaseIndex] = useState(0)
  const [testResult, setTestResult] = useState<any>(null)
  const [resultCaseIndex, setResultCaseIndex] = useState(0)
  const pollTimerRef = useRef<number | null>(null)
  const pollSeqRef = useRef(0)
  const selectedIdRef = useRef<string | null>(null)
  const submissionsBootstrappedRef = useRef(false)
  const dragKindRef = useRef<'col' | 'row' | null>(null)

  selectedIdRef.current = selectedSubmissionId

  const languageOptions = useMemo(() => {
    const limits = Array.isArray(problem?.language_limits) ? problem.language_limits : []
    const allowed = limits
      .map((item: any) => String(item?.language ?? '').trim())
      .filter(Boolean)
    const cluster = new Set(clusterLanguages.map((item) => item.toLowerCase()))
    const filtered = allowed.length
      ? allowed.filter((item: string) => !cluster.size || cluster.has(item.toLowerCase()))
      : clusterLanguages
    return filtered.map((item: string) => ({
      label: ojLanguageLabel(item),
      value: item,
    }))
  }, [problem, clusterLanguages])

  const submissionLanguageOptions = useMemo(() => {
    const fromRows = Array.from(
      new Set(submissionRows.map((row) => String(row.language || '').trim()).filter(Boolean)),
    )
    const fromProblem = languageOptions.map((item) => String(item.value))
    const keys = Array.from(new Set([...fromProblem, ...fromRows]))
    return [
      { label: '所有语言', value: '' },
      ...keys.map((key) => ({ label: ojLanguageLabel(key), value: key })),
    ]
  }, [languageOptions, submissionRows])

  const stopPoll = useCallback(() => {
    pollSeqRef.current += 1
    if (pollTimerRef.current != null) {
      window.clearTimeout(pollTimerRef.current)
      pollTimerRef.current = null
    }
  }, [])

  const applySubmissionDetail = useCallback((data: SubmissionRow | null | undefined) => {
    if (!data) return
    setActiveSubmission((prev) => (sameSubmissionSnapshot(prev, data) ? prev : data))
    setSubmissionRows((rows) => {
      const idx = rows.findIndex((row) => row.id === data.id)
      if (idx < 0) {
        return [data, ...rows]
      }
      const current = rows[idx]
      if (
        current.status === data.status
        && current.score === data.score
        && current.time_ms === data.time_ms
      ) {
        return rows
      }
      const next = rows.slice()
      next[idx] = { ...current, ...data }
      return next
    })
  }, [])

  const startPoll = useCallback(
    (submissionId: string) => {
      stopPoll()
      const seq = pollSeqRef.current
      const tick = async () => {
        if (pollSeqRef.current !== seq) return
        try {
          const res = await ojSubmissionApi.detail({ id: submissionId })
          if (pollSeqRef.current !== seq) return
          const data = res.data
          applySubmissionDetail(data)
          if (isJudgingStatus(data?.status)) {
            pollTimerRef.current = window.setTimeout(() => {
              void tick()
            }, POLL_MS)
          } else {
            pollTimerRef.current = null
          }
        } catch {
          if (pollSeqRef.current === seq) {
            pollTimerRef.current = null
          }
        }
      }
      pollTimerRef.current = window.setTimeout(() => {
        void tick()
      }, POLL_MS)
    },
    [applySubmissionDetail, stopPoll],
  )

  const loadSubmissions = useCallback(
    async (opts?: {
      focusId?: string | null
      selectDetail?: boolean
      current?: number
      size?: number
      status?: string
      language?: string
    }) => {
      if (!id || !loggedIn) return
      const firstLoad = !submissionsBootstrappedRef.current
      if (firstLoad) setSubmissionBootstrapping(true)
      const current = opts?.current ?? submissionPageRef.current.current
      const size = opts?.size ?? submissionPageRef.current.size
      const status = opts?.status ?? submissionFiltersRef.current.status
      const language = opts?.language ?? submissionFiltersRef.current.language
      try {
        const res = await ojSubmissionApi.page({
          problem_id: id,
          current,
          size,
          status: status || undefined,
          language: language || undefined,
        })
        const rows = (res.data?.records ?? []) as SubmissionRow[]
        const total = Number(res.data?.total ?? 0)
        setSubmissionRows(rows)
        setSubmissionTotal(total)
        setSubmissionPage({ current, size })
        submissionsBootstrappedRef.current = true

        if (opts?.selectDetail === false) return

        const focusId = opts?.focusId ?? selectedIdRef.current
        const stillOnPage = focusId ? rows.some((row) => row.id === focusId) : false
        const nextFocusId = stillOnPage ? focusId : rows[0]?.id
        if (!nextFocusId) {
          setSelectedSubmissionId(null)
          setActiveSubmission(null)
          stopPoll()
          return
        }
        setSelectedSubmissionId(nextFocusId)
        const detailRes = await ojSubmissionApi.detail({ id: nextFocusId })
        applySubmissionDetail(detailRes.data)
        if (isJudgingStatus(detailRes.data?.status)) {
          startPoll(nextFocusId)
        } else {
          stopPoll()
        }
      } catch {
        if (firstLoad) {
          setSubmissionRows([])
          setSubmissionTotal(0)
        }
      } finally {
        if (firstLoad) setSubmissionBootstrapping(false)
      }
    },
    [applySubmissionDetail, id, loggedIn, startPoll, stopPoll],
  )

  useEffect(() => {
    let mounted = true
    async function load() {
      if (!id) return
      setLoading(true)
      submissionsBootstrappedRef.current = false
      setSubmissionPage({ current: 1, size: 20 })
      setSubmissionTotal(0)
      setSubmissionRows([])
      try {
        const [detailRes, langRes] = await Promise.all([
          ojProblemApi.detail({ id }),
          ojMetaApi.languages().catch(() => ({ data: { languages: [] } })),
        ])
        if (!mounted) return
        const data = detailRes.data
        setProblem(data)
        setTestCases(samplesToCases(data?.samples))
        setActiveCaseIndex(0)
        setTestResult(null)
        const langs = Array.isArray(langRes.data?.languages) ? langRes.data.languages : []
        setClusterLanguages(langs.map((item: unknown) => String(item)))

        const allowed = (Array.isArray(data?.language_limits) ? data.language_limits : [])
          .map((item: any) => String(item?.language ?? '').trim())
          .filter(Boolean)
        const savedLang = localStorage.getItem(languageStorageKey(id)) || ''
        const nextLang =
          (savedLang && allowed.includes(savedLang) && savedLang) ||
          allowed[0] ||
          langs[0] ||
          ''
        setLanguage(nextLang)
        const draft = nextLang ? localStorage.getItem(draftStorageKey(id, nextLang)) : ''
        setSourceCode(draft || '')
      } catch {
        if (mounted) setProblem(null)
      } finally {
        if (mounted) setLoading(false)
      }
    }
    void load()
    return () => {
      mounted = false
      stopPoll()
    }
  }, [id, stopPoll])

  useEffect(() => {
    if (leftTab !== 'submissions') return
    if (!loggedIn) return
    void loadSubmissions({
      selectDetail: false,
    })
    // eslint-disable-next-line react-hooks/exhaustive-deps -- intentional
  }, [leftTab, id, loggedIn])

  useEffect(() => {
    setNoteDraft(activeSubmission?.note ? String(activeSubmission.note) : '')
    setCodeExpanded(false)
  }, [activeSubmission?.id, activeSubmission?.note])

  const languageRef = useRef(language)
  languageRef.current = language

  useEffect(() => {
    if (!id || !language) return
    localStorage.setItem(languageStorageKey(id), language)
    const draft = localStorage.getItem(draftStorageKey(id, language))
    setSourceCode(draft || '')
  }, [id, language])

  useEffect(() => {
    localStorage.setItem(editorThemeStorageKey(), editorTheme)
  }, [editorTheme])

  const handleCodeChange = useCallback(
    (value: string) => {
      setSourceCode(value)
      if (id && languageRef.current) {
        localStorage.setItem(draftStorageKey(id, languageRef.current), value)
      }
    },
    [id],
  )

  useEffect(() => {
    function onMove(event: MouseEvent) {
      if (!dragKindRef.current) return
      if (dragKindRef.current === 'col') {
        const next = (event.clientX / window.innerWidth) * 100
        setLeftWidth(Math.min(70, Math.max(28, next)))
      } else {
        const next = window.innerHeight - event.clientY
        setBottomHeight(Math.min(420, Math.max(140, next)))
      }
    }
    function onUp() {
      dragKindRef.current = null
    }
    window.addEventListener('mousemove', onMove)
    window.addEventListener('mouseup', onUp)
    return () => {
      window.removeEventListener('mousemove', onMove)
      window.removeEventListener('mouseup', onUp)
    }
  }, [])

  function requireLoginAction(): boolean {
    if (loggedIn) return true
    message.warning('请先登录后再运行或提交')
    return false
  }

  async function handleTestRun() {
    if (!id) return
    if (!requireLoginAction()) return
    if (!language) {
      message.warning('请选择语言')
      return
    }
    if (!sourceCode.trim()) {
      message.warning('请输入代码')
      return
    }
    if (!testCases.length) {
      message.warning('请至少保留一组测试用例')
      return
    }
    setTesting(true)
    setBottomTab('test-result')
    try {
      const res = await ojProblemApi.run({
        problem_id: id,
        language,
        source_code: sourceCode,
        cases: testCases.map((item) => ({
          input: item.input,
          output: item.output != null && item.output.length > 0 ? item.output : undefined,
        })),
      })
      setTestResult(res.data)
      setResultCaseIndex(0)
      message.success('测试完成')
    } catch (err: any) {
      message.error(err?.message || '测试失败')
    } finally {
      setTesting(false)
    }
  }

  async function handleSubmit() {
    if (!id) return
    if (!requireLoginAction()) return
    if (!language) {
      message.warning('请选择语言')
      return
    }
    if (!sourceCode.trim()) {
      message.warning('请输入代码')
      return
    }
    setSubmitting(true)
    try {
      const res = await ojSubmissionApi.create({
        problem_id: id,
        language,
        source_code: sourceCode,
      })
      const created = res.data
      if (created?.id) {
        setSelectedSubmissionId(created.id)
        applySubmissionDetail(created)
        setLeftTab('submissions')
        setSubmissionView('detail')
        startPoll(created.id)
        void loadSubmissions({
          focusId: created.id,
          selectDetail: false,
          current: 1,
          size: submissionPageRef.current.size,
        })
      } else {
        setActiveSubmission(created)
        setLeftTab('submissions')
        setSubmissionView('detail')
      }
      message.success('已提交，判题中')
    } finally {
      setSubmitting(false)
    }
  }

  async function openSubmission(row: SubmissionRow) {
    if (selectedSubmissionId === row.id && activeSubmission?.id === row.id && submissionView === 'detail') {
      return
    }
    setSelectedSubmissionId(row.id)
    setLeftTab('submissions')
    setSubmissionView('detail')
    setActiveSubmission((prev) => (prev?.id === row.id ? prev : { ...row }))
    try {
      const res = await ojSubmissionApi.detail({ id: row.id })
      applySubmissionDetail(res.data)
      if (isJudgingStatus(res.data?.status)) {
        startPoll(row.id)
      } else {
        stopPoll()
      }
    } catch {
      message.error('加载提交详情失败')
    }
  }

  function updateCaseInput(index: number, input: string) {
    setTestCases((prev) => {
      const next = prev.slice()
      next[index] = { ...next[index], input }
      return next
    })
  }

  function updateCaseOutput(index: number, output: string) {
    setTestCases((prev) => {
      const next = prev.slice()
      next[index] = { ...next[index], output }
      return next
    })
  }

  function addCase() {
    if (testCases.length >= MAX_TEST_CASES) {
      message.warning(`最多 ${MAX_TEST_CASES} 组用例`)
      return
    }
    setTestCases((prev) => [...prev, { input: '' }])
    setActiveCaseIndex(testCases.length)
  }

  function removeCase(index: number) {
    if (testCases.length <= 1) {
      message.warning('至少保留一组用例')
      return
    }
    setTestCases((prev) => prev.filter((_, i) => i !== index))
    setActiveCaseIndex((prev) => {
      if (index < prev) return prev - 1
      if (index === prev) return Math.max(0, Math.min(prev, testCases.length - 2))
      return prev
    })
  }

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center p-8">
        <Spin size="large" />
      </div>
    )
  }

  if (!problem) {
    return (
      <div className="flex h-full flex-col items-center justify-center gap-4">
        <Empty description="题目不存在或未发布" />
        <Link to="/problems">
          <Button type="primary">返回题库</Button>
        </Link>
      </div>
    )
  }

  const tags = Array.isArray(problem.tags) ? problem.tags : []
  const busy = submitting || testing
  const activeCase = testCases[activeCaseIndex] ?? testCases[0]
  const solved = problem.my_status === 'ACCEPTED'

  async function saveNoteForSubmission(submissionId: string, note: string) {
    const next = note.trim()
    setNoteSaving(true)
    try {
      const res = await ojSubmissionApi.updateNote({
        id: submissionId,
        note: next,
      })
      const nextNote = res.data?.note ?? (next || null)
      setSubmissionRows((rows) =>
        rows.map((row) => (row.id === submissionId ? { ...row, note: nextNote } : row)),
      )
      setActiveSubmission((current) =>
        current?.id === submissionId ? { ...current, note: nextNote } : current,
      )
      if (activeSubmission?.id === submissionId) {
        setNoteDraft(nextNote ? String(nextNote) : '')
      }
      message.success('备注已保存')
      return true
    } catch (err: any) {
      message.error(err?.message || '保存失败')
      return false
    } finally {
      setNoteSaving(false)
    }
  }

  async function saveDetailNote() {
    if (!activeSubmission?.id) return
    const prev = activeSubmission.note ? String(activeSubmission.note) : ''
    if (noteDraft.trim() === prev) return
    await saveNoteForSubmission(activeSubmission.id, noteDraft)
  }

  function resetDetailNote() {
    setNoteDraft(activeSubmission?.note ? String(activeSubmission.note) : '')
  }

  async function saveQuickNote() {
    if (!quickNote?.id) return
    const row = submissionRows.find((item) => item.id === quickNote.id)
    const prev = row?.note ? String(row.note) : ''
    if (quickNote.note.trim() === prev) {
      setQuickNote(null)
      return
    }
    const ok = await saveNoteForSubmission(quickNote.id, quickNote.note)
    if (ok) setQuickNote(null)
  }

  function openQuickNote(row: SubmissionRow, e?: MouseEvent) {
    e?.stopPropagation()
    setQuickNote({ id: row.id, note: row.note ? String(row.note) : '' })
  }

  const submissionTableScrollX = 560
  const submissionColumns: ColumnsType<SubmissionRow> = [
    {
      title: (
        <Select
          variant="borderless"
          size="small"
          className="w-full"
          options={submissionStatusFilterOptions}
          value={submissionStatusFilter}
          onClick={(e) => e.stopPropagation()}
          onChange={(value) => {
            setSubmissionStatusFilter(value)
            void loadSubmissions({ current: 1, selectDetail: false, status: value })
          }}
        />
      ),
      key: 'status',
      width: 150,
      render: (_value, row) => (
        <div>
          <Typography.Text
            strong
            type={
              row.status === 'AC'
                ? 'success'
                : row.status === 'PENDING' || row.status === 'JUDGING'
                  ? 'secondary'
                  : 'danger'
            }
          >
            {row.status || '—'}
          </Typography.Text>
          <div className="muted-text text-xs">{formatDateTime(row.created_at)}</div>
        </div>
      ),
    },
    {
      title: (
        <Select
          variant="borderless"
          size="small"
          className="w-full"
          options={submissionLanguageOptions}
          value={submissionLanguageFilter}
          onClick={(e) => e.stopPropagation()}
          onChange={(value) => {
            setSubmissionLanguageFilter(value)
            void loadSubmissions({ current: 1, selectDetail: false, language: value })
          }}
        />
      ),
      dataIndex: 'language',
      key: 'language',
      width: 100,
      render: (value: string) => <Tag className="m-0">{ojLanguageLabel(value)}</Tag>,
    },
    {
      title: '执行用时',
      dataIndex: 'time_ms',
      key: 'time_ms',
      width: 100,
      render: (value: number) => (
        <Space size={4}>
          <ClockCircleOutlined className="muted-text" />
          <span>{formatTimeMs(value)}</span>
        </Space>
      ),
    },
    {
      title: '消耗内存',
      dataIndex: 'memory_bytes',
      key: 'memory_bytes',
      width: 110,
      render: (value: number) => (
        <Space size={4}>
          <DatabaseOutlined className="muted-text" />
          <span>{formatMemory(value)}</span>
        </Space>
      ),
    },
    {
      title: '备注',
      dataIndex: 'note',
      key: 'note',
      ellipsis: true,
      render: (value: string, row) =>
        value ? (
          <Button
            type="link"
            size="small"
            className="max-w-full truncate px-0"
            onClick={(e) => openQuickNote(row, e)}
          >
            {value}
          </Button>
        ) : (
          <Button
            type="link"
            size="small"
            className="px-0 opacity-0 transition-opacity group-hover:opacity-100"
            icon={<PlusOutlined />}
            onClick={(e) => openQuickNote(row, e)}
          >
            备注
          </Button>
        ),
    },
  ]

  return (
    <div className="flex h-full flex-col overflow-hidden">
      <header className="toolbar flex h-12 shrink-0 items-center justify-between gap-3 px-3">
        <div className="flex min-w-0 items-center gap-3">
          <Link
            to="/problems"
            className="inline-flex items-center gap-1 text-sm text-[var(--ant-color-text-secondary)] hover:text-[var(--ant-color-text)]"
          >
            <ArrowLeftOutlined />
            题库
          </Link>
          <div className="min-w-0 truncate text-sm font-medium">
            <span className="font-mono">{problem.problem_key}</span>
            <span className="mx-2 text-[var(--ant-color-border)]">|</span>
            {problem.title}
          </div>
        </div>
        <Space>
          <Button
            icon={<CaretRightOutlined />}
            loading={testing}
            disabled={busy && !testing}
            onClick={() => void handleTestRun()}
          >
            测试提交
          </Button>
          <Button
            type="primary"
            icon={<SendOutlined />}
            loading={submitting}
            disabled={busy && !submitting}
            onClick={() => void handleSubmit()}
          >
            正式提交
          </Button>
        </Space>
      </header>

      <div className="flex min-h-0 flex-1 overflow-hidden">
        <aside className="flex min-h-0 flex-col overflow-hidden" style={{ width: `${leftWidth}%` }}>
          <div className="tabs-bar flex shrink-0 gap-1 px-2">
            <button
              type="button"
              className={`tabs-btn rounded-t px-3 py-2 text-sm ${leftTab === 'description' ? 'tabs-btn-active font-medium' : ''}`}
              onClick={() => setLeftTab('description')}
            >
              题目描述
            </button>
            <button
              type="button"
              className={`tabs-btn rounded-t px-3 py-2 text-sm ${leftTab === 'submissions' ? 'tabs-btn-active font-medium' : ''}`}
              onClick={() => setLeftTab('submissions')}
            >
              提交记录
            </button>
          </div>

          <div className="min-h-0 flex-1 overflow-auto bg-[var(--ant-color-bg-container)] px-4 py-3">
            {leftTab === 'description' ? (
              <div className="space-y-4">
                <Flex align="flex-start" justify="space-between" gap={12}>
                  <Typography.Title level={4} className="!mb-0 min-w-0">
                    {problem.problem_key ? `${problem.problem_key}. ` : ''}
                    {problem.title}
                  </Typography.Title>
                  {solved ? (
                    <Flex align="center" gap={6} className="shrink-0 text-[var(--ant-color-success)]">
                      <CheckCircleFilled />
                      <span className="text-sm">已解答</span>
                    </Flex>
                  ) : null}
                </Flex>

                <Flex wrap="wrap" gap={8} align="center">
                  <Tag color={difficultyColor(problem.difficulty)} className="m-0">
                    {difficultyLabel(problem.difficulty)}
                  </Tag>
                  {tags.length ? (
                    <Popover
                      trigger="click"
                      content={
                        <Flex wrap="wrap" gap={8} className="max-w-xs">
                          {tags.map((tag: any) => (
                            <Tag key={tag.id || tag.name} className="m-0">
                              {tag.name}
                            </Tag>
                          ))}
                        </Flex>
                      }
                    >
                      <Tag icon={<TagsOutlined />} className="m-0 cursor-pointer">
                        相关标签
                      </Tag>
                    </Popover>
                  ) : null}
                  {problem.hint ? (
                    <Tag className="m-0 cursor-default">提示</Tag>
                  ) : null}
                </Flex>

                <MdPreview value={problem.statement_md || ''} />
                {problem.input_format ? (
                  <section>
                    <h3 className="mb-2 text-sm font-semibold">输入格式</h3>
                    <MdPreview value={problem.input_format} />
                  </section>
                ) : null}
                {problem.output_format ? (
                  <section>
                    <h3 className="mb-2 text-sm font-semibold">输出格式</h3>
                    <MdPreview value={problem.output_format} />
                  </section>
                ) : null}
                {problem.hint ? (
                  <Collapse
                    size="small"
                    items={[
                      {
                        key: 'hint',
                        label: '提示',
                        children: <MdPreview value={problem.hint} />,
                      },
                    ]}
                  />
                ) : null}
              </div>
            ) : !loggedIn ? (
              <div className="flex flex-col items-center gap-3 py-10 text-sm">
                <Empty description="登录后可查看提交记录" />
                <Link to={loginRedirect}>
                  <Button type="primary">去登录</Button>
                </Link>
              </div>
            ) : submissionView === 'detail' && activeSubmission ? (
              <div className="space-y-4 text-sm">
                <div className="flex items-center justify-between gap-2">
                  <Button
                    type="link"
                    size="small"
                    className="px-0"
                    icon={<ArrowLeftOutlined />}
                    onClick={() => setSubmissionView('list')}
                  >
                    返回列表
                  </Button>
                  <span className="muted-text text-xs">
                    {formatDateTime(activeSubmission.created_at)}
                  </span>
                </div>

                <div className="flex items-center gap-2">
                  <Typography.Title
                    level={4}
                    className="!mb-0"
                    type={
                      activeSubmission.status === 'AC'
                        ? 'success'
                        : isJudgingStatus(activeSubmission.status)
                          ? 'secondary'
                          : 'danger'
                    }
                  >
                    {activeSubmission.status || '—'}
                  </Typography.Title>
                  {isJudgingStatus(activeSubmission.status) ? <Spin size="small" /> : null}
                </div>

                <div className="flex flex-wrap gap-4 text-xs text-[var(--ant-color-text-secondary)]">
                  <span>
                    <ClockCircleOutlined className="mr-1" />
                    {formatTimeMs(activeSubmission.time_ms)}
                  </span>
                  <span>
                    <DatabaseOutlined className="mr-1" />
                    {formatMemory(activeSubmission.memory_bytes)}
                  </span>
                  <span>得分 {activeSubmission.score ?? '—'}</span>
                  <Tag className="m-0">{ojLanguageLabel(activeSubmission.language)}</Tag>
                </div>

                {isJudgingStatus(activeSubmission.status) ? (
                  <div className="result-box rounded p-2 text-xs whitespace-pre-wrap">
                    {activeSubmission.status === 'JUDGING' ? '正在判题…' : '排队判题中…'}
                  </div>
                ) : null}

                {activeSubmission.judge_message
                && !isJudgingStatus(activeSubmission.status)
                && activeSubmission.judge_message !== activeSubmission.status ? (
                  <div className="result-box rounded p-2 text-xs whitespace-pre-wrap">
                    {activeSubmission.judge_message}
                  </div>
                ) : null}

                {activeSubmission.compile_output ? (
                  <div>
                    <div className="mb-1 text-xs font-medium">编译输出</div>
                    <CodeBlock
                      value={activeSubmission.compile_output}
                      language="plaintext"
                      maxHeight={240}
                    />
                  </div>
                ) : null}

                <div>
                  <div className="mb-2 text-xs text-[var(--ant-color-text-secondary)]">
                    代码 | {ojLanguageLabel(activeSubmission.language)}
                  </div>
                  <CodeBlock
                    value={activeSubmission.source_code || ''}
                    language={mapOjLanguageToMonaco(activeSubmission.language)}
                    maxHeight={codeExpanded ? 720 : 220}
                  />
                  {activeSubmission.source_code && String(activeSubmission.source_code).length > 400 ? (
                    <div className="mt-1 text-center">
                      <Button type="link" size="small" onClick={() => setCodeExpanded((v) => !v)}>
                        {codeExpanded ? '收起' : '查看更多'}
                      </Button>
                    </div>
                  ) : null}
                </div>

                <div className="rounded-lg border border-[var(--ant-color-border)] bg-[var(--ant-color-bg-container)] p-4 shadow-sm">
                  <div className="mb-3 text-base font-semibold">备注</div>
                  <Input.TextArea
                    autoSize={{ minRows: 4, maxRows: 8 }}
                    maxLength={255}
                    showCount
                    value={noteDraft}
                    placeholder="添加备注，例如「暴力解法」「方法一」等"
                    styles={{
                      textarea: { resize: 'none' },
                      count: { bottom: -22 },
                    }}
                    className="mb-6"
                    onChange={(e) => setNoteDraft(e.target.value)}
                  />
                  <div className="flex justify-end gap-2">
                    <Button
                      disabled={
                        noteDraft === (activeSubmission.note ? String(activeSubmission.note) : '')
                      }
                      onClick={resetDetailNote}
                    >
                      取消
                    </Button>
                    <Button
                      type="primary"
                      loading={noteSaving && !quickNote}
                      disabled={
                        noteDraft.trim() === (activeSubmission.note ? String(activeSubmission.note) : '')
                      }
                      onClick={() => void saveDetailNote()}
                    >
                      保存
                    </Button>
                  </div>
                </div>
              </div>
            ) : submissionBootstrapping && !submissionRows.length ? (
              <div className="flex justify-center py-10">
                <Spin />
              </div>
            ) : (
              <Table
                rowKey="id"
                size="small"
                columns={submissionColumns}
                dataSource={submissionRows}
                scroll={{ x: submissionTableScrollX }}
                locale={{ emptyText: <Empty description="暂无提交" /> }}
                pagination={
                  submissionTotal > submissionPage.size
                    ? {
                        current: submissionPage.current,
                        pageSize: submissionPage.size,
                        total: submissionTotal,
                        size: 'small',
                        showSizeChanger: false,
                        onChange: (page) => {
                          void loadSubmissions({
                            current: page,
                            size: submissionPage.size,
                            selectDetail: false,
                          })
                        },
                      }
                    : false
                }
                onRow={(row) => ({
                  onClick: () => {
                    void openSubmission(row)
                  },
                  className: 'group cursor-pointer',
                })}
              />
            )}
          </div>
        </aside>

        <Modal
          title="备注"
          open={!!quickNote}
          confirmLoading={noteSaving}
          okText="保存"
          cancelText="取消"
          onCancel={() => setQuickNote(null)}
          onOk={() => void saveQuickNote()}
          destroyOnHidden
          styles={{ body: { paddingTop: 8, paddingBottom: 4 } }}
        >
          <Input.TextArea
            autoSize={{ minRows: 5, maxRows: 10 }}
            maxLength={255}
            showCount
            value={quickNote?.note ?? ''}
            placeholder="添加备注，例如「暴力解法」「方法一」等"
            styles={{
              textarea: { resize: 'none' },
              count: { bottom: -22 },
            }}
            className="mb-6"
            onChange={(e) =>
              setQuickNote((prev) => (prev ? { ...prev, note: e.target.value } : prev))
            }
          />
        </Modal>

        <div
          className="w-1 shrink-0 cursor-col-resize bg-[var(--ant-color-border)] hover:bg-[var(--ant-color-primary)]"
          onMouseDown={() => {
            dragKindRef.current = 'col'
          }}
        />

        <section className="flex min-h-0 min-w-0 flex-1 flex-col overflow-hidden">
          <div className="toolbar flex h-10 shrink-0 items-center gap-3 px-3">
            <Select
              className="w-40"
              options={languageOptions}
              value={language || undefined}
              placeholder="选择语言"
              onChange={setLanguage}
            />
            <Select
              className="w-32"
              options={editorThemeOptions}
              value={editorTheme}
              onChange={setEditorTheme}
            />
          </div>
          <div className="editor-shell min-h-0 flex-1 overflow-hidden rounded-none border-0 border-t">
            <CodeEditor
              value={sourceCode}
              language={mapOjLanguageToMonaco(language)}
              themeMode={editorTheme}
              onChange={handleCodeChange}
            />
          </div>

          {!loggedIn ? (
            <div className="toolbar flex shrink-0 items-center justify-center gap-1 px-3 py-2 text-sm">
              <span className="text-[var(--ant-color-text-secondary)]">运行和提交代码需要</span>
              <Link to={loginRedirect} className="text-[var(--ant-color-primary)]">
                登录
              </Link>
            </div>
          ) : null}

          <div
            className="h-1 shrink-0 cursor-row-resize bg-[var(--ant-color-border)] hover:bg-[var(--ant-color-primary)]"
            onMouseDown={() => {
              dragKindRef.current = 'row'
            }}
          />

          <div
            className="flex shrink-0 flex-col overflow-hidden border-t border-[var(--ant-color-border)] bg-[var(--ant-color-bg-container)]"
            style={{ height: bottomHeight }}
          >
            <div className="tabs-bar flex shrink-0 gap-1 px-2">
              <button
                type="button"
                className={`tabs-btn rounded-t px-3 py-2 text-sm ${bottomTab === 'cases' ? 'tabs-btn-active font-medium' : ''}`}
                onClick={() => setBottomTab('cases')}
              >
                测试用例
              </button>
              <button
                type="button"
                className={`tabs-btn rounded-t px-3 py-2 text-sm ${bottomTab === 'test-result' ? 'tabs-btn-active font-medium' : ''}`}
                onClick={() => setBottomTab('test-result')}
              >
                测试结果
              </button>
            </div>

            <div className="min-h-0 flex-1 overflow-y-auto px-4 py-3">
              {bottomTab === 'cases' ? (
                <Flex vertical gap="middle">
                  <Space wrap>
                    {testCases.map((_, index) => (
                      <Tag
                        key={index}
                        className="m-0 cursor-pointer px-3 py-1"
                        color={activeCaseIndex === index ? 'processing' : undefined}
                        closable={testCases.length > 1}
                        onClick={() => setActiveCaseIndex(index)}
                        onClose={(e) => {
                          e.preventDefault()
                          removeCase(index)
                        }}
                      >
                        Case {index + 1}
                      </Tag>
                    ))}
                    <Tag
                      className="m-0 cursor-pointer border-dashed px-3 py-1"
                      onClick={addCase}
                      style={{
                        opacity: testCases.length >= MAX_TEST_CASES ? 0.45 : 1,
                        pointerEvents: testCases.length >= MAX_TEST_CASES ? 'none' : undefined,
                      }}
                    >
                      <PlusOutlined /> Case
                    </Tag>
                  </Space>
                  <div>
                    <Typography.Paragraph type="secondary" className="!mb-2">
                      输入
                    </Typography.Paragraph>
                    <Input.TextArea
                      variant="filled"
                      rows={2}
                      value={activeCase?.input ?? ''}
                      onChange={(e) => updateCaseInput(activeCaseIndex, e.target.value)}
                      placeholder="stdin"
                      style={{
                        fontFamily:
                          'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
                      }}
                    />
                  </div>
                  <div>
                    <Typography.Paragraph type="secondary" className="!mb-2">
                      期望输出（可选）
                    </Typography.Paragraph>
                    <Input.TextArea
                      variant="filled"
                      rows={2}
                      value={activeCase?.output ?? ''}
                      onChange={(e) => updateCaseOutput(activeCaseIndex, e.target.value)}
                      placeholder="留空则只跑不比对"
                      style={{
                        fontFamily:
                          'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
                      }}
                    />
                  </div>
                </Flex>
              ) : null}

              {bottomTab === 'test-result' ? (
                testing ? (
                  <Flex align="center" gap="middle" className="py-6">
                    <Spin />
                    <span>测试运行中…</span>
                  </Flex>
                ) : !testResult ? (
                  <Empty className="py-8" description="请先执行代码" />
                ) : (
                  <Flex vertical gap="middle">
                    <Space align="baseline" wrap>
                      <Typography.Title
                        level={4}
                        className="!mb-0"
                        type={
                          testResult.status === 'AC'
                            ? 'success'
                            : testResult.status === 'PENDING' || testResult.status === 'JUDGING'
                              ? 'secondary'
                              : 'danger'
                        }
                      >
                        {testResult.status || '—'}
                      </Typography.Title>
                      <Typography.Text type="secondary">
                        执行用时: {formatTimeMs(testResult.time_ms)}
                      </Typography.Text>
                      <Typography.Text type="secondary">
                        消耗内存: {formatMemory(testResult.memory_bytes)}
                      </Typography.Text>
                    </Space>

                    {testResult.compile_output ? (
                      <div>
                        <Typography.Paragraph type="secondary" className="!mb-2">
                          编译输出
                        </Typography.Paragraph>
                        <Input.TextArea
                          variant="filled"
                          readOnly
                          rows={3}
                          value={String(testResult.compile_output)}
                          style={{
                            fontFamily:
                              'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
                          }}
                        />
                      </div>
                    ) : null}

                    {Array.isArray(testResult.case_results) && testResult.case_results.length ? (
                      <>
                        <Space wrap>
                          {testResult.case_results.map((item: any, index: number) => (
                            <Tag
                              key={index}
                              className="m-0 cursor-pointer px-3 py-1"
                              color={resultCaseIndex === index ? 'processing' : undefined}
                              icon={
                                item.status === 'AC' ? (
                                  <CheckCircleFilled />
                                ) : undefined
                              }
                              onClick={() => setResultCaseIndex(index)}
                            >
                              Case {index + 1}
                            </Tag>
                          ))}
                        </Space>

                        {(() => {
                          const item = testResult.case_results[resultCaseIndex] ?? testResult.case_results[0]
                          if (!item) return null
                          return (
                            <Flex vertical gap="middle">
                              <div>
                                <Typography.Paragraph type="secondary" className="!mb-2">
                                  输入
                                </Typography.Paragraph>
                                <Input.TextArea
                                  variant="filled"
                                  readOnly
                                  rows={2}
                                  value={String(item.stdin ?? '')}
                                  style={{
                                    fontFamily:
                                      'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
                                  }}
                                />
                              </div>
                              <div>
                                <Typography.Paragraph type="secondary" className="!mb-2">
                                  输出
                                </Typography.Paragraph>
                                <Input.TextArea
                                  variant="filled"
                                  readOnly
                                  rows={2}
                                  value={String(item.stdout ?? '')}
                                  style={{
                                    fontFamily:
                                      'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
                                  }}
                                />
                              </div>
                              <div>
                                <Typography.Paragraph type="secondary" className="!mb-2">
                                  预期结果
                                </Typography.Paragraph>
                                <Input.TextArea
                                  variant="filled"
                                  readOnly
                                  rows={2}
                                  value={item.expected == null ? '（未设置）' : String(item.expected)}
                                  style={{
                                    fontFamily:
                                      'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
                                  }}
                                />
                              </div>
                            </Flex>
                          )
                        })()}
                      </>
                    ) : null}
                  </Flex>
                )
              ) : null}
            </div>
          </div>
        </section>
      </div>
    </div>
  )
}
