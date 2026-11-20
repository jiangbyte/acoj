import { request as $alova } from '@/utils'

/**
  * Implementation details.
 */
export function useSysUserFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/user'

  /*
      * Implementation details.
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
     * Implementation details.
    */
    sysUserDefaultData,

    /*
      * Implementation details.
     */
    sysUserPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    sysUserAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
      * Implementation details.
     */
    sysUserEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
      * Implementation details.
     */
    sysUserDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
      * Implementation details.
     */
    sysUserDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    sysUserDetailClient(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail/client`, {
        params: {
          ...data,
        },
      })
    },
    /*
      * Implementation details.
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
