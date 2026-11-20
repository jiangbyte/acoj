import { request as $alova } from '@/utils'

/**
  * Implementation details.
 */
export function useDataLibraryFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/library'

  /*
      * Implementation details.
     */
  const dataLibraryDefaultData = {
    id: '',
    userId: '',
    setId: '',
    isSet: false,
    problemId: '',
    submitId: '',
    submitTime: '',
    language: '',
    code: '',
    codeLength: 0,
    accessCount: 0,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
     * Implementation details.
    */
    dataLibraryDefaultData,

    /*
      * Implementation details.
     */
    dataLibraryPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    dataLibraryAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
      * Implementation details.
     */
    dataLibraryEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
      * Implementation details.
     */
    dataLibraryDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
      * Implementation details.
     */
    dataLibraryDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}
