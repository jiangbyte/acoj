<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useSysConfigFetch } from '@/composables'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysConfigDefaultData, sysConfigAdd, sysConfigEdit } = useSysConfigFetch()
const formData = ref<any>({ ...sysConfigDefaultData })
const rules = {
  name: [
    { required: true, message: '请输入名称', trigger: ['input', 'blur'] },
  ],
  code: [
    { required: true, message: '请输入编码', trigger: ['input', 'blur'] },
  ],
  value: [
    { required: true, message: '请输入值', trigger: ['input', 'blur'] },
  ],
  componentType: [
    // { required: true, message: '请输入组件类型', trigger: ['input', 'blur'] },
  ],
  // description: [
  //   { required: true, message: '请输入描述', trigger: ['input', 'blur'] },
  // ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysConfigDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await sysConfigEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await sysConfigAdd(formData.value)
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
        <NFormItem label="名称" path="name">
          <NInput v-model:value="formData.name" placeholder="请输入名称" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="编码" path="code">
          <NInput v-model:value="formData.code" placeholder="请输入编码" :disabled="isEdit" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="值" path="value">
          <NInput type="textarea" v-model:value="formData.value" placeholder="请输入值" />
        </NFormItem>
        <!-- 输入框 -->
        <!--        <NFormItem label="组件类型" path="componentType"> -->
        <!--          <NInput v-model:value="formData.componentType" placeholder="请输入组件类型" /> -->
        <!--        </NFormItem> -->
        <!-- 输入框 -->
        <NFormItem label="描述" path="description">
          <NInput v-model:value="formData.description" type="textarea" placeholder="请输入描述" />
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
