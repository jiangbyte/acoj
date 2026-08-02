import type { Message, MessagePart } from '~/utils/ai'
import { getMockReply, getMockToolResult, getMockReasoning } from '~/utils/ai'

type ChatStatus = 'ready' | 'streaming' | 'submitted' | 'error'

export function useChatMessages() {
  const messages = ref<Message[]>([])
  const status = ref<ChatStatus>('ready')

  function buildMockReplyParts(): MessagePart[] {
    const parts: MessagePart[] = []
    const hasReasoning = Math.random() > 0.5
    const hasToolCall = Math.random() > 0.6

    if (hasReasoning) {
      parts.push({
        type: 'reasoning',
        text: getMockReasoning(),
      })
    }

    if (hasToolCall) {
      const toolName = ['weather', 'chart', 'web_search'][Math.floor(Math.random() * 3)]
      const result = getMockToolResult(toolName)
      if (result) {
        parts.push({
          type: 'tool-invocation',
          toolName,
          state: 'result',
          input: toolName === 'weather' ? { location: '上海' } : {},
          output: result as Record<string, unknown>,
        })
      }
    }

    parts.push({
      type: 'text',
      text: getMockReply(),
    })

    return parts
  }

  function sendMessage(text: string, fileParts?: { url: string; mediaType: string }[]) {
    const userParts: MessagePart[] = []
    if (fileParts) {
      for (const fp of fileParts) {
        userParts.push({
          type: 'source-url' as MessagePart['type'],
          url: fp.url,
          title: fp.url.split('/').pop(),
        })
      }
    }
    userParts.push({ type: 'text', text })

    messages.value = [
      ...messages.value,
      {
        id: crypto.randomUUID(),
        role: 'user',
        content: text,
        parts: userParts,
      },
    ]

    status.value = 'streaming'

    setTimeout(() => {
      messages.value = [
        ...messages.value,
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          content: '',
          parts: buildMockReplyParts(),
        },
      ]
      status.value = 'ready'
    }, 1200)
  }

  function regenerate() {
    const lastUserIndex = [...messages.value].reverse().findIndex((m) => m.role === 'user')
    if (lastUserIndex === -1) return

    // 删除最后一条助手消息
    if (messages.value[messages.value.length - 1]?.role === 'assistant') {
      messages.value = messages.value.slice(0, -1)
    }

    status.value = 'streaming'

    setTimeout(() => {
      messages.value = [
        ...messages.value,
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          content: '',
          parts: buildMockReplyParts(),
        },
      ]
      status.value = 'ready'
    }, 1200)
  }

  function editMessage(messageId: string, newText: string) {
    const idx = messages.value.findIndex((m) => m.id === messageId)
    if (idx === -1) return

    // 更新用户消息
    messages.value[idx] = {
      ...messages.value[idx],
      content: newText,
      parts: [{ type: 'text', text: newText }],
    }

    // 删除该消息之后的助手回复
    messages.value = messages.value.slice(0, idx + 1)

    status.value = 'streaming'

    setTimeout(() => {
      messages.value = [
        ...messages.value,
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          content: '',
          parts: buildMockReplyParts(),
        },
      ]
      status.value = 'ready'
    }, 1200)
  }

  function loadMessages(msgs: Message[]) {
    messages.value = msgs
    status.value = 'ready'
  }

  function resetMessages() {
    messages.value = []
    status.value = 'ready'
  }

  return {
    messages,
    status,
    sendMessage,
    regenerate,
    editMessage,
    loadMessages,
    resetMessages,
  }
}
