import { request } from '../http'

export function fetchRolePage(params: any) { return request.Get('/sys/role/page', { params }) }
export function fetchRoleCreate(data: any) { return request.Post('/sys/role/create', data) }
export function fetchRoleModify(data: any) { return request.Post('/sys/role/modify', data) }
export function fetchRoleRemove(data: any) { return request.Post('/sys/role/remove', data) }
export function fetchRoleDetail(params: any) { return request.Get('/sys/role/detail', { params }) }
export function fetchRoleGrantPermission(data: any) { return request.Post('/sys/role/grant-permission', data) }
export function fetchRoleGrantResource(data: any) { return request.Post('/sys/role/grant-resource', data) }
export function fetchRoleOwnPermission(params: any) { return request.Get('/sys/role/own-permission', { params }) }
export function fetchRoleOwnResource(params: any) { return request.Get('/sys/role/own-resource', { params }) }
