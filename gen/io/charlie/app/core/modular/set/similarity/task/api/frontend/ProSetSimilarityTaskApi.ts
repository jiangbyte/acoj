/**
* 题单代码相似度检测任务 API请求
*/
export function useProSetSimilarityTaskFetch() {
  const { $alova } = useNuxtApp()
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/set/similarity/task'

    /*
     * 题单代码相似度检测任务 默认数据
     */
  const proSetSimilarityTaskDefaultData = {
    id: '',
      userId: '',
      problemId: '',
      setId: '',
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
     * 题单代码相似度检测任务 默认数据
     */
    proSetSimilarityTaskDefaultData,

    /*
     * 题单代码相似度检测任务 分页接口
     */
    proSetSimilarityTaskPage(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/page', {
        params: {
        ...data,
        },
      })
    },

    /*
     * 题单代码相似度检测任务 新增接口
     */
    proSetSimilarityTaskAdd(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/add', data)
    },

    /*
     * 题单代码相似度检测任务 修改接口
     */
    proSetSimilarityTaskEdit(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/edit', data)
    },

    /*
     * 题单代码相似度检测任务 删除接口
     */
    proSetSimilarityTaskDelete(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/delete', data)
    },

    /*
     * 题单代码相似度检测任务 详情接口
     */
    proSetSimilarityTaskDetail(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/detail', {
        params: {
        ...data,
        },
      })
    },
  }
}