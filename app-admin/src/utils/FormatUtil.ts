export function FormatJsonString(jsonString: string) {
  if (!jsonString)
    return 'No parameters'

  try {
    const parsed = JSON.parse(jsonString)
    return JSON.stringify(parsed, null, 2)
  }
  catch (error) {
    return jsonString // Return the original string if parsing fails
  }
}

// Define the formatter in setup
export function FormatLanguages(languages: string[] | undefined): string[] {
  if (!languages || !Array.isArray(languages))
    return []

  return languages.map((lang) => {
    // Capitalize the first letter and lowercase the rest
    return lang.charAt(0).toUpperCase() + lang.slice(1).toLowerCase()
  })
}

const MODULE_TYPE_LABEL: Record<string, string> = {
  PROBLEM: 'Problem Library',
  SET: 'Problem Set',
  CONTEST: 'Contest',
}

/** Submission module label when the backend omits moduleTypeName */
export function FormatModuleType(moduleType: string | null | undefined): string {
  if (!moduleType)
    return '-'
  return MODULE_TYPE_LABEL[moduleType] ?? moduleType
}
