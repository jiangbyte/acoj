import { request as $alova } from '@/utils'

/**
 * 检测结果任务库 API请求
 */
export function useTaskSimilarityFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'task/similarity'

  /*
     * 检测结果任务库 默认数据
     */
  const taskSimilarityDefaultData = {
    id: '',
    taskId: '',
    taskType: false,
    problemId: '',
    setId: '',
    isSet: false,
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
    * 检测结果任务库 默认数据
    */
    taskSimilarityDefaultData,

    /*
     * 检测结果任务库 分页接口
     */
    taskSimilarityPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 检测结果任务库 新增接口
     */
    taskSimilarityAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 检测结果任务库 修改接口
     */
    taskSimilarityEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 检测结果任务库 删除接口
     */
    taskSimilarityDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 检测结果任务库 详情接口
     */
    taskSimilarityDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}
