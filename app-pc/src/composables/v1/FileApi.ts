import { request as $alova } from '@/utils'

export function useFileFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/file'

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
