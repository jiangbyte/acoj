import { request as $alova } from '@/utils'

/**
 * 题目题集提交样本库 API请求
 */
export function useProSetSampleLibraryFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/set/sample/library'

  /*
     * 题目题集提交样本库 默认数据
     */
  const proSetSampleLibraryDefaultData = {
    id: '',
    userId: '',
    setId: '',
    problemId: '',
    submitId: '',
    submitTime: '',
    language: '',
    code: '',
    codeLength: 0,
    accessCount: 0,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * 题目题集提交样本库 默认数据
    */
    proSetSampleLibraryDefaultData,

    /*
     * 题目题集提交样本库 分页接口
     */
    proSetSampleLibraryPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 题目题集提交样本库 新增接口
     */
    proSetSampleLibraryAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 题目题集提交样本库 修改接口
     */
    proSetSampleLibraryEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 题目题集提交样本库 删除接口
     */
    proSetSampleLibraryDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 题目题集提交样本库 详情接口
     */
    proSetSampleLibraryDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}
