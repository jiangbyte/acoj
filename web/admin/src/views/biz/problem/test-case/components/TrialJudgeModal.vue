<script setup lang="ts">
import { ref } from 'vue'
import TrialJudgePanel from './TrialJudgePanel.vue'

const props = defineProps<{ problemId?: string }>()
const showModal = ref(false)

function openModal() {
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

defineExpose({ openModal })
</script>

<template>
  <NModal
    v-model:show="showModal"
    preset="card"
    title="试测"
    class="w-[min(1200px,96vw)]"
    :mask-closable="false"
    :segmented="{ content: true, footer: true }"
  >
    <TrialJudgePanel v-if="props.problemId && showModal" :problem-id="props.problemId" />
    <template #footer>
      <NFlex justify="end">
        <NButton @click="closeModal">
          关闭
        </NButton>
      </NFlex>
    </template>
  </NModal>
</template>
