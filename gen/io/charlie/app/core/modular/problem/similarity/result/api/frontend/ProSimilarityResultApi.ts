/**
* 代码相似度检测结果详情 API请求
*/
export function useProSimilarityResultFetch() {
  const { $alova } = useNuxtApp()
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/similarity/result'

    /*
     * 代码相似度检测结果详情 默认数据
     */
  const proSimilarityResultDefaultData = {
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
     * 代码相似度检测结果详情 默认数据
     */
    proSimilarityResultDefaultData,

    /*
     * 代码相似度检测结果详情 分页接口
     */
    proSimilarityResultPage(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/page', {
        params: {
        ...data,
        },
      })
    },

    /*
     * 代码相似度检测结果详情 新增接口
     */
    proSimilarityResultAdd(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/add', data)
    },

    /*
     * 代码相似度检测结果详情 修改接口
     */
    proSimilarityResultEdit(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/edit', data)
    },

    /*
     * 代码相似度检测结果详情 删除接口
     */
    proSimilarityResultDelete(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/delete', data)
    },

    /*
     * 代码相似度检测结果详情 详情接口
     */
    proSimilarityResultDetail(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/detail', {
        params: {
        ...data,
        },
      })
    },
  }
}