import { request } from '@/utils'

export function fetchHome() {
  return request.Get<Service.ResponseResult<any>>('/api/v1/sys/home')
}

export function addQuickAction(resource_id: string) {
  return request.Post<Service.ResponseResult<null>>('/api/v1/sys/home/quick-actions/add', { resource_id })
}

export function removeQuickAction(id: string) {
  return request.Post<Service.ResponseResult<null>>('/api/v1/sys/home/quick-actions/remove', { id })
}

export function sortQuickActions(ids: string[]) {
  return request.Post<Service.ResponseResult<null>>('/api/v1/sys/home/quick-actions/sort', { ids })
}
