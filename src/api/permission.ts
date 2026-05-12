import { request } from '@/utils'

export function fetchPermissionModules() {
  return request.Get<Service.ResponseResult<string[]>>('/api/v1/sys/permission/modules')
}

export function fetchPermissionByModule(params: { module: string }) {
  return request.Get<Service.ResponseResult>('/api/v1/sys/permission/by-module', { params })
}
