import { useEffect, useRef } from 'react'
import type { editor as MonacoEditor } from 'monaco-editor'
import { setupMonacoWorkers } from './monacoWorkers'

type MonacoModule = typeof import('monaco-editor')

type Props = {
  value: string
  language?: string
  onChange?: (value: string) => void
  readOnly?: boolean
  height?: number | string
  className?: string
}

export function CodeEditor({ value, language = 'plaintext', onChange, readOnly = false, height = 420, className }: Props) {
  const containerRef = useRef<HTMLDivElement>(null)
  const editorRef = useRef<MonacoEditor.IStandaloneCodeEditor | null>(null)
  const monacoRef = useRef<MonacoModule | null>(null)
  const onChangeRef = useRef(onChange)

  useEffect(() => {
    onChangeRef.current = onChange
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
        value,
        language,
        theme: 'vs',
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
    if (!instance || instance.getValue() === value) {
      return
    }
    instance.setValue(value)
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
    editorRef.current?.updateOptions({ readOnly })
  }, [readOnly])

  return <div ref={containerRef} className={className} style={{ height }} />
}
