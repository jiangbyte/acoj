import type { FormItemRule } from 'naive-ui'

const USERNAME_REGEX = /^[a-zA-Z][a-zA-Z0-9_]*$/

export const usernameRule: FormItemRule = {
  required: true,
  trigger: ['blur', 'input'],
  validator(_rule, value: string) {
    if (!value) {
      return new Error('请输入用户名')
    }
    if (value.length < 2 || value.length > 15) {
      return new Error('用户名长度必须在2-15位之间')
    }
    if (!USERNAME_REGEX.test(value)) {
      return new Error('用户名须字母开头，仅含字母、数字、下划线')
    }
    return true
  },
}

export const passwordRule: FormItemRule = {
  required: true,
  trigger: ['blur', 'input'],
  validator(_rule, value: string) {
    if (!value) {
      return new Error('请输入密码')
    }
    if (value.length < 6 || value.length > 20) {
      return new Error('密码长度须为6-20位')
    }
    return true
  },
}

export const captchaRule: FormItemRule = {
  required: true,
  message: '请输入验证码',
  trigger: ['blur', 'input'],
}
