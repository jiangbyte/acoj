import { http } from '@/utils'

const prefix = '/api/v1/portal'

export function problemPage(params: any) {
  return http.get<any>(`${prefix}/biz/problem/page`, {
    params,
  })
}

/** 个性化题目推荐（登录按做题画像；游客按热门入门） */
export function problemRecommend(params?: any) {
  return http.get<any>(`${prefix}/biz/problem/recommend`, {
    params,
  })
}

export function problemGroups() {
  return http.get<any>(`${prefix}/biz/problem/group/list`, {
    addToken: false,
  })
}

export function problemTypes() {
  return http.get<any>(`${prefix}/biz/problem/type/list`, {
    addToken: false,
  })
}

export function problemDetail(id: string) {
  return http.get<any>(`${prefix}/biz/problem/detail`, {
    params: { id },
  })
}

export function problemLanguages(problemId: string) {
  return http.get<any>(`${prefix}/biz/problem/languages`, {
    params: { problem_id: problemId },
    addToken: false,
  })
}

export function problemSubmit(problemId: string, data: any) {
  return http.post<any>(`${prefix}/biz/problem/submit`, {
    ...data,
    problem_id: problemId,
  })
}
