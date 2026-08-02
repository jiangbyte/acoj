import { MdPreview as MdPreviewBase } from 'md-editor-rt'
import 'md-editor-rt/lib/preview.css'

type Props = {
  value?: string | null
  className?: string
  previewTheme?: string
  codeTheme?: string
  showCodeRowNumber?: boolean
}

export function MdPreview({
  value = '',
  className,
  previewTheme = 'github',
  codeTheme = 'atom',
  showCodeRowNumber = true,
}: Props) {
  return (
    <div className={className}>
      <MdPreviewBase
        value={value ?? ''}
        previewTheme={previewTheme}
        codeTheme={codeTheme}
        showCodeRowNumber={showCodeRowNumber}
        style={{ padding: 0 }}
      />
    </div>
  )
}
