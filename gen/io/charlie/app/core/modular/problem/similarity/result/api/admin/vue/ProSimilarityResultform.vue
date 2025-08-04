<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NModal, NUpload, type UploadFileInfo } from 'naive-ui'
import { useProSimilarityResultFetch } from '@/composables'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { proSimilarityResultDefaultData, proSimilarityResultAdd, proSimilarityResultEdit } = useProSimilarityResultFetch()
const formData = ref<any>({ ...proSimilarityResultDefaultData })
const rules = {
  taskId: [
    { required: true, message: '请输入关联的任务ID', trigger: ['input', 'blur'] },
  ],
  originSubmitId: [
    { required: true, message: '请输入提交ID', trigger: ['input', 'blur'] },
  ],
  comparedSubmitId: [
    { required: true, message: '请输入被比较的提交ID', trigger: ['input', 'blur'] },
  ],
  similarity: [
    { required: true, message: '请输入相似度值', type: 'number', trigger: ['input', 'blur'] },
  ],
  details: [
    { required: true, message: '请输入详细比对结果', trigger: ['input', 'blur'] },
  ],
  matchDetails: [
    { required: true, message: '请输入匹配部分详情', trigger: ['input', 'blur'] },
  ],
  threshold: [
    { required: true, message: '请输入相似度阈值', type: 'number', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...proSimilarityResultDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await proSimilarityResultEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await proSimilarityResultAdd(formData.value)
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
              <NFormItem label="关联的任务ID" path="taskId">
                <NInput v-model:value="formData.taskId" placeholder="请输入关联的任务ID" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="提交ID" path="originSubmitId">
                <NInput v-model:value="formData.originSubmitId" placeholder="请输入提交ID" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="被比较的提交ID" path="comparedSubmitId">
                <NInput v-model:value="formData.comparedSubmitId" placeholder="请输入被比较的提交ID" />
              </NFormItem>
              <!-- 数字输入 -->
              <NFormItem label="相似度值" path="similarity">
                <NInputNumber v-model:value="formData.similarity" :min="0" :max="100" placeholder="请输入相似度值" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="详细比对结果" path="details">
                <NInput v-model:value="formData.details" placeholder="请输入详细比对结果" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="匹配部分详情" path="matchDetails">
                <NInput v-model:value="formData.matchDetails" placeholder="请输入匹配部分详情" />
              </NFormItem>
              <!-- 数字输入 -->
              <NFormItem label="相似度阈值" path="threshold">
                <NInputNumber v-model:value="formData.threshold" :min="0" :max="100" placeholder="请输入相似度阈值" />
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
