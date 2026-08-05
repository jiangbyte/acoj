import { useEffect, useRef } from 'react'
import type { editor as MonacoEditor } from 'monaco-editor'
import { toCssSize } from './shared'
import { setupMonacoWorkers } from './monacoWorkers'

type MonacoModule = typeof import('monaco-editor')

type Props = {
  value?: string | null
  language?: string
  theme?: string
  height?: string | number
  readOnly?: boolean
  options?: MonacoEditor.IStandaloneEditorConstructionOptions
  onChange?: (value: string) => void
  onMount?: (editor: MonacoEditor.IStandaloneCodeEditor, monaco: MonacoModule) => void
  className?: string
}

export function MonacoEditor({
  value = '',
  language = 'typescript',
  theme = 'vs',
  height = 360,
  readOnly = false,
  options,
  onChange,
  onMount,
  className,
}: Props) {
  const containerRef = useRef<HTMLDivElement>(null)
  const editorRef = useRef<MonacoEditor.IStandaloneCodeEditor | null>(null)
  const monacoRef = useRef<MonacoModule | null>(null)
  const onChangeRef = useRef(onChange)
  const onMountRef = useRef(onMount)

  useEffect(() => {
    onChangeRef.current = onChange
    onMountRef.current = onMount
  })

  useEffect(() => {
    let disposed = false
    let instance: MonacoEditor.IStandaloneCodeEditor | null = null

    setupMonacoWorkers()

    void import('monaco-editor').then((monaco) => {
      if (disposed || !containerRef.current) {
        return
      }
      monacoRef.current = monaco
      instance = monaco.editor.create(containerRef.current, {
        ...options,
        value: value ?? '',
        language,
        theme,
        readOnly,
        automaticLayout: true,
        minimap: { enabled: false },
        fontSize: 14,
        lineNumbers: 'on',
        scrollBeyondLastLine: false,
        tabSize: 4,
        wordWrap: 'off',
      })
      instance.onDidChangeModelContent(() => {
        onChangeRef.current?.(instance?.getValue() ?? '')
      })
      editorRef.current = instance
      onMountRef.current?.(instance, monaco)
    })

    return () => {
      disposed = true
      instance?.dispose()
      instance = null
      editorRef.current = null
      monacoRef.current = null
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    const instance = editorRef.current
    if (!instance || instance.getValue() === (value ?? '')) {
      return
    }
    instance.setValue(value ?? '')
  }, [value])

  useEffect(() => {
    const instance = editorRef.current
    const monaco = monacoRef.current
    const model = instance?.getModel()
    if (!instance || !model || !monaco) {
      return
    }
    monaco.editor.setModelLanguage(model, language)
  }, [language])

  useEffect(() => {
    const instance = editorRef.current
    const monaco = monacoRef.current
    if (!instance || !monaco) {
      return
    }
    monaco.editor.setTheme(theme)
  }, [theme])

  useEffect(() => {
    if (options) {
      editorRef.current?.updateOptions(options)
    }
  }, [options])

  useEffect(() => {
    editorRef.current?.updateOptions({ readOnly })
  }, [readOnly])

  return (
    <div
      ref={containerRef}
      className={className}
      style={{ height: toCssSize(height), width: '100%', minWidth: 0 }}
    />
  )
}
