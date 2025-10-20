<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useSysLogFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysLogDefaultData, sysLogAdd, sysLogEdit } = useSysLogFetch()
const formData = ref<any>({ ...sysLogDefaultData })
const rules = {
  userId: [
    { required: true, message: '请输入用户ID', trigger: ['input', 'blur'] },
  ],
  operation: [
    { required: true, message: '请输入操作', trigger: ['input', 'blur'] },
  ],
  method: [
    { required: true, message: '请输入方法', trigger: ['input', 'blur'] },
  ],
  params: [
    { required: true, message: '请输入参数', trigger: ['input', 'blur'] },
  ],
  ip: [
    { required: true, message: '请输入IP', trigger: ['input', 'blur'] },
  ],
  category: [
    { required: true, message: '请输入操作分类', trigger: ['input', 'blur'] },
  ],
  module: [
    { required: true, message: '请输入模块名称', trigger: ['input', 'blur'] },
  ],
  description: [
    { required: true, message: '请输入操作描述', trigger: ['input', 'blur'] },
  ],
  status: [
    { required: true, message: '请输入操作状态', trigger: ['input', 'blur'] },
  ],
  message: [
    { required: true, message: '请输入操作消息', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysLogDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await sysLogEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await sysLogAdd(formData.value)
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
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent :title="isEdit ? '编辑' : '新增'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- 输入框 -->
        <NFormItem v-if="isEdit" label="主键" path="id">
          <NInput v-model:value="formData.id" placeholder="请输入主键" :disabled="true" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="用户ID" path="userId">
          <NInput v-model:value="formData.userId" placeholder="请输入用户ID" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="操作" path="operation">
          <NInput v-model:value="formData.operation" placeholder="请输入操作" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="方法" path="method">
          <NInput v-model:value="formData.method" placeholder="请输入方法" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="参数" path="params">
          <NInput v-model:value="formData.params" placeholder="请输入参数" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="IP" path="ip">
          <NInput v-model:value="formData.ip" placeholder="请输入IP" />
        </NFormItem>
        <!-- 日期选择 -->
        <NFormItem label="操作时间" path="operationTime">
          <!-- <NDatePicker type="datetime" v-model:value="formData.operationTime" /> -->
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="操作分类" path="category">
          <NInput v-model:value="formData.category" placeholder="请输入操作分类" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="模块名称" path="module">
          <NInput v-model:value="formData.module" placeholder="请输入模块名称" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="操作描述" path="description">
          <NInput v-model:value="formData.description" placeholder="请输入操作描述" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="操作状态" path="status">
          <NInput v-model:value="formData.status" placeholder="请输入操作状态" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="操作消息" path="message">
          <NInput v-model:value="formData.message" placeholder="请输入操作消息" />
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
