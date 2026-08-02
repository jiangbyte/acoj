import { getToken } from '@/utils/storage'
import { submissionDetail } from './submission'
import type { SubmissionSnapshot } from './problem'

const prefix = '/api/v1/portal'

const TERMINAL = new Set(['COMPLETED', 'FAILED'])

export function isTerminalStatus(status: string) {
  return TERMINAL.has(status)
}

function resolveBaseURL() {
  return (import.meta.env.VITE_API_URL || '').replace(/\/$/, '')
}

function buildEventsUrl(submissionId: string, maxWaitSec: number) {
  const base = resolveBaseURL()
  const params = new URLSearchParams({ id: submissionId, max_wait_sec: String(maxWaitSec) })
  return `${base}${prefix}/biz/submission/events?${params.toString()}`
}

export interface SubmissionWatchHandlers {
  onSnapshot?: (snap: SubmissionSnapshot) => void
  onUpdate?: (snap: SubmissionSnapshot) => void
  onDone?: (snap: SubmissionSnapshot) => void
  onTimeout?: (snap: SubmissionSnapshot) => void
  onError?: (error: unknown) => void
}

export interface SubmissionWatchOptions {
  maxWaitSec?: number
  signal?: AbortSignal
}

function parseSseBlock(block: string) {
  let event = 'message'
  const data: string[] = []
  for (const line of block.split('\n')) {
    if (line.startsWith('event:')) {
      event = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      data.push(line.slice(5).trimStart())
    }
  }
  return { event, data: data.join('\n') }
}

/**
 * 订阅判题事件流（SSE）。事件: snapshot / update / done / timeout。
 * 通过 options.signal 取消；连接异常时回调 onError。
 */
export async function watchSubmissionEvents(
  submissionId: string,
  handlers: SubmissionWatchHandlers,
  options: SubmissionWatchOptions = {},
) {
  const maxWaitSec = options.maxWaitSec ?? 120
  const token = getToken()
  const controller = new AbortController()
  const external = options.signal
  const onExternalAbort = () => controller.abort()
  if (external) {
    if (external.aborted) {
      controller.abort()
    } else {
      external.addEventListener('abort', onExternalAbort, { once: true })
    }
  }

  try {
    const response = await fetch(buildEventsUrl(submissionId, maxWaitSec), {
      headers: token ? { Authorization: token } : {},
      signal: controller.signal,
    })
    if (!response.ok) {
      throw new Error(`SSE 请求失败: ${response.status}`)
    }
    const body = response.body
    if (!body) {
      throw new Error('SSE 响应无 body')
    }
    const reader = body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    const read = async (): Promise<void> => {
      while (true) {
        const { value, done } = await reader.read()
        if (done) {
          break
        }
        buffer += decoder.decode(value, { stream: true })
        let sepIndex = buffer.indexOf('\n\n')
        while (sepIndex >= 0) {
          const block = buffer.slice(0, sepIndex)
          buffer = buffer.slice(sepIndex + 2)
          handleBlock(block)
          sepIndex = buffer.indexOf('\n\n')
        }
      }
    }

    const handleBlock = (block: string) => {
      if (!block.trim() || block.trim().startsWith(':')) {
        return
      }
      const { event, data } = parseSseBlock(block)
      if (!data) {
        return
      }
      try {
        const snap = JSON.parse(data) as SubmissionSnapshot
        if (event === 'snapshot') {
          handlers.onSnapshot?.(snap)
        } else if (event === 'update') {
          handlers.onUpdate?.(snap)
        } else if (event === 'done') {
          handlers.onDone?.(snap)
        } else if (event === 'timeout') {
          handlers.onTimeout?.(snap)
        }
      } catch {
        // ignore malformed frames
      }
    }

    await read()
  } catch (error) {
    if ((error as Error)?.name === 'AbortError') {
      return
    }
    handlers.onError?.(error)
  } finally {
    if (external) {
      external.removeEventListener('abort', onExternalAbort)
    }
  }
}

/**
 * 轮询兜底：反复拉取提交详情直到终态。
 */
export async function pollSubmissionUntilDone(
  submissionId: string,
  options: { maxWaitMs?: number; intervalMs?: number; signal?: AbortSignal } = {},
) {
  const maxWaitMs = options.maxWaitMs ?? 120_000
  const intervalMs = options.intervalMs ?? 1000
  const deadline = Date.now() + maxWaitMs
  const signal = options.signal

  while (true) {
    if (signal?.aborted) {
      throw new Error('poll aborted')
    }
    const res = await submissionDetail(submissionId)
    const detail = res.data
    const snap: SubmissionSnapshot = {
      submission_id: detail.id,
      status: detail.status,
      result: detail.result,
      score: detail.score,
      time_ms: detail.time_ms,
      memory_kb: detail.memory_kb,
      compile_output: detail.compile_output,
      compile_error: detail.result === 'CE' || Boolean(detail.error && detail.status === 'FAILED'),
      cases: detail.cases.map((c) => ({
        case_no: c.case_no,
        result: c.result,
        points: c.score,
        score: c.score,
        time_ms: c.time_ms,
        memory_kb: c.memory_kb,
        stdout_preview: c.stdout_preview ?? '',
        stderr_preview: c.stderr_preview ?? '',
      })),
      error: detail.error,
      wall_time_ms: 0,
    }
    if (isTerminalStatus(detail.status) || Date.now() >= deadline) {
      return snap
    }
    await new Promise((resolve) => setTimeout(resolve, intervalMs))
  }
}
