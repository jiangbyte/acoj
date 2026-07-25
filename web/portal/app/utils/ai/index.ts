import type { Source } from './tool'

export interface TextPart {
  type: 'text'
  text: string
}

export interface ReasoningPart {
  type: 'reasoning'
  text: string
}

export interface ToolInvocationPart {
  type: 'tool-invocation'
  toolName: string
  state: 'call' | 'result'
  input?: Record<string, unknown>
  output?: Record<string, unknown>
}

export interface SourceUrlPart {
  type: 'source-url'
  url: string
  title?: string
}

export type MessagePart = TextPart | ReasoningPart | ToolInvocationPart | SourceUrlPart

export function isReasoningUIPart(part: MessagePart): part is ReasoningPart {
  return part.type === 'reasoning'
}

export function isTextUIPart(part: MessagePart): part is TextPart {
  return part.type === 'text'
}

export function isToolUIPart(part: MessagePart): part is ToolInvocationPart {
  return part.type === 'tool-invocation'
}

export function getToolName(part: ToolInvocationPart): string {
  return part.toolName
}

export function getMergedParts(parts: MessagePart[]): MessagePart[] {
  const merged: MessagePart[] = []
  for (const part of parts) {
    const last = merged[merged.length - 1]
    if (last?.type === 'text' && part.type === 'text') {
      last.text += part.text
    } else {
      merged.push(part)
    }
  }
  return merged
}

export function sourceToInlineMdc(source: Source): string {
  const favicon = source.url ? `/api/placeholder/favicon?url=${encodeURIComponent(source.url)}` : ''
  return `:source-link{url="${source.url}" favicon="${favicon}" label="${source.title ?? source.url}"}`
}

export function getMockToolResult(toolName: string) {
  const charts: Record<string, { title: string; labels: string[]; values: number[] }> = {
    sales: {
      title: '月度销售额',
      labels: ['1月', '2月', '3月', '4月', '5月', '6月'],
      values: [12000, 19000, 15000, 22000, 28000, 24000],
    },
    users: {
      title: '月活跃用户',
      labels: ['1月', '2月', '3月', '4月', '5月', '6月'],
      values: [4500, 5200, 4800, 6100, 7300, 6900],
    },
  }

  switch (toolName) {
    case 'weather':
      return {
        location: '上海',
        temperature: 28,
        condition: 'sunny' as const,
        humidity: 65,
        windSpeed: 12,
      }
    case 'chart': {
      const keys = Object.keys(charts)
      return charts[keys[Math.floor(Math.random() * keys.length)]]
    }
    case 'web_search':
      return {
        sources: [
          { url: 'https://example.com/article-1', title: '最新技术趋势分析' },
          { url: 'https://example.com/article-2', title: 'AI 领域突破性进展' },
          { url: 'https://example.com/article-3', title: '开发者工具推荐' },
        ],
      }
    default:
      return null
  }
}

export function getMockReply(): string {
  const replies = [
    '你好！我是 HEI AI 助手，有什么可以帮助你的？',
    '这是一个很好的问题！让我来为你解答。\n\n从技术角度来说，这个问题的核心在于数据流的设计。我们可以采用以下几种方案：\n\n1. **事件驱动架构** — 通过事件总线解耦各个模块\n2. **响应式数据流** — 利用 Vue 的响应式系统自动追踪变更\n3. **单向数据流** — 借鉴 Flux 模式，保证数据流向清晰\n\n每种方案都有不同的适用场景，具体取决于你的业务需求。',
    '好的，我明白了。让我思考一下最佳方案。\n\n根据你的需求，我建议可以这样做：\n- 首先，梳理核心功能模块\n- 其次，确定数据存储方案\n- 最后，设计 API 接口和组件结构\n\n这个方案的优势在于：\n1. 可维护性强\n2. 扩展性好\n3. 团队协作成本低',
    '根据你的需求，我建议可以这样做。\n\n```typescript\n// 示例代码\nfunction processData(input: string): string {\n  return `处理结果: ${input}`\n}\n```\n\n这样实现简洁高效，同时保持了代码的可读性。',
    '请继续提问，我很乐意帮助你！',
  ]
  return replies[Math.floor(Math.random() * replies.length)]
}

export function getMockReasoning(): string {
  const thoughts = [
    '让我分析一下这个问题的关键点。\n\n首先，用户的核心需求是什么？我需要从问题的上下文出发，理解用户的真正意图。\n\n其次，考虑可行的解决方案。在现有的技术框架下，有多种方法可以实现这个功能。',
    '这是一个有意思的问题。\n\n从技术角度分析，需要考虑以下几个方面：\n1. 功能需求是否明确\n2. 技术方案的可行性\n3. 实现的复杂度\n\n让我逐步思考。',
  ]
  return thoughts[Math.floor(Math.random() * thoughts.length)]
}
