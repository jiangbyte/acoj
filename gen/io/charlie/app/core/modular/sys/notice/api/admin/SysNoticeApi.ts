import { request as $alova } from '@/utils'

/**
* 公告 API请求
*/
export function useSysNoticeFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'sys/notice'

    /*
     * 公告 默认数据
     */
    const sysNoticeDefaultData = {
    id: '',
        title: '',
        cover: '',
        url: '',
        sort: 0,
        content: '',
        createTime: '',
        createUser: '',
        updateTime: '',
        updateUser: '',
    }
 return {
    /*
    * 公告 默认数据
    */
    sysNoticeDefaultData,

    /*
     * 公告 分页接口
     */
    sysNoticePage(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/page', {
        params: {
        ...data,
        },
      })
    },

    /*
     * 公告 新增接口
     */
    sysNoticeAdd(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/add', data)
    },

    /*
     * 公告 修改接口
     */
    sysNoticeEdit(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/edit', data)
    },

    /*
     * 公告 删除接口
     */
    sysNoticeDelete(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/delete', data)
    },

    /*
     * 公告 详情接口
     */
    sysNoticeDetail(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/detail', {
        params: {
        ...data,
        },
      })
    },
  }
}