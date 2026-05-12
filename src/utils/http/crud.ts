import { request } from './index'

interface CrudConfig {
  basePath: string
  hasTree?: boolean
}

interface CrudApiBase<T> {
  page(params: any): Promise<Service.ResponseResult<Service.PageResult<T>>>
  create(data: any): Promise<Service.ResponseResult<T>>
  modify(data: any): Promise<Service.ResponseResult<T>>
  remove(data: { ids: string[] }): Promise<Service.ResponseResult>
  detail(params: { id: string; [key: string]: any }): Promise<Service.ResponseResult<T>>
  export(params: any): Promise<Blob>
  template(): Promise<Blob>
  importFile(file: File): Promise<Service.ResponseResult>
}

interface CrudApiWithTree<T> extends CrudApiBase<T> {
  tree(params?: any): Promise<Service.ResponseResult>
}

export function createCrudApi<T = any>(config: CrudConfig & { hasTree: true }): CrudApiWithTree<T>
export function createCrudApi<T = any>(config: CrudConfig & { hasTree?: false }): CrudApiBase<T>
export function createCrudApi<T = any>(config: CrudConfig): CrudApiBase<T> | CrudApiWithTree<T> {
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
