<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useTaskReportsFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { taskReportsDefaultData, taskReportsAdd, taskReportsEdit } = useTaskReportsFetch()
const formData = ref<any>({ ...taskReportsDefaultData })
const rules = {
  reportType: [
    { required: true, message: 'Enter Report Type', type: 'number', trigger: ['input', 'blur'] },
  ],
  taskId: [
    { required: true, message: 'Enter Task ID', trigger: ['input', 'blur'] },
  ],
  setId: [
    { required: true, message: 'Enter Set ID', trigger: ['input', 'blur'] },
  ],
  problemId: [
    { required: true, message: 'Enter Problem ID', trigger: ['input', 'blur'] },
  ],
  sampleCount: [
    { required: true, message: 'Enter Sample Count', type: 'number', trigger: ['input', 'blur'] },
  ],
  similarityGroupCount: [
    { required: true, message: 'Enter Similar Group Count', type: 'number', trigger: ['input', 'blur'] },
  ],
  avgSimilarity: [
    { required: true, message: 'Enter Average Similarity', type: 'number', trigger: ['input', 'blur'] },
  ],
  maxSimilarity: [
    { required: true, message: 'Enter Maximum Similarity', type: 'number', trigger: ['input', 'blur'] },
  ],
  threshold: [
    { required: true, message: 'Enter Check Threshold', type: 'number', trigger: ['input', 'blur'] },
  ],
  similarityDistribution: [
    { required: true, message: 'Enter Similarity Distribution', trigger: ['input', 'blur'] },
  ],
  degreeStatistics: [
    { required: true, message: 'Enter Level Statistics', trigger: ['input', 'blur'] },
  ],
  checkMode: [
    { required: true, message: 'Enter Check Mode', type: 'number', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...taskReportsDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await taskReportsEdit(formData.value)
        if (success) {
          window.$message.success('Updated successfully')
        }
      }
      else {
        const { success } = await taskReportsAdd(formData.value)
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
        <!-- Number input -->
        <NFormItem label="Report Type" path="reportType">
          <NInputNumber v-model:value="formData.reportType" :min="0" :max="100" placeholder="Enter Report Type" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Task ID" path="taskId">
          <NInput v-model:value="formData.taskId" placeholder="Enter Task ID" />
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
        <!-- Number input -->
        <NFormItem label="Sample Count" path="sampleCount">
          <NInputNumber v-model:value="formData.sampleCount" :min="0" :max="100" placeholder="Enter Sample Count" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Similar Group Count" path="similarityGroupCount">
          <NInputNumber v-model:value="formData.similarityGroupCount" :min="0" :max="100" placeholder="Enter Similar Group Count" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Average Similarity" path="avgSimilarity">
          <NInputNumber v-model:value="formData.avgSimilarity" :min="0" :max="100" placeholder="Enter Average Similarity" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Maximum Similarity" path="maxSimilarity">
          <NInputNumber v-model:value="formData.maxSimilarity" :min="0" :max="100" placeholder="Enter Maximum Similarity" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Check Threshold" path="threshold">
          <NInputNumber v-model:value="formData.threshold" :min="0" :max="100" placeholder="Enter Check Threshold" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Similarity Distribution" path="similarityDistribution">
          <NInput v-model:value="formData.similarityDistribution" placeholder="Enter Similarity Distribution" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Level Statistics" path="degreeStatistics">
          <NInput v-model:value="formData.degreeStatistics" placeholder="Enter Level Statistics" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Check Mode" path="checkMode">
          <NInputNumber v-model:value="formData.checkMode" :min="0" :max="100" placeholder="Enter Check Mode" />
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
