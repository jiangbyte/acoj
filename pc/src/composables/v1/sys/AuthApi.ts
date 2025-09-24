import { request as $alova } from '@/utils'

export function useAuthFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/auth'
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
