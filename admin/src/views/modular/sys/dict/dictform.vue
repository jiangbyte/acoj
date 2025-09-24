<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useSysDictFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysDictDefaultData, sysDictAdd, sysDictEdit } = useSysDictFetch()
const formData = ref<any>({ ...sysDictDefaultData })
const rules = {
  dictType: [
    { required: true, message: '请输入字典类型', trigger: ['input', 'blur'] },
  ],
  typeLabel: [
    { required: true, message: '请输入类型名称', trigger: ['input', 'blur'] },
  ],
  dictValue: [
    { required: true, message: '请输入字典值', trigger: ['input', 'blur'] },
  ],
  dictLabel: [
    { required: true, message: '请输入字典标签', trigger: ['input', 'blur'] },
  ],
  sortOrder: [
    { required: true, message: '请输入排序', type: 'number', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysDictDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await sysDictEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await sysDictAdd(formData.value)
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
        <NFormItem v-if="isEdit" label="主键ID" path="id">
          <NInput v-model:value="formData.id" placeholder="请输入主键ID" :disabled="true" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="类型名称" path="typeLabel">
          <NInput v-model:value="formData.typeLabel" placeholder="请输入类型名称" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="字典类型" path="dictType">
          <NInput v-model:value="formData.dictType" placeholder="请输入字典类型" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="字典标签" path="dictLabel">
          <NInput v-model:value="formData.dictLabel" placeholder="请输入字典标签" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="字典值" path="dictValue">
          <NInput v-model:value="formData.dictValue" placeholder="请输入字典值" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="排序" path="sortOrder">
          <NInputNumber v-model:value="formData.sortOrder" :min="0" :max="100" placeholder="请输入排序" />
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
