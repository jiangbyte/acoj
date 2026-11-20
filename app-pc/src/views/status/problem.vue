<script lang="ts" setup>
import { useDataSubmitFetch } from '@/composables/v1'
import { AesCrypto } from '@/utils'
import { NSpin } from 'naive-ui'
// import CodeEditor from '@/components/common/editor/code/CodeEditor.vue'

// Implementation note.
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

const route = useRoute()
const detailData = ref()
const originalId = AesCrypto.decrypt(route.query.submit as string)

async function loadData() {
  try {
    const { dataSubmitDetail } = useDataSubmitFetch()
    const { data } = await dataSubmitDetail({ id: originalId })

    if (data) {
      detailData.value = data
    }
  }
  catch {
    // Implementation note.
  }
}

loadData()

function refreshSimilarityData() {
  // Implementation note.
  useDataSubmitFetch().dataSubmitDetail({ id: detailData.value.id }).then(({ data }) => {
    if (data) {
      // Implementation note.
      detailData.value = data

      window.$message.success('Refreshed')
    }
  })
}
</script>

<template>
  <main class="container mx-auto px-2 py-6">
    <n-grid
      cols="1 l:6"
      :x-gap="24"
      :y-gap="24"
      responsive="screen"
    >
      <!-- Section -->
      <n-gi span="1 l:2">
        <NSpace
          vertical
          :size="24"
        >
          <n-card class="rounded-xl" size="small">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                Submission Details
              </n-h2>
            </template>
            <n-space vertical :size="16">
              <JudgeResultHeader :result-task-data="detailData" />
              <n-flex justify="end">
                <n-button size="small" type="primary" @click="refreshSimilarityData">
                  Refresh
                </n-button>
              </n-flex>
              <!-- Section -->
              <JudgeResultStats :result-task-data="detailData" class="w-full" />
            </n-space>
            <!-- <n-space v-if="detailData?.submitType" vertical class="w-full mt-4" :size="16">
              <n-space align="center" justify="space-between">
                <n-text>Code Similarity</n-text>
                <n-space align="center">
                  <n-tag v-if="detailData?.similarityCategoryName" type="info">
                    {{ detailData?.similarityCategoryName }}
                  </n-tag>
                  <n-tag type="info">
                    {{ ((detailData?.similarity || 0) * 100).toFixed(2) }} %
                  </n-tag>
                </n-space>
              </n-space>
              <n-progress
                type="line"
                :show-indicator="false"
                :percentage="(detailData?.similarity || 0) * 100"
              />
              <n-alert type="info" show-icon>
                Clone detection estimates code similarity and is for reference only
              </n-alert>
            </n-space> -->
          </n-card>
        </NSpace>
      </n-gi>
      <!-- Section -->
      <n-gi span="1 l:4">
        <!-- Section -->
        <NSpace vertical :size="24">
          <n-card v-if="detailData?.message" size="small" class="rounded-xl">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                Error Details
              </n-h2>
            </template>
            <!-- <n-code :language="detailData?.language" :code="detailData?.message" show-line-numbers word-wrap /> -->
            <CodeEditor
              :model-value="detailData?.message"
              :language="detailData?.language"
              width="100%"
              style="height: calc(100vh - 500px)"
              :options="{
                readOnly: true,
                minimap: {
                  enabled: false,
                },
              }"
            />
          </n-card>
          <n-card size="small" class="rounded-xl">
            <template #header>
              <n-h2 class="pb-0 mb-0">
                Submit Code
              </n-h2>
            </template>
            <template #header-extra>
              <span class="text-sm text-gray-500 dark:text-gray-400">{{ detailData?.languageName }}</span>
            </template>
            <CodeEditor
              :model-value="detailData?.code"
              :language="detailData?.language"
              width="100%"
              style="height: calc(100vh - 250px)"
              :options="{
                readOnly: true,
              }"
            />
          </n-card>
        </NSpace>
      </n-gi>
    </n-grid>
  </main>
</template>

<style scoped>
</style>
