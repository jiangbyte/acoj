<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NSpin, NTime } from 'naive-ui'
import { useDataJudgeCaseFetch } from '@/composables/v1'

const emit = defineEmits(['close'])

// Lazy-load heavy components
const CodeEditor = defineAsyncComponent({
  loader: () => import('@/components/common/editor/code/CodeEditor.vue'),
  // loader: () =>
  //   new Promise((resolve) => {
  //     // Simulate a three-second delay
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

const show = ref(false)
const { dataJudgeCaseDefaultData } = useDataJudgeCaseFetch()
const formData = ref<any>({ ...dataJudgeCaseDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataJudgeCaseDefaultData }
}

function doOpen(row: any) {
  show.value = true
  formData.value = row
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="Details">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="ID">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Submission ID">
          {{ formData.submitId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Input Data">
          <CodeEditor :model-value="formData.inputData" height="200px" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Output Data">
          <CodeEditor :model-value="formData.outputData" height="200px" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Expected Output">
          <CodeEditor :model-value="formData.expectedOutput" height="200px" />
        </NDescriptionsItem>
        <!-- <NDescriptionsItem label="Input File Path">
          {{ formData.inputFilePath }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Input File Size">
          {{ formData.inputFileSize }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Output File Path">
          {{ formData.outputFilePath }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Output File Size">
          {{ formData.outputFileSize }}
        </NDescriptionsItem> -->
        <NDescriptionsItem label="Maximum Time">
          {{ formData.maxTime }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Maximum Memory">
          {{ formData.maxMemory }}
        </NDescriptionsItem>
        <!-- <NDescriptionsItem label="Sample Judge Case">
          {{ formData.isSampleName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Score">
          {{ formData.score }}
        </NDescriptionsItem> -->
        <NDescriptionsItem label="RunStatus">
          {{ formData.status }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Exit Code">
          {{ formData.exitCode }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Created At">
          <NTime :time="Number(formData.createTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Created By">
          <!-- {{ formData.createUserName }} -->
          <NSpace align="center" size="small">
            <NAvatar
              size="small"
              :round="true"
              :src="formData.createUserAvatar"
            />
            <NText>{{ formData.createUserName }}</NText>
          </NSpace>
        </NDescriptionsItem>
        <NDescriptionsItem label="Updated At">
          <NTime :time="Number(formData.updateTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Updated By">
          <!-- {{ formData.updateUserName }} -->
          <NSpace align="center" size="small">
            <NAvatar
              size="small"
              :round="true"
              :src="formData.updateUserAvatar"
            />
            <NText>{{ formData.updateUserName }}</NText>
          </NSpace>
        </NDescriptionsItem>
      </NDescriptions>
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            Close
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
