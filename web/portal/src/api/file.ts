import { http } from '@/utils'

const prefix = '/api/v1/portal/sys/file'

export interface PortalFileItem {
  id: string
  object_name: string
  original_name: string
  content_type: string
  size: number
  url: string
}

export function uploadFile(file: File) {
  const data = new FormData()
  data.append('file', file)
  return http.post<PortalFileItem>(`${prefix}/upload`, data)
}
