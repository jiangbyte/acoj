/** Author: Charlie */

import { useId } from 'react'
import DOMPurify from 'dompurify'
import { MdPreview as MdPreviewBase } from 'md-editor-rt'
import type { Themes } from 'md-editor-rt'
import { useAppStore } from '@/stores/app'
import 'md-editor-rt/lib/preview.css'
import './md-preview.css'

type Props = {
  value?: string | null
  className?: string
  previewTheme?: string
  codeTheme?: string
  showCodeRowNumber?: boolean
}

/** 只读 Markdown 回显：统一走 md-editor-rt，跟随站点明暗。 */
export function MdPreview({
  value = '',
  className,
  previewTheme = 'github',
  codeTheme = 'atom',
  showCodeRowNumber = true,
}: Props) {
  const resolvedTheme = useAppStore((s) => s.resolvedTheme)
  const theme: Themes = resolvedTheme === 'dark' ? 'dark' : 'light'
  const uid = useId().replace(/:/g, '')

  return (
    <div className={`md-preview ${className ?? ''}`}>
      <MdPreviewBase
        id={`md-preview-${uid}`}
        value={value ?? ''}
        theme={theme}
        language="zh-CN"
        previewTheme={previewTheme}
        codeTheme={codeTheme}
        showCodeRowNumber={showCodeRowNumber}
        sanitize={(html) => DOMPurify.sanitize(html)}
      />
    </div>
  )
}
