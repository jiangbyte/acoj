<script lang="ts" setup>
import type { SelectOption } from 'naive-ui'
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useSysDictFetch, useSysGroupFetch, useSysRoleFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysRoleDefaultData, sysRoleAdd, sysRoleEdit } = useSysRoleFetch()
const formData = ref<any>({ ...sysRoleDefaultData })
const rules = {
  name: [
    { required: true, message: 'Enter Name', trigger: ['input', 'blur'] },
  ],
  code: [
    { required: true, message: 'Enter Code', trigger: ['input', 'blur'] },
  ],
  dataScope: [
    { required: true, message: 'Select a data scope', trigger: ['input', 'blur'] },
  ],
  description: [
    { required: true, message: 'Enter Description', trigger: ['input', 'blur'] },
  ],
  level: [
    { required: true, message: 'Enter Role Level', type: 'number', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysRoleDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await sysRoleEdit(formData.value)
        if (success) {
          window.$message.success('Updated successfully')
        }
      }
      else {
        const { success } = await sysRoleAdd(formData.value)
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

const dataScopeRef = ref()
const groupOptionsLoading = ref(false)
const groupOptions = ref<SelectOption[]>([])
function doOpen(row: any = null, edit: boolean = false) {
  show.value = true
  isEdit.value = edit
  formData.value = Object.assign(formData.value, row)

  useSysDictFetch().sysDictOptions({ dictType: 'DATA_SCOPE' }).then(({ data }) => {
    dataScopeRef.value = data
  })
  useSysGroupFetch().sysGroupAuthTree({ keyword: '' }).then(({ data }) => {
    groupOptions.value = data
    groupOptionsLoading.value = false
  })
}
defineExpose({
  doOpen,
})

watch(
  () => formData.value.dataScope,
  (newValue) => {
    if (newValue !== 'SPECIFIED_GROUP') {
      formData.value.assignGroupIds = []
    }
  },
  { immediate: true },
)
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
        <NFormItem label="Name" path="name">
          <NInput v-model:value="formData.name" placeholder="Enter Name" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Code" path="code">
          <NInput v-model:value="formData.code" placeholder="Enter Code" :disabled="isEdit" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Description" path="description">
          <NInput v-model:value="formData.description" placeholder="Enter Description" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Data Scope" path="dataScope">
          <!-- <NInput v-model:value="formData.dataScope" placeholder="Enter Description" /> -->
          <NSelect
            v-model:value="formData.dataScope"
            placeholder="Select a data scope"
            :options="dataScopeRef"
          />
        </NFormItem>
        <NFormItem v-if="formData.dataScope === 'SPECIFIED_GROUP'" label="User Group" path="assignGroupIds">
          <n-tree-select
            v-model:value="formData.assignGroupIds"
            :options="groupOptions"
            label-field="name"
            key-field="id"
            :indent="12"
            multiple
          />
        </NFormItem>
        <!-- Number input -->
        <!-- <NFormItem label="Role Level" path="level">
          <NInputNumber v-model:value="formData.level" :min="0" :max="100" placeholder="Enter Role Level" />
        </NFormItem> -->
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
