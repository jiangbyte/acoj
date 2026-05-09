import { request } from '../http'

export function fetchModulePage(params: any) { return request.Get('/sys/module/page', { params }) }
export function fetchModuleCreate(data: any) { return request.Post('/sys/module/create', data) }
export function fetchModuleModify(data: any) { return request.Post('/sys/module/modify', data) }
export function fetchModuleRemove(data: any) { return request.Post('/sys/module/remove', data) }
export function fetchModuleDetail(params: any) { return request.Get('/sys/module/detail', { params }) }

export function fetchResourcePage(params: any) { return request.Get('/sys/resource/page', { params }) }
export function fetchResourceCreate(data: any) { return request.Post('/sys/resource/create', data) }
export function fetchResourceModify(data: any) { return request.Post('/sys/resource/modify', data) }
export function fetchResourceRemove(data: any) { return request.Post('/sys/resource/remove', data) }
export function fetchResourceDetail(params: any) { return request.Get('/sys/resource/detail', { params }) }
