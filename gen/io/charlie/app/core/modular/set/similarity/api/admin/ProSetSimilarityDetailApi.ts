import { request as $alova } from '@/utils'

/**
* 题集题目检测结果任务库 API请求
*/
export function useProSetSimilarityDetailFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/set/similarity/detail'

    /*
     * 题集题目检测结果任务库 默认数据
     */
    const proSetSimilarityDetailDefaultData = {
    id: '',
        taskId: '',
        taskType: false,
        setId: '',
        problemId: '',
        language: '',
        similarity: 0.0,
        submitUser: '',
        submitCode: '',
        submitCodeLength: 0,
        submitId: '',
        submitTime: '',
        submitTokenName: '',
        submitTokenTexts: '',
        originUser: '',
        originCode: '',
        originCodeLength: 0,
        originId: '',
        originTime: '',
        originTokenName: '',
        originTokenTexts: '',
        createTime: '',
        createUser: '',
        updateTime: '',
        updateUser: '',
    }
 return {
    /*
    * 题集题目检测结果任务库 默认数据
    */
    proSetSimilarityDetailDefaultData,

    /*
     * 题集题目检测结果任务库 分页接口
     */
    proSetSimilarityDetailPage(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/page', {
        params: {
        ...data,
        },
      })
    },

    /*
     * 题集题目检测结果任务库 新增接口
     */
    proSetSimilarityDetailAdd(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/add', data)
    },

    /*
     * 题集题目检测结果任务库 修改接口
     */
    proSetSimilarityDetailEdit(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/edit', data)
    },

    /*
     * 题集题目检测结果任务库 删除接口
     */
    proSetSimilarityDetailDelete(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/delete', data)
    },

    /*
     * 题集题目检测结果任务库 详情接口
     */
    proSetSimilarityDetailDetail(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/detail', {
        params: {
        ...data,
        },
      })
    },
  }
}