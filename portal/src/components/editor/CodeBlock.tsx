/** Author: Charlie */

import { useMemo } from 'react'
import hljs from 'highlight.js/lib/core'
import plaintext from 'highlight.js/lib/languages/plaintext'
import cpp from 'highlight.js/lib/languages/cpp'
import c from 'highlight.js/lib/languages/c'
import java from 'highlight.js/lib/languages/java'
import python from 'highlight.js/lib/languages/python'
import go from 'highlight.js/lib/languages/go'
import javascript from 'highlight.js/lib/languages/javascript'
import rust from 'highlight.js/lib/languages/rust'
import sql from 'highlight.js/lib/languages/sql'
import { useAppStore } from '@/stores/app'
import 'highlight.js/styles/github.css'
import 'highlight.js/styles/github-dark.css'
import './code-block.css'

hljs.registerLanguage('plaintext', plaintext)
hljs.registerLanguage('cpp', cpp)
hljs.registerLanguage('c', c)
hljs.registerLanguage('java', java)
hljs.registerLanguage('python', python)
hljs.registerLanguage('go', go)
hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('rust', rust)
hljs.registerLanguage('sql', sql)

type Props = {
  value?: string | null
  language?: string
  className?: string
  /** 空内容占位 */
  placeholder?: string
  maxHeight?: number | string
}

/** 只读代码块：highlight.js 高亮，跟随站点明暗。 */
export function CodeBlock({
  value,
  language = 'plaintext',
  className,
  placeholder = '',
  maxHeight = 280,
}: Props) {
  const resolvedTheme = useAppStore((s) => s.resolvedTheme)
  const text = value ?? ''
  const empty = !text.length
  const html = useMemo(() => {
    const source = empty ? placeholder : text
    const lang = language.trim().toLowerCase() || 'plaintext'
    try {
      if (hljs.getLanguage(lang)) {
        return hljs.highlight(source, { language: lang }).value
      }
    } catch {
      // fall through
    }
    return hljs.highlight(source, { language: 'plaintext' }).value
  }, [empty, placeholder, text, language])

  return (
    <pre
      className={`code-block result-box overflow-auto rounded p-2 text-xs ${
        resolvedTheme === 'dark' ? 'code-block-dark' : 'code-block-light'
      } ${empty ? 'code-block-empty' : ''} ${className ?? ''}`}
      style={{ maxHeight }}
    >
      <code
        className={`hljs language-${language}`}
        dangerouslySetInnerHTML={{ __html: html || '&nbsp;' }}
      />
    </pre>
  )
}
