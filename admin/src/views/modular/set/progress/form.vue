<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useProSetProgressFetch } from '@/composables'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { proSetProgressDefaultData, proSetProgressAdd, proSetProgressEdit } = useProSetProgressFetch()
const formData = ref<any>({ ...proSetProgressDefaultData })
const rules = {
  userId: [
    { required: true, message: '请输入用户ID', trigger: ['input', 'blur'] },
  ],
  problemSetId: [
    { required: true, message: '请输入题单ID', trigger: ['input', 'blur'] },
  ],
  progress: [
    { required: true, message: '请输入进度信息', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...proSetProgressDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await proSetProgressEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await proSetProgressAdd(formData.value)
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
        <NFormItem label="题单ID" path="problemSetId">
          <NInput v-model:value="formData.problemSetId" placeholder="请输入题单ID" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="进度信息" path="progress">
          <NInput v-model:value="formData.progress" placeholder="请输入进度信息" />
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="是否完成" path="completed">
          <NRadioGroup v-model:value="formData.completed">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- 日期选择 -->
        <NFormItem label="完成时间" path="completedAt">
          <!-- <NDatePicker type="datetime" v-model:value="formData.completedAt" /> -->
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
