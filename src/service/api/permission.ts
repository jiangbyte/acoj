import { request } from '../http'

export function fetchPermissionPage(params: any) { return request.Get('/sys/permission/page', { params }) }
export function fetchPermissionCreate(data: any) { return request.Post('/sys/permission/create', data) }
export function fetchPermissionModify(data: any) { return request.Post('/sys/permission/modify', data) }
export function fetchPermissionRemove(data: any) { return request.Post('/sys/permission/remove', data) }
export function fetchPermissionDetail(params: any) { return request.Get('/sys/permission/detail', { params }) }
export function fetchPermissionModules() { return request.Get('/sys/permission/modules') }
export function fetchPermissionByModule(params: any) { return request.Get('/sys/permission/by-module', { params }) }
