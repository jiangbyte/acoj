/**
 * Watch a submission via fetch-based SSE (Authorization header; EventSource cannot).
 * Avoid Nuxt composables here — callers pass apiBaseUrl/token from setup.
 */

const TERMINAL = new Set(['COMPLETED', 'FAILED'])

export type SubmissionWatchSnapshot = {
  submission_id: string
  status: string
  result?: string | null
  score?: number
  time_ms?: number
  memory_kb?: number
  compile_output?: string | null
  compile_error?: boolean
  cases?: any[]
  error?: string | null
  wall_time_ms?: number
}

function eventsUrl(apiBaseUrl: string, submissionId: string, maxWaitSec: number) {
  const base = String(apiBaseUrl || '').replace(/\/$/, '')
  const qs = new URLSearchParams({
    id: submissionId,
    max_wait_sec: String(maxWaitSec),
  })
  return `${base}/api/v1/portal/biz/submission/events?${qs}`
}

function parseSseChunk(buffer: string): { events: Array<{ event: string, data: string }>, rest: string } {
  const parts = buffer.split('\n\n')
  const rest = parts.pop() ?? ''
  const events: Array<{ event: string, data: string }> = []
  for (const part of parts) {
    if (!part.trim() || part.startsWith(':'))
      continue
    let event = 'message'
    const dataLines: string[] = []
    for (const line of part.split('\n')) {
      if (line.startsWith('event:'))
        event = line.slice(6).trim()
      else if (line.startsWith('data:'))
        dataLines.push(line.slice(5).trim())
    }
    if (dataLines.length)
      events.push({ event, data: dataLines.join('\n') })
  }
  return { events, rest }
}

export async function watchSubmissionEvents(
  submissionId: string,
  options: {
    apiBaseUrl: string
    token?: string | null
    maxWaitSec?: number
    signal?: AbortSignal
    onUpdate: (snap: SubmissionWatchSnapshot) => void
  },
): Promise<SubmissionWatchSnapshot | null> {
  const maxWaitSec = options.maxWaitSec ?? 120
  const response = await fetch(eventsUrl(options.apiBaseUrl, submissionId, maxWaitSec), {
    method: 'GET',
    headers: {
      Accept: 'text/event-stream',
      ...(options.token ? { Authorization: options.token } : {}),
    },
    signal: options.signal,
  })
  if (!response.ok || !response.body) {
    throw new Error(`SSE 连接失败 (${response.status})`)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  let last: SubmissionWatchSnapshot | null = null

  while (true) {
    const { done, value } = await reader.read()
    if (done)
      break
    buffer += decoder.decode(value, { stream: true })
    const parsed = parseSseChunk(buffer)
    buffer = parsed.rest
    for (const item of parsed.events) {
      try {
        const snap = JSON.parse(item.data) as SubmissionWatchSnapshot
        last = snap
        options.onUpdate(snap)
        if (item.event === 'done' || item.event === 'timeout' || TERMINAL.has(String(snap.status))) {
          try {
            await reader.cancel()
          }
          catch {
            // ignore
          }
          return snap
        }
      }
      catch {
        // ignore malformed frames
      }
    }
  }
  return last
}

export async function pollSubmissionUntilDone(
  submissionId: string,
  options: {
    maxWaitSec?: number
    intervalMs?: number
    signal?: AbortSignal
    fetchDetail: (id: string) => Promise<any>
    onUpdate: (snap: SubmissionWatchSnapshot) => void
  },
): Promise<SubmissionWatchSnapshot | null> {
  const maxWaitSec = options.maxWaitSec ?? 120
  const intervalMs = options.intervalMs ?? 800
  const deadline = Date.now() + maxWaitSec * 1000
  let last: SubmissionWatchSnapshot | null = null
  while (Date.now() < deadline) {
    if (options.signal?.aborted)
      throw new DOMException('Aborted', 'AbortError')
    const detail = await options.fetchDetail(submissionId)
    const snap: SubmissionWatchSnapshot = {
      submission_id: detail.id,
      status: detail.status,
      result: detail.result,
      score: detail.score,
      time_ms: detail.time_ms,
      memory_kb: detail.memory_kb,
      compile_output: detail.compile_output,
      compile_error: detail.result === 'CE',
      cases: (detail.cases || []).map((c: any) => ({
        case_no: c.case_no,
        result: c.result,
        points: c.score,
        score: c.score,
        time_ms: c.time_ms,
        memory_kb: c.memory_kb,
      })),
      error: detail.error,
      wall_time_ms: 0,
    }
    last = snap
    options.onUpdate(snap)
    if (TERMINAL.has(String(snap.status)))
      return snap
    await new Promise(resolve => setTimeout(resolve, intervalMs))
  }
  return last
}
