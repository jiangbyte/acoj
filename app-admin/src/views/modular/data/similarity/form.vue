<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useTaskSimilarityFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { taskSimilarityDefaultData, taskSimilarityAdd, taskSimilarityEdit } = useTaskSimilarityFetch()
const formData = ref<any>({ ...taskSimilarityDefaultData })
const rules = {
  taskId: [
    { required: true, message: 'Enter Task ID', trigger: ['input', 'blur'] },
  ],
  problemId: [
    { required: true, message: 'Enter Problem ID', trigger: ['input', 'blur'] },
  ],
  setId: [
    { required: true, message: 'Enter Set ID', trigger: ['input', 'blur'] },
  ],
  language: [
    { required: true, message: 'Enter Language', trigger: ['input', 'blur'] },
  ],
  similarity: [
    { required: true, message: 'Enter Similarity', type: 'number', trigger: ['input', 'blur'] },
  ],
  submitUser: [
    { required: true, message: 'Enter SubmissionUser', trigger: ['input', 'blur'] },
  ],
  submitCode: [
    { required: true, message: 'Enter Source Code', trigger: ['input', 'blur'] },
  ],
  submitCodeLength: [
    { required: true, message: 'Enter Source Length', type: 'number', trigger: ['input', 'blur'] },
  ],
  submitId: [
    { required: true, message: 'Enter Submission ID', trigger: ['input', 'blur'] },
  ],
  submitTokenName: [
    { required: true, message: 'Enter Submission User Token Name', trigger: ['input', 'blur'] },
  ],
  submitTokenTexts: [
    { required: true, message: 'Enter Submission User Token Content', trigger: ['input', 'blur'] },
  ],
  originUser: [
    { required: true, message: 'Enter Sample User', trigger: ['input', 'blur'] },
  ],
  originCode: [
    { required: true, message: 'Enter Sample Source Code', trigger: ['input', 'blur'] },
  ],
  originCodeLength: [
    { required: true, message: 'Enter Sample Source Length', type: 'number', trigger: ['input', 'blur'] },
  ],
  originId: [
    { required: true, message: 'Enter Sample Submission ID', trigger: ['input', 'blur'] },
  ],
  originTokenName: [
    { required: true, message: 'Enter Sample User Token Name', trigger: ['input', 'blur'] },
  ],
  originTokenTexts: [
    { required: true, message: 'Enter Sample User Token Content', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...taskSimilarityDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await taskSimilarityEdit(formData.value)
        if (success) {
          window.$message.success('Updated successfully')
        }
      }
      else {
        const { success } = await taskSimilarityAdd(formData.value)
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
        <NFormItem label="Task ID" path="taskId">
          <NInput v-model:value="formData.taskId" placeholder="Enter Task ID" />
        </NFormItem>
        <!-- Boolean selector -->
        <NFormItem label="Manual" path="taskType">
          <NRadioGroup v-model:value="formData.taskType">
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
        <NFormItem label="Language" path="language">
          <NInput v-model:value="formData.language" placeholder="Enter Language" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Similarity" path="similarity">
          <NInputNumber v-model:value="formData.similarity" :min="0" :max="100" placeholder="Enter Similarity" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="SubmissionUser" path="submitUser">
          <NInput v-model:value="formData.submitUser" placeholder="Enter SubmissionUser" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Source Code" path="submitCode">
          <NInput v-model:value="formData.submitCode" placeholder="Enter Source Code" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Source Length" path="submitCodeLength">
          <NInputNumber v-model:value="formData.submitCodeLength" :min="0" :max="100" placeholder="Enter Source Length" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Submission ID" path="submitId">
          <NInput v-model:value="formData.submitId" placeholder="Enter Submission ID" />
        </NFormItem>
        <!-- Date selector -->
        <NFormItem label="Submitted At" path="submitTime">
          <NDatePicker v-model:value="formData.submitTime" type="datetime" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Submission User Token Name" path="submitTokenName">
          <NInput v-model:value="formData.submitTokenName" placeholder="Enter Submission User Token Name" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Submission User Token Content" path="submitTokenTexts">
          <NInput v-model:value="formData.submitTokenTexts" placeholder="Enter Submission User Token Content" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Sample User" path="originUser">
          <NInput v-model:value="formData.originUser" placeholder="Enter Sample User" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Sample Source Code" path="originCode">
          <NInput v-model:value="formData.originCode" placeholder="Enter Sample Source Code" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Sample Source Length" path="originCodeLength">
          <NInputNumber v-model:value="formData.originCodeLength" :min="0" :max="100" placeholder="Enter Sample Source Length" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Sample Submission ID" path="originId">
          <NInput v-model:value="formData.originId" placeholder="Enter Sample Submission ID" />
        </NFormItem>
        <!-- Date selector -->
        <NFormItem label="Sample Submitted At" path="originTime">
          <NDatePicker v-model:value="formData.originTime" type="datetime" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Sample User Token Name" path="originTokenName">
          <NInput v-model:value="formData.originTokenName" placeholder="Enter Sample User Token Name" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Sample User Token Content" path="originTokenTexts">
          <NInput v-model:value="formData.originTokenTexts" placeholder="Enter Sample User Token Content" />
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
