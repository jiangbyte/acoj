<script lang="ts" setup>
import * as monaco from 'monaco-editor'
import CodeEditor from '@/components/common/editor/code/CodeEditor.vue'

const props = defineProps<{
  leftCode?: string
  rightCode?: string
  leftTokens?: string[] | string
  rightTokens?: string[] | string
  tiles?: Array<{ start1: number, start2: number, length: number }> | string
  language?: string
  height?: string
}>()

const COLORS = ['sim-hit-0', 'sim-hit-1', 'sim-hit-2', 'sim-hit-3', 'sim-hit-4', 'sim-hit-5']
const SWATCH = ['#ffd54f', '#81c784', '#64b5f6', '#f48fb1', '#ce93d8', '#ffab91']
const fallback = ref(false)
const leftEditor = shallowRef<monaco.editor.IStandaloneCodeEditor>()
const rightEditor = shallowRef<monaco.editor.IStandaloneCodeEditor>()
let leftDecos: monaco.editor.IEditorDecorationsCollection | null = null
let rightDecos: monaco.editor.IEditorDecorationsCollection | null = null

const editorOptions = {
  readOnly: true,
  automaticLayout: true,
  minimap: { enabled: false },
  scrollBeyondLastLine: false,
  fontSize: 13,
  wordWrap: 'on' as const,
}

function asList(value?: string[] | string) {
  if (!value)
    return []
  if (Array.isArray(value))
    return value
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed : []
  }
  catch {
    return []
  }
}

function asTiles(value?: Array<{ start1: number, start2: number, length: number }> | string) {
  if (!value)
    return []
  if (Array.isArray(value))
    return value.filter(tile => tile && tile.length > 0)
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed.filter((tile: { length?: number }) => tile && (tile.length || 0) > 0) : []
  }
  catch {
    return []
  }
}

/**
 * 没有落库匹配块时，按两侧 token 文本贪心对齐，只用于查看页着色。
 */
function inferTiles(left: string[], right: string[], minLen = 4) {
  // 1. 已用过的 token 不再进入下一块，避免同一段被涂两次
  const usedL = Array.from({ length: left.length }, () => false)
  const usedR = Array.from({ length: right.length }, () => false)
  const tiles: { start1: number, start2: number, length: number }[] = []
  while (left.length && right.length) {
    // 2. 找当前最长的连续相同 token；短于下限的不计，免得单个符号整页发亮
    let best: { start1: number, start2: number, length: number } | null = null
    for (let i = 0; i < left.length; i++) {
      if (usedL[i])
        continue
      for (let j = 0; j < right.length; j++) {
        if (usedR[j])
          continue
        let k = 0
        while (
          i + k < left.length
          && j + k < right.length
          && !usedL[i + k]
          && !usedR[j + k]
          && left[i + k] === right[j + k]
          && left[i + k]
        ) {
          k++
        }
        if (k >= minLen && (!best || k > best.length))
          best = { start1: i, start2: j, length: k }
      }
    }
    if (!best)
      break
    // 3. 记下这块并标记占用，再找下一段
    for (let k = 0; k < best.length; k++) {
      usedL[best.start1 + k] = true
      usedR[best.start2 + k] = true
    }
    tiles.push(best)
  }
  return tiles
}

const activeTiles = computed(() => {
  const stored = asTiles(props.tiles)
  if (stored.length)
    return stored
  return inferTiles(asList(props.leftTokens), asList(props.rightTokens))
})

/**
 * 按 token 文本顺序在源码中定位字符区间。
 */
function locate(code: string, texts: string[]) {
  const spans: { start: number, end: number }[] = []
  let cursor = 0
  for (const text of texts) {
    if (!text) {
      spans.push({ start: cursor, end: cursor })
      continue
    }
    const idx = code.indexOf(text, cursor)
    if (idx < 0)
      return null
    spans.push({ start: idx, end: idx + text.length })
    cursor = idx + text.length
  }
  return spans
}

function paint(editor: monaco.editor.IStandaloneCodeEditor, side: 1 | 2) {
  const model = editor.getModel()
  if (!model)
    return
  const code = side === 1 ? (props.leftCode || '') : (props.rightCode || '')
  const texts = asList(side === 1 ? props.leftTokens : props.rightTokens)
  const spans = locate(code, texts)
  if (!spans) {
    fallback.value = true
    return
  }
  const decorations = activeTiles.value.flatMap((tile, index) => {
    const start = side === 1 ? tile.start1 : tile.start2
    const first = spans[start]
    const last = spans[start + tile.length - 1]
    if (!first || !last || last.end <= first.start)
      return []
    const from = model.getPositionAt(first.start)
    const to = model.getPositionAt(last.end)
    return [{
      range: new monaco.Range(from.lineNumber, from.column, to.lineNumber, to.column),
      options: {
        inlineClassName: COLORS[index % COLORS.length],
        overviewRuler: {
          color: SWATCH[index % SWATCH.length],
          position: monaco.editor.OverviewRulerLane.Center,
        },
      },
    }]
  })
  const collection = side === 1 ? leftDecos : rightDecos
  if (collection) {
    collection.set(decorations)
    return
  }
  const created = editor.createDecorationsCollection(decorations)
  if (side === 1)
    leftDecos = created
  else
    rightDecos = created
}

function repaint() {
  fallback.value = false
  if (leftEditor.value)
    paint(leftEditor.value, 1)
  if (rightEditor.value)
    paint(rightEditor.value, 2)
}

function onLeft(editor: monaco.editor.IStandaloneCodeEditor) {
  leftEditor.value = editor
  editor.onDidChangeModelContent(() => paint(editor, 1))
  paint(editor, 1)
}
function onRight(editor: monaco.editor.IStandaloneCodeEditor) {
  rightEditor.value = editor
  editor.onDidChangeModelContent(() => paint(editor, 2))
  paint(editor, 2)
}

watch(() => [props.leftCode, props.rightCode, props.tiles, props.leftTokens, props.rightTokens], () => {
  nextTick(repaint)
}, { deep: true })

const leftList = computed(() => asList(props.leftTokens))
const rightList = computed(() => asList(props.rightTokens))

function hit(side: 1 | 2, index: number) {
  return activeTiles.value.some((tile) => {
    const start = side === 1 ? tile.start1 : tile.start2
    return index >= start && index < start + tile.length
  })
}
</script>

<template>
  <div>
    <div v-if="activeTiles.length" class="legend">
      Matching regions share a color.
    </div>
    <n-grid :cols="2" x-gap="12">
      <n-gi>
        <CodeEditor
          :model-value="leftCode || ''"
          :language="language || 'plaintext'"
          width="100%"
          :height="height || '320px'"
          :options="editorOptions"
          @editor-mounted="onLeft"
        />
      </n-gi>
      <n-gi>
        <CodeEditor
          :model-value="rightCode || ''"
          :language="language || 'plaintext'"
          width="100%"
          :height="height || '320px'"
          :options="editorOptions"
          @editor-mounted="onRight"
        />
      </n-gi>
    </n-grid>
    <n-grid v-if="fallback" :cols="2" x-gap="12" class="mt-3">
      <n-gi>
        <div class="token-col">
          <span
            v-for="(text, index) in leftList"
            :key="`l-${index}`"
            :class="{ hit: hit(1, index) }"
          >{{ text }} </span>
        </div>
      </n-gi>
      <n-gi>
        <div class="token-col">
          <span
            v-for="(text, index) in rightList"
            :key="`r-${index}`"
            :class="{ hit: hit(2, index) }"
          >{{ text }} </span>
        </div>
      </n-gi>
    </n-grid>
  </div>
</template>

<style>
.monaco-editor .view-line span.sim-hit-0 { background-color: rgba(255, 213, 79, 0.55) !important; }
.monaco-editor .view-line span.sim-hit-1 { background-color: rgba(129, 199, 132, 0.55) !important; }
.monaco-editor .view-line span.sim-hit-2 { background-color: rgba(100, 181, 246, 0.55) !important; }
.monaco-editor .view-line span.sim-hit-3 { background-color: rgba(244, 143, 177, 0.55) !important; }
.monaco-editor .view-line span.sim-hit-4 { background-color: rgba(206, 147, 216, 0.55) !important; }
.monaco-editor .view-line span.sim-hit-5 { background-color: rgba(255, 171, 145, 0.55) !important; }
</style>

<style scoped>
.legend {
  margin-bottom: 8px;
  font-size: 12px;
  opacity: 0.7;
}
.token-col {
  max-height: 160px;
  overflow: auto;
  font-family: monospace;
  line-height: 1.6;
  white-space: pre-wrap;
}
.hit {
  background: rgba(255, 213, 79, 0.7);
}
</style>
