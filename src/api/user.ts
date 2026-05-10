import { request } from '@/utils'

export function fetchUserPage(params: any) { return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/user/page', { params }) }
export function fetchUserCreate(data: any) { return request.Post<Service.ResponseResult>('/api/v1/sys/user/create', data) }
export function fetchUserModify(data: any) { return request.Post<Service.ResponseResult>('/api/v1/sys/user/modify', data) }
export function fetchUserRemove(data: any) { return request.Post<Service.ResponseResult>('/api/v1/sys/user/remove', data) }
export function fetchUserDetail(params: any) { return request.Get<Service.ResponseResult>('/api/v1/sys/user/detail', { params }) }
export function fetchUserGrantRole(data: any) { return request.Post<Service.ResponseResult>('/api/v1/sys/user/grant-role', data) }
export function fetchUserOwnRoles(params: any) { return request.Get<Service.ResponseResult>('/api/v1/sys/user/own-roles', { params }) }
