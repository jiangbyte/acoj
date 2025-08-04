<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NModal, NUpload, type UploadFileInfo } from 'naive-ui'
import { useProSetFetch } from '@/composables'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { proSetDefaultData, proSetAdd, proSetEdit } = useProSetFetch()
const formData = ref<any>({ ...proSetDefaultData })
const rules = {
  setType: [
    { required: true, message: '请输入题集类型', type: 'number', trigger: ['input', 'blur'] },
  ],
  title: [
    { required: true, message: '请输入标题', trigger: ['input', 'blur'] },
  ],
  cover: [
    { required: true, message: '请输入封面', trigger: ['input', 'blur'] },
  ],
  description: [
    { required: true, message: '请输入描述', trigger: ['input', 'blur'] },
  ],
  categoryId: [
    { required: true, message: '请输入分类', trigger: ['input', 'blur'] },
  ],
  difficulty: [
    { required: true, message: '请输入难度', type: 'number', trigger: ['input', 'blur'] },
  ],
  config: [
    { required: true, message: '请输入配置信息', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...proSetDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await proSetEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await proSetAdd(formData.value)
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
              <!-- 数字输入 -->
              <NFormItem label="题集类型" path="setType">
                <NInputNumber v-model:value="formData.setType" :min="0" :max="100" placeholder="请输入题集类型" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="标题" path="title">
                <NInput v-model:value="formData.title" placeholder="请输入标题" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="封面" path="cover">
                <NInput v-model:value="formData.cover" placeholder="请输入封面" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="描述" path="description">
                <NInput v-model:value="formData.description" placeholder="请输入描述" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="分类" path="categoryId">
                <NInput v-model:value="formData.categoryId" placeholder="请输入分类" />
              </NFormItem>
              <!-- 数字输入 -->
              <NFormItem label="难度" path="difficulty">
                <NInputNumber v-model:value="formData.difficulty" :min="0" :max="100" placeholder="请输入难度" />
              </NFormItem>
              <!-- 日期选择 -->
              <NFormItem label="开始时间" path="startTime">
                <NDatePicker type="datetime" v-model:value="formData.startTime" />
              </NFormItem>
              <!-- 日期选择 -->
              <NFormItem label="结束时间" path="endTime">
                <NDatePicker type="datetime" v-model:value="formData.endTime" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="配置信息" path="config">
                <NInput v-model:value="formData.config" placeholder="请输入配置信息" />
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
