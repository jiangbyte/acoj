import { createCrudApi } from '@/utils/http/crud'

export const orgApi = createCrudApi({ basePath: '/api/v1/sys/org', hasTree: true })
