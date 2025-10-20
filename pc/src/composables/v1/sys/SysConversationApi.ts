import { request as $alova } from '@/utils'

/**
 * 大模型对话 API请求
 */
export function useSysConversationFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/conversation'

  /*
     * 大模型对话 默认数据
     */
  const sysConversationDefaultData = {
    id: '',
    conversationId: '',
    problemId: '',
    setId: '',
    isSet: false,
    userId: '',
    messageType: '',
    messageRole: '',
    messageContent: '',
    userCode: '',
    language: '',
    promptTokens: 0,
    completionTokens: 0,
    totalTokens: 0,
    responseTime: '',
    streamingDuration: 0,
    status: '',
    errorMessage: '',
    userPlatform: '',
    ipAddress: '',
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * 大模型对话 默认数据
    */
    sysConversationDefaultData,

    /*
     * 大模型对话 分页接口
     */
    sysConversationPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 大模型对话 新增接口
     */
    sysConversationAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 大模型对话 修改接口
     */
    sysConversationEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 大模型对话 删除接口
     */
    sysConversationDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 大模型对话 详情接口
     */
    sysConversationDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },

    sysConversationHistory(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/history/${data}`)
    },
  }
}
