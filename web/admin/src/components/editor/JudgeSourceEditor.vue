<script setup lang="ts">
import MonacoEditor from '@/components/editor/MonacoEditor.vue'
import { computed, ref } from 'vue'

const props = withDefaults(defineProps<{
  value?: string | null
  language?: string
  height?: string | number
  loadButtonText?: string
  accept?: string
}>(), {
  value: '',
  language: 'cpp',
  height: '360px',
  loadButtonText: '从本地文件载入',
  accept: '.cpp,.cc,.cxx,.c,.h,.hpp,.txt,text/*',
})

const emit = defineEmits<{
  'update:value': [value: string]
}>()

const fileInputRef = ref<HTMLInputElement | null>(null)

const editorValue = computed({
  get: () => props.value ?? '',
  set: (v: string) => emit('update:value', v),
})

function openFilePicker() {
  fileInputRef.value?.click()
}

async function onFileSelected(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) {
    return
  }
  try {
    const text = await file.text()
    emit('update:value', text)
  }
  catch {
    window.$message?.error?.('读取本地文件失败')
  }
}
</script>

<template>
  <NFlex vertical :size="8" class="w-full">
    <NFlex :size="8">
      <NButton size="small" @click="openFilePicker">
        {{ loadButtonText }}
      </NButton>
      <input
        ref="fileInputRef"
        type="file"
        class="hidden"
        :accept="accept"
        @change="onFileSelected"
      >
    </NFlex>
    <MonacoEditor
      v-model:value="editorValue"
      :language="language"
      :height="height"
      theme="vs"
    />
  </NFlex>
</template>
