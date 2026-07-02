import { http } from '@/utils'

const filePrefix = '/api/v1/admin/sys/file'

export function page(params: any) {
  return http.get<any>(`${filePrefix}/page`, {
    params,
  })
}

export function detail(params: any) {
  return http.get<any>(`${filePrefix}/detail`, {
    params,
  })
}

export function update(data: any) {
  return http.post<any>(`${filePrefix}/update`, data)
}

export function remove(data: any) {
  return http.post<any>(`${filePrefix}/delete`, data)
}

export function upload(file: File) {
  const data = new FormData()
  data.append('file', file)
  return http.post<any>(`${filePrefix}/upload`, data)
}
