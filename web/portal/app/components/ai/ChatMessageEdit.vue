<script setup lang="ts">
const props = defineProps<{
  messageId: string
  text: string
}>()

const emit = defineEmits<{
  save: [messageId: string, text: string]
  cancel: []
}>()

const editText = ref(props.text)

function handleSave() {
  if (editText.value.trim()) {
    emit('save', props.messageId, editText.value)
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) {
    handleSave()
  }
  if (e.key === 'Escape') {
    emit('cancel')
  }
}
</script>

<template>
  <div class="flex flex-col gap-2">
    <UTextarea
      v-model="editText"
      class="w-full"
      :ui="{ base: 'min-h-20' }"
      @keydown="handleKeydown"
    />
    <div class="flex gap-2 justify-end">
      <UButton label="取消" color="neutral" variant="outline" size="sm" @click="emit('cancel')" />
      <UButton label="保存" color="primary" size="sm" @click="handleSave" />
    </div>
  </div>
</template>
