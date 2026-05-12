import { createCrudApi } from '@/utils/http/crud'

export const noticeApi = createCrudApi({ basePath: '/api/v1/sys/notice' })
