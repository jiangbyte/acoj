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
    { required: true, message: '请输入用户组', trigger: ['input', 'blur'] },
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: ['input', 'blur'] },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: ['input', 'blur'] },
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: ['input', 'blur'] },
  ],
  avatar: [
    { required: true, message: '请输入头像', trigger: ['input', 'blur'] },
  ],
  background: [
    { required: true, message: '请输入背景图片', trigger: ['input', 'blur'] },
  ],
  quote: [
    { required: true, message: '请输入签名', trigger: ['input', 'blur'] },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: ['input', 'blur'] },
  ],
  telephone: [
    { required: true, message: '请输入电话', trigger: ['input', 'blur'] },
  ],
  // studentNumber: [
  //   { required: true, message: '请输入学号', trigger: ['input', 'blur'] },
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
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await sysUserAdd(formData.value)
        if (success) {
          window.$message.success('新增成功')
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
    // 对value进行num转换
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
    <NDrawerContent :title="isEdit ? '编辑' : '新增'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- 输入框 -->
        <NFormItem v-if="isEdit" label="主键" path="id">
          <NInput v-model:value="formData.id" placeholder="请输入主键" :disabled="true" />
        </NFormItem>
        <NFormItem label="学号" path="studentNumber">
          <NInput v-model:value="formData.studentNumber" placeholder="请输入学号" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="用户组" path="groupId">
          <n-tree-select
            v-model:value="formData.groupId"
            :options="groupOptions"
            label-field="name"
            key-field="id"
            :indent="12"
          />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="用户名" path="username">
          <NInput v-model:value="formData.username" placeholder="请输入用户名" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="昵称" path="nickname">
          <NInput v-model:value="formData.nickname" placeholder="请输入昵称" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="头像" path="avatar">
          <FileUpload v-model="formData.avatar" :is-image="true" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="背景图片" path="background">
          <FileUpload v-model="formData.background" :is-image="true" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="签名" path="quote">
          <NInput v-model:value="formData.quote" type="textarea" placeholder="请输入签名" />
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="性别" path="gender">
          <!-- <NSelect
            v-model:value="formData.gender"
            placeholder="请选择性别"
            :options="[
              { label: '男', value: 1 },
              { label: '女', value: 2 },
              { label: '未知', value: 0 },
            ]"
          /> -->

          <NSelect
            v-model:value="formData.gender"
            placeholder="请选择性别"
            :options="genderRef"
          />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="邮箱" path="email">
          <NInput v-model:value="formData.email" placeholder="请输入邮箱" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="电话" path="telephone">
          <NInput v-model:value="formData.telephone" placeholder="请输入电话" />
        </NFormItem>
      </NForm>
      <n-alert type="warning" v-if="!isEdit">
        创建的用户将设置默认密码为：123456789
      </n-alert>
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            关闭
          </NButton>
          <NButton type="primary" :loading="loading" @click="doSubmit">
            <template #icon>
              <IconParkOutlineSave />
            </template>
            保存
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
