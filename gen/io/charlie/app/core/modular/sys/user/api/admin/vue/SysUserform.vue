<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NModal, NUpload, type UploadFileInfo } from 'naive-ui'
import { useSysUserFetch } from '@/composables'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysUserDefaultData, sysUserAdd, sysUserEdit } = useSysUserFetch()
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
  <NDrawer v-model:show="show" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent :title="isEdit ? '编辑' : '新增'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
            <!-- 输入框 -->
              <NFormItem v-if="isEdit" label="主键" path="id">
                <NInput v-model:value="formData.id" placeholder="请输入主键" :disabled="true" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="用户组" path="groupId">
                <NInput v-model:value="formData.groupId" placeholder="请输入用户组" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="用户名" path="username">
                <NInput v-model:value="formData.username" placeholder="请输入用户名" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="密码" path="password">
                <NInput v-model:value="formData.password" placeholder="请输入密码" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="昵称" path="nickname">
                <NInput v-model:value="formData.nickname" placeholder="请输入昵称" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="头像" path="avatar">
                <NInput v-model:value="formData.avatar" placeholder="请输入头像" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="背景图片" path="background">
                <NInput v-model:value="formData.background" placeholder="请输入背景图片" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="签名" path="quote">
                <NInput v-model:value="formData.quote" placeholder="请输入签名" />
              </NFormItem>
              <!-- Boolean 选择框 -->
              <NFormItem label="性别" path="gender">
                <NRadioGroup v-model:value="formData.gender">
                  <NRadio :value="true">
                    是
                  </NRadio>
                  <NRadio :value="false">
                    否
                  </NRadio>
                </NRadioGroup>
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="邮箱" path="email">
                <NInput v-model:value="formData.email" placeholder="请输入邮箱" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="电话" path="telephone">
                <NInput v-model:value="formData.telephone" placeholder="请输入电话" />
              </NFormItem>
              <!-- 日期选择 -->
              <NFormItem label="登录时间" path="loginTime">
                <NDatePicker type="datetime" v-model:value="formData.loginTime" />
              </NFormItem>
      </NForm>
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
