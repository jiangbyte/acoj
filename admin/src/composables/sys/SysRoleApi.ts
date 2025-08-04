import { request as $alova } from '@/utils'

/**
 * 角色 API请求
 */
export function useSysRoleFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'sys/role'

  /*
     * 角色 默认数据
     */
  const sysRoleDefaultData = {
    id: '',
    name: '',
    code: '',
    description: '',
    level: 0,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * 角色 默认数据
    */
    sysRoleDefaultData,

    /*
     * 角色 分页接口
     */
    sysRolePage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 角色 新增接口
     */
    sysRoleAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 角色 修改接口
     */
    sysRoleEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 角色 删除接口
     */
    sysRoleDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 角色 详情接口
     */
    sysRoleDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}
