import { request } from '../http'
export function fetchBannerPage(params: any) { return request.Get('/sys/banner/page', { params }) }
export function fetchBannerCreate(data: any) { return request.Post('/sys/banner/create', data) }
export function fetchBannerModify(data: any) { return request.Post('/sys/banner/modify', data) }
export function fetchBannerRemove(data: any) { return request.Post('/sys/banner/remove', data) }
export function fetchBannerDetail(params: any) { return request.Get('/sys/banner/detail', { params }) }
