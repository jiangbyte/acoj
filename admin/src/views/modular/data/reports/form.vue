<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useTaskReportsFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { taskReportsDefaultData, taskReportsAdd, taskReportsEdit } = useTaskReportsFetch()
const formData = ref<any>({ ...taskReportsDefaultData })
const rules = {
  reportType: [
    { required: true, message: '请输入报告类型', type: 'number', trigger: ['input', 'blur'] },
  ],
  taskId: [
    { required: true, message: '请输入任务ID', trigger: ['input', 'blur'] },
  ],
  setId: [
    { required: true, message: '请输入题集ID', trigger: ['input', 'blur'] },
  ],
  problemId: [
    { required: true, message: '请输入题目ID', trigger: ['input', 'blur'] },
  ],
  sampleCount: [
    { required: true, message: '请输入样例数量', type: 'number', trigger: ['input', 'blur'] },
  ],
  similarityGroupCount: [
    { required: true, message: '请输入相似组数量', type: 'number', trigger: ['input', 'blur'] },
  ],
  avgSimilarity: [
    { required: true, message: '请输入平均相似度', type: 'number', trigger: ['input', 'blur'] },
  ],
  maxSimilarity: [
    { required: true, message: '请输入最大相似度', type: 'number', trigger: ['input', 'blur'] },
  ],
  threshold: [
    { required: true, message: '请输入检测阈值', type: 'number', trigger: ['input', 'blur'] },
  ],
  similarityDistribution: [
    { required: true, message: '请输入相似度分布', trigger: ['input', 'blur'] },
  ],
  degreeStatistics: [
    { required: true, message: '请输入程度统计', trigger: ['input', 'blur'] },
  ],
  checkMode: [
    { required: true, message: '请输入检测模式', type: 'number', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...taskReportsDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await taskReportsEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await taskReportsAdd(formData.value)
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
        <!-- 数字输入 -->
        <NFormItem label="报告类型" path="reportType">
          <NInputNumber v-model:value="formData.reportType" :min="0" :max="100" placeholder="请输入报告类型" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="任务ID" path="taskId">
          <NInput v-model:value="formData.taskId" placeholder="请输入任务ID" />
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
        <NFormItem label="题目ID" path="problemId">
          <NInput v-model:value="formData.problemId" placeholder="请输入题目ID" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="样例数量" path="sampleCount">
          <NInputNumber v-model:value="formData.sampleCount" :min="0" :max="100" placeholder="请输入样例数量" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="相似组数量" path="similarityGroupCount">
          <NInputNumber v-model:value="formData.similarityGroupCount" :min="0" :max="100" placeholder="请输入相似组数量" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="平均相似度" path="avgSimilarity">
          <NInputNumber v-model:value="formData.avgSimilarity" :min="0" :max="100" placeholder="请输入平均相似度" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="最大相似度" path="maxSimilarity">
          <NInputNumber v-model:value="formData.maxSimilarity" :min="0" :max="100" placeholder="请输入最大相似度" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="检测阈值" path="threshold">
          <NInputNumber v-model:value="formData.threshold" :min="0" :max="100" placeholder="请输入检测阈值" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="相似度分布" path="similarityDistribution">
          <NInput v-model:value="formData.similarityDistribution" placeholder="请输入相似度分布" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="程度统计" path="degreeStatistics">
          <NInput v-model:value="formData.degreeStatistics" placeholder="请输入程度统计" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="检测模式" path="checkMode">
          <NInputNumber v-model:value="formData.checkMode" :min="0" :max="100" placeholder="请输入检测模式" />
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
