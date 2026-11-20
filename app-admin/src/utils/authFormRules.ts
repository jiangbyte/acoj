import type { FormItemRule } from 'naive-ui'

const USERNAME_REGEX = /^[a-zA-Z][a-zA-Z0-9_]*$/

export const usernameRule: FormItemRule = {
  required: true,
  trigger: ['blur', 'input'],
  validator(_rule, value: string) {
    if (!value) {
      return new Error('Enter username')
    }
    if (value.length < 2 || value.length > 15) {
      return new Error('Username must be 2-15 characters')
    }
    if (!USERNAME_REGEX.test(value)) {
      return new Error('Username must start with a letter and contain only letters, numbers, and underscores')
    }
    return true
  },
}

export const passwordRule: FormItemRule = {
  required: true,
  trigger: ['blur', 'input'],
  validator(_rule, value: string) {
    if (!value) {
      return new Error('Enter password')
    }
    if (value.length < 6 || value.length > 20) {
      return new Error('Password must be 6-20 characters')
    }
    return true
  },
}

export const captchaRule: FormItemRule = {
  required: true,
  message: 'Enter verification code',
  trigger: ['blur', 'input'],
}
