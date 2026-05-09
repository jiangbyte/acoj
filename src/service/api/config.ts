import { request } from '../http'
export function fetchConfigPage(params: any) { return request.Get('/api/v1/sys/config/page', { params }) }
export function fetchConfigListByCategory(params: any) { return request.Get('/api/v1/sys/config/list-by-category', { params }) }
export function fetchConfigCreate(data: any) { return request.Post('/api/v1/sys/config/create', data) }
export function fetchConfigModify(data: any) { return request.Post('/api/v1/sys/config/modify', data) }
export function fetchConfigRemove(data: any) { return request.Post('/api/v1/sys/config/remove', data) }
export function fetchConfigDetail(params: any) { return request.Get('/api/v1/sys/config/detail', { params }) }
export function fetchConfigEditBatch(data: any) { return request.Post('/api/v1/sys/config/edit-batch', data) }
