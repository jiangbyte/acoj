import { request } from '../http'

export function fetchRolePage(params: any) { return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/role/page', { params }) }
export function fetchRoleCreate(data: any) { return request.Post<Service.ResponseResult>('/api/v1/sys/role/create', data) }
export function fetchRoleModify(data: any) { return request.Post<Service.ResponseResult>('/api/v1/sys/role/modify', data) }
export function fetchRoleRemove(data: any) { return request.Post<Service.ResponseResult>('/api/v1/sys/role/remove', data) }
export function fetchRoleDetail(params: any) { return request.Get<Service.ResponseResult>('/api/v1/sys/role/detail', { params }) }
export function fetchRoleGrantPermission(data: any) { return request.Post<Service.ResponseResult>('/api/v1/sys/role/grant-permission', data) }
export function fetchRoleGrantResource(data: any) { return request.Post<Service.ResponseResult>('/api/v1/sys/role/grant-resource', data) }
export function fetchRoleOwnPermission(params: any) { return request.Get<Service.ResponseResult>('/api/v1/sys/role/own-permission', { params }) }
export function fetchRoleOwnResource(params: any) { return request.Get<Service.ResponseResult>('/api/v1/sys/role/own-resource', { params }) }
