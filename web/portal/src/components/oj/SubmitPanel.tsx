import { useEffect, useRef, useState } from 'react'
import { Button, Collapse, Dropdown, Space, Spin, Splitter, Tag, Tooltip, Typography, message } from 'antd'
import type { MenuProps } from 'antd'
import type { editor as MonacoEditorType } from 'monaco-editor'
import {
  BulbOutlined,
  CheckCircleOutlined,
  DownOutlined,
  ExperimentOutlined,
  FormatPainterOutlined,
  MinusOutlined,
  PlayCircleOutlined,
  PlusOutlined,
  RobotOutlined,
  SendOutlined,
} from '@ant-design/icons'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/auth'
import { CustomTabs } from '@/components/common/CustomTabs'
import { MonacoEditor } from '@/components/editor/MonacoEditor'
import { monacoLanguage } from '@/utils/monacoLanguage'
import { useAppStore } from '@/stores/app'
import { VerdictBadge } from './VerdictBadge'
import { SolveProblemNav } from './SolveProblemNav'
import { isTerminalStatus, pollSubmissionUntilDone, watchSubmissionEvents } from '@/api/submissionWatch'
import type { SubmissionSnapshot } from '@/api/problem'
import { useDict } from '@/hooks/useDict'
import { dictTypeColor, dictTypeData } from '@/utils/dict'

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
  mobileStacked?: boolean
  aiChatOpen?: boolean
  onToggleAiChat?: () => void
}

const caseColor = (result?: string | null) => {
  if (!result) return undefined
  return dictTypeColor('SUBMISSION_RESULT', result) || undefined
}

export function SubmitPanel({
  languages,
  defaultLanguage,
  onSubmit,
  fillHeight = false,
  mobileStacked = false,
  aiChatOpen = false,
  onToggleAiChat,
}: Props) {
  useDict()
  const navigate = useNavigate()
  const { pathname, search } = useLocation()
  const isLogin = useAuthStore((s) => s.isLogin)
  const resolvedTheme = useAppStore((s) => s.resolvedTheme)

  const [languageKey, setLanguageKey] = useState<string>(defaultLanguage ?? '')
  const [source, setSource] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [result, setResult] = useState<SubmissionSnapshot | null>(null)
  const [watching, setWatching] = useState(false)
  const [activeTab, setActiveTab] = useState('cases')
  const [editorTheme, setEditorTheme] = useState<'vs' | 'vs-dark'>(
    resolvedTheme === 'dark' ? 'vs-dark' : 'vs',
  )
  const [editorFontSize, setEditorFontSize] = useState(14)

  const abortRef = useRef<AbortController | null>(null)
  const editorRef = useRef<MonacoEditorType.IStandaloneCodeEditor | null>(null)

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

  useEffect(() => {
    setEditorTheme(resolvedTheme === 'dark' ? 'vs-dark' : 'vs')
  }, [resolvedTheme])

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

  async function handleFormatCode() {
    const action = editorRef.current?.getAction('editor.action.formatDocument')
    if (!action) {
      message.info('当前语言暂不支持格式化')
      return
    }

    try {
      await action.run()
    } catch {
      message.info('当前语言暂不支持格式化')
    }
  }

  const options = languages.map((lang) => ({
    value: lang.language_key,
    label: lang.label || lang.language_key,
  }))
  const languageItems: MenuProps['items'] = options.map((option) => ({
    key: option.value,
    label: option.label,
  }))
  const currentLanguageLabel =
    options.find((option) => option.value === effectiveLanguage)?.label || '选择语言'

  const terminal = result ? isTerminalStatus(result.status) : false

  const languageBar = (
    <div className="toolbar flex shrink-0 flex-wrap items-center gap-2 px-3 py-2">
      <SolveProblemNav compact />
      <Dropdown
        menu={{
          items: languageItems,
          selectedKeys: effectiveLanguage ? [effectiveLanguage] : [],
          onClick: ({ key }) => setLanguageKey(key),
        }}
        trigger={['click']}
        disabled={languageItems.length === 0}
      >
        <Button type="text" className="!px-2 font-medium">
          <Space size={4}>
            <span>{currentLanguageLabel}</span>
            <DownOutlined className="text-xs" />
          </Space>
        </Button>
      </Dropdown>
      <div className="ml-auto flex flex-wrap items-center gap-2">
        <Space size={4}>
          {!mobileStacked && onToggleAiChat ? (
            <Tooltip title={aiChatOpen ? '关闭 AI 助手' : '打开 AI 助手'}>
              <Button
                type="text"
                icon={<RobotOutlined />}
                aria-label={aiChatOpen ? '关闭 AI 助手' : '打开 AI 助手'}
                onClick={onToggleAiChat}
              />
            </Tooltip>
          ) : null}
          <Tooltip title="格式化代码">
            <Button
              type="text"
              icon={<FormatPainterOutlined />}
              onClick={() => void handleFormatCode()}
            />
          </Tooltip>
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
          <span className="muted-text w-8 text-center text-xs">{editorFontSize}px</span>
          <Tooltip title="增大字号">
            <Button
              type="text"
              icon={<PlusOutlined />}
              disabled={editorFontSize >= 24}
              onClick={() => setEditorFontSize((size) => Math.min(24, size + 1))}
            />
          </Tooltip>
        </Space>
        <Tooltip title="运行">
          <Button disabled icon={<PlayCircleOutlined />} aria-label="运行" />
        </Tooltip>
        <Tooltip title="提交">
          <Button
            type="primary"
            icon={<SendOutlined />}
            loading={submitting}
            aria-label="提交"
            onClick={() => void handleSubmit()}
          />
        </Tooltip>
      </div>
    </div>
  )

  const resultArea = result ? (
    <div className="result-box rounded-md p-3">
      <div className="flex flex-wrap items-center gap-3">
        <Space size={8}>
          <VerdictBadge status={result.status} result={result.result} />
          {watching ? <Spin /> : null}
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
        <div className="error-box mt-2 rounded p-2 text-xs">{result.error}</div>
      ) : null}

      {result.compile_output ? (
        <Collapse
          className="mt-2"

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
          <div className="muted-text mb-1 text-xs font-medium">测试用例（{result.cases.length}）</div>
          <div className="space-y-1">
            {result.cases.map((c) => (
              <div
                key={c.case_no}
                className="case-row flex flex-wrap items-center gap-2 rounded px-2 py-1 text-xs"
              >
                <span className="muted-text w-6 shrink-0">#{c.case_no}</span>
                <Tag color={caseColor(c.result)} className="m-0">
                  {c.result ? dictTypeData('SUBMISSION_RESULT', c.result) || c.result : '-'}
                </Tag>
                <span className="muted-text">得分 {c.score}</span>
                <span className="muted-text">耗时 {c.time_ms} ms</span>
                <span className="muted-text">内存 {(c.memory_kb / 1024).toFixed(1)} MB</span>
              </div>
            ))}
          </div>
        </div>
      ) : null}
    </div>
  ) : (
    <div className="muted-box rounded-md px-3 py-6 text-center text-sm">
      提交代码后，判题结果将在此展示
    </div>
  )

  const bottomTabs = (
    <CustomTabs
      activeKey={activeTab}
      onChange={setActiveTab}
      contentClassName="p-3"
      fillHeight={!mobileStacked}
      items={[
        {
          key: 'cases',
          label: '测试用例',
          icon: <ExperimentOutlined />,
          children: (
            <div className="muted-box rounded-md px-3 py-6 text-center text-sm">
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

  if (fillHeight && !mobileStacked) {
    return (
      <Splitter orientation="vertical" style={{ height: '100%' }} className="min-w-0 bg-[var(--ant-color-bg-layout)]">
        <Splitter.Panel
          defaultSize="72%"
          min={200}
          className="min-w-0"
          collapsible={{ start: true, end: true, showCollapsibleIcon: 'auto' }}
        >
          <div className="panel flex h-full min-w-0 flex-col rounded-md">
            {languageBar}
            <div className="editor-shell min-h-0 flex-1 border-0">
              <MonacoEditor
                value={source}
                language={monacoLanguage(effectiveLanguage)}
                onChange={setSource}
                onMount={(editor) => {
                  editorRef.current = editor
                }}
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
          <div className="panel h-full min-h-0 rounded-md">{bottomTabs}</div>
        </Splitter.Panel>
      </Splitter>
    )
  }

  return (
    <div className="flex min-w-0 flex-col gap-3">
      <div className="panel flex min-w-0 flex-col rounded-md">
        {languageBar}
        <div className="editor-shell border-0">
          <MonacoEditor
            value={source}
            language={monacoLanguage(effectiveLanguage)}
            onChange={setSource}
            onMount={(editor) => {
              editorRef.current = editor
            }}
            theme={editorTheme}
            options={{ fontSize: editorFontSize }}
            height={mobileStacked ? 420 : 480}
          />
        </div>
      </div>
      {mobileStacked ? <div className="panel rounded-md">{bottomTabs}</div> : resultArea}
    </div>
  )
}
