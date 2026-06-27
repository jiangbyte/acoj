import type { Gender, RoleOption, UserStatus } from './types'

export const genderOptions: Array<{ labelKey: string; value: Gender }> = [
  { labelKey: 'common.status.male', value: '1' },
  { labelKey: 'common.status.female', value: '0' },
  { labelKey: 'common.status.other', value: '-1' },
]

export const statusOptions: Array<{ labelKey: string; value: UserStatus }> = [
  { labelKey: 'common.often.enable', value: '1' },
  { labelKey: 'common.often.disable', value: '0' },
]

export const roleOptions: RoleOption[] = [
  { labelKey: 'pages.system.user.roles.superAdmin', value: 'super-admin' },
  { labelKey: 'pages.system.user.roles.admin', value: 'admin' },
  { labelKey: 'pages.system.user.roles.problemAdmin', value: 'problem-admin' },
  { labelKey: 'pages.system.user.roles.user', value: 'user' },
]

export const genderLabelKeyMap: Record<Gender, string> = {
  '1': 'common.status.male',
  '0': 'common.status.female',
  '-1': 'common.status.other',
}

export const statusLabelKeyMap: Record<UserStatus, string> = {
  '1': 'common.often.enable',
  '0': 'common.often.disable',
}

export const genderTagTypeMap: Record<Gender, 'success' | 'error' | 'default'> = {
  '1': 'success',
  '0': 'error',
  '-1': 'default',
}

export const statusTagTypeMap: Record<UserStatus, 'success' | 'error'> = {
  '1': 'success',
  '0': 'error',
}
