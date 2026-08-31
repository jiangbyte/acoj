/** Author: Charlie */

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
