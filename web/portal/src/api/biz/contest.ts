import { http } from '@/utils'

const prefix = '/api/v1/portal'

export function contestPage(params: any) {
  return http.get<any>(`${prefix}/biz/contest/page`, {
    params,
  })
}

export function contestMine(params: any) {
  return http.get<any>(`${prefix}/biz/contest/mine`, { params })
}

export function contestDetail(id: string) {
  return http.get<any>(`${prefix}/biz/contest/detail`, {
    params: { id },
  })
}

export function contestRegister(contestId: string, data: any = {}) {
  return http.post<any>(`${prefix}/biz/contest/register`, data, {
    params: { contest_id: contestId },
  })
}

export function contestUnregister(contestId: string) {
  return http.post<any>(`${prefix}/biz/contest/unregister`, null, {
    params: { contest_id: contestId },
  })
}

export function contestEnter(contestId: string) {
  return http.post<any>(
    `${prefix}/biz/contest/enter`,
    null,
    { params: { contest_id: contestId } },
  )
}

export function contestLeave(contestId: string) {
  return http.post<any>(`${prefix}/biz/contest/leave`, null, {
    params: { contest_id: contestId },
  })
}

export function contestSubmit(contestId: string, data: any) {
  return http.post<any>(`${prefix}/biz/contest/submit`, data, {
    params: { contest_id: contestId },
  })
}

export function contestProblems(contestId: string) {
  return http.get<any>(`${prefix}/biz/contest/problems`, {
    params: { contest_id: contestId },
  })
}

export function contestProblemDetail(contestId: string, problemId: string) {
  return http.get<any>(`${prefix}/biz/contest/problem/detail`, {
    params: { contest_id: contestId, problem_id: problemId },
  })
}

export function contestScoreboard(contestId: string) {
  return http.get<any>(`${prefix}/biz/contest/scoreboard`, {
    params: { contest_id: contestId },
  })
}

export function contestMySubmissions(contestId: string) {
  return http.get<any>(`${prefix}/biz/contest/my-submissions`, {
    params: { contest_id: contestId },
  })
}

export function contestClarifications(contestId: string) {
  return http.get<any>(`${prefix}/biz/contest/clarifications`, {
    params: { contest_id: contestId },
  })
}

export function contestMyThreads(contestId: string) {
  return http.get<any>(`${prefix}/biz/contest/clarification-threads/mine`, {
    params: { contest_id: contestId },
  })
}

export function contestCreateThread(contestId: string, data: any) {
  return http.post<any>(`${prefix}/biz/contest/clarification-threads`, data, {
    params: { contest_id: contestId },
  })
}

export function contestAddThreadMessage(contestId: string, threadId: string, data: any) {
  return http.post<any>(
    `${prefix}/biz/contest/clarification-threads/messages`,
    data,
    { params: { contest_id: contestId, thread_id: threadId } },
  )
}
