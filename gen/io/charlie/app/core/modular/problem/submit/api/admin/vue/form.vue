<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NModal, NUpload, type UploadFileInfo } from 'naive-ui'
import { useProSubmitFetch } from '@/composables'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { proSubmitDefaultData, proSubmitAdd, proSubmitEdit } = useProSubmitFetch()
const formData = ref<any>({ ...proSubmitDefaultData })
const rules = {
  userId: [
    { required: true, message: '请输入用户ID', trigger: ['input', 'blur'] },
  ],
  problemId: [
    { required: true, message: '请输入题目ID', trigger: ['input', 'blur'] },
  ],
  language: [
    { required: true, message: '请输入编程语言', trigger: ['input', 'blur'] },
  ],
  code: [
    { required: true, message: '请输入源代码', trigger: ['input', 'blur'] },
  ],
  maxTime: [
    { required: true, message: '请输入最大耗时', type: 'number', trigger: ['input', 'blur'] },
  ],
  maxMemory: [
    { required: true, message: '请输入最大内存使用', type: 'number', trigger: ['input', 'blur'] },
  ],
  message: [
    { required: true, message: '请输入执行结果消息', trigger: ['input', 'blur'] },
  ],
  testCases: [
    { required: true, message: '请输入测试用例结果', trigger: ['input', 'blur'] },
  ],
  status: [
    { required: true, message: '请输入执行状态', trigger: ['input', 'blur'] },
  ],
  similarity: [
    { required: true, message: '请输入相似度', type: 'number', trigger: ['input', 'blur'] },
  ],
  taskId: [
    { required: true, message: '请输入相似检测任务ID', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...proSubmitDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await proSubmitEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await proSubmitAdd(formData.value)
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
              <NFormItem label="题目ID" path="problemId">
                <NInput v-model:value="formData.problemId" placeholder="请输入题目ID" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="编程语言" path="language">
                <NInput v-model:value="formData.language" placeholder="请输入编程语言" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="源代码" path="code">
                <NInput v-model:value="formData.code" placeholder="请输入源代码" />
              </NFormItem>
              <!-- Boolean 选择框 -->
              <NFormItem label="执行类型" path="submitType">
                <NRadioGroup v-model:value="formData.submitType">
                  <NRadio :value="true">
                    是
                  </NRadio>
                  <NRadio :value="false">
                    否
                  </NRadio>
                </NRadioGroup>
              </NFormItem>
              <!-- 数字输入 -->
              <NFormItem label="最大耗时" path="maxTime">
                <NInputNumber v-model:value="formData.maxTime" :min="0" :max="100" placeholder="请输入最大耗时" />
              </NFormItem>
              <!-- 数字输入 -->
              <NFormItem label="最大内存使用" path="maxMemory">
                <NInputNumber v-model:value="formData.maxMemory" :min="0" :max="100" placeholder="请输入最大内存使用" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="执行结果消息" path="message">
                <NInput v-model:value="formData.message" placeholder="请输入执行结果消息" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="测试用例结果" path="testCases">
                <NInput v-model:value="formData.testCases" placeholder="请输入测试用例结果" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="执行状态" path="status">
                <NInput v-model:value="formData.status" placeholder="请输入执行状态" />
              </NFormItem>
              <!-- 数字输入 -->
              <NFormItem label="相似度" path="similarity">
                <NInputNumber v-model:value="formData.similarity" :min="0" :max="100" placeholder="请输入相似度" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="相似检测任务ID" path="taskId">
                <NInput v-model:value="formData.taskId" placeholder="请输入相似检测任务ID" />
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
