import { request as $alova } from '@/utils'

/**
 * 报告库 API请求
 */
export function useDataReportsFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'data/reports'

  /*
     * 报告库 默认数据
     */
  const dataReportsDefaultData = {
    id: '',
    reportType: 0,
    taskId: '',
    isSet: false,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * 报告库 默认数据
    */
    dataReportsDefaultData,

    /*
     * 报告库 分页接口
     */
    dataReportsPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 报告库 新增接口
     */
    dataReportsAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 报告库 修改接口
     */
    dataReportsEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 报告库 删除接口
     */
    dataReportsDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 报告库 详情接口
     */
    dataReportsDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}
