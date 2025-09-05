import { request as $alova } from '@/utils'

/**
* 题目检测结果任务库 API请求
*/
export function useProSimilarityDetailFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/similarity/detail'

    /*
     * 题目检测结果任务库 默认数据
     */
    const proSimilarityDetailDefaultData = {
    id: '',
        taskId: '',
        taskType: false,
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
    * 题目检测结果任务库 默认数据
    */
    proSimilarityDetailDefaultData,

    /*
     * 题目检测结果任务库 分页接口
     */
    proSimilarityDetailPage(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/page', {
        params: {
        ...data,
        },
      })
    },

    /*
     * 题目检测结果任务库 新增接口
     */
    proSimilarityDetailAdd(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/add', data)
    },

    /*
     * 题目检测结果任务库 修改接口
     */
    proSimilarityDetailEdit(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/edit', data)
    },

    /*
     * 题目检测结果任务库 删除接口
     */
    proSimilarityDetailDelete(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/delete', data)
    },

    /*
     * 题目检测结果任务库 详情接口
     */
    proSimilarityDetailDetail(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/detail', {
        params: {
        ...data,
        },
      })
    },
  }
}