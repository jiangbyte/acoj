import { request as $alova } from '@/utils'

/**
* 题单代码相似度检测结果详情 API请求
*/
export function useProSetSimilarityResultFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/set/similarity/result'

    /*
     * 题单代码相似度检测结果详情 默认数据
     */
    const proSetSimilarityResultDefaultData = {
    id: '',
        taskId: '',
        originSubmitId: '',
        comparedSubmitId: '',
        similarity: 0.0,
        details: '',
        matchDetails: '',
        threshold: 0.0,
        createTime: '',
        createUser: '',
        updateTime: '',
        updateUser: '',
    }
 return {
    /*
    * 题单代码相似度检测结果详情 默认数据
    */
    proSetSimilarityResultDefaultData,

    /*
     * 题单代码相似度检测结果详情 分页接口
     */
    proSetSimilarityResultPage(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/page', {
        params: {
        ...data,
        },
      })
    },

    /*
     * 题单代码相似度检测结果详情 新增接口
     */
    proSetSimilarityResultAdd(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/add', data)
    },

    /*
     * 题单代码相似度检测结果详情 修改接口
     */
    proSetSimilarityResultEdit(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/edit', data)
    },

    /*
     * 题单代码相似度检测结果详情 删除接口
     */
    proSetSimilarityResultDelete(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/delete', data)
    },

    /*
     * 题单代码相似度检测结果详情 详情接口
     */
    proSetSimilarityResultDetail(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/detail', {
        params: {
        ...data,
        },
      })
    },
  }
}