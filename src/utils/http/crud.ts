import { request } from './index'

interface CrudConfig {
  basePath: string
  hasTree?: boolean
}

export function createCrudApi(config: CrudConfig) {
  const { basePath, hasTree } = config

  return {
    page(params: any) {
      return request.Get<Service.ResponseResult<Service.PageResult>>(`${basePath}/page`, { params })
    },
    create(data: any) {
      return request.Post<Service.ResponseResult>(`${basePath}/create`, data)
    },
    modify(data: any) {
      return request.Post<Service.ResponseResult>(`${basePath}/modify`, data)
    },
    remove(data: { ids: string[] }) {
      return request.Post<Service.ResponseResult>(`${basePath}/remove`, data)
    },
    detail(params: { id: string }) {
      return request.Get<Service.ResponseResult>(`${basePath}/detail`, { params })
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
