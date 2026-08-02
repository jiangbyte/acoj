export const extensionToMonaco: Record<string, string> = {
  '.cpp': 'cpp',
  '.c': 'c',
  '.py': 'python',
  '.java': 'java',
  '.go': 'go',
  '.js': 'javascript',
  '.rs': 'rust',
}

export function monacoLanguageFromExtension(extension?: string | null, fallback = 'cpp') {
  if (!extension)
    return fallback
  return extensionToMonaco[extension] || fallback
}
