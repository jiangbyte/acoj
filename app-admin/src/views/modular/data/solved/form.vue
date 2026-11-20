<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useDataSolvedFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { dataSolvedDefaultData, dataSolvedAdd, dataSolvedEdit } = useDataSolvedFetch()
const formData = ref<any>({ ...dataSolvedDefaultData })
const rules = {
  setId: [
    { required: true, message: 'Enter Set ID', trigger: ['input', 'blur'] },
  ],
  userId: [
    { required: true, message: 'Enter User ID', trigger: ['input', 'blur'] },
  ],
  problemId: [
    { required: true, message: 'Enter Problem ID', trigger: ['input', 'blur'] },
  ],
  submitId: [
    { required: true, message: 'Enter Submission ID', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataSolvedDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await dataSolvedEdit(formData.value)
        if (success) {
          window.$message.success('Updated successfully')
        }
      }
      else {
        const { success } = await dataSolvedAdd(formData.value)
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
        <NFormItem label="User ID" path="userId">
          <NInput v-model:value="formData.userId" placeholder="Enter User ID" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Problem ID" path="problemId">
          <NInput v-model:value="formData.problemId" placeholder="Enter Problem ID" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Submission ID" path="submitId">
          <NInput v-model:value="formData.submitId" placeholder="Enter Submission ID" />
        </NFormItem>
        <!-- Boolean selector -->
        <NFormItem label="Solved" path="solved">
          <NRadioGroup v-model:value="formData.solved">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
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
