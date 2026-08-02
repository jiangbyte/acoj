<script setup lang="ts">
import type * as monaco from 'monaco-editor'
import { computed, onBeforeUnmount, onMounted, shallowRef, watch } from 'vue'
import './monacoWorkers'

const props = withDefaults(
  defineProps<{
    modelValue?: string | null
    language?: string
    theme?: string
    height?: string | number
    readOnly?: boolean
  }>(),
  {
    modelValue: '',
    language: 'cpp',
    theme: 'vs',
    height: 360,
    readOnly: false,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const colorMode = useColorMode()
const containerRef = ref<HTMLElement | null>(null)
const monacoRef = shallowRef<typeof monaco | null>(null)
const editorRef = shallowRef<monaco.editor.IStandaloneCodeEditor | null>(null)
let disposed = false

const containerStyle = computed(() => ({
  height: typeof props.height === 'number' ? `${props.height}px` : props.height,
}))

const resolvedTheme = computed(() => {
  if (props.theme !== 'vs')
    return props.theme
  return colorMode.value === 'dark' ? 'vs-dark' : 'vs'
})

async function loadMonaco() {
  if (!monacoRef.value)
    monacoRef.value = await import('monaco-editor')
  return monacoRef.value
}

onMounted(async () => {
  if (!containerRef.value)
    return
  const monacoInstance = await loadMonaco()
  if (disposed || !containerRef.value)
    return
  editorRef.value = monacoInstance.editor.create(containerRef.value, {
    value: props.modelValue ?? '',
    language: props.language,
    theme: resolvedTheme.value,
    readOnly: props.readOnly,
    automaticLayout: true,
    minimap: { enabled: false },
    scrollBeyondLastLine: false,
    fontSize: 14,
  })
  editorRef.value.onDidChangeModelContent(() => {
    emit('update:modelValue', editorRef.value?.getValue() ?? '')
  })
})

watch(
  () => props.modelValue,
  (value) => {
    const editor = editorRef.value
    if (!editor || editor.getValue() === (value ?? ''))
      return
    editor.setValue(value ?? '')
  },
)

watch(
  () => props.language,
  (language) => {
    const editor = editorRef.value
    const monacoInstance = monacoRef.value
    if (!editor || !monacoInstance)
      return
    const model = editor.getModel()
    if (model)
      monacoInstance.editor.setModelLanguage(model, language)
  },
)

watch(resolvedTheme, (theme) => {
  monacoRef.value?.editor.setTheme(theme)
})

onBeforeUnmount(() => {
  disposed = true
  editorRef.value?.dispose()
  editorRef.value = null
})
</script>

<template>
  <div
    ref="containerRef"
    class="w-full overflow-hidden rounded-lg border border-default"
    :style="containerStyle"
  />
</template>
