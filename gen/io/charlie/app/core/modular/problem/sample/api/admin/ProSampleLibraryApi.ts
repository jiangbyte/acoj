import { request as $alova } from '@/utils'

/**
* 题目提交样本库 API请求
*/
export function useProSampleLibraryFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/sample/library'

    /*
     * 题目提交样本库 默认数据
     */
    const proSampleLibraryDefaultData = {
    id: '',
        userId: '',
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
    * 题目提交样本库 默认数据
    */
    proSampleLibraryDefaultData,

    /*
     * 题目提交样本库 分页接口
     */
    proSampleLibraryPage(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/page', {
        params: {
        ...data,
        },
      })
    },

    /*
     * 题目提交样本库 新增接口
     */
    proSampleLibraryAdd(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/add', data)
    },

    /*
     * 题目提交样本库 修改接口
     */
    proSampleLibraryEdit(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/edit', data)
    },

    /*
     * 题目提交样本库 删除接口
     */
    proSampleLibraryDelete(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/delete', data)
    },

    /*
     * 题目提交样本库 详情接口
     */
    proSampleLibraryDetail(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/detail', {
        params: {
        ...data,
        },
      })
    },
  }
}