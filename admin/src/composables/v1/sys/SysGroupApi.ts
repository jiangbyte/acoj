import { request as $alova } from '@/utils'

/**
 * 用户组 API请求
 */
export function useSysGroupFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/group'

  /*
     * 用户组 默认数据
     */
  const sysGroupDefaultData = {
    id: '',
    parentId: '',
    name: '',
    code: '',
    description: '',
    sort: 0,
    adminId: null,
    groupType: false,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * 用户组 默认数据
    */
    sysGroupDefaultData,

    /*
     * 用户组 分页接口
     */
    sysGroupPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 用户组 新增接口
     */
    sysGroupAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 用户组 修改接口
     */
    sysGroupEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 用户组 删除接口
     */
    sysGroupDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 用户组 详情接口
     */
    sysGroupDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    sysGroupAuthTree(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/tree`, {
        params: {
          ...data,
        },
      })
    },
  }
}
