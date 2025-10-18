import { request as $alova } from '@/utils'

/**
 * 用户 API请求
 */
export function useSysUserFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/user'

  /*
     * 用户 默认数据
     */
  const sysUserDefaultData = {
    id: '',
    groupId: '',
    groupIdName: '',
    username: '',
    password: '',
    nickname: '',
    avatar: '',
    background: '',
    quote: '',
    gender: 0,
    genderName: '',
    email: '',
    studentNumber: '',
    telephone: '',
    loginTime: Date.now(),
    createTime: Date.now(),
    createUser: '',
    updateTime: Date.now(),
    updateUser: '',
    roleNames: [],
  }
  return {
    /*
    * 用户 默认数据
    */
    sysUserDefaultData,

    /*
     * 用户 分页接口
     */
    sysUserPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 用户 新增接口
     */
    sysUserAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 用户 修改接口
     */
    sysUserEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 用户 删除接口
     */
    sysUserDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 用户 详情接口
     */
    sysUserDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    /*
     * 用户 信息接口
     */
    getProfile() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/profile`)
    },
    sysUserUpdateAvatar(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/update/avatar`, data)
    },
    sysUserUpdateBackground(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/update/background`, data)
    },
    sysUserUpdatePassword(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/update/password`, data)
    },
    sysUserUpdateProfile(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/update/profile`, data)
    },
  }
}
