// 定义状态枚举类型
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

// 状态颜色映射工具类
export class StatusColorUtil {
  private static readonly COLOR_MAP: Record<JudgeStatus, string> = {
    // 编译相关状态 - 蓝色系
    COMPILING: '#1890FF',
    COMPILATION_ERROR: '#FF6B6B',
    COMPILED_OK: '#52C41A',

    // 执行时异常 - 橙色系
    RUNTIME_ERROR: '#FA8C16',
    TIME_LIMIT_EXCEEDED: '#FA541C',
    MEMORY_LIMIT_EXCEEDED: '#FAAD14',
    OUTPUT_LIMIT_EXCEEDED: '#D48806',

    // 答案相关状态 - 绿色/红色系
    ACCEPTED: '#52C41A',
    WRONG_ANSWER: '#FF4D4F',
    PARTIAL_ACCEPTED: '#A0D911',
    PRESENTATION_ERROR: '#D46B08',

    // 其他重要状态
    SYSTEM_ERROR: '#CF1322',
    RUNNING: '#13C2C2',
    JUDGING: '#1890FF',

    // 中性状态（无颜色或默认灰色）
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
   * 根据状态获取对应的颜色值
   * @param status 判题状态
   * @returns 十六进制颜色值
   */
  static getColor(status: JudgeStatus): string {
    return this.COLOR_MAP[status] || '#D9D9D9' // 默认返回灰色
  }

  /**
   * 获取所有状态的颜色映射
   * @returns 完整的颜色映射对象
   */
  static getAllColors(): Record<JudgeStatus, string> {
    return { ...this.COLOR_MAP }
  }

  /**
   * 添加或更新状态颜色
   * @param status 状态
   * @param color 颜色值
   */
  static setColor(status: JudgeStatus, color: string): void {
    this.COLOR_MAP[status] = color
  }
}

// 方案1：使用 boolean 作为键，但需要类型转换
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

// 常见编程语言类型
type ProgrammingLanguage
  = | 'java' | 'python' | 'javascript' | 'typescript' | 'c' | 'cpp' | 'c++'
    | 'csharp' | 'c#' | 'go' | 'rust' | 'php' | 'ruby' | 'swift' | 'kotlin'
    | 'scala' | 'r' | 'matlab' | 'html' | 'css' | 'sql' | 'bash' | 'shell'
    | 'perl' | 'lua' | 'dart' | 'haskell' | 'elixir' | 'clojure' | 'erlang'
    | 'objective-c' | 'vb' | 'assembly' | 'pascal' | 'fortran' | 'cobol'
    | string // 支持其他未知语言

export class LanguageColorUtil {
  // 常见编程语言颜色映射（参考 GitHub 语言颜色）
  private static readonly LANGUAGE_COLOR_MAP: Record<string, string> = {
    // 主流语言
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

    // 其他语言
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
   * 根据语言名称获取颜色（不区分大小写）
   */
  static getColor(language: string): string {
    if (!language)
      return '#D9D9D9'

    const normalizedLang = language.toLowerCase().trim()
    return this.LANGUAGE_COLOR_MAP[normalizedLang] || this.generateColorFromString(language)
  }

  /**
   * 根据语言名称生成确定性颜色（用于未知语言）
   */
  private static generateColorFromString(str: string): string {
    let hash = 0
    for (let i = 0; i < str.length; i++) {
      hash = str.charCodeAt(i) + ((hash << 5) - hash)
    }

    // 生成柔和的颜色（避免太亮或太暗）
    const hue = hash % 360
    const saturation = 70 + (hash % 20) // 70-90%
    const lightness = 50 + (hash % 15) // 50-65%

    return `hsl(${hue}, ${saturation}%, ${lightness}%)`
  }

  /**
   * 获取标准化后的语言名称
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
   * 获取语言信息（颜色和标准化名称）
   */
  static getLanguageInfo(language: string): { color: string, name: string } {
    return {
      color: this.getColor(language),
      name: this.getNormalizedLanguage(language),
    }
  }
}

// 难度等级类型
type DifficultyLevel = 1 | 2 | 3 | number

export class DifficultyColorUtil {
  // 难度颜色映射
  private static readonly DIFFICULTY_COLOR_MAP: Record<number, string> = {
    1: '#52C41A', // 简单 - 绿色
    2: '#FAAD14', // 中等 - 橙色
    3: '#FF4D4F', // 困难 - 红色
  }

  // 难度文本映射
  private static readonly DIFFICULTY_TEXT_MAP: Record<number, string> = {
    1: '简单',
    2: '中等',
    3: '困难',
  }

  /**
   * 根据难度等级获取颜色
   */
  static getColor(difficulty: DifficultyLevel): string {
    return this.DIFFICULTY_COLOR_MAP[difficulty] || this.generateColorForLevel(difficulty)
  }

  /**
   * 获取难度文本
   */
  static getText(difficulty: DifficultyLevel): string {
    return this.DIFFICULTY_TEXT_MAP[difficulty] || `难度${difficulty}`
  }

  /**
   * 获取难度信息（颜色和文本）
   */
  static getDifficultyInfo(difficulty: DifficultyLevel): { color: string, text: string } {
    return {
      color: this.getColor(difficulty),
      text: this.getText(difficulty),
    }
  }

  /**
   * 为超出范围的难度等级生成颜色
   */
  private static generateColorForLevel(level: number): string {
    // 等级越高，颜色越深（从绿色到红色渐变）
    if (level <= 1)
      return '#52C41A' // 简单
    if (level >= 10)
      return '#820014' // 极难

    // 在绿色到红色之间渐变
    const ratio = Math.min(level / 10, 1) // 限制在0-1之间
    if (ratio <= 0.5) {
      // 绿色到黄色渐变
      const green = Math.round(196 - (196 - 173) * ratio * 2)
      const red = Math.round(82 + (250 - 82) * ratio * 2)
      return `#${red.toString(16).padStart(2, '0')}${green.toString(16).padStart(2, '0')}14`
    }
    else {
      // 黄色到红色渐变
      const green = Math.round(173 - (173 - 77) * (ratio - 0.5) * 2)
      const red = 250
      return `#${red.toString(16).padStart(2, '0')}${green.toString(16).padStart(2, '0')}4D`
    }
  }
}

export class RandomColorUtil {
  /**
   * 生成随机的十六进制颜色
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
   * 生成柔和的随机颜色（避免太亮或太刺眼的颜色）
   */
  static generateSoft(): string {
    // 生成在较柔和范围内的颜色
    const hue = Math.floor(Math.random() * 360)
    const saturation = 40 + Math.floor(Math.random() * 40) // 40-80%
    const lightness = 50 + Math.floor(Math.random() * 20) // 50-70%

    return this.hslToHex(hue, saturation, lightness)
  }

  /**
   * 生成一组不重复的随机颜色
   * @param count 颜色数量
   * @param soft 是否生成柔和颜色
   */
  static generateMultiple(count: number, soft: boolean = true): string[] {
    const colors: string[] = []
    const existingColors = new Set<string>()

    while (colors.length < count) {
      const color = soft ? this.generateSoft() : this.generate()

      // 确保颜色不重复
      if (!existingColors.has(color)) {
        existingColors.add(color)
        colors.push(color)
      }

      // 防止无限循环
      if (existingColors.size > 1000)
        break
    }

    return colors
  }

  /**
   * 从预定义的好看颜色池中随机选择一个
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
   * HSL颜色值转换为十六进制
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
  // 排行榜颜色映射
  private static readonly RANK_COLOR_MAP: Record<number, string> = {
    1: '#FFD700', // 金牌 - 金色
    2: '#C0C0C0', // 银牌 - 银色
    3: '#CD7F32', // 铜牌 - 古铜色
  }

  // 排名文本映射
  private static readonly RANK_TEXT_MAP: Record<number, string> = {
    1: '🥇',
    2: '🥈',
    3: '🥉',
  }

  /**
   * 根据排名获取颜色
   */
  static getColor(rank: number): string {
    if (rank <= 3) {
      return this.RANK_COLOR_MAP[rank]
    }

    // 第4-10名使用渐变的蓝色系
    if (rank <= 10) {
      const blueShades = [
        '#1890FF',
        '#40A9FF',
        '#69C0FF',
        '#91D5FF',
        '#BAE7FF',
        '#1890FF',
        '#40A9FF',
        '#69C0FF',
        '#91D5FF',
        '#BAE7FF',
      ]
      return blueShades[rank - 1] || '#1890FF'
    }

    // 第11名及以后使用灰色系
    if (rank <= 20) {
      const grayShades = [
        '#8C8C8C',
        '#999999',
        '#A6A6A6',
        '#B3B3B3',
        '#BFBFBF',
        '#CCCCCC',
        '#D9D9D9',
        '#E6E6E6',
        '#F0F0F0',
        '#F5F5F5',
      ]
      return grayShades[rank - 11] || '#D9D9D9'
    }

    // 20名以后统一用浅灰色
    return '#F5F5F5'
  }

  /**
   * 获取排名图标
   */
  static getIcon(rank: number): string {
    if (rank <= 3) {
      return this.RANK_TEXT_MAP[rank]
    }
    // return `${rank}`
    return ''
  }

  /**
   * 获取排名显示文本
   */
  static getDisplayText(rank: number): string {
    if (rank <= 3) {
    //   return `${this.RANK_TEXT_MAP[rank]} 第${rank}名`
      return `第${rank}名`
    }
    return `第${rank}名`
  }

  /**
   * 获取排名显示文本
   */
  static getDisplayTextNoPE(rank: number): string {
    if (rank <= 3) {
      //   return `${this.RANK_TEXT_MAP[rank]} 第${rank}名`
      return `${rank}`
    }
    return `${rank}`
  }

  /**
   * 获取排名显示文本
   */
  static getDisplayTextPE(rank: number): string {
    if (rank <= 3) {
      return `${this.RANK_TEXT_MAP[rank]} 第${rank}名`
    }
    return `第${rank}名`
  }

  /**
   * 获取排名显示文本
   */
  static getDisplayTextAPE(rank: number): string {
    if (rank <= 3) {
      return `${this.RANK_TEXT_MAP[rank]}`
    }
    return `${rank}`
  }

  /**
   * 获取完整的排名信息
   */
  static getRankInfo(rank: number): { color: string, icon: string, text: string } {
    return {
      color: this.getColor(rank),
      icon: this.getIcon(rank),
      text: this.getDisplayText(rank),
    }
  }

  /**
   * 获取排名样式配置（用于UI组件）
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

// 状态映射对象
const STATUS_MAP: Record<JudgeStatus, string> = {
  PENDING: '等待判题',
  JUDGING: '判题中',
  COMPILING: '编译中',
  COMPILED_OK: '编译成功',
  RUNNING: '运行中',
  ACCEPTED: '答案正确',
  WRONG_ANSWER: '答案错误',
  TIME_LIMIT_EXCEEDED: '时间超出限制',
  MEMORY_LIMIT_EXCEEDED: '内存超出限制',
  RUNTIME_ERROR: '运行时错误',
  COMPILATION_ERROR: '编译错误',
  PRESENTATION_ERROR: '格式错误',
  OUTPUT_LIMIT_EXCEEDED: '输出超出限制',
  PARTIAL_ACCEPTED: '部分正确',
  SYSTEM_ERROR: '系统错误',
  RESTRICTED_FUNCTION: '使用了限制函数',
  DANGEROUS_OPERATION: '危险操作',
  QUEUING: '排队中',
  HIDDEN: '隐藏',
  SKIPPED: '跳过',
  FROZEN: '冻结',
  FIRST_BLOOD: '第一个通过该题目',
  HACKED: '被成功hack',
  PENDING_REJUDGE: '等待重新判题',
  REJUDGING: '重新判题中',
  UNKNOWN_ERROR: '未知错误',
  VALIDATOR_ERROR: '验证器错误',
  CHECKER_ERROR: '检查器错误',
  IDLENESS_LIMIT_EXCEEDED: '空闲时间超出限制',
  SECURITY_VIOLATION: '安全违规',
  IGNORED: '被忽略',
  SIMILARITY_SUSPICIOUS: '可疑提交',
  SIMILARITY_ACCEPTED: '通过',
}

// 工具函数
export class StatusUtils {
  /**
   * 根据状态key获取对应的描述
   * @param key 状态key
   * @returns 对应的描述字符串，如果key不存在则返回未知状态
   */
  static getStatusText(key: JudgeStatus): string {
    return STATUS_MAP[key] || '未知状态'
  }

  /**
   * 获取所有状态映射
   * @returns 完整的状态映射对象
   */
  static getAllStatus(): Record<JudgeStatus, string> {
    return { ...STATUS_MAP }
  }

  /**
   * 检查key是否存在
   * @param key 要检查的key
   * @returns 是否存在
   */
  static hasStatus(key: string): key is JudgeStatus {
    return key in STATUS_MAP
  }

  /**
   * 安全获取状态文本，如果key不存在返回默认值
   * @param key 状态key
   * @param defaultValue 默认值，默认为"未知状态"
   * @returns 状态描述
   */
  static getStatusTextSafe(key: string, defaultValue: string = '未知状态'): string {
    return this.hasStatus(key as JudgeStatus) ? STATUS_MAP[key as JudgeStatus] : defaultValue
  }
}
