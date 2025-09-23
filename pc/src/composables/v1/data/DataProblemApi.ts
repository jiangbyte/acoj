import { request as $alova } from '@/utils'

/**
 * 题目 API请求
 */
export function useDataProblemFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/problem'

  /*
     * 题目 默认数据
     */
  const dataProblemDefaultData = {
    id: '',
    displayId: '',
    categoryId: '',
    title: '',
    source: '',
    url: '',
    maxTime: 0,
    maxMemory: 0,
    description: '',
    testCase: '',
    allowedLanguages: '',
    difficulty: 0,
    threshold: 0.0,
    useTemplate: false,
    codeTemplate: '',
    isPublic: false,
    isVisible: false,
    useAi: false,
    solved: 0,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * 题目 默认数据
    */
    dataProblemDefaultData,

    /*
     * 题目 分页接口
     */
    dataProblemPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 题目 新增接口
     */
    dataProblemAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 题目 修改接口
     */
    dataProblemEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 题目 删除接口
     */
    dataProblemDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 题目 详情接口
     */
    dataProblemDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    /*
     * 题目 详情接口
     */
    dataProblemClientDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/client/detail`, {
        params: {
          ...data,
        },
      })
    },
    dataProblemCount() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/count`)
    },
    dataProblemHot() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/hot`)
    },
    dataProblemLatest() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/latest`)
    },
  }
}
