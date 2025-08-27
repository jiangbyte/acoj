/**
 * 去除Markdown语法符号，返回纯文本内容
 * @param markdownText 输入的Markdown文本
 * @returns 清理后的纯文本
 */
export function CleanMarkdown(markdownText: string): string {
  if (!markdownText || typeof markdownText !== 'string') {
    return ''
  }

  let cleanedText = markdownText

  // 1. 移除代码块 (```code``` 或 ~~~code~~~)
  cleanedText = cleanedText.replace(/```[\s\S]*?```|~~~[\s\S]*?~~~/g, '')

  // 2. 移除行内代码 (`code`)
  cleanedText = cleanedText.replace(/`[^`]*`/g, '')

  // 3. 移除图片标记 (![alt](url))
  cleanedText = cleanedText.replace(/!\[.*?\]\(.*?\)/g, '')

  // 4. 移除链接标记 ([text](url) 或 [text][ref])
  cleanedText = cleanedText.replace(/\[(.*?)\]\(.*?\)/g, '$1')
  cleanedText = cleanedText.replace(/\[(.*?)\]\[.*?\]/g, '$1')

  // 5. 移除粗体 (**text** 或 __text__)
  cleanedText = cleanedText.replace(/\*\*(.*?)\*\*|__(.*?)__/g, '$1$2')

  // 6. 移除斜体 (*text* 或 _text_)
  cleanedText = cleanedText.replace(/\*(.*?)\*|_(.*?)_/g, '$1$2')

  // 7. 移除删除线 (~~text~~)
  cleanedText = cleanedText.replace(/~~(.*?)~~/g, '$1')

  // 8. 移除引用标记 (> text)
  cleanedText = cleanedText.replace(/^>\s+/gm, '')

  // 9. 移除标题标记 (#, ##, ### 等)
  cleanedText = cleanedText.replace(/^#{1,6}\s+/gm, '')

  // 10. 移除水平分割线 (---, ***, ___)
  cleanedText = cleanedText.replace(/^[-*_]{3,}\s*$/gm, '')

  // 11. 移除无序列表标记 (*, -, +)
  cleanedText = cleanedText.replace(/^\s*[-*+]\s+/gm, '')

  // 12. 移除有序列表标记 (1., 2., 3. 等)
  cleanedText = cleanedText.replace(/^\s*\d+\.\s+/gm, '')

  // 13. 移除表格标记 (| --- | --- |)
  cleanedText = cleanedText.replace(/^\|.*\|$/gm, '')
  cleanedText = cleanedText.replace(/\|/g, '')

  // 14. 移除HTML标签（简单的处理）
  cleanedText = cleanedText.replace(/<[^>]*>/g, '')

  // 15. 移除注释 (<!-- comment -->)
  cleanedText = cleanedText.replace(/<!--[\s\S]*?-->/g, '')

  // 16. 清理多余的空行和空格
  cleanedText = cleanedText.replace(/\n\s*\n\s*\n/g, '\n\n')
  cleanedText = cleanedText.replace(/[ \t]+/g, ' ')
  cleanedText = cleanedText.trim()

  return cleanedText
}
