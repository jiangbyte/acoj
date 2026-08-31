/** Author: Charlie */

const CHINA_OFFSET_MS = 8 * 60 * 60 * 1000
const LOCAL_DATE_TIME_PATTERN =
  /^(\d{4})-(\d{1,2})-(\d{1,2})(?:[T\s](\d{1,2})(?::(\d{1,2})(?::(\d{1,2})(?:\.\d+)?)?)?)?$/
const TIME_ZONE_PATTERN = /(?:[zZ]|[+-]\d{2}:?\d{2})$/

export function formatDateTime(value: unknown, fallback = '-') {
  if (value === undefined || value === null || value === '') {
    return fallback
  }

  if (value instanceof Date) {
    return Number.isNaN(value.getTime()) ? fallback : formatChinaDate(value.getTime())
  }

  if (typeof value === 'number') {
    return formatTimestamp(value, fallback)
  }

  const text = String(value).trim()
  if (!text) {
    return fallback
  }

  if (/^\d+$/.test(text)) {
    return formatTimestamp(Number(text), fallback)
  }

  const normalized = text.replace(/\//g, '-')
  if (!TIME_ZONE_PATTERN.test(normalized)) {
    const localMatch = normalized.match(LOCAL_DATE_TIME_PATTERN)
    if (localMatch) {
      return formatLocalMatch(localMatch)
    }
  }

  const timestamp = Date.parse(normalized)
  return Number.isNaN(timestamp) ? text : formatChinaDate(timestamp)
}

export function formatDateMinute(value: unknown, fallback = '-') {
  const full = formatDateTime(value, fallback)
  if (full === fallback || full.length < 16) {
    return full
  }
  return full.slice(0, 16)
}

/** API/任意时间 → 表单展示用中国本地时间；空则 null */
export function toFormDateTime(value: unknown): string | null {
  const text = formatDateTime(value, '')
  return text || null
}

/**
 * 表单/展示时间 → API ISO8601 UTC（无毫秒）。
 * 已带时区或 ISO `T` 的值原样返回；中国本地 `YYYY-MM-DD HH:mm:ss` 按 +08:00 解释。
 */
export function toApiDateTime(value: unknown): string | null {
  if (value === undefined || value === null || value === '') return null
  const s = String(value).trim()
  if (!s) return null
  if (/[TZz]/.test(s) || /[+-]\d{2}:?\d{2}$/.test(s)) return s
  const text = formatDateTime(value, '')
  if (!text) return null
  const date = new Date(`${text.replace(' ', 'T')}+08:00`)
  return Number.isNaN(date.getTime()) ? null : date.toISOString().replace(/\.\d{3}Z$/, 'Z')
}

function formatTimestamp(value: number, fallback: string) {
  if (!Number.isFinite(value)) {
    return fallback
  }
  const timestamp = Math.abs(value) < 1_000_000_000_000 ? value * 1000 : value
  return formatChinaDate(timestamp)
}

function formatLocalMatch(match: RegExpMatchArray) {
  const [, year, month, day, hour = '0', minute = '0', second = '0'] = match
  return `${pad(year, 4)}-${pad(month)}-${pad(day)} ${pad(hour)}:${pad(minute)}:${pad(second)}`
}

function formatChinaDate(timestamp: number) {
  const date = new Date(timestamp + CHINA_OFFSET_MS)
  return (
    [date.getUTCFullYear(), pad(date.getUTCMonth() + 1), pad(date.getUTCDate())].join('-') +
    ` ${pad(date.getUTCHours())}:${pad(date.getUTCMinutes())}:${pad(date.getUTCSeconds())}`
  )
}

function pad(value: string | number, length = 2) {
  return String(value).padStart(length, '0')
}

/** 相对时间（中文简写）；无法解析时回退到完整时间。 */
export function formatRelativeTime(value: unknown, fallback = '-') {
  if (value === undefined || value === null || value === '') {
    return fallback
  }
  const full = formatDateTime(value, '')
  if (!full) {
    return fallback
  }
  const match = full.match(/^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/)
  if (!match) {
    return full
  }
  const [, y, m, d, hh, mm, ss] = match
  const target = Date.UTC(Number(y), Number(m) - 1, Number(d), Number(hh), Number(mm), Number(ss)) - CHINA_OFFSET_MS
  const now = Date.now()
  const diffSec = Math.floor((now - target) / 1000)
  if (!Number.isFinite(diffSec)) {
    return full
  }
  if (diffSec < 60) {
    return '刚刚'
  }
  if (diffSec < 3600) {
    return `${Math.floor(diffSec / 60)} 分钟前`
  }
  if (diffSec < 86400) {
    return `${Math.floor(diffSec / 3600)} 小时前`
  }
  if (diffSec < 86400 * 30) {
    return `${Math.floor(diffSec / 86400)} 天前`
  }
  if (diffSec < 86400 * 365) {
    return `${Math.floor(diffSec / (86400 * 30))} 个月前`
  }
  return `${Math.floor(diffSec / (86400 * 365))} 年前`
}

