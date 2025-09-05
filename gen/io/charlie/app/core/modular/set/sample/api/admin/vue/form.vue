<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NModal, NUpload, type UploadFileInfo } from 'naive-ui'
import { useProSetSampleLibraryFetch } from '@/composables'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { proSetSampleLibraryDefaultData, proSetSampleLibraryAdd, proSetSampleLibraryEdit } = useProSetSampleLibraryFetch()
const formData = ref<any>({ ...proSetSampleLibraryDefaultData })
const rules = {
  userId: [
    { required: true, message: '请输入用户ID', trigger: ['input', 'blur'] },
  ],
  setId: [
    { required: true, message: '请输入题集ID', trigger: ['input', 'blur'] },
  ],
  problemId: [
    { required: true, message: '请输入题目ID', trigger: ['input', 'blur'] },
  ],
  submitId: [
    { required: true, message: '请输入提交ID', trigger: ['input', 'blur'] },
  ],
  language: [
    { required: true, message: '请输入编程语言', trigger: ['input', 'blur'] },
  ],
  code: [
    { required: true, message: '请输入源代码', trigger: ['input', 'blur'] },
  ],
  codeLength: [
    { required: true, message: '请输入源代码长度', type: 'number', trigger: ['input', 'blur'] },
  ],
  accessCount: [
    { required: true, message: '请输入访问次数', type: 'number', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...proSetSampleLibraryDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await proSetSampleLibraryEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await proSetSampleLibraryAdd(formData.value)
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
              <NFormItem label="用户ID" path="userId">
                <NInput v-model:value="formData.userId" placeholder="请输入用户ID" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="题集ID" path="setId">
                <NInput v-model:value="formData.setId" placeholder="请输入题集ID" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="题目ID" path="problemId">
                <NInput v-model:value="formData.problemId" placeholder="请输入题目ID" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="提交ID" path="submitId">
                <NInput v-model:value="formData.submitId" placeholder="请输入提交ID" />
              </NFormItem>
              <!-- 日期选择 -->
              <NFormItem label="提交时间" path="submitTime">
                <NDatePicker type="datetime" v-model:value="formData.submitTime" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="编程语言" path="language">
                <NInput v-model:value="formData.language" placeholder="请输入编程语言" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="源代码" path="code">
                <NInput v-model:value="formData.code" placeholder="请输入源代码" />
              </NFormItem>
              <!-- 数字输入 -->
              <NFormItem label="源代码长度" path="codeLength">
                <NInputNumber v-model:value="formData.codeLength" :min="0" :max="100" placeholder="请输入源代码长度" />
              </NFormItem>
              <!-- 数字输入 -->
              <NFormItem label="访问次数" path="accessCount">
                <NInputNumber v-model:value="formData.accessCount" :min="0" :max="100" placeholder="请输入访问次数" />
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
