import { request } from '@/utils'
import { createCrudApi } from '@/utils/http/crud'

export const roleApi = createCrudApi({ basePath: '/api/v1/sys/role' })

export function fetchRoleGrantPermission(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/role/grant-permission', data)
}
export function fetchRoleOwnPermissionDetail(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/role/own-permission-detail', { params })
}
export function fetchRoleGrantResource(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/sys/role/grant-resource', data)
}
export function fetchRoleOwnPermission(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/role/own-permission', { params })
}
export function fetchRoleOwnResource(params: any) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/role/own-resource', { params })
}
