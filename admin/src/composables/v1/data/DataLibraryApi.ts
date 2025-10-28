import { request as $alova } from '@/utils'

/**
 * 提交样本库 API请求
 */
export function useDataLibraryFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/library'

  /*
     * 提交样本库 默认数据
     */
  const dataLibraryDefaultData = {
    id: '',
    userId: '',
    setId: '',
    isSet: false,
    problemId: '',
    submitId: '',
    submitTime: Date.now(),
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
    * 提交样本库 默认数据
    */
    dataLibraryDefaultData,

    /*
     * 提交样本库 分页接口
     */
    dataLibraryPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 提交样本库 新增接口
     */
    dataLibraryAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 提交样本库 修改接口
     */
    dataLibraryEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 提交样本库 删除接口
     */
    dataLibraryDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 提交样本库 详情接口
     */
    dataLibraryDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    dataLibraryiUserPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/userPage`, {
        params: {
          ...data,
        },
      })
    },
  }
}
