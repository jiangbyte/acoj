import { generate } from '@ant-design/colors'

export type ThemeMode = 'light' | 'dark' | 'realDark'

const THEME_VAR_ID = 'hei-theme-var'
const DARK_CLASS = 'hei-theme-dark'

function hexToRgb(hex: string): string {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  if (!result) return '0 0 0'
  return `${parseInt(result[1], 16)} ${parseInt(result[2], 16)} ${parseInt(result[3], 16)}`
}

export function changeColor(newPrimaryColor: string, theme: ThemeMode) {
  const old = document.querySelector(`#${THEME_VAR_ID}`)
  if (old?.parentNode) old.parentNode.removeChild(old)

  const isRealDark = theme === 'realDark'
  const colors = generate(newPrimaryColor, { theme: isRealDark ? 'dark' : 'default' })
  const rgb = hexToRgb(newPrimaryColor)

  const style = document.createElement('style')
  style.id = THEME_VAR_ID
  style.setAttribute('type', 'text/css')

  const cssVars = colors.map((c, i) => `--primary-${i + 1}:${c};`).join('')

  style.innerHTML = `:root {
  --primary-color: ${newPrimaryColor};
  --primary-rgb: ${rgb};
  ${cssVars}
}`
  document.head.appendChild(style)

  if (isRealDark) {
    document.documentElement.classList.add(DARK_CLASS)
  } else {
    document.documentElement.classList.remove(DARK_CLASS)
  }
}

export function initTheme(theme: ThemeMode, colorPrimary: string) {
  changeColor(colorPrimary, theme)
}

export function toggleGrayMode(enable: boolean) {
  document.documentElement.style.filter = enable ? 'grayscale(100%)' : ''
}

export function toggleColorWeak(enable: boolean) {
  if (enable) {
    document.documentElement.classList.add('color-weak')
  } else {
    document.documentElement.classList.remove('color-weak')
  }
}
