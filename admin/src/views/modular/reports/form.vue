<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useDataReportsFetch } from '@/composables'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { dataReportsDefaultData, dataReportsAdd, dataReportsEdit } = useDataReportsFetch()
const formData = ref<any>({ ...dataReportsDefaultData })
const rules = {
  reportType: [
    { required: true, message: '请输入报告类型', type: 'number', trigger: ['input', 'blur'] },
  ],
  taskId: [
    { required: true, message: '请输入任务ID', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataReportsDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await dataReportsEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await dataReportsAdd(formData.value)
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
        <NFormItem label="报告类型" path="reportType">
          <NInputNumber v-model:value="formData.reportType" :min="0" :max="100" placeholder="请输入报告类型" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="任务ID" path="taskId">
          <NInput v-model:value="formData.taskId" placeholder="请输入任务ID" />
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="是否是题单报告" path="isSet">
          <NRadioGroup v-model:value="formData.isSet">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
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
