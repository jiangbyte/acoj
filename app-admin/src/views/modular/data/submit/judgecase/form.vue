<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NSpin } from 'naive-ui'
import { useDataJudgeCaseFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])

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
        h(NSpin, { size: 'small', description: 'Loading editor...' }, { }),
      ])
    },
  },
  delay: 200,
  timeout: 10000,
})

const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { dataJudgeCaseDefaultData, dataJudgeCaseAdd, dataJudgeCaseEdit } = useDataJudgeCaseFetch()
const formData = ref<any>({ ...dataJudgeCaseDefaultData })
const rules = {
  submitId: [
    { required: true, message: 'Enter Submission ID', trigger: ['input', 'blur'] },
  ],
  inputData: [
    { required: true, message: 'Enter Input Data', trigger: ['input', 'blur'] },
  ],
  outputData: [
    { required: true, message: 'Enter Output Data', trigger: ['input', 'blur'] },
  ],
  expectedOutput: [
    { required: true, message: 'Enter Expected Output', trigger: ['input', 'blur'] },
  ],
  inputFilePath: [
    { required: true, message: 'Enter Input File Path', trigger: ['input', 'blur'] },
  ],
  inputFileSize: [
    { required: true, message: 'Enter Input File Size', type: 'number', trigger: ['input', 'blur'] },
  ],
  outputFilePath: [
    { required: true, message: 'Enter Output File Path', trigger: ['input', 'blur'] },
  ],
  outputFileSize: [
    { required: true, message: 'Enter Output File Size', type: 'number', trigger: ['input', 'blur'] },
  ],
  maxTime: [
    { required: true, message: 'Enter Maximum Time', type: 'number', trigger: ['input', 'blur'] },
  ],
  maxMemory: [
    { required: true, message: 'Enter Maximum Memory', type: 'number', trigger: ['input', 'blur'] },
  ],
  score: [
    { required: true, message: 'Enter Score', type: 'number', trigger: ['input', 'blur'] },
  ],
  status: [
    { required: true, message: 'Enter RunStatus', trigger: ['input', 'blur'] },
  ],
  exitCode: [
    { required: true, message: 'Enter Exit Code', type: 'number', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataJudgeCaseDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await dataJudgeCaseEdit(formData.value)
        if (success) {
          window.$message.success('Updated successfully')
        }
      }
      else {
        const { success } = await dataJudgeCaseAdd(formData.value)
        if (success) {
          window.$message.success('Created successfully')
        }
      }
      emit('submit', true)
      doClose()
      show.value = false
      loading.value = false
    }
    else {
      //
    }
  })
}

function doOpen(row: any = null, edit: boolean = false, submitIdStr: string) {
  show.value = true
  isEdit.value = edit
  formData.value = Object.assign(formData.value, row)
  formData.value.submitId = submitIdStr
  console.log('Form Problem ID-->', formData.value.submitId, submitIdStr)
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent :title="isEdit ? 'Edit' : 'Add'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- Text input -->
        <NFormItem v-if="isEdit" label="ID" path="id">
          <NInput v-model:value="formData.id" placeholder="Enter ID" :disabled="true" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Submission ID" path="submitId">
          <NInput v-model:value="formData.submitId" placeholder="Enter Submission ID" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Input Data" path="inputData">
          <!-- <NInput v-model:value="formData.inputData" placeholder="Enter Input Data" /> -->
          <CodeEditor v-model="formData.inputData" height="200px" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Output Data" path="outputData">
          <!-- <NInput v-model:value="formData.outputData" placeholder="Enter Output Data" /> -->
          <CodeEditor v-model="formData.outputData" height="200px" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Expected Output" path="expectedOutput">
          <!-- <NInput v-model:value="formData.expectedOutput" placeholder="Enter Expected Output" /> -->
          <CodeEditor v-model="formData.expectedOutput" height="200px" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Input File Path" path="inputFilePath">
          <NInput v-model:value="formData.inputFilePath" placeholder="Enter Input File Path" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Input File Size" path="inputFileSize">
          <NInputNumber v-model:value="formData.inputFileSize" :min="0" :max="100" placeholder="Enter Input File Size" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Output File Path" path="outputFilePath">
          <NInput v-model:value="formData.outputFilePath" placeholder="Enter Output File Path" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Output File Size" path="outputFileSize">
          <NInputNumber v-model:value="formData.outputFileSize" :min="0" :max="100" placeholder="Enter Output File Size" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Maximum Time" path="maxTime">
          <NInputNumber v-model:value="formData.maxTime" :min="0" :max="100" placeholder="Enter Maximum Time" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Maximum Memory" path="maxMemory">
          <NInputNumber v-model:value="formData.maxMemory" :min="0" :max="100" placeholder="Enter Maximum Memory" />
        </NFormItem>
        <!-- Boolean selector -->
        <NFormItem label="Sample Judge Case" path="isSample">
          <NRadioGroup v-model:value="formData.isSample">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Score" path="score">
          <NInputNumber v-model:value="formData.score" :min="0" :max="100" placeholder="Enter Score" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="RunStatus" path="status">
          <NInput v-model:value="formData.status" placeholder="Enter RunStatus" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Exit Code" path="exitCode">
          <NInputNumber v-model:value="formData.exitCode" :min="0" :max="100" placeholder="Enter Exit Code" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            Close
          </NButton>
          <NButton type="primary" :loading="loading" @click="doSubmit">
            <template #icon>
              <IconParkOutlineSave />
            </template>
            Save
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
