/**
  * Implementation details.
  * Implementation details.
  * Implementation details.
 */
export function CleanMarkdown(markdownText: string): string {
  if (!markdownText || typeof markdownText !== 'string') {
    return ''
  }

  let cleanedText = markdownText

  // Implementation note.
  cleanedText = cleanedText.replace(/```[\s\S]*?```|~~~[\s\S]*?~~~/g, '')

  // Implementation note.
  cleanedText = cleanedText.replace(/`[^`]*`/g, '')

  // Implementation note.
  cleanedText = cleanedText.replace(/!\[.*?\]\(.*?\)/g, '')

  // Implementation note.
  cleanedText = cleanedText.replace(/\[(.*?)\]\(.*?\)/g, '$1')
  cleanedText = cleanedText.replace(/\[(.*?)\]\[.*?\]/g, '$1')

  // Implementation note.
  cleanedText = cleanedText.replace(/\*\*(.*?)\*\*|__(.*?)__/g, '$1$2')

  // Implementation note.
  cleanedText = cleanedText.replace(/\*(.*?)\*|_(.*?)_/g, '$1$2')

  // Implementation note.
  cleanedText = cleanedText.replace(/~~(.*?)~~/g, '$1')

  // Implementation note.
  cleanedText = cleanedText.replace(/^>\s+/gm, '')

  // Implementation note.
  cleanedText = cleanedText.replace(/^#{1,6}\s+/gm, '')

  // Implementation note.
  cleanedText = cleanedText.replace(/^[-*_]{3,}\s*$/gm, '')

  // Implementation note.
  cleanedText = cleanedText.replace(/^\s*[-*+]\s+/gm, '')

  // Implementation note.
  cleanedText = cleanedText.replace(/^\s*\d+\.\s+/gm, '')

  // Implementation note.
  cleanedText = cleanedText.replace(/^\|.*\|$/gm, '')
  cleanedText = cleanedText.replace(/\|/g, '')

  // Implementation note.
  cleanedText = cleanedText.replace(/<[^>]*>/g, '')

  // Implementation note.
  cleanedText = cleanedText.replace(/<!--[\s\S]*?-->/g, '')

  // Implementation note.
  cleanedText = cleanedText.replace(/\n\s*\n\s*\n/g, '\n\n')
  cleanedText = cleanedText.replace(/[ \t]+/g, ' ')
  cleanedText = cleanedText.trim()

  return cleanedText
}
