import { request as $alova } from '@/utils'

/**
 * Set Progress API requests
 */
export function useDataProgressFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/progress'

  /*
     * Set Progress default data
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
    * Set Progress default data
    */
    dataProgressDefaultData,

    /*
     * Set Progress pagination endpoint
     */
    dataProgressPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Set Progress create endpoint
     */
    dataProgressAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * Set Progress update endpoint
     */
    dataProgressEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Set Progress delete endpoint
     */
    dataProgressDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * Set Progress details endpoint
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
