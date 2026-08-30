/** Author: Charlie */

import type { FormItemRule, FormRules } from 'naive-ui'
import { createRequiredRule, isValidAccountLogin, isValidEmail, toNullableString } from '@/utils'
import { dictList } from '@/utils/dict'
import { encryptPasswords } from '@/utils/security'
import { wireFields } from '@/utils/wire'

export type AccountFormModel = {
  account: string
  password: string
  account_type: string
  account_status: string
  nickname: string
  avatar: string
  signature: string
  phone: string
  email: string
  remark?: string
}

export type LoginIdentityFormModel = {
  email: string
  phone: string
  email_login_enabled: boolean
  phone_login_enabled: boolean
}

export function createDefaultLoginIdentityForm(): LoginIdentityFormModel {
  return {
    email: '',
    phone: '',
    email_login_enabled: false,
    phone_login_enabled: false,
  }
}

const loginIdentityWireDefaults = createDefaultLoginIdentityForm()

/** 将详情 API 响应映射为登录身份表单（显式 wire 转换）。 */
export function mapLoginIdentityFormFromDetail(
  data: Record<string, unknown> = {},
): LoginIdentityFormModel {
  const wired = wireFields(
    data,
    {
      email_login_enabled: 'bool',
      phone_login_enabled: 'bool',
    },
    loginIdentityWireDefaults,
  )
  return {
    ...loginIdentityWireDefaults,
    email: String(data.email_identity ?? data.email ?? ''),
    phone: String(data.phone_identity ?? data.phone ?? ''),
    ...wired,
  }
}

/** 将详情 API 响应映射为账号 + 资料表单。 */
export function mapAccountFormFromDetail(
  data: Record<string, unknown>,
  accountType: string,
  options: { includeRemark?: boolean } = {},
): AccountFormModel {
  const form: AccountFormModel = {
    account_type: accountType,
    account_status: String(data.account_status ?? 'ENABLED'),
    password: '',
    account: String(data.account ?? ''),
    nickname: String(data.nickname ?? ''),
    avatar: String(data.avatar ?? ''),
    signature: String(data.signature ?? ''),
    phone: String(data.phone ?? ''),
    email: String(data.email ?? ''),
  }
  if (options.includeRemark) {
    form.remark = String(data.remark ?? '')
  }
  return form
}

export function accountStatusOptions() {
  return dictList('ACCOUNT_STATUS').filter((o: any) => !String(o.value).includes('CANCELLED'))
}

export function createAccountFormRules(
  isEdit: () => boolean,
): FormRules {
  return {
    account: [
      createRequiredRule('账号', 'input'),
      {
        validator: (_rule, value) => {
          if (!isValidAccountLogin(value)) {
            return new Error('账号仅允许字母、数字和下划线，长度 3-64')
          }
          return true
        },
        trigger: ['input', 'blur'],
      },
    ],
    password: [
      {
        validator: (_rule, value) => {
          if (!isEdit() && !String(value ?? '').trim()) {
            return new Error('请输入密码')
          }
          return true
        },
        trigger: ['input', 'blur'],
      },
    ],
    account_status: createRequiredRule('账号状态', 'change'),
  }
}

export function createLoginIdentityFormRules(
  form: LoginIdentityFormModel,
): FormRules {
  return {
    email: [
      {
        validator: (_rule: FormItemRule, value: string) => {
          const text = String(value ?? '').trim()
          if (!text) {
            return form.email_login_enabled ? new Error('请输入邮箱') : true
          }
          if (!isValidEmail(text)) {
            return new Error('请输入有效邮箱')
          }
          return true
        },
        trigger: ['input', 'blur'],
      },
    ],
    phone: [
      {
        validator: (_rule: FormItemRule, value: string) => {
          const text = String(value ?? '').trim()
          if (!text && form.phone_login_enabled) {
            return new Error('请输入手机号')
          }
          return true
        },
        trigger: ['input', 'blur'],
      },
    ],
  }
}

/** 组装 create/update 请求：仅 sys_account + profile 字段。 */
export async function buildAccountPayload(form: AccountFormModel) {
  const payload: Record<string, unknown> = {
    account: form.account.trim(),
    password: toNullableString(form.password),
    account_type: form.account_type,
    account_status: form.account_status,
    nickname: toNullableString(form.nickname),
    avatar: toNullableString(form.avatar),
    signature: toNullableString(form.signature),
    phone: toNullableString(form.phone),
    email: toNullableString(form.email),
  }
  if (form.remark !== undefined) {
    payload.remark = toNullableString(form.remark)
  }

  if (payload.password) {
    const encrypted = await encryptPasswords({ password: String(payload.password) })
    payload.password = encrypted.values.password
    payload.password_key_id = encrypted.password_key_id
  } else {
    payload.password_key_id = null
  }

  return payload
}

export function buildLoginIdentityPayload(accountId: string, form: LoginIdentityFormModel) {
  return {
    id: accountId,
    email_login_enabled: form.email_login_enabled,
    email: toNullableString(form.email),
    phone_login_enabled: form.phone_login_enabled,
    phone: toNullableString(form.phone),
  }
}
