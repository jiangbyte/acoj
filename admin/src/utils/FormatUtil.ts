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

const MODULE_TYPE_LABEL: Record<string, string> = {
  PROBLEM: '题库',
  SET: '题集',
  CONTEST: '竞赛',
}

/** 提交模块类型展示（后端未返回 moduleTypeName） */
export function FormatModuleType(moduleType: string | null | undefined): string {
  if (!moduleType)
    return '-'
  return MODULE_TYPE_LABEL[moduleType] ?? moduleType
}
