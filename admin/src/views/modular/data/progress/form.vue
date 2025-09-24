<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useDataProgressFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { dataProgressDefaultData, dataProgressAdd, dataProgressEdit } = useDataProgressFetch()
const formData = ref<any>({ ...dataProgressDefaultData })
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
  status: [
    { required: true, message: '请输入状态', trigger: ['input', 'blur'] },
  ],
  extraJson: [
    { required: true, message: '请输入额外信息', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataProgressDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await dataProgressEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await dataProgressAdd(formData.value)
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
        <NFormItem label="状态" path="status">
          <NInput v-model:value="formData.status" placeholder="请输入状态" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="额外信息" path="extraJson">
          <NInput v-model:value="formData.extraJson" placeholder="请输入额外信息" />
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
          <NDatePicker v-model:value="formData.completedAt" type="datetime" />
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
