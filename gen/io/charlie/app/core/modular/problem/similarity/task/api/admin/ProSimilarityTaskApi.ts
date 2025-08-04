import { request as $alova } from '@/utils'

/**
* 代码相似度检测任务 API请求
*/
export function useProSimilarityTaskFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/similarity/task'

    /*
     * 代码相似度检测任务 默认数据
     */
    const proSimilarityTaskDefaultData = {
    id: '',
        userId: '',
        problemId: '',
        status: '',
        compareRange: '',
        daysBefore: 0,
        totalCompared: 0,
        maxSimilarity: 0.0,
        isManual: false,
        createTime: '',
        createUser: '',
        updateTime: '',
        updateUser: '',
    }
 return {
    /*
    * 代码相似度检测任务 默认数据
    */
    proSimilarityTaskDefaultData,

    /*
     * 代码相似度检测任务 分页接口
     */
    proSimilarityTaskPage(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/page', {
        params: {
        ...data,
        },
      })
    },

    /*
     * 代码相似度检测任务 新增接口
     */
    proSimilarityTaskAdd(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/add', data)
    },

    /*
     * 代码相似度检测任务 修改接口
     */
    proSimilarityTaskEdit(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/edit', data)
    },

    /*
     * 代码相似度检测任务 删除接口
     */
    proSimilarityTaskDelete(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/delete', data)
    },

    /*
     * 代码相似度检测任务 详情接口
     */
    proSimilarityTaskDetail(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/detail', {
        params: {
        ...data,
        },
      })
    },
  }
}