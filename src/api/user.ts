import { request } from '@/utils'
import { createCrudApi } from '@/utils/http/crud'

export const userApi = createCrudApi({ basePath: '/api/v1/sys/user' })

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
