import { request as $alova } from '@/utils'

/**
 * Submission Library API requests
 */
export function useDataLibraryFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/library'

  /*
     * Submission Library default data
     */
  const dataLibraryDefaultData = {
    id: '',
    userId: '',
    setId: '',
    isSet: false,
    problemId: '',
    submitId: '',
    submitTime: Date.now(),
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
    * Submission Library default data
    */
    dataLibraryDefaultData,

    /*
     * Submission Library pagination endpoint
     */
    dataLibraryPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },
    dataLibraryProblems(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/module/problems`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Submission Library create endpoint
     */
    dataLibraryAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },
    dataLibraryBatchQuery(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/batch/query`, data)
    },
    /*
     * Submission Library update endpoint
     */
    dataLibraryEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Submission Library delete endpoint
     */
    dataLibraryDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * Submission Library details endpoint
     */
    dataLibraryDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    dataLibraryiUserPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/userPage`, {
        params: {
          ...data,
        },
      })
    },
    dataLibraryiUserGroupList(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/userGroupList`, {
        params: {
          ...data,
        },
      })
    },
  }
}
