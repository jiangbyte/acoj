import { request } from '../http'
export function fetchConfigPage(params: any) { return request.Get('/sys/config/page', { params }) }
export function fetchConfigListByCategory(params: any) { return request.Get('/sys/config/list-by-category', { params }) }
export function fetchConfigCreate(data: any) { return request.Post('/sys/config/create', data) }
export function fetchConfigModify(data: any) { return request.Post('/sys/config/modify', data) }
export function fetchConfigRemove(data: any) { return request.Post('/sys/config/remove', data) }
export function fetchConfigDetail(params: any) { return request.Get('/sys/config/detail', { params }) }
export function fetchConfigEditBatch(data: any) { return request.Post('/sys/config/edit-batch', data) }
