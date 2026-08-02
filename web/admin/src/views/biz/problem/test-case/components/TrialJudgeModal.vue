<script setup lang="ts">
import { computed, ref } from 'vue'
import TrialJudgePanel from './TrialJudgePanel.vue'

const props = defineProps<{ problemId?: string }>()
const showModal = ref(false)
const caseIds = ref<string[] | undefined>(undefined)
const caseLabel = ref<string | number | null>(null)

const modalTitle = computed(() => {
  if (caseLabel.value != null && caseLabel.value !== '') {
    return `试测 · 测例 #${caseLabel.value}`
  }
  if (caseIds.value?.length === 1) {
    return '试测 · 单条测例'
  }
  if (caseIds.value?.length) {
    return `试测 · ${caseIds.value.length} 条测例`
  }
  return '试测 · 全部测例'
})

function openModal(options?: { caseIds?: string[], caseLabel?: string | number | null }) {
  caseIds.value = options?.caseIds?.length ? [...options.caseIds] : undefined
  caseLabel.value = options?.caseLabel ?? null
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
    :title="modalTitle"
    class="w-[min(1200px,96vw)]"
    :mask-closable="false"
    :segmented="{ content: true, footer: true }"
  >
    <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
      <TrialJudgePanel
        v-if="props.problemId && showModal"
        :problem-id="props.problemId"
        :case-ids="caseIds"
        :case-label="caseLabel"
      />
    </NScrollbar>
    <template #footer>
      <NFlex justify="end">
        <NButton @click="closeModal">
          关闭
        </NButton>
      </NFlex>
    </template>
  </NModal>
</template>
