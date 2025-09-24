<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useTaskSimilarityFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { taskSimilarityDefaultData, taskSimilarityAdd, taskSimilarityEdit } = useTaskSimilarityFetch()
const formData = ref<any>({ ...taskSimilarityDefaultData })
const rules = {
  taskId: [
    { required: true, message: '请输入任务ID', trigger: ['input', 'blur'] },
  ],
  problemId: [
    { required: true, message: '请输入题目ID', trigger: ['input', 'blur'] },
  ],
  setId: [
    { required: true, message: '请输入题集ID', trigger: ['input', 'blur'] },
  ],
  language: [
    { required: true, message: '请输入编程语言', trigger: ['input', 'blur'] },
  ],
  similarity: [
    { required: true, message: '请输入相似度', type: 'number', trigger: ['input', 'blur'] },
  ],
  submitUser: [
    { required: true, message: '请输入提交用户', trigger: ['input', 'blur'] },
  ],
  submitCode: [
    { required: true, message: '请输入源代码', trigger: ['input', 'blur'] },
  ],
  submitCodeLength: [
    { required: true, message: '请输入源代码长度', type: 'number', trigger: ['input', 'blur'] },
  ],
  submitId: [
    { required: true, message: '请输入提交ID', trigger: ['input', 'blur'] },
  ],
  submitTokenName: [
    { required: true, message: '请输入提交用户Token名称', trigger: ['input', 'blur'] },
  ],
  submitTokenTexts: [
    { required: true, message: '请输入提交用户Token内容', trigger: ['input', 'blur'] },
  ],
  originUser: [
    { required: true, message: '请输入样本用户', trigger: ['input', 'blur'] },
  ],
  originCode: [
    { required: true, message: '请输入样本源代码', trigger: ['input', 'blur'] },
  ],
  originCodeLength: [
    { required: true, message: '请输入样本源代码长度', type: 'number', trigger: ['input', 'blur'] },
  ],
  originId: [
    { required: true, message: '请输入样本提交ID', trigger: ['input', 'blur'] },
  ],
  originTokenName: [
    { required: true, message: '请输入样本用户Token名称', trigger: ['input', 'blur'] },
  ],
  originTokenTexts: [
    { required: true, message: '请输入样本用户Token内容', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...taskSimilarityDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await taskSimilarityEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await taskSimilarityAdd(formData.value)
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
        <NFormItem label="任务ID" path="taskId">
          <NInput v-model:value="formData.taskId" placeholder="请输入任务ID" />
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="手动" path="taskType">
          <NRadioGroup v-model:value="formData.taskType">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="题目ID" path="problemId">
          <NInput v-model:value="formData.problemId" placeholder="请输入题目ID" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="题集ID" path="setId">
          <NInput v-model:value="formData.setId" placeholder="请输入题集ID" />
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="是否是题集提交" path="isSet">
          <NRadioGroup v-model:value="formData.isSet">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="编程语言" path="language">
          <NInput v-model:value="formData.language" placeholder="请输入编程语言" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="相似度" path="similarity">
          <NInputNumber v-model:value="formData.similarity" :min="0" :max="100" placeholder="请输入相似度" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="提交用户" path="submitUser">
          <NInput v-model:value="formData.submitUser" placeholder="请输入提交用户" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="源代码" path="submitCode">
          <NInput v-model:value="formData.submitCode" placeholder="请输入源代码" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="源代码长度" path="submitCodeLength">
          <NInputNumber v-model:value="formData.submitCodeLength" :min="0" :max="100" placeholder="请输入源代码长度" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="提交ID" path="submitId">
          <NInput v-model:value="formData.submitId" placeholder="请输入提交ID" />
        </NFormItem>
        <!-- 日期选择 -->
        <NFormItem label="提交时间" path="submitTime">
          <NDatePicker v-model:value="formData.submitTime" type="datetime" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="提交用户Token名称" path="submitTokenName">
          <NInput v-model:value="formData.submitTokenName" placeholder="请输入提交用户Token名称" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="提交用户Token内容" path="submitTokenTexts">
          <NInput v-model:value="formData.submitTokenTexts" placeholder="请输入提交用户Token内容" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="样本用户" path="originUser">
          <NInput v-model:value="formData.originUser" placeholder="请输入样本用户" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="样本源代码" path="originCode">
          <NInput v-model:value="formData.originCode" placeholder="请输入样本源代码" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="样本源代码长度" path="originCodeLength">
          <NInputNumber v-model:value="formData.originCodeLength" :min="0" :max="100" placeholder="请输入样本源代码长度" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="样本提交ID" path="originId">
          <NInput v-model:value="formData.originId" placeholder="请输入样本提交ID" />
        </NFormItem>
        <!-- 日期选择 -->
        <NFormItem label="样本提交时间" path="originTime">
          <NDatePicker v-model:value="formData.originTime" type="datetime" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="样本用户Token名称" path="originTokenName">
          <NInput v-model:value="formData.originTokenName" placeholder="请输入样本用户Token名称" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="样本用户Token内容" path="originTokenTexts">
          <NInput v-model:value="formData.originTokenTexts" placeholder="请输入样本用户Token内容" />
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
