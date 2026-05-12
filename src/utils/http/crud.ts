import { request } from './index'

interface CrudConfig {
  basePath: string
  hasTree?: boolean
}

export function createCrudApi<T = any>(config: CrudConfig) {
  const { basePath, hasTree } = config

  return {
    page(params: any) {
      return request.Get<Service.ResponseResult<Service.PageResult<T>>>(`${basePath}/page`, { params })
    },
    create(data: any) {
      return request.Post<Service.ResponseResult<T>>(`${basePath}/create`, data)
    },
    modify(data: any) {
      return request.Post<Service.ResponseResult<T>>(`${basePath}/modify`, data)
    },
    remove(data: { ids: string[] }) {
      return request.Post<Service.ResponseResult>(`${basePath}/remove`, data)
    },
    detail(params: { id: string; [key: string]: any }) {
      return request.Get<Service.ResponseResult<T>>(`${basePath}/detail`, { params })
    },
    export(params: any) {
      return request.Get(`${basePath}/export`, { params, meta: { isBlob: true } }) as Promise<Blob>
    },
    template() {
      return request.Get(`${basePath}/template`, { meta: { isBlob: true } }) as Promise<Blob>
    },
    importFile(file: File) {
      const formData = new FormData()
      formData.append('file', file)
      return request.Post<Service.ResponseResult>(`${basePath}/import`, formData)
    },
    ...(hasTree
      ? {
          tree(params: any = {}) {
            return request.Get<Service.ResponseResult>(`${basePath}/tree`, { params })
          },
        }
      : {}),
  }
}
