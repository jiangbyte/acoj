<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NModal, NUpload } from 'naive-ui'
import type { UploadFileInfo } from 'naive-ui'
import { useSysBannerFetch } from '@/composables'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysBannerDefaultData, sysBannerAdd, sysBannerEdit } = useSysBannerFetch()
const formData = ref<any>({ ...sysBannerDefaultData })
const rules = {
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysBannerDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  loading.value = true
  if (isEdit.value) {
    const { success } = await sysBannerEdit(formData.value)
    if (success) {
      window.$message.success('修改成功')
    }
  }
  else {
    const { success } = await sysBannerAdd(formData.value)
    if (success) {
      window.$message.success('新增成功')
    }
  }
  emit('submit', true)
  doClose()
  show.value = false
  loading.value = false
}

function doOpen(row: any = null, edit: boolean = false) {
  show.value = true
  isEdit.value = edit
  formData.value = Object.assign(formData.value, row)
}
defineExpose({
  doOpen,
})

const fileList = ref<UploadFileInfo[]>([])
const showImagePreviewModal = ref(false)
const previewImageUrl = ref('')
function handlePreview(file: UploadFileInfo) {
  const { url } = file
  previewImageUrl.value = url as string
  showImagePreviewModal.value = true
}
</script>

<template>
  <NDrawer v-model:show="show" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent :title="isEdit ? '编辑' : '新增'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- 输入框 -->
        <NFormItem v-if="isEdit" label="ID" path="id">
          <NInput v-model:value="formData.id" disabled />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="标题" path="title">
          <NInput v-model:value="formData.title" placeholder="请输入标题" />
        </NFormItem>
        <!-- 文件上传 -->
        <NFormItem label="图片" path="image">
          <NUpload list-type="image-card" :max="1" :default-file-list="fileList" @preview="handlePreview">
            <NP v-if="!formData.banner">
              点击上传
            </NP>
            <img v-else :src="formData.banner" style="width: 100%">
            <NModal
              v-model:show="showImagePreviewModal"
              preset="card"
              style="width: 600px"
              title="一张很酷的图片"
            >
              <img :src="previewImageUrl" style="width: 100%">
            </NModal>
          </NUpload>
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="按钮文字" path="buttonText">
          <NInput v-model:value="formData.buttonText" placeholder="请输入按钮文字" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="链接" path="toUrl">
          <NInput v-model:value="formData.toUrl" placeholder="请输入链接" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="排序" path="sort">
          <NInputNumber v-model:value="formData.sort" :min="0" :max="100" placeholder="请输入排序" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="子标题" path="subtitle">
          <NInput v-model:value="formData.subtitle" placeholder="请输入子标题" />
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
