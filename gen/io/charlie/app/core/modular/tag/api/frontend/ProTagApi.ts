/**
* 标签 API请求
*/
export function useProTagFetch() {
  const { $alova } = useNuxtApp()
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/tag'

    /*
     * 标签 默认数据
     */
  const proTagDefaultData = {
    id: '',
      name: '',
      createTime: '',
      createUser: '',
      updateTime: '',
      updateUser: '',
    }
  return {
    /*
     * 标签 默认数据
     */
    proTagDefaultData,

    /*
     * 标签 分页接口
     */
    proTagPage(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/page', {
        params: {
        ...data,
        },
      })
    },

    /*
     * 标签 新增接口
     */
    proTagAdd(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/add', data)
    },

    /*
     * 标签 修改接口
     */
    proTagEdit(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/edit', data)
    },

    /*
     * 标签 删除接口
     */
    proTagDelete(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/delete', data)
    },

    /*
     * 标签 详情接口
     */
    proTagDetail(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/detail', {
        params: {
        ...data,
        },
      })
    },
  }
}