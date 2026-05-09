import { request } from '../http'

export function fetchUserPage(params: any) { return request.Get('/api/v1/sys/user/page', { params }) }
export function fetchUserCreate(data: any) { return request.Post('/api/v1/sys/user/create', data) }
export function fetchUserModify(data: any) { return request.Post('/api/v1/sys/user/modify', data) }
export function fetchUserRemove(data: any) { return request.Post('/api/v1/sys/user/remove', data) }
export function fetchUserDetail(params: any) { return request.Get('/api/v1/sys/user/detail', { params }) }
export function fetchUserGrantRole(data: any) { return request.Post('/api/v1/sys/user/grant-role', data) }
export function fetchUserOwnRoles(params: any) { return request.Get('/api/v1/sys/user/own-roles', { params }) }
