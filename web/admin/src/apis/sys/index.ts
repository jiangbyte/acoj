import type { PageData, SysBannerItem, SysDictItem, SysDictTreeNode } from '@/types/api'
import type { ResponseResult } from '@hei/shared'
import { request } from '@/utils/http'

interface BackendPageData<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

export interface BannerQuery {
  page?: number
  page_size?: number
  title?: string
  display_scope?: string
  category?: string
  type?: string
  position?: string
  status?: string
}

export interface BannerPayload {
  id?: string
  title: string
  image: string
  url?: string | null
  link_type: string
  summary?: string | null
  description?: string | null
  category: string
  type: string
  position: string
  display_scope: string
  sort: number
  status: string
  start_at?: string | null
  end_at?: string | null
}

export interface DictQuery {
  page?: number
  page_size?: number
  code?: string
  category?: string
  parent_id?: string
  status?: string
}

export interface DictPayload {
  id?: string
  code: string
  label?: string | null
  value?: string | null
  color?: string | null
  category?: string | null
  parent_id?: string | null
  status: string
  sort: number
}

function compactParams(params: Record<string, unknown>) {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  )
}

async function send<T>(method: Promise<ResponseResult<T | null>> | Promise<T>) {
  const result = await method
  if (isResponseResult<T>(result)) {
    if (!result.isSuccess || result.data === null) {
      throw new Error(result.message)
    }
    return result.data
  }
  return result
}

function isResponseResult<T>(result: ResponseResult<T | null> | T): result is ResponseResult<T | null> {
  return Boolean(result && typeof result === 'object' && 'isSuccess' in result)
}

function toPageData<T>(data: BackendPageData<T>): PageData<T> {
  return {
    items: data.records,
    total: data.total,
    page: data.current,
    page_size: data.size,
  }
}

export async function listBanners(query: BannerQuery): Promise<PageData<SysBannerItem>> {
  const data = await send(
    request.get<ResponseResult<BackendPageData<SysBannerItem> | null>>('/api/v1/admin/banner/sys/banners/list', {
      params: compactParams({
        current: query.page,
        size: query.page_size,
        title: query.title,
        display_scope: query.display_scope,
        category: query.category,
        type: query.type,
        position: query.position,
        status: query.status,
      }),
    }),
  )
  return toPageData(data)
}

export function getBannerDetail(id: string) {
  return send(
    request.get<ResponseResult<SysBannerItem | null>>('/api/v1/admin/banner/sys/banners/detail', {
      params: { id },
    }),
  )
}

export function createBanner(payload: BannerPayload) {
  return send(request.post<ResponseResult<SysBannerItem | null>>('/api/v1/admin/banner/sys/banners/create', payload))
}

export function updateBanner(payload: BannerPayload & { id: string }) {
  return send(request.post<ResponseResult<SysBannerItem | null>>('/api/v1/admin/banner/sys/banners/update', payload))
}

export function deleteBanners(ids: string[]) {
  return send(request.post<ResponseResult<string[] | null>>('/api/v1/admin/banner/sys/banners/delete', { ids }))
}

export async function listDicts(query: DictQuery): Promise<PageData<SysDictItem>> {
  const data = await send(
    request.get<ResponseResult<BackendPageData<SysDictItem> | null>>('/api/v1/admin/dict/sys/dicts/list', {
      params: compactParams({
        current: query.page,
        size: query.page_size,
        code: query.code,
        category: query.category,
        parent_id: query.parent_id,
        status: query.status,
      }),
    }),
  )
  return toPageData(data)
}

export function getDictDetail(id: string) {
  return send(
    request.get<ResponseResult<SysDictItem | null>>('/api/v1/admin/dict/sys/dicts/detail', {
      params: { id },
    }),
  )
}

export function createDict(payload: DictPayload) {
  return send(request.post<ResponseResult<SysDictItem | null>>('/api/v1/admin/dict/sys/dicts/create', payload))
}

export function updateDict(payload: DictPayload & { id: string }) {
  return send(request.post<ResponseResult<SysDictItem | null>>('/api/v1/admin/dict/sys/dicts/update', payload))
}

export function deleteDicts(ids: string[]) {
  return send(request.post<ResponseResult<string[] | null>>('/api/v1/admin/dict/sys/dicts/delete', { ids }))
}

export function listDictTree(category?: string) {
  return send(
    request.get<ResponseResult<SysDictTreeNode[] | null>>('/api/v1/admin/dict/sys/dicts/tree', {
      params: compactParams({ category }),
    }),
  )
}
