import { request as $alova } from '@/utils'

/**
 * 系统配置 API请求
 */
export function useSysConfigFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/config'

  /*
     * 系统配置 默认数据
     */
  const sysConfigDefaultData = {
    id: '',
    name: '',
    code: '',
    value: '',
    componentType: '',
    description: '',
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * 系统配置 默认数据
    */
    sysConfigDefaultData,

    /*
     * 系统配置 分页接口
     */
    sysConfigPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 系统配置 新增接口
     */
    sysConfigAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 系统配置 修改接口
     */
    sysConfigEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 系统配置 删除接口
     */
    sysConfigDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 系统配置 详情接口
     */
    sysConfigDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    /*
     * 系统配置 获取配置值接口
     */
    getValueByCode(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/code`, {
        params: {
          ...data,
        },
      })
    },
  }
}
