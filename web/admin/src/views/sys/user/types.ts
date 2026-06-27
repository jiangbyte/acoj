export type Gender = '1' | '0' | '-1'
export type UserStatus = '1' | '0'

export interface User {
  id: string
  username: string
  nickname: string
  password: string
  roleIds: string[]
  gender: Gender
  status: UserStatus
  email?: string
  phone?: string
  remark?: string
  createTime: string
  updateTime: string
}

export interface UserFormModel {
  id?: string
  username: string
  nickname: string
  password: string
  roleIds: string[]
  gender: Gender
  status: UserStatus
  email?: string
  phone?: string
  remark?: string
}

export interface UserSearchParams {
  username?: string
  nickname?: string
  gender?: Gender
  status?: UserStatus
}

export interface RoleOption {
  labelKey: string
  value: string
}
