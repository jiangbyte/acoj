// Implementation note.
type JudgeStatus
    = | 'PENDING' | 'JUDGING' | 'COMPILING' | 'COMPILED_OK' | 'RUNNING'
      | 'ACCEPTED' | 'WRONG_ANSWER' | 'TIME_LIMIT_EXCEEDED' | 'MEMORY_LIMIT_EXCEEDED'
      | 'RUNTIME_ERROR' | 'COMPILATION_ERROR' | 'PRESENTATION_ERROR' | 'OUTPUT_LIMIT_EXCEEDED'
      | 'PARTIAL_ACCEPTED' | 'SYSTEM_ERROR' | 'RESTRICTED_FUNCTION' | 'DANGEROUS_OPERATION'
      | 'QUEUING' | 'HIDDEN' | 'SKIPPED' | 'FROZEN'
      | 'FIRST_BLOOD' | 'HACKED' | 'PENDING_REJUDGE' | 'REJUDGING'
      | 'UNKNOWN_ERROR' | 'VALIDATOR_ERROR' | 'CHECKER_ERROR' | 'IDLENESS_LIMIT_EXCEEDED'
      | 'SECURITY_VIOLATION' | 'IGNORED'
      | 'SIMILARITY_SUSPICIOUS' | 'SIMILARITY_ACCEPTED'

// Implementation note.
export class StatusColorUtil {
  private static readonly COLOR_MAP: Record<JudgeStatus, string> = {
    // Implementation note.
    COMPILING: '#1890FF',
    COMPILATION_ERROR: '#FF6B6B',
    COMPILED_OK: '#52C41A',

    // Implementation note.
    RUNTIME_ERROR: '#FA8C16',
    TIME_LIMIT_EXCEEDED: '#FA541C',
    MEMORY_LIMIT_EXCEEDED: '#FAAD14',
    OUTPUT_LIMIT_EXCEEDED: '#D48806',

    // Implementation note.
    ACCEPTED: '#52C41A',
    WRONG_ANSWER: '#FF4D4F',
    PARTIAL_ACCEPTED: '#A0D911',
    PRESENTATION_ERROR: '#D46B08',

    // Implementation note.
    SYSTEM_ERROR: '#CF1322',
    RUNNING: '#13C2C2',
    JUDGING: '#1890FF',

    // Implementation note.
    PENDING: '#8C8C8C',
    QUEUING: '#8C8C8C',
    HIDDEN: '#595959',
    SKIPPED: '#BFBFBF',
    FROZEN: '#2F54EB',
    FIRST_BLOOD: '#FAAD14',
    HACKED: '#CF1322',
    PENDING_REJUDGE: '#1890FF',
    REJUDGING: '#52C41A',
    RESTRICTED_FUNCTION: '#9E1068',
    DANGEROUS_OPERATION: '#C41D7F',
    UNKNOWN_ERROR: '#D9D9D9',
    VALIDATOR_ERROR: '#FF7A45',
    CHECKER_ERROR: '#FF9C6E',
    IDLENESS_LIMIT_EXCEEDED: '#FFBB96',
    SECURITY_VIOLATION: '#FF4D4F',
    IGNORED: '#BFBFBF',
    SIMILARITY_SUSPICIOUS: '#FFA940',
    SIMILARITY_ACCEPTED: '#52C41A',
  }

  /**
    * Implementation details.
    * Implementation details.
    * Implementation details.
   */
  static getColor(status: JudgeStatus): string {
    return this.COLOR_MAP[status] || '#D9D9D9' // Implementation note.
  }

  /**
    * Implementation details.
    * Implementation details.
   */
  static getAllColors(): Record<JudgeStatus, string> {
    return { ...this.COLOR_MAP }
  }

  /**
    * Implementation details.
   * @param status Status
    * Implementation details.
   */
  static setColor(status: JudgeStatus, color: string): void {
    this.COLOR_MAP[status] = color
  }
}

// Implementation note.
type SubmitType = boolean

export class SubmitTypeColorUtil {
  private static readonly COLOR_MAP: Record<string, string> = {
    false: '#52C41A',
    true: '#1890FF',
  }

  static getColor(submitType: SubmitType): string {
    const key = submitType ? 'true' : 'false'
    return this.COLOR_MAP[key] || '#D9D9D9'
  }

  static getAllColors(): Record<string, string> {
    return { ...this.COLOR_MAP }
  }

  static setColor(submitType: SubmitType, color: string): void {
    const key = submitType ? 'true' : 'false'
    this.COLOR_MAP[key] = color
  }
}

export class LanguageColorUtil {
  // Implementation note.
  private static readonly LANGUAGE_COLOR_MAP: Record<string, string> = {
    // Implementation note.
    'java': '#B07219',
    'python': '#3572A5',
    'javascript': '#F7DF1E',
    'typescript': '#3178C6',
    'c': '#555555',
    'cpp': '#F34B7D',
    'c++': '#F34B7D',
    'csharp': '#178600',
    'c#': '#178600',
    'go': '#00ADD8',
    'rust': '#DEA584',
    'php': '#4F5D95',
    'ruby': '#701516',
    'swift': '#FFAC45',
    'kotlin': '#A97BFF',

    // Implementation note.
    'scala': '#DC322F',
    'r': '#198CE7',
    'matlab': '#E16737',
    'html': '#E34C26',
    'css': '#563D7C',
    'sql': '#E38C00',
    'bash': '#89E051',
    'shell': '#89E051',
    'perl': '#0298C3',
    'lua': '#000080',
    'dart': '#00B4AB',
    'haskell': '#5E5086',
    'elixir': '#6E4A7E',
    'clojure': '#DB5855',
    'erlang': '#A90533',
    'objective-c': '#438EFF',
    'vb': '#945DB7',
    'assembly': '#6E4C13',
    'pascal': '#E3F171',
    'fortran': '#4D41B1',
    'cobol': '#005CA5',
  }

  /**
    * Implementation details.
   */
  static getColor(language: string): string {
    if (!language)
      return '#D9D9D9'

    const normalizedLang = language.toLowerCase().trim()
    return this.LANGUAGE_COLOR_MAP[normalizedLang] || this.generateColorFromString(language)
  }

  /**
    * Implementation details.
   */
  private static generateColorFromString(str: string): string {
    let hash = 0
    for (let i = 0; i < str.length; i++) {
      hash = str.charCodeAt(i) + ((hash << 5) - hash)
    }

    // Implementation note.
    const hue = hash % 360
    const saturation = 70 + (hash % 20) // 70-90%
    const lightness = 50 + (hash % 15) // 50-65%

    return `hsl(${hue}, ${saturation}%, ${lightness}%)`
  }

  /**
    * Implementation details.
   */
  static getNormalizedLanguage(language: string): string {
    if (!language)
      return 'Unknown'

    const normalizedLang = language.toLowerCase().trim()
    const languageMap: Record<string, string> = {
      'cpp': 'C++',
      'c++': 'C++',
      'csharp': 'C#',
      'c#': 'C#',
      'javascript': 'JavaScript',
      'typescript': 'TypeScript',
      'python': 'Python',
      'java': 'Java',
      'go': 'Go',
      'rust': 'Rust',
      'php': 'PHP',
      'ruby': 'Ruby',
      'swift': 'Swift',
      'kotlin': 'Kotlin',
    }

    return languageMap[normalizedLang] || language.charAt(0).toUpperCase() + language.slice(1).toLowerCase()
  }

  /**
    * Implementation details.
   */
  static getLanguageInfo(language: string): { color: string, name: string } {
    return {
      color: this.getColor(language),
      name: this.getNormalizedLanguage(language),
    }
  }
}

// Implementation note.
type DifficultyLevel = 1 | 2 | 3 | number

export class DifficultyColorUtil {
  // Implementation note.
  private static readonly DIFFICULTY_COLOR_MAP: Record<number, string> = {
    1: '#52C41A', // Implementation note.
    2: '#FAAD14', // Implementation note.
    3: '#FF4D4F', // Implementation note.
  }

  // Implementation note.
  private static readonly DIFFICULTY_TEXT_MAP: Record<number, string> = {
    1: 'Easy',
    2: 'Medium',
    3: 'Hard',
  }

  /**
    * Implementation details.
   */
  static getColor(difficulty: DifficultyLevel): string {
    return this.DIFFICULTY_COLOR_MAP[difficulty] || this.generateColorForLevel(difficulty)
  }

  /**
    * Implementation details.
   */
  static getText(difficulty: DifficultyLevel): string {
    return this.DIFFICULTY_TEXT_MAP[difficulty] || `Difficulty${difficulty}`
  }

  /**
    * Implementation details.
   */
  static getDifficultyInfo(difficulty: DifficultyLevel): { color: string, text: string } {
    return {
      color: this.getColor(difficulty),
      text: this.getText(difficulty),
    }
  }

  /**
    * Implementation details.
   */
  private static generateColorForLevel(level: number): string {
    // Implementation note.
    if (level <= 1)
      return '#52C41A' // Easy
    if (level >= 10)
      return '#820014' // Extreme

    // Implementation note.
    const ratio = Math.min(level / 10, 1) // Implementation note.
    if (ratio <= 0.5) {
      // Implementation note.
      const green = Math.round(196 - (196 - 173) * ratio * 2)
      const red = Math.round(82 + (250 - 82) * ratio * 2)
      return `#${red.toString(16).padStart(2, '0')}${green.toString(16).padStart(2, '0')}14`
    }
    else {
      // Implementation note.
      const green = Math.round(173 - (173 - 77) * (ratio - 0.5) * 2)
      const red = 250
      return `#${red.toString(16).padStart(2, '0')}${green.toString(16).padStart(2, '0')}4D`
    }
  }
}

export class RandomColorUtil {
  /**
    * Implementation details.
   */
  static generate(): string {
    const letters = '0123456789ABCDEF'
    let color = '#'
    for (let i = 0; i < 6; i++) {
      color += letters[Math.floor(Math.random() * 16)]
    }
    return color
  }

  /**
    * Implementation details.
   */
  static generateSoft(): string {
    // Implementation note.
    const hue = Math.floor(Math.random() * 360)
    const saturation = 40 + Math.floor(Math.random() * 40) // 40-80%
    const lightness = 50 + Math.floor(Math.random() * 20) // 50-70%

    return this.hslToHex(hue, saturation, lightness)
  }

  /**
    * Implementation details.
    * Implementation details.
    * Implementation details.
   */
  static generateMultiple(count: number, soft: boolean = true): string[] {
    const colors: string[] = []
    const existingColors = new Set<string>()

    while (colors.length < count) {
      const color = soft ? this.generateSoft() : this.generate()

      // Implementation note.
      if (!existingColors.has(color)) {
        existingColors.add(color)
        colors.push(color)
      }

      // Implementation note.
      if (existingColors.size > 1000)
        break
    }

    return colors
  }

  /**
    * Implementation details.
   */
  static generateFromPool(): string {
    const colorPool = [
      '#1890FF',
      '#52C41A',
      '#FAAD14',
      '#FF4D4F',
      '#722ED1',
      '#13C2C2',
      '#EB2F96',
      '#F5222D',
      '#FA541C',
      '#FA8C16',
      '#FADB14',
      '#A0D911',
      '#52C41A',
      '#1890FF',
      '#2F54EB',
      '#722ED1',
      '#EB2F96',
      '#FA8C16',
      '#FAAD14',
      '#FFC53D',
    ]

    return colorPool[Math.floor(Math.random() * colorPool.length)]
  }

  /**
    * Implementation details.
   */
  private static hslToHex(h: number, s: number, l: number): string {
    s /= 100
    l /= 100

    const c = (1 - Math.abs(2 * l - 1)) * s
    const x = c * (1 - Math.abs((h / 60) % 2 - 1))
    const m = l - c / 2

    let r = 0; let g = 0; let b = 0

    if (h >= 0 && h < 60) {
      r = c; g = x; b = 0
    }
    else if (h >= 60 && h < 120) {
      r = x; g = c; b = 0
    }
    else if (h >= 120 && h < 180) {
      r = 0; g = c; b = x
    }
    else if (h >= 180 && h < 240) {
      r = 0; g = x; b = c
    }
    else if (h >= 240 && h < 300) {
      r = x; g = 0; b = c
    }
    else if (h >= 300 && h < 360) {
      r = c; g = 0; b = x
    }

    r = Math.round((r + m) * 255)
    g = Math.round((g + m) * 255)
    b = Math.round((b + m) * 255)

    return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`
  }
}

export class RankColorUtil {
  // Implementation note.
  private static readonly RANK_COLOR_MAP: Record<number, string> = {
    1: '#FFD700', // Implementation note.
    2: '#C0C0C0', // Implementation note.
    3: '#CD7F32', // Implementation note.
  }

  // Implementation note.
  private static readonly RANK_TEXT_MAP: Record<number, string> = {
    1: '🥇',
    2: '🥈',
    3: '🥉',
  }

  /**
    * Implementation details.
   */
  static getColor(rank: number): string {
    if (rank <= 3) {
      return this.RANK_COLOR_MAP[rank]
    }

    return '#60a5fa'
  }

  /**
    * Implementation details.
   */
  static getIcon(rank: number): string {
    if (rank <= 3) {
      return this.RANK_TEXT_MAP[rank]
    }
    // return `${rank}`
    return ''
  }

  /**
    * Implementation details.
   */
  static getDisplayText(rank: number): string {
    if (rank <= 3) {
    //   return `${this.RANK_TEXT_MAP[rank]} #${rank}`
      return `#${rank}`
    }
    return `#${rank}`
  }

  /**
    * Implementation details.
   */
  static getDisplayTextNoPE(rank: number): string {
    if (rank <= 3) {
      //   return `${this.RANK_TEXT_MAP[rank]} #${rank}`
      return `${rank}`
    }
    return `${rank}`
  }

  /**
    * Implementation details.
   */
  static getDisplayTextPE(rank: number): string {
    if (rank <= 3) {
      return `${this.RANK_TEXT_MAP[rank]} #${rank}`
    }
    return `#${rank}`
  }

  /**
    * Implementation details.
   */
  static getDisplayTextAPE(rank: number): string {
    if (rank <= 3) {
      return `${this.RANK_TEXT_MAP[rank]}`
    }
    return `${rank}`
  }

  /**
    * Implementation details.
   */
  static getRankInfo(rank: number): { color: string, icon: string, text: string } {
    return {
      color: this.getColor(rank),
      icon: this.getIcon(rank),
      text: this.getDisplayText(rank),
    }
  }

  /**
    * Implementation details.
   */
  static getRankStyle(rank: number): { color: string, fontWeight: string, fontSize: string } {
    let fontWeight = 'normal'
    let fontSize = '14px'

    if (rank === 1) {
      fontWeight = 'bold'
      fontSize = '16px'
    }
    else if (rank <= 3) {
      fontWeight = '600'
      fontSize = '15px'
    }
    else if (rank <= 10) {
      fontWeight = '500'
    }

    return {
      color: this.getColor(rank),
      fontWeight,
      fontSize,
    }
  }
}

// Implementation note.
const STATUS_MAP: Record<JudgeStatus, string> = {
  PENDING: 'Pending',
  JUDGING: 'Judging',
  COMPILING: 'Compiling',
  COMPILED_OK: 'Compiled',
  RUNNING: 'Running',
  ACCEPTED: 'Accepted',
  WRONG_ANSWER: 'Wrong Answer',
  TIME_LIMIT_EXCEEDED: 'Time Limit Exceeded',
  MEMORY_LIMIT_EXCEEDED: 'Memory Limit Exceeded',
  RUNTIME_ERROR: 'Runtime Error',
  COMPILATION_ERROR: 'Compilation Error',
  PRESENTATION_ERROR: 'Presentation Error',
  OUTPUT_LIMIT_EXCEEDED: 'Output Limit Exceeded',
  PARTIAL_ACCEPTED: 'Partially Accepted',
  SYSTEM_ERROR: 'System Error',
  RESTRICTED_FUNCTION: 'Restricted Function',
  DANGEROUS_OPERATION: 'Dangerous Operation',
  QUEUING: 'Queued',
  HIDDEN: 'Hidden',
  SKIPPED: 'Skipped',
  FROZEN: 'Frozen',
  FIRST_BLOOD: 'First Accepted',
  HACKED: 'Hacked',
  PENDING_REJUDGE: 'Pending Rejudge',
  REJUDGING: 'Rejudging',
  UNKNOWN_ERROR: 'Unknown Error',
  VALIDATOR_ERROR: 'Validator Error',
  CHECKER_ERROR: 'Checker Error',
  IDLENESS_LIMIT_EXCEEDED: 'Idleness Limit Exceeded',
  SECURITY_VIOLATION: 'Security Violation',
  IGNORED: 'Ignored',
  SIMILARITY_SUSPICIOUS: 'Suspicious Submission',
  SIMILARITY_ACCEPTED: 'Accepted',
}

// Implementation note.
export class StatusUtils {
  /**
    * Implementation details.
   * @param key Statuskey
    * Implementation details.
   */
  static getStatusText(key: JudgeStatus): string {
    return STATUS_MAP[key] || 'Unknown Status'
  }

  /**
    * Implementation details.
    * Implementation details.
   */
  static getAllStatus(): Record<JudgeStatus, string> {
    return { ...STATUS_MAP }
  }

  /**
    * Implementation details.
    * Implementation details.
    * Implementation details.
   */
  static hasStatus(key: string): key is JudgeStatus {
    return key in STATUS_MAP
  }

  /**
    * Implementation details.
   * @param key Statuskey
    * Implementation details.
    * Implementation details.
   */
  static getStatusTextSafe(key: string, defaultValue: string = 'Unknown Status'): string {
    return this.hasStatus(key as JudgeStatus) ? STATUS_MAP[key as JudgeStatus] : defaultValue
  }
}
