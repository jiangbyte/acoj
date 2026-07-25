<script setup lang="ts">
import type { FileWithStatus } from '~/composables/ai/useChatFileUpload'

const props = defineProps<{
  file: FileWithStatus
  removable?: boolean
}>()

const emit = defineEmits<{
  remove: [id: string]
}>()

const showZoom = ref(false)
const isImage = props.file.type.startsWith('image/')
</script>

<template>
  <div class="relative group rounded-lg overflow-hidden border border-default size-20 shrink-0">
    <!-- 图片预览 -->
    <button
      v-if="isImage"
      class="size-full bg-elevated/50 overflow-hidden cursor-pointer"
      @click="showZoom = true"
    >
      <img
        :src="file.url"
        :alt="file.name"
        class="size-full object-cover transition-transform duration-200 group-hover:scale-105"
      />
    </button>

    <!-- 非图片 -->
    <div v-else class="size-full flex flex-col items-center justify-center gap-1 bg-elevated/50">
      <UIcon name="i-lucide-file" class="size-6 text-muted shrink-0" />
      <span class="text-[10px] text-muted truncate max-w-[90%] text-center leading-tight">{{
        file.name
      }}</span>
    </div>

    <!-- 上传状态 -->
    <div
      v-if="file.status === 'uploading'"
      class="absolute inset-0 bg-default/60 flex items-center justify-center"
    >
      <UIcon name="i-lucide-loader" class="size-5 animate-spin text-muted" />
    </div>

    <!-- 删除按钮 -->
    <button
      v-if="removable && file.status !== 'uploading'"
      class="absolute top-0.5 right-0.5 size-4 flex items-center justify-center rounded-full bg-default/80 opacity-0 group-hover:opacity-100 transition-opacity"
      @click="emit('remove', file.id)"
    >
      <UIcon name="i-lucide-x" class="size-2.5" />
    </button>

    <!-- 图片放大模态框 -->
    <ClientOnly>
      <Teleport to="body">
        <div
          v-if="showZoom"
          class="fixed inset-0 z-50 flex items-center justify-center bg-black/60"
          @click="showZoom = false"
        >
          <img
            :src="file.url"
            :alt="file.name"
            class="max-w-[95vw] max-h-[95vh] object-contain"
            @click.stop
          />
        </div>
      </Teleport>
    </ClientOnly>
  </div>
</template>
