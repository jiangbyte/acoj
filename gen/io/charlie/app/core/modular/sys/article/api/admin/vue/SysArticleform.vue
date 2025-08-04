<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NModal, NUpload, type UploadFileInfo } from 'naive-ui'
import { useSysArticleFetch } from '@/composables'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysArticleDefaultData, sysArticleAdd, sysArticleEdit } = useSysArticleFetch()
const formData = ref<any>({ ...sysArticleDefaultData })
const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: ['input', 'blur'] },
  ],
  subtitle: [
    { required: true, message: '请输入子标题', trigger: ['input', 'blur'] },
  ],
  cover: [
    { required: true, message: '请输入封面', trigger: ['input', 'blur'] },
  ],
  author: [
    { required: true, message: '请输入作者', trigger: ['input', 'blur'] },
  ],
  summary: [
    { required: true, message: '请输入摘要', trigger: ['input', 'blur'] },
  ],
  sort: [
    { required: true, message: '请输入排序', type: 'number', trigger: ['input', 'blur'] },
  ],
  toUrl: [
    { required: true, message: '请输入链接', trigger: ['input', 'blur'] },
  ],
  parentId: [
    { required: true, message: '请输入父级', trigger: ['input', 'blur'] },
  ],
  type: [
    { required: true, message: '请输入类型', trigger: ['input', 'blur'] },
  ],
  category: [
    { required: true, message: '请输入分类', trigger: ['input', 'blur'] },
  ],
  content: [
    { required: true, message: '请输入内容', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysArticleDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await sysArticleEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await sysArticleAdd(formData.value)
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
              <NFormItem label="标题" path="title">
                <NInput v-model:value="formData.title" placeholder="请输入标题" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="子标题" path="subtitle">
                <NInput v-model:value="formData.subtitle" placeholder="请输入子标题" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="封面" path="cover">
                <NInput v-model:value="formData.cover" placeholder="请输入封面" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="作者" path="author">
                <NInput v-model:value="formData.author" placeholder="请输入作者" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="摘要" path="summary">
                <NInput v-model:value="formData.summary" placeholder="请输入摘要" />
              </NFormItem>
              <!-- 数字输入 -->
              <NFormItem label="排序" path="sort">
                <NInputNumber v-model:value="formData.sort" :min="0" :max="100" placeholder="请输入排序" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="链接" path="toUrl">
                <NInput v-model:value="formData.toUrl" placeholder="请输入链接" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="父级" path="parentId">
                <NInput v-model:value="formData.parentId" placeholder="请输入父级" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="类型" path="type">
                <NInput v-model:value="formData.type" placeholder="请输入类型" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="分类" path="category">
                <NInput v-model:value="formData.category" placeholder="请输入分类" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="内容" path="content">
                <NInput v-model:value="formData.content" placeholder="请输入内容" />
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
