import { request } from '../http'

export function fetchDictPage(params: any) { return request.Get('/sys/dict/page', { params }) }
export function fetchDictList(params: any) { return request.Get('/sys/dict/list', { params }) }
export function fetchDictTree(params: any) { return request.Get('/sys/dict/tree', { params }) }
export function fetchDictCreate(data: any) { return request.Post('/sys/dict/create', data) }
export function fetchDictModify(data: any) { return request.Post('/sys/dict/modify', data) }
export function fetchDictRemove(data: any) { return request.Post('/sys/dict/remove', data) }
export function fetchDictDetail(params: any) { return request.Get('/sys/dict/detail', { params }) }
export function fetchDictGetChildren(params: any) { return request.Get('/sys/dict/get-children', { params }) }
