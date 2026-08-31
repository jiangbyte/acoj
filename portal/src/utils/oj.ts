/** Author: Charlie */

const LANGUAGE_LABELS: Record<string, string> = {
  c11: 'C11',
  cpp17: 'C++17',
  python3: 'Python 3',
  java17: 'Java 17',
  go: 'Go',
  nodejs: 'Node.js',
  rust: 'Rust',
  sql_sqlite: 'SQLite',
}

/** OJ 语言 key → 展示名 */
export function ojLanguageLabel(language: string): string {
  const key = (language || '').trim().toLowerCase()
  return LANGUAGE_LABELS[key] || language || '—'
}

/** OJ 语言 key → Monaco Editor language id */
export function mapOjLanguageToMonaco(language: string): string {
  const key = (language || '').trim().toLowerCase()
  const mapping: Record<string, string> = {
    c11: 'c',
    cpp17: 'cpp',
    python3: 'python',
    java17: 'java',
    go: 'go',
    nodejs: 'javascript',
    rust: 'rust',
    sql_sqlite: 'sql',
  }
  return mapping[key] ?? 'plaintext'
}

export type EditorThemeMode = 'auto' | 'vs' | 'vs-dark'

export function editorThemeStorageKey() {
  return 'acoj:oj:editor-theme'
}

export function resolveMonacoTheme(
  mode: EditorThemeMode,
  resolvedAppTheme: 'light' | 'dark',
): 'vs' | 'vs-dark' {
  if (mode === 'vs' || mode === 'vs-dark') return mode
  return resolvedAppTheme === 'dark' ? 'vs-dark' : 'vs'
}

export function formatPassRate(acceptCount?: number | null, submitCount?: number | null) {
  const accept = Number(acceptCount ?? 0)
  const submit = Number(submitCount ?? 0)
  if (submit <= 0) {
    return '—'
  }
  return `${((accept / submit) * 100).toFixed(1)}%`
}

export function formatMemory(bytes?: number | null) {
  const value = Number(bytes ?? 0)
  if (!value) {
    return '—'
  }
  if (value >= 1024 * 1024) {
    return `${(value / (1024 * 1024)).toFixed(value >= 10 * 1024 * 1024 ? 0 : 1)} MB`
  }
  if (value >= 1024) {
    return `${(value / 1024).toFixed(0)} KB`
  }
  return `${value} B`
}

export function formatTimeMs(ms?: number | null) {
  const value = Number(ms ?? 0)
  if (!Number.isFinite(value) || value <= 0) {
    return '—'
  }
  return `${value} ms`
}

export function difficultyLabel(difficulty?: string | null) {
  const key = String(difficulty || '').toUpperCase()
  if (key === 'EASY') return '简单'
  if (key === 'MEDIUM') return '中等'
  if (key === 'HARD') return '困难'
  return difficulty || '—'
}

export function difficultyColor(difficulty?: string | null): string {
  const key = String(difficulty || '').toUpperCase()
  if (key === 'EASY') return 'success'
  if (key === 'MEDIUM') return 'warning'
  if (key === 'HARD') return 'error'
  return 'default'
}

export function myStatusLabel(status?: string | null) {
  if (status === 'ACCEPTED') return '已通过'
  if (status === 'ATTEMPTED') return '未通过'
  return '未尝试'
}

export function submissionStatusColor(status?: string | null): string {
  const key = String(status || '').toUpperCase()
  if (key === 'AC') return 'success'
  if (key === 'PENDING' || key === 'JUDGING') return 'processing'
  if (key === 'CE') return 'warning'
  if (key === 'WA' || key === 'TLE' || key === 'MLE' || key === 'OLE' || key === 'RE' || key === 'SE') {
    return 'error'
  }
  return 'default'
}

export function isJudgingStatus(status?: string | null) {
  const key = String(status || '').toUpperCase()
  return key === 'PENDING' || key === 'JUDGING'
}

export function draftStorageKey(problemId: string, language: string) {
  return `acoj:oj:draft:${problemId}:${language}`
}

export function languageStorageKey(problemId: string) {
  return `acoj:oj:lang:${problemId}`
}
