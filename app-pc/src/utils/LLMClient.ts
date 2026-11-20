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
    * Implementation details.
   */
  async chatStream(options: ChatStreamOptions): Promise<void> {
    const {
      message = 'Hello, who are you?',
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
        signal, // Implementation note.
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

          // Implementation note.
          let position = 0
          while (position < buffer.length) {
            // Implementation note.
            const lineEnd = buffer.indexOf('\n', position)
            if (lineEnd === -1)
              break // Implementation note.

            const line = buffer.slice(position, lineEnd).trim()
            position = lineEnd + 1

            if (line.startsWith('data: ')) {
              const content = line.slice(6).trim()
              if (content) {
                onMessage(content)
              }
            }
          }

          // Implementation note.
          buffer = buffer.slice(position)
        }
      }
      finally {
        reader.releaseLock()
      }
    }
    catch (error) {
      if (error instanceof DOMException && error.name === 'AbortError') {
        // Implementation note.
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
      // Implementation note.
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
