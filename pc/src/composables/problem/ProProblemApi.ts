import { request as $alova } from '@/utils'

/**
 * 题目 API请求
 */
export function useProProblemFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/problem'

  /*
     * 题目 默认数据
     */
  const proProblemDefaultData = {
    id: '',
    categoryId: '',
    title: '',
    source: '',
    url: '',
    maxTime: 0,
    maxMemory: 0,
    description: '',
    testCase: [],
    allowedLanguages: [],
    difficulty: 1,
    useTemplate: false,
    codeTemplate: [],
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
    proProblemDefaultData,

    /*
     * 题目 分页接口
     */
    proProblemPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 题目 新增接口
     */
    proProblemAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 题目 修改接口
     */
    proProblemEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 题目 删除接口
     */
    proProblemDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 题目 详情接口
     */
    proProblemDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}
