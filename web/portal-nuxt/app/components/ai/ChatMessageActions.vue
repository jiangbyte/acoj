<script setup lang="ts">
import type { Message } from '~/utils/ai'

const props = defineProps<{
  message: Message
  streaming: boolean
  editing: boolean
  vote: boolean | null
}>()

const emit = defineEmits<{
  vote: [message: Message, isUpvoted: boolean]
  edit: [message: Message]
  regenerate: [message: Message]
}>()

const { copy, copied } = useClipboard()

function handleCopy() {
  const text = props.message.parts
    .filter((p) => p.type === 'text')
    .map((p) => (p as { text: string }).text)
    .join('\n')
  copy(text)
}
</script>

<template>
  <div class="flex items-center gap-1 py-1">
    <!-- 复制 -->
    <UTooltip :text="copied ? '已复制' : '复制'" :delay="500">
      <UButton
        :icon="copied ? 'i-lucide-check' : 'i-lucide-copy'"
        color="neutral"
        variant="ghost"
        size="xs"
        @click="handleCopy"
      />
    </UTooltip>

    <!-- 点赞/点踩（仅助手消息） -->
    <template v-if="message.role === 'assistant' && !streaming">
      <UTooltip text="有用" :delay="500">
        <UButton
          icon="i-lucide-thumbs-up"
          color="neutral"
          variant="ghost"
          size="xs"
          :class="{ 'text-primary': vote === true }"
          @click="emit('vote', message, true)"
        />
      </UTooltip>

      <UTooltip text="没用" :delay="500">
        <UButton
          icon="i-lucide-thumbs-down"
          color="neutral"
          variant="ghost"
          size="xs"
          :class="{ 'text-primary': vote === false }"
          @click="emit('vote', message, false)"
        />
      </UTooltip>
    </template>

    <!-- 重新生成（仅助手消息，非 streaming） -->
    <UTooltip v-if="message.role === 'assistant' && !streaming" text="重新生成" :delay="500">
      <UButton
        icon="i-lucide-refresh-ccw"
        color="neutral"
        variant="ghost"
        size="xs"
        @click="emit('regenerate', message)"
      />
    </UTooltip>

    <!-- 编辑（仅用户消息，非编辑状态） -->
    <UTooltip v-if="message.role === 'user' && !editing" text="编辑" :delay="500">
      <UButton
        icon="i-lucide-pencil"
        color="neutral"
        variant="ghost"
        size="xs"
        @click="emit('edit', message)"
      />
    </UTooltip>
  </div>
</template>
