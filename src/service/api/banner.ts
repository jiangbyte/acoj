import { request } from '../http'
export function fetchBannerPage(params: any) { return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/banner/page', { params }) }
export function fetchBannerCreate(data: any) { return request.Post<Service.ResponseResult>('/api/v1/sys/banner/create', data) }
export function fetchBannerModify(data: any) { return request.Post<Service.ResponseResult>('/api/v1/sys/banner/modify', data) }
export function fetchBannerRemove(data: any) { return request.Post<Service.ResponseResult>('/api/v1/sys/banner/remove', data) }
export function fetchBannerDetail(params: any) { return request.Get<Service.ResponseResult>('/api/v1/sys/banner/detail', { params }) }
