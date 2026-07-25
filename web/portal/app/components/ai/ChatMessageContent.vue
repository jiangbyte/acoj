<script setup lang="ts">
import type { Message, ToolInvocationPart } from '~/utils/ai'
import {
  isReasoningUIPart,
  isTextUIPart,
  isToolUIPart,
  getToolName,
  getMergedParts,
} from '~/utils/ai'
import { getSources, getSearchQuery } from '~/utils/ai/tool'

const _props = defineProps<{
  message: Message
  editing: boolean
}>()

const emit = defineEmits<{
  save: [message: Message, text: string]
  cancelEdit: []
}>()
</script>

<template>
  <template
    v-for="(part, index) in getMergedParts(message.parts)"
    :key="`${message.id}-${part.type}-${index}`"
  >
    <!-- 推理过程 -->
    <UChatReasoning
      v-if="isReasoningUIPart(part)"
      :text="part.text"
      :streaming="false"
      chevron="leading"
    >
      <AiChatComark :markdown="part.text" />
    </UChatReasoning>

    <!-- 工具调用 -->
    <template v-else-if="isToolUIPart(part)">
      <!-- 图表工具 -->
      <AiChatToolChart
        v-if="getToolName(part as ToolInvocationPart) === 'chart'"
        :title="((part as ToolInvocationPart).output?.title as string) || '图表'"
        :labels="((part as ToolInvocationPart).output?.labels as string[]) || []"
        :values="((part as ToolInvocationPart).output?.values as number[]) || []"
      />
      <!-- 天气工具 -->
      <AiChatToolWeather
        v-else-if="getToolName(part as ToolInvocationPart) === 'weather'"
        :location="((part as ToolInvocationPart).output?.location as string) || '未知'"
        :temperature="((part as ToolInvocationPart).output?.temperature as number) || 0"
        :condition="((part as ToolInvocationPart).output?.condition as string) || 'sunny'"
        :humidity="((part as ToolInvocationPart).output?.humidity as number) || 0"
        :wind-speed="((part as ToolInvocationPart).output?.windSpeed as number) || 0"
      />
      <!-- 搜索工具 -->
      <UChatTool
        v-else-if="getToolName(part as ToolInvocationPart) === 'web_search'"
        text="已搜索网络"
        :suffix="getSearchQuery(part as ToolInvocationPart)"
        :streaming="false"
        chevron="leading"
      >
        <AiChatToolSources :sources="getSources(part as ToolInvocationPart)" />
      </UChatTool>
    </template>

    <!-- 文本内容 -->
    <template v-else-if="isTextUIPart(part)">
      <!-- 助手消息使用 Comark 渲染 -->
      <AiChatComark v-if="message.role === 'assistant'" :markdown="part.text" />
      <!-- 用户消息 -->
      <template v-else-if="message.role === 'user'">
        <!-- 编辑模式 -->
        <AiChatMessageEdit
          v-if="editing"
          :message-id="message.id"
          :text="part.text"
          @save="(msgId, text) => emit('save', message, text)"
          @cancel="emit('cancelEdit')"
        />
        <!-- 非编辑模式 -->
        <p v-else class="whitespace-pre-wrap text-sm leading-6">
          {{ part.text }}
        </p>
      </template>
    </template>
  </template>
</template>
