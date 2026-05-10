import { request } from '../http'
export function fetchOrgPage(params: any) { return request.Get<Service.ResponseResult<Service.PageResult>>('/api/v1/sys/org/page', { params }) }
export function fetchOrgCreate(data: any) { return request.Post<Service.ResponseResult>('/api/v1/sys/org/create', data) }
export function fetchOrgModify(data: any) { return request.Post<Service.ResponseResult>('/api/v1/sys/org/modify', data) }
export function fetchOrgRemove(data: any) { return request.Post<Service.ResponseResult>('/api/v1/sys/org/remove', data) }
export function fetchOrgDetail(params: any) { return request.Get<Service.ResponseResult>('/api/v1/sys/org/detail', { params }) }
export function fetchOrgGrantRole(data: any) { return request.Post<Service.ResponseResult>('/api/v1/sys/org/grant-role', data) }
export function fetchOrgOwnRoles(params: any) { return request.Get<Service.ResponseResult>('/api/v1/sys/org/own-roles', { params }) }
