import { request as $alova } from '@/utils'

const pathPrefix = '/core/api/v1/'
const table = 'sys/auth'

export function useAuthFetch() {
  return {
    captcha() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/captcha`)
    },
    doLogin(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/login`, data)
    },
    doLogout() {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/logout`)
    },
    doRegister(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/register`, data)
    },
    getProfile() {
      return $alova.Get<IResult<any>>(`${pathPrefix}sys/user/profile`)
    },
    getProfileNoe() {
      return $alova.Get<IResult<any>>(`${pathPrefix}sys/user/profile/noe`)
    },
  }
}
