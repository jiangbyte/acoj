import { request as $alova } from '@/utils'

const pathPrefix = '/core/api/v1/'
const table = 'sys/file'

export function useFileFetch() {
  return {
    uploadFile(formData: FormData | undefined) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/upload`, formData)
    },
    serveFile(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}`, {
        params: {
          ...data,
        },
      })
    },
    getFileInfo(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/info`, {
        params: {
          ...data,
        },
      })
    },
  }
}
