/** Author: Charlie */

import { memo, useEffect, useRef } from 'react'
import * as monaco from 'monaco-editor'
import { useAppStore } from '@/stores/app'
import { resolveMonacoTheme, type EditorThemeMode } from '@/utils/oj'
import './monacoWorkers'

type Props = {
  value: string
  language?: string
  readOnly?: boolean
  /** auto | vs | vs-dark；默认跟随站点 */
  themeMode?: EditorThemeMode
  onChange?: (value: string) => void
  className?: string
}

export const CodeEditor = memo(function CodeEditor({
  value,
  language = 'plaintext',
  readOnly = false,
  themeMode = 'auto',
  onChange,
  className,
}: Props) {
  const containerRef = useRef<HTMLDivElement | null>(null)
  const editorRef = useRef<monaco.editor.IStandaloneCodeEditor | null>(null)
  const onChangeRef = useRef(onChange)
  const resolvedTheme = useAppStore((s) => s.resolvedTheme)
  const monacoTheme = resolveMonacoTheme(themeMode, resolvedTheme)

  useEffect(() => {
    onChangeRef.current = onChange
  }, [onChange])

  useEffect(() => {
    if (!containerRef.current) {
      return
    }
    const editor = monaco.editor.create(containerRef.current, {
      value,
      language,
      readOnly,
      automaticLayout: true,
      minimap: { enabled: false },
      fontSize: 14,
      scrollBeyondLastLine: false,
      theme: monacoTheme,
      padding: { top: 12 },
    })
    editorRef.current = editor
    const disposable = editor.onDidChangeModelContent(() => {
      onChangeRef.current?.(editor.getValue())
    })
    return () => {
      disposable.dispose()
      editor.dispose()
      editorRef.current = null
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps -- mount once
  }, [])

  useEffect(() => {
    const editor = editorRef.current
    if (!editor) {
      return
    }
    if (editor.getValue() !== value) {
      editor.setValue(value)
    }
  }, [value])

  useEffect(() => {
    const editor = editorRef.current
    const model = editor?.getModel()
    if (model) {
      monaco.editor.setModelLanguage(model, language)
    }
  }, [language])

  useEffect(() => {
    monaco.editor.setTheme(monacoTheme)
  }, [monacoTheme])

  useEffect(() => {
    editorRef.current?.updateOptions({ readOnly })
  }, [readOnly])

  return <div ref={containerRef} className={`h-full w-full ${className ?? ''}`} />
})
