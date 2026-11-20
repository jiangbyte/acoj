import { request as $alova } from '@/utils'

/**
 * LLM Conversation API requests
 */
export function useSysConversationFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/conversation'

  /*
     * LLM Conversation default data
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
    * LLM Conversation default data
    */
    sysConversationDefaultData,

    /*
     * LLM Conversation pagination endpoint
     */
    sysConversationPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * LLM Conversation create endpoint
     */
    sysConversationAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * LLM Conversation update endpoint
     */
    sysConversationEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * LLM Conversation delete endpoint
     */
    sysConversationDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * LLM Conversation details endpoint
     */
    sysConversationDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}
