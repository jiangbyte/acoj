import { request } from '../http'

export function fetchDictPage(params: any) { return request.Get('/api/v1/sys/dict/page', { params }) }
export function fetchDictList(params: any) { return request.Get('/api/v1/sys/dict/list', { params }) }
export function fetchDictTree(params: any) { return request.Get('/api/v1/sys/dict/tree', { params }) }
export function fetchDictCreate(data: any) { return request.Post('/api/v1/sys/dict/create', data) }
export function fetchDictModify(data: any) { return request.Post('/api/v1/sys/dict/modify', data) }
export function fetchDictRemove(data: any) { return request.Post('/api/v1/sys/dict/remove', data) }
export function fetchDictDetail(params: any) { return request.Get('/api/v1/sys/dict/detail', { params }) }
export function fetchDictGetChildren(params: any) { return request.Get('/api/v1/sys/dict/get-children', { params }) }
