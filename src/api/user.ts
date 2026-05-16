import { request } from '@/utils'

export function fetchUserPage(params: any) {
  return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/user/page', {
    params,
  })
}
export function fetchUserCreate(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/user/create', data)
}
export function fetchUserModify(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/user/modify', data)
}
export function fetchUserRemove(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/user/remove', data)
}
export function fetchUserDetail(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/user/detail', { params })
}

export function fetchUserGrantRole(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/user/grant-role', data)
}
export function fetchUserOwnRoles(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/user/own-roles', { params })
}
export function fetchUserGrantGroup(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/user/grant-group', data)
}
export function fetchUserOwnGroups(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/user/own-groups', { params })
}

export function fetchUserGrantPermission(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/user/grant-permission', data)
}

export function fetchUserOwnPermissionDetail(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/user/own-permission-detail', { params })
}

export function fetchUserUpdateProfile(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/user/update-profile', data)
}

export function fetchUserUpdateAvatar(data: { avatar: string }) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/user/update-avatar', data)
}

export function fetchUserUpdatePassword(data: { current_password: string; new_password: string }) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/user/update-password', data)
}
