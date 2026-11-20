export interface ChatStreamOptions {
  message?: string
  problemId?: string
  conversantId: string
  onMessage: (content: string) => void
  onError?: (error: Error) => void
  onComplete?: () => void
}

export class LLMClient {
  private baseUrl: string
  private token: string

  constructor(baseUrl: string, token: string) {
    this.baseUrl = baseUrl.replace(/\/$/, '')
    this.token = token
  }

  /**
   * 创建并处理服务器发送事件 (SSE) 流
   */
  async chatStream(options: ChatStreamOptions): Promise<void> {
    const {
      message = '你好，你是谁？',
      problemId,
      conversantId,
      onMessage,
      onError,
      onComplete,
    } = options

    const params = new URLSearchParams()
    params.append('message', message)
    params.append('conversantId', conversantId)

    if (problemId) {
      params.append('problemId', problemId)
    }

    const controller = new AbortController()
    const signal = controller.signal

    try {
      const response = await fetch(`${this.baseUrl}/core/api/v1/chat/stream?${params}`, {
        method: 'GET',
        headers: {
          'Authorization': `${this.token}`,
          'Accept': 'text/event-stream',
          'Cache-Control': 'no-cache',
        },
        signal, // 添加信号控制
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      if (!response.body) {
        throw new Error('Response body is null')
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      try {
        while (true) {
          const { done, value } = await reader.read()

          if (done) {
            if (onComplete)
              onComplete()
            break
          }

          buffer += decoder.decode(value, { stream: true })

          // 改进的SSE解析逻辑
          let position = 0
          while (position < buffer.length) {
            // 查找下一个换行符
            const lineEnd = buffer.indexOf('\n', position)
            if (lineEnd === -1)
              break // 没有完整的行

            const line = buffer.slice(position, lineEnd).trim()
            position = lineEnd + 1

            if (line.startsWith('data: ')) {
              const content = line.slice(6).trim()
              if (content) {
                onMessage(content)
              }
            }
          }

          // 保留未处理的部分
          buffer = buffer.slice(position)
        }
      }
      finally {
        reader.releaseLock()
      }
    }
    catch (error) {
      if (error instanceof DOMException && error.name === 'AbortError') {
        // 请求被取消，不触发onError
        return
      }
      if (onError) {
        onError(error instanceof Error ? error : new Error(String(error)))
      }
    }
  }

  async streamChatSimple(options: Omit<ChatStreamOptions, 'onMessage' | 'onError' | 'onComplete'>): Promise<{
    content: string
    cancel: () => void
  }> {
    let content = ''
    let isCancelled = false

    const promise = new Promise<{ content: string }>((resolve, reject) => {
      this.chatStream({
        ...options,
        onMessage: (chunk) => {
          if (!isCancelled) {
            content += chunk
          }
        },
        onError: reject,
        onComplete: () => {
          if (!isCancelled) {
            resolve({ content })
          }
        },
      })
    })

    const cancel = () => {
      isCancelled = true
      // 这里可以添加额外的取消逻辑
    }

    return {
      content: '',
      cancel,
      then: promise.then.bind(promise),
      catch: promise.catch.bind(promise),
      finally: promise.finally.bind(promise),
    } as any
  }
}
