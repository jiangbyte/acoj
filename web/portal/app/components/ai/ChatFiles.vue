<script setup lang="ts">
import type { FileWithStatus } from '~/composables/ai/useChatFileUpload'

defineProps<{
  files: FileWithStatus[]
}>()

const emit = defineEmits<{
  remove: [id: string]
}>()
</script>

<template>
  <div v-if="files.length > 0" class="flex flex-wrap gap-2 px-3 pt-2">
    <AiChatFilePreview
      v-for="file in files"
      :key="file.id"
      :file="file"
      removable
      @remove="emit('remove', $event)"
    />
  </div>
</template>
