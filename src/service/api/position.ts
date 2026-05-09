import { request } from '../http'
export function fetchPositionPage(params: any) { return request.Get('/sys/position/page', { params }) }
export function fetchPositionCreate(data: any) { return request.Post('/sys/position/create', data) }
export function fetchPositionModify(data: any) { return request.Post('/sys/position/modify', data) }
export function fetchPositionRemove(data: any) { return request.Post('/sys/position/remove', data) }
export function fetchPositionDetail(params: any) { return request.Get('/sys/position/detail', { params }) }
