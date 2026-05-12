import { createCrudApi } from '@/utils/http/crud'

export const groupApi = createCrudApi({ basePath: '/api/v1/sys/group', hasTree: true })
