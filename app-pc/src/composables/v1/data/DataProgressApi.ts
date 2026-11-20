import { request as $alova } from '@/utils'

/**
  * Implementation details.
 */
export function useDataProgressFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/progress'

  /*
      * Implementation details.
     */
  const dataProgressDefaultData = {
    id: '',
    userId: '',
    setId: '',
    problemId: '',
    status: '',
    extraJson: '',
    completed: false,
    completedAt: '',
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
     * Implementation details.
    */
    dataProgressDefaultData,

    /*
      * Implementation details.
     */
    dataProgressPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    dataProgressAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
      * Implementation details.
     */
    dataProgressEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
      * Implementation details.
     */
    dataProgressDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
      * Implementation details.
     */
    dataProgressDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}
