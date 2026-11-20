import { request as $alova } from '@/utils'

/**
 * User API requests
 */
export function useSysUserFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/user'

  /*
     * User default data
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
    permissions: [],
  }
  return {
    /*
    * User default data
    */
    sysUserDefaultData,

    /*
     * User pagination endpoint
     */
    sysUserPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * User create endpoint
     */
    sysUserAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * User update endpoint
     */
    sysUserEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * User delete endpoint
     */
    sysUserDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * User details endpoint
     */
    sysUserDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    sysUserOptions(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/options`, {
        params: {
          ...data,
        },
      })
    },
    /*
     * User profile endpoint
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
