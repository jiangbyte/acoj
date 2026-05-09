import { request } from '../http'
export function fetchGroupPage(params: any) { return request.Get('/api/v1/sys/group/page', { params }) }
export function fetchGroupCreate(data: any) { return request.Post('/api/v1/sys/group/create', data) }
export function fetchGroupModify(data: any) { return request.Post('/api/v1/sys/group/modify', data) }
export function fetchGroupRemove(data: any) { return request.Post('/api/v1/sys/group/remove', data) }
export function fetchGroupDetail(params: any) { return request.Get('/api/v1/sys/group/detail', { params }) }
export function fetchGroupGrantRole(data: any) { return request.Post('/api/v1/sys/group/grant-role', data) }
export function fetchGroupOwnRoles(params: any) { return request.Get('/api/v1/sys/group/own-roles', { params }) }
