import { http } from '@/utils'

const prefix = '/api/v1/portal'

export function solved(params?: any) {
  return http.get<any>(`${prefix}/biz/rank/solved`, {
    params,
    addToken: false,
  })
}

export function rating(params?: any) {
  return http.get<any>(`${prefix}/biz/rank/rating`, {
    params,
    addToken: false,
  })
}

export function me(board: any) {
  return http.get<any>(`${prefix}/biz/rank/me`, { params: { board } })
}

export function summary(board: any) {
  return http.get<any>(`${prefix}/biz/rank/summary`, {
    params: { board },
    addToken: false,
  })
}
