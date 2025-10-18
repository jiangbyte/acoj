export function FormatJsonString(jsonString: string) {
  if (!jsonString)
    return '无参数'

  try {
    const parsed = JSON.parse(jsonString)
    return JSON.stringify(parsed, null, 2)
  }
  catch (error) {
    return jsonString // 如果解析失败，返回原字符串
  }
}

// 在 setup 中定义格式化函数
export function FormatLanguages(languages: string[] | undefined): string[] {
  if (!languages || !Array.isArray(languages))
    return []

  return languages.map((lang) => {
    // 首字母大写，其余小写
    return lang.charAt(0).toUpperCase() + lang.slice(1).toLowerCase()
  })
}
