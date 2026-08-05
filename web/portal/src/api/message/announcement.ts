import { http } from '@/utils'
import type { PageData } from '@/typing/api'

const prefix = '/api/v1/portal'

export interface PortalAnnouncement {
  id: string
  title: string
  content: string
  content_type: string
  severity: string
  is_pinned: boolean
  publish_at: string | null
  expire_at: string | null
  is_read: boolean
  view_count: number
}

export const announcementApi = {
  list: (params?: { current?: number; size?: number }) =>
    http.get<PageData<PortalAnnouncement>>(`${prefix}/message/announcements/list`, {
      params,
      // 有 token 时带上，便于填充 is_read；无 token 也可访问
    }),
  myDetail: (id: string) =>
    http.get<PortalAnnouncement>(`${prefix}/message/announcements/my-detail`, {
      params: { id },
    }),
  read: (ids: string[]) =>
    http.post<null>(`${prefix}/message/announcements/read`, { ids }),
}
