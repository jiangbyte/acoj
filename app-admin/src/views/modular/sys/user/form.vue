<script lang="ts" setup>
import type { SelectOption } from 'naive-ui'
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useSysDictFetch, useSysGroupFetch, useSysUserFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysUserDefaultData, sysUserAdd, sysUserEdit } = useSysUserFetch()
const groupOptionsLoading = ref(false)
const groupOptions = ref<SelectOption[]>([])
const formData = ref<any>({ ...sysUserDefaultData })
const rules = {
  groupId: [
    { required: true, message: 'Enter User Group', trigger: ['input', 'blur'] },
  ],
  username: [
    { required: true, message: 'Enter username', trigger: ['input', 'blur'] },
  ],
  password: [
    { required: true, message: 'Enter password', trigger: ['input', 'blur'] },
  ],
  nickname: [
    { required: true, message: 'Enter Nickname', trigger: ['input', 'blur'] },
  ],
  avatar: [
    // { required: true, message: 'Enter Avatar', trigger: ['input', 'blur'] },
  ],
  background: [
    // { required: true, message: 'Enter Background Image', trigger: ['input', 'blur'] },
  ],
  quote: [
    // { required: true, message: 'Enter Bio', trigger: ['input', 'blur'] },
  ],
  email: [
    { required: true, message: 'Enter Email', trigger: ['input', 'blur'] },
  ],
  telephone: [
    // { required: true, message: 'Enter Phone', trigger: ['input', 'blur'] },
  ],
  // studentNumber: [
  //   { required: true, message: 'Enter Student ID', trigger: ['input', 'blur'] },
  // ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysUserDefaultData }
}

const isEdit = ref(false)

async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await sysUserEdit(formData.value)
        if (success) {
          window.$message.success('Updated successfully')
        }
      }
      else {
        const { success } = await sysUserAdd(formData.value)
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

const genderRef = ref()
function doOpen(row: any = null, edit: boolean = false) {
  show.value = true
  isEdit.value = edit
  formData.value = Object.assign(formData.value, row)

  useSysGroupFetch().sysGroupAuthTree({ keyword: '' }).then(({ data }) => {
    groupOptions.value = data
    groupOptionsLoading.value = false
  })

  useSysDictFetch().sysDictOptions({ dictType: 'SYS_GENDER' }).then(({ data }) => {
    // genderRef.value = data
    // Convert value to a number
    genderRef.value = data.map((item: { label: string, value: number }) => {
      item.value = Number(item.value)
      return item
    })
  })
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
        <NFormItem label="Student ID" path="studentNumber">
          <NInput v-model:value="formData.studentNumber" placeholder="Enter Student ID" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="User Group" path="groupId">
          <n-tree-select
            v-model:value="formData.groupId"
            :options="groupOptions"
            label-field="name"
            key-field="id"
            :indent="12"
          />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="username" path="username">
          <NInput v-model:value="formData.username" placeholder="Enter username" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Nickname" path="nickname">
          <NInput v-model:value="formData.nickname" placeholder="Enter Nickname" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Avatar" path="avatar">
          <FileUpload v-model="formData.avatar" :is-image="true" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Background Image" path="background">
          <FileUpload v-model="formData.background" :is-image="true" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Bio" path="quote">
          <NInput v-model:value="formData.quote" type="textarea" placeholder="Enter Bio" />
        </NFormItem>
        <!-- Boolean selector -->
        <NFormItem label="Gender" path="gender">
          <!-- <NSelect
            v-model:value="formData.gender"
            placeholder="Select Gender"
            :options="[
              { label: 'Male', value: 1 },
              { label: 'Female', value: 2 },
              { label: 'Unknown', value: 0 },
            ]"
          /> -->

          <NSelect
            v-model:value="formData.gender"
            placeholder="Select Gender"
            :options="genderRef"
          />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Email" path="email">
          <NInput v-model:value="formData.email" placeholder="Enter Email" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Phone" path="telephone">
          <NInput v-model:value="formData.telephone" placeholder="Enter Phone" />
        </NFormItem>
        <n-alert v-if="!isEdit" type="warning">
          New users receive the default password <n-tag type="primary">
            123456789
          </n-tag>，and the default role <n-tag type="primary">
            Standard User
          </n-tag>
        </n-alert>
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
