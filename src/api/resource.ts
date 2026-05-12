import { request } from '@/utils'
import { createCrudApi } from '@/utils/http/crud'

export const moduleApi = createCrudApi({ basePath: '/api/v1/sys/module' })

export const resourceApi = createCrudApi({ basePath: '/api/v1/sys/resource', hasTree: true })

export function fetchResourceTree() {
  return request.Get<Service.ResponseResult>('/api/v1/sys/resource/tree')
}
