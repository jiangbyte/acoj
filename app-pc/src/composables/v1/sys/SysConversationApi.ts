import { request as $alova } from '@/utils'

/**
  * Implementation details.
 */
export function useSysConversationFetch() {
  const gateway = import.meta.env.VITE_GATEWAY || ''
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT || ''
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/conversation'

  /*
      * Implementation details.
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

  const sysConversationStreamUrl = `${gateway + pathPrefix}chat/stream`
  return {
    /*
     * Implementation details.
    */
    sysConversationDefaultData,
    sysConversationStreamUrl,

    /*
      * Implementation details.
     */
    sysConversationPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    sysConversationAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
      * Implementation details.
     */
    sysConversationEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
      * Implementation details.
     */
    sysConversationDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
      * Implementation details.
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
    sysConversationUserHistory(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/userHistory`, {
        params: {
          ...data,
        },
      })
    },
    /**
     * 当前用户按题的会话列表
     */
    userSessionPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/userSession/page`, {
        params: {
          ...data,
        },
      })
    },

    sysConversationStream(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix}chat/stream`, data, {
        headers: {
          'Content-Type': 'application/json',
        },
        params: {
          // sse: true,
        },
      })
    },
  }
}
