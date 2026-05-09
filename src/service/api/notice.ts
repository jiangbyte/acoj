import { request } from '../http'
export function fetchNoticePage(params: any) { return request.Get('/sys/notice/page', { params }) }
export function fetchNoticeCreate(data: any) { return request.Post('/sys/notice/create', data) }
export function fetchNoticeModify(data: any) { return request.Post('/sys/notice/modify', data) }
export function fetchNoticeRemove(data: any) { return request.Post('/sys/notice/remove', data) }
export function fetchNoticeDetail(params: any) { return request.Get('/sys/notice/detail', { params }) }
