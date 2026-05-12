import { createCrudApi } from '@/utils/http/crud'

export const positionApi = createCrudApi({ basePath: '/api/v1/sys/position' })
