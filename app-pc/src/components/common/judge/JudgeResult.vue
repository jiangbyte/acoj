<script lang="ts" setup>
import { useDataSubmitFetch } from '@/composables/v1'
import { NSpin } from 'naive-ui'

interface Props {
  isPolling: boolean
  pollingCount: number
  maxPollingCount: number
  resultTaskData: any
}

const props = defineProps<Props>()

const CodeEditor = defineAsyncComponent({
  loader: () => import('@/components/common/editor/code/CodeEditor.vue'),
  // loader: () =>
  //   new Promise((resolve) => {
  // Implementation note.
  //     setTimeout(() => {
  //       resolve(import('@/components/common/editor/code/CodeEditor.vue'))
  //     }, 3000)
  //   }),
  loadingComponent: {
    setup() {
      return () => h('div', {
        class: 'h-full p-4',
        style: {
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        },
      }, [
        h(NSpin, { size: 'small', description: 'Loading code preview...' }, { }),
      ])
    },
  },
  delay: 200,
  timeout: 10000,
})

const similarityData = ref(props.resultTaskData)

watch(() => props.resultTaskData, (newValue) => {
  similarityData.value = newValue
})

function refreshSimilarityData() {
  // Implementation note.
  useDataSubmitFetch().dataSubmitDetail({ id: props.resultTaskData.id }).then(({ data }) => {
    if (data) {
      // Implementation note.
      similarityData.value = data

      window.$notification.success({
        title: 'Refreshed',
        duration: 3000,
      })
    }
  })
}
</script>

<template>
  <div>
    <n-alert v-if="isPolling" type="warning" :show-icon="false">
      <n-flex align="center" justify="center">
        <NSpin size="small" />
        <n-text type="info">
          Judging... ({{ pollingCount }}/{{ maxPollingCount }})
        </n-text>
      </n-flex>
    </n-alert>

    <n-card v-if="isPolling || resultTaskData.id" class="w-full max-w-3xl mx-auto" :bordered="false" size="small">
      <n-space vertical :size="16">
        <!-- Section -->
        <JudgeResultHeader :result-task-data="resultTaskData" />

        <n-flex justify="end">
          <n-button size="small" type="primary" @click="refreshSimilarityData">
            Refresh
          </n-button>
        </n-flex>

        <!-- Section -->
        <JudgeResultStats :result-task-data="resultTaskData" />

        <!-- <n-alert type="info">
          You can view code similarity later under Status, or wait a moment and click <n-button size="small" type="primary" @click="refreshSimilarityData">Refresh</n-button>
        </n-alert> -->

        <!-- Code Similarity -->
        <!-- <SimilarityReport v-if="resultTaskData.submitType" :result-task-data="similarityData" /> -->

        <!-- <n-card v-if="resultTaskData?.message" size="small" hoverable>
          <n-code :language="resultTaskData?.language" :code="resultTaskData?.message" show-line-numbers word-wrap />
        </n-card> -->
        <!-- Error Details -->
        <n-card v-if="resultTaskData.message" size="small" class="rounded-xl">
          <template #header>
            <n-h2 class="pb-0 mb-0">
              Error Details
            </n-h2>
          </template>
          <!-- <n-code :language="detailData?.language" :code="detailData?.message" show-line-numbers word-wrap /> -->
          <CodeEditor
            :model-value="resultTaskData.message"
            :language="resultTaskData.language"
            width="100%"
            style="height: 300px"
            :options="{
              readOnly: true,
              minimap: {
                enabled: false,
              },
            }"
          />
        </n-card>
      </n-space>
    </n-card>

    <n-empty v-else class="flex flex-col items-center justify-center py-12" description="No judge result">
      <template #icon>
        <n-icon size="40" class="text-gray-300 dark:text-gray-600">
          <icon-park-outline-info />
        </n-icon>
      </template>
      <n-text depth="3" class="text-center max-w-xs">
        No submission or judge result was found
      </n-text>
    </n-empty>
  </div>
</template>
