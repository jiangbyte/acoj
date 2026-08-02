import { useEffect, useRef, useState } from 'react'
import { Button, Collapse, Select, Space, Spin, Splitter, Tag, Tooltip, Typography, message } from 'antd'
import {
  BulbOutlined,
  CheckCircleOutlined,
  ExperimentOutlined,
  MinusOutlined,
  PlusOutlined,
} from '@ant-design/icons'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/auth'
import { CustomTabs } from '@/components/common/CustomTabs'
import { MonacoEditor } from '@/components/editor/MonacoEditor'
import { monacoLanguage } from '@/utils/monacoLanguage'
import { VerdictBadge } from './VerdictBadge'
import { isTerminalStatus, pollSubmissionUntilDone, watchSubmissionEvents } from '@/api/submissionWatch'
import type { SubmissionSnapshot } from '@/api/problem'

export interface LanguageOption {
  language_key: string
  label?: string | null
  extension?: string | null
}

type Props = {
  languages: LanguageOption[]
  defaultLanguage?: string
  onSubmit: (payload: { language_key: string; source: string }) => Promise<SubmissionSnapshot>
  fillHeight?: boolean
}

const caseColor = (result?: string | null) => {
  if (!result) return 'default'
  if (result === 'AC') return 'success'
  if (result === 'WA') return 'error'
  if (result === 'TLE' || result === 'MLE') return 'warning'
  return 'error'
}

export function SubmitPanel({ languages, defaultLanguage, onSubmit, fillHeight = false }: Props) {
  const navigate = useNavigate()
  const { pathname, search } = useLocation()
  const isLogin = useAuthStore((s) => s.isLogin)

  const [languageKey, setLanguageKey] = useState<string>(defaultLanguage ?? '')
  const [source, setSource] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [result, setResult] = useState<SubmissionSnapshot | null>(null)
  const [watching, setWatching] = useState(false)
  const [activeTab, setActiveTab] = useState('cases')
  const [editorTheme, setEditorTheme] = useState<'vs' | 'vs-dark'>('vs')
  const [editorFontSize, setEditorFontSize] = useState(14)

  const abortRef = useRef<AbortController | null>(null)

  const effectiveLanguage = (() => {
    if (languageKey && languages.some((l) => l.language_key === languageKey)) {
      return languageKey
    }
    return languages[0]?.language_key ?? defaultLanguage ?? ''
  })()

  useEffect(() => {
    return () => {
      abortRef.current?.abort()
    }
  }, [])

  async function startWatching(submissionId: string, initial: SubmissionSnapshot) {
    if (isTerminalStatus(initial.status)) {
      setResult(initial)
      return
    }
    const controller = new AbortController()
    abortRef.current?.abort()
    abortRef.current = controller
    setWatching(true)
    setResult(initial)

    await watchSubmissionEvents(
      submissionId,
      {
        onUpdate: (snap) => setResult(snap),
        onDone: (snap) => {
          setResult(snap)
          setWatching(false)
        },
        onTimeout: (snap) => {
          setResult(snap)
          setWatching(false)
        },
        onError: async () => {
          // SSE 不可用时降级轮询
          try {
            const final = await pollSubmissionUntilDone(submissionId, {
              signal: controller.signal,
              maxWaitMs: 120_000,
            })
            setResult(final)
          } catch {
            // aborted
          } finally {
            if (!controller.signal.aborted) {
              setWatching(false)
            }
          }
        },
      },
      { signal: controller.signal, maxWaitSec: 120 },
    )
    if (!controller.signal.aborted) {
      setWatching(false)
    }
  }

  async function handleSubmit() {
    if (!source.trim()) {
      message.warning('请输入代码')
      return
    }
    if (!effectiveLanguage) {
      message.warning('请选择语言')
      return
    }
    if (!isLogin()) {
      const redirect = `${pathname}${search}`
      navigate(`/auth/login?redirect=${encodeURIComponent(redirect)}`)
      return
    }

    setSubmitting(true)
    setResult(null)
    setActiveTab('result')
    try {
      const snap = await onSubmit({ language_key: effectiveLanguage, source })
      await startWatching(snap.submission_id, snap)
    } catch {
      // 错误提示由 axios 拦截器统一处理
    } finally {
      setSubmitting(false)
    }
  }

  const options = languages.map((lang) => ({
    value: lang.language_key,
    label: lang.label || lang.language_key,
  }))

  const terminal = result ? isTerminalStatus(result.status) : false

  const languageBar = (
    <div className="flex shrink-0 items-center gap-2 pb-3">
      <Select
        className="min-w-32 flex-1"
        placeholder="选择语言"
        value={effectiveLanguage || undefined}
        options={options}
        onChange={setLanguageKey}
      />
      <Space size={4}>
        <Tooltip title={editorTheme === 'vs' ? '切换到深色主题' : '切换到浅色主题'}>
          <Button
            type="text"
            icon={<BulbOutlined />}
            onClick={() => setEditorTheme(editorTheme === 'vs' ? 'vs-dark' : 'vs')}
          />
        </Tooltip>
        <Tooltip title="减小字号">
          <Button
            type="text"
            icon={<MinusOutlined />}
            disabled={editorFontSize <= 10}
            onClick={() => setEditorFontSize((size) => Math.max(10, size - 1))}
          />
        </Tooltip>
        <span className="w-8 text-center text-xs text-gray-500">{editorFontSize}px</span>
        <Tooltip title="增大字号">
          <Button
            type="text"
            icon={<PlusOutlined />}
            disabled={editorFontSize >= 24}
            onClick={() => setEditorFontSize((size) => Math.min(24, size + 1))}
          />
        </Tooltip>
      </Space>
      <Button onClick={() => undefined}>测试</Button>
      <Button type="primary" loading={submitting} onClick={() => void handleSubmit()}>
        提交
      </Button>
    </div>
  )

  const resultArea = result ? (
    <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
      <div className="flex flex-wrap items-center gap-3">
        <Space size={8}>
          <VerdictBadge status={result.status} result={result.result} />
          {watching ? <Spin size="small" /> : null}
        </Space>
        <Typography.Text type="secondary" className="text-sm">
          得分 {result.score}
        </Typography.Text>
        <Typography.Text type="secondary" className="text-sm">
          耗时 {result.time_ms} ms
        </Typography.Text>
        <Typography.Text type="secondary" className="text-sm">
          内存 {(result.memory_kb / 1024).toFixed(1)} MB
        </Typography.Text>
        {terminal ? (
          <Link to={`/submissions/${result.submission_id}`} className="text-sm">
            查看提交详情
          </Link>
        ) : null}
      </div>

      {result.error ? (
        <div className="mt-2 rounded bg-red-50 p-2 text-xs text-red-600">{result.error}</div>
      ) : null}

      {result.compile_output ? (
        <Collapse
          className="mt-2"
          size="small"
          items={[
            {
              key: 'compile',
              label: result.compile_error ? '编译输出（有错误）' : '编译输出',
              children: (
                <pre className="m-0 overflow-auto whitespace-pre-wrap break-words text-xs">
                  {result.compile_output}
                </pre>
              ),
            },
          ]}
        />
      ) : null}

      {result.cases.length > 0 ? (
        <div className="mt-2">
          <div className="mb-1 text-xs font-medium text-gray-500">测试用例（{result.cases.length}）</div>
          <div className="space-y-1">
            {result.cases.map((c) => (
              <div
                key={c.case_no}
                className="flex items-center gap-2 rounded bg-white px-2 py-1 text-xs"
              >
                <span className="w-6 shrink-0 text-gray-400">#{c.case_no}</span>
                <Tag color={caseColor(c.result)} className="m-0">
                  {c.result || '等待'}
                </Tag>
                <span className="text-gray-500">得分 {c.score}</span>
                <span className="text-gray-500">耗时 {c.time_ms} ms</span>
                <span className="text-gray-500">内存 {(c.memory_kb / 1024).toFixed(1)} MB</span>
              </div>
            ))}
          </div>
        </div>
      ) : null}
    </div>
  ) : (
    <div className="rounded-md border border-dashed border-gray-200 px-3 py-6 text-center text-sm text-gray-400">
      提交代码后，判题结果将在此展示
    </div>
  )

  const bottomTabs = (
    <CustomTabs
      activeKey={activeTab}
      onChange={setActiveTab}
      contentClassName="p-3"
      items={[
        {
          key: 'cases',
          label: '测试用例',
          icon: <ExperimentOutlined />,
          children: (
            <div className="rounded-md border border-dashed border-gray-200 px-3 py-6 text-center text-sm text-gray-400">
              自定义测试用例功能即将上线，敬请期待
            </div>
          ),
        },
        {
          key: 'result',
          label: '测试结果',
          icon: <CheckCircleOutlined />,
          children: resultArea,
        },
      ]}
    />
  )

  if (fillHeight) {
    return (
      <Splitter orientation="vertical" style={{ height: '100%' }} className="min-w-0">
        <Splitter.Panel
          defaultSize="72%"
          min={200}
          className="min-w-0"
          collapsible={{ start: true, end: true, showCollapsibleIcon: 'auto' }}
        >
          <div className="flex h-full min-w-0 flex-col">
            {languageBar}
            <div className="min-h-0 flex-1 overflow-hidden rounded-md border border-gray-200">
              <MonacoEditor
                value={source}
                language={monacoLanguage(effectiveLanguage)}
                onChange={setSource}
                theme={editorTheme}
                options={{ fontSize: editorFontSize }}
                height="100%"
              />
            </div>
          </div>
        </Splitter.Panel>
        <Splitter.Panel
          min={90}
          className="flex min-w-0 flex-col"
          collapsible={{ start: true, end: true, showCollapsibleIcon: 'auto' }}
        >
          {bottomTabs}
        </Splitter.Panel>
      </Splitter>
    )
  }

  return (
    <div className="flex min-w-0 flex-col">
      {languageBar}
      <div className="overflow-hidden rounded-md border border-gray-200">
        <MonacoEditor
          value={source}
          language={monacoLanguage(effectiveLanguage)}
          onChange={setSource}
          theme={editorTheme}
          options={{ fontSize: editorFontSize }}
          height={480}
        />
      </div>
      <div className="mt-3">{resultArea}</div>
    </div>
  )
}
