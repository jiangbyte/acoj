import { request as $alova } from '@/utils'

/**
  * Implementation details.
 */
export function useDataSetFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/set'

  /*
      * Implementation details.
     */
  const dataSetDefaultData = {
    id: '',
    setType: 0,
    title: '',
    cover: '',
    description: '',
    categoryId: '',
    difficulty: 0,
    startTime: '',
    endTime: '',
    exJson: '',
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
     * Implementation details.
    */
    dataSetDefaultData,

    /*
      * Implementation details.
     */
    dataSetPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page/client`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    dataSetAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
      * Implementation details.
     */
    dataSetEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
      * Implementation details.
     */
    dataSetDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
      * Implementation details.
     */
    dataSetDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail/client`, {
        params: {
          ...data,
        },
      })
    },
    dataSetProblem(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/problem`, {
        params: {
          ...data,
        },
      })
    },
    dataSetLatest() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/latest`)
    },
    dataSetHot() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/hot`)
    },
    dataSetProblemDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/problem/detail`, {
        params: {
          ...data,
        },
      })
    },
    dataSetUserPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/user`, {
        params: {
          ...data,
        },
      })
    },
    dataSetDifficultyDistribution() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/difficulty/distribution`)
    },
  }
}
