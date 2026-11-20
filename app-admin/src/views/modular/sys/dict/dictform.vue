<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useSysDictFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysDictDefaultData, sysDictAdd, sysDictEdit } = useSysDictFetch()
const formData = ref<any>({ ...sysDictDefaultData })
const rules = {
  dictType: [
    { required: true, message: 'Enter Dictionary Type', trigger: ['input', 'blur'] },
  ],
  typeLabel: [
    { required: true, message: 'Enter Type Name', trigger: ['input', 'blur'] },
  ],
  dictValue: [
    { required: true, message: 'Enter Dictionary Value', trigger: ['input', 'blur'] },
  ],
  dictLabel: [
    { required: true, message: 'Enter Dictionary Label', trigger: ['input', 'blur'] },
  ],
  sortOrder: [
    { required: true, message: 'Enter Sort Order', type: 'number', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysDictDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await sysDictEdit(formData.value)
        if (success) {
          window.$message.success('Updated successfully')
        }
      }
      else {
        const { success } = await sysDictAdd(formData.value)
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
        <NFormItem v-if="isEdit" label="IDID" path="id">
          <NInput v-model:value="formData.id" placeholder="Enter ID" :disabled="true" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Type Name" path="typeLabel">
          <NInput v-model:value="formData.typeLabel" placeholder="Enter Type Name" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Dictionary Type（Code）" path="dictType">
          <NInput v-model:value="formData.dictType" placeholder="Enter Dictionary Type" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Dictionary Label（Name）" path="dictLabel">
          <NInput v-model:value="formData.dictLabel" placeholder="Enter Dictionary Label" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Dictionary Value" path="dictValue">
          <NInput v-model:value="formData.dictValue" placeholder="Enter Dictionary Value" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Sort Order" path="sortOrder">
          <NInputNumber v-model:value="formData.sortOrder" :min="0" :max="100" placeholder="Enter Sort Order" />
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
