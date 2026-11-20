<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NModal, NUpload, type UploadFileInfo } from 'naive-ui'
import { use${entity}Fetch } from '@/composables'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { ${table.entityPath}DefaultData, ${table.entityPath}Add, ${table.entityPath}Edit } = use${entity}Fetch()
const formData = ref<any>({ ...${table.entityPath}DefaultData })
const rules = {
  <#list table.fields as field>
  <#if !["id", "deleted", "createTime", "createUser", "updateTime", "updateUser"]?seq_contains(field.propertyName)>
  <#if field.propertyType == 'String'>
  ${field.propertyName}: [
    { required: true, message: '请输入${field.comment}', trigger: ['input', 'blur'] },
  ],
  <#elseif ['Long','Integer','Short','Byte','Double','Float','BigDecimal']?seq_contains(field.propertyType)>
  ${field.propertyName}: [
    { required: true, message: '请输入${field.comment}', type: 'number', trigger: ['input', 'blur'] },
  ],
  </#if>
  </#if>
  </#list>
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...${table.entityPath}DefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await ${table.entityPath}Edit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await ${table.entityPath}Add(formData.value)
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
        <#list table.fields as field>
          <#if !["deleted", "createTime", "createUser", "updateTime", "updateUser"]?seq_contains(field.propertyName)>
            <#if field.keyFlag>
            <!-- 输入框 -->
              <NFormItem v-if="isEdit" label="${field.comment}" path="${field.propertyName}">
                <NInput v-model:value="formData.${field.propertyName}" placeholder="请输入${field.comment}" :disabled="true" />
              </NFormItem>
              <#continue>
            </#if>
            <#if field.propertyType == 'String'>
            <!-- 输入框 -->
              <NFormItem label="${field.comment}" path="${field.propertyName}">
                <NInput v-model:value="formData.${field.propertyName}" placeholder="请输入${field.comment}" />
              </NFormItem>
            <#elseif field.propertyType == 'Long' || field.propertyType == 'Integer' || field.propertyType == 'Short' || field.propertyType == 'Byte' || field.propertyType == 'Double' || field.propertyType == 'Float' || field.propertyType == 'BigDecimal'>
              <!-- 数字输入 -->
              <NFormItem label="${field.comment}" path="${field.propertyName}">
                <NInputNumber v-model:value="formData.${field.propertyName}" :min="0" :max="100" placeholder="请输入${field.comment}" />
              </NFormItem>
            <#elseif field.propertyType == 'Boolean'>
              <!-- Boolean 选择框 -->
              <NFormItem label="${field.comment}" path="${field.propertyName}">
                <NRadioGroup v-model:value="formData.${field.propertyName}">
                  <NRadio :value="true">
                    是
                  </NRadio>
                  <NRadio :value="false">
                    否
                  </NRadio>
                </NRadioGroup>
              </NFormItem>
            <#elseif field.propertyType == 'LocalDateTime' || field.propertyType == 'LocalDate' || field.propertyType == 'Date'>
              <!-- 日期选择 -->
              <NFormItem label="${field.comment}" path="${field.propertyName}">
                <NDatePicker type="datetime" v-model:value="formData.${field.propertyName}" />
              </NFormItem>
            <#else>
              <!-- 输入框 -->
              <NFormItem label="${field.comment}" path="${field.propertyName}">
                <NInput v-model:value="formData.${field.propertyName}" placeholder="请输入${field.comment}" />
              </NFormItem>
            </#if>
          </#if>
        </#list>
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
