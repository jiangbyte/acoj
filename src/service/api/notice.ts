import { request } from '../http'
export function fetchNoticePage(params: any) { return request.Get('/api/v1/sys/notice/page', { params }) }
export function fetchNoticeCreate(data: any) { return request.Post('/api/v1/sys/notice/create', data) }
export function fetchNoticeModify(data: any) { return request.Post('/api/v1/sys/notice/modify', data) }
export function fetchNoticeRemove(data: any) { return request.Post('/api/v1/sys/notice/remove', data) }
export function fetchNoticeDetail(params: any) { return request.Get('/api/v1/sys/notice/detail', { params }) }
