import { useMemo } from 'react'

type Props = {
  password: string
}

interface StrengthLevel {
  label: string
  color: string
  percent: number
}

const levels: StrengthLevel[] = [
  { label: '弱', color: '#e74c3c', percent: 25 },
  { label: '较弱', color: '#f39c12', percent: 50 },
  { label: '中等', color: '#f1c40f', percent: 75 },
  { label: '强', color: '#2ecc71', percent: 100 },
]

const policyItems = [
  { label: '至少 8 个字符', met: (pwd: string) => pwd.length >= 8 },
  { label: '包含大写字母', met: (pwd: string) => /[A-Z]/.test(pwd) },
  { label: '包含小写字母', met: (pwd: string) => /[a-z]/.test(pwd) },
  { label: '包含数字', met: (pwd: string) => /[0-9]/.test(pwd) },
  { label: '包含特殊字符', met: (pwd: string) => /[^A-Za-z0-9]/.test(pwd) },
]

export function PasswordStrength({ password }: Props) {
  const pwd = password ?? ''

  const strength = useMemo(() => {
    if (!pwd) {
      return { label: '', color: '#e0e0e0', percent: 0 }
    }
    let score = 0
    if (pwd.length >= 8) score += 1
    if (pwd.length >= 12) score += 1
    if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) score += 1
    if (/[0-9]/.test(pwd) && /[^A-Za-z0-9]/.test(pwd)) score += 1
    return levels[Math.min(score, levels.length - 1)]
  }, [pwd])

  if (!pwd) {
    return null
  }

  return (
    <div className="password-strength-bar mt-1">
      <div className="h-1 rounded overflow-hidden bg-gray-100 mb-1">
        <div
          className="h-full rounded transition-all duration-300"
          style={{ width: `${strength.percent}%`, backgroundColor: strength.color }}
        />
      </div>
      <span className="text-xs font-semibold inline-block mb-1" style={{ color: strength.color }}>
        {strength.label}
      </span>
      <div className="flex flex-wrap gap-x-3 gap-y-0.5">
        {policyItems.map((item) => {
          const met = item.met(pwd)
          return (
            <span
              key={item.label}
              className={`inline-flex items-center gap-0.5 text-[11px] ${
                met ? 'text-gray-800' : 'text-gray-400'
              }`}
            >
              <span className={`text-[10px] ${met ? 'text-green-500' : 'text-gray-300'}`}>
                {met ? '✓' : '○'}
              </span>
              {item.label}
            </span>
          )
        })}
      </div>
    </div>
  )
}
