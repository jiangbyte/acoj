<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useDataSubmitFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { dataSubmitDefaultData, dataSubmitAdd, dataSubmitEdit } = useDataSubmitFetch()
const formData = ref<any>({ ...dataSubmitDefaultData })
const rules = {
  userId: [
    { required: true, message: 'Enter User ID', trigger: ['input', 'blur'] },
  ],
  setId: [
    { required: true, message: 'Enter Set ID', trigger: ['input', 'blur'] },
  ],
  problemId: [
    { required: true, message: 'Enter Problem ID', trigger: ['input', 'blur'] },
  ],
  language: [
    { required: true, message: 'Enter Language', trigger: ['input', 'blur'] },
  ],
  code: [
    { required: true, message: 'Enter Source Code', trigger: ['input', 'blur'] },
  ],
  codeLength: [
    { required: true, message: 'Enter Source Length', type: 'number', trigger: ['input', 'blur'] },
  ],
  maxTime: [
    { required: true, message: 'Enter Maximum Time', type: 'number', trigger: ['input', 'blur'] },
  ],
  maxMemory: [
    { required: true, message: 'Enter Maximum Memory', type: 'number', trigger: ['input', 'blur'] },
  ],
  message: [
    { required: true, message: 'Enter Run Result Message', trigger: ['input', 'blur'] },
  ],
  testCase: [
    { required: true, message: 'Enter Test Case Results', trigger: ['input', 'blur'] },
  ],
  status: [
    { required: true, message: 'Enter RunStatus', trigger: ['input', 'blur'] },
  ],
  similarity: [
    { required: true, message: 'Enter Similarity', type: 'number', trigger: ['input', 'blur'] },
  ],
  taskId: [
    { required: true, message: 'Enter Similarity Task ID', trigger: ['input', 'blur'] },
  ],
  reportId: [
    { required: true, message: 'Enter Report ID', trigger: ['input', 'blur'] },
  ],
  similarityCategory: [
    { required: true, message: 'Enter Similarity Level', trigger: ['input', 'blur'] },
  ],
  judgeTaskId: [
    { required: true, message: 'Enter Similarity Task ID', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataSubmitDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await dataSubmitEdit(formData.value)
        if (success) {
          window.$message.success('Updated successfully')
        }
      }
      else {
        const { success } = await dataSubmitAdd(formData.value)
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

function doOpen(row: any = null, edit: boolean = false) {
  show.value = true
  isEdit.value = edit
  formData.value = Object.assign(formData.value, row)
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
        <NFormItem label="User ID" path="userId">
          <NInput v-model:value="formData.userId" placeholder="Enter User ID" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Set ID" path="setId">
          <NInput v-model:value="formData.setId" placeholder="Enter Set ID" />
        </NFormItem>
        <!-- Boolean selector -->
        <NFormItem label="Set Submission" path="isSet">
          <NRadioGroup v-model:value="formData.isSet">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Problem ID" path="problemId">
          <NInput v-model:value="formData.problemId" placeholder="Enter Problem ID" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Language" path="language">
          <NInput v-model:value="formData.language" placeholder="Enter Language" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Source Code" path="code">
          <NInput v-model:value="formData.code" placeholder="Enter Source Code" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Source Length" path="codeLength">
          <NInputNumber v-model:value="formData.codeLength" :min="0" :max="100" placeholder="Enter Source Length" />
        </NFormItem>
        <!-- Boolean selector -->
        <NFormItem label="Run Type" path="submitType">
          <NRadioGroup v-model:value="formData.submitType">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Maximum Time" path="maxTime">
          <NInputNumber v-model:value="formData.maxTime" :min="0" :max="100" placeholder="Enter Maximum Time" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Maximum Memory" path="maxMemory">
          <NInputNumber v-model:value="formData.maxMemory" :min="0" :max="100" placeholder="Enter Maximum Memory" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Run Result Message" path="message">
          <NInput v-model:value="formData.message" placeholder="Enter Run Result Message" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Test Case Results" path="testCase">
          <NInput v-model:value="formData.testCase" placeholder="Enter Test Case Results" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="RunStatus" path="status">
          <NInput v-model:value="formData.status" placeholder="Enter RunStatus" />
        </NFormItem>
        <!-- Boolean selector -->
        <NFormItem label="Workflow Complete" path="isFinish">
          <NRadioGroup v-model:value="formData.isFinish">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Similarity" path="similarity">
          <NInputNumber v-model:value="formData.similarity" :min="0" :max="100" placeholder="Enter Similarity" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Similarity Task ID" path="taskId">
          <NInput v-model:value="formData.taskId" placeholder="Enter Similarity Task ID" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Report ID" path="reportId">
          <NInput v-model:value="formData.reportId" placeholder="Enter Report ID" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Similarity Level" path="similarityCategory">
          <NInput v-model:value="formData.similarityCategory" placeholder="Enter Similarity Level" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Similarity Task ID" path="judgeTaskId">
          <NInput v-model:value="formData.judgeTaskId" placeholder="Enter Similarity Task ID" />
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
