<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useSysBannerFetch } from '@/composables'
import type { ISysBanner } from '@/composables'
import MDEditor from '@/components/common/editor/md/Editor.vue'
import CodeEditor from '@/components/common/editor/code/Editor.vue'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { sysBannerDefaultData } = useSysBannerFetch()
const formData = ref<ISysBanner>({ ...sysBannerDefaultData })
const rules = {
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysBannerDefaultData }
}

function doSubmit() {
  loading.value = true
  emit('submit', true)
  doClose()
  show.value = false
  loading.value = false
}

const isEdit = ref(false)
function doOpen(row: ISysBanner | null = null, edit: boolean = false) {
  show.value = true
  isEdit.value = edit
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
        <NFormItem label="标题" path="title">
          <NInput v-model:value="formData.title" placeholder="请输入标题" />
        </NFormItem>
        <!-- 文本域 -->
        <NFormItem label="标题" path="title">
          <NInput type="textarea" placeholder="请输入标题" />
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="状态" path="status">
          <NRadioGroup>
            <NRadio :value="true">
              启用
            </NRadio>
            <NRadio :value="false">
              禁用
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- 下拉选择 -->
        <NFormItem label="类型" path="type">
          <NSelect :options="[{ label: '类型1', value: 'type1' }, { label: '类型2', value: 'type2' }]" />
        </NFormItem>
        <!-- 下拉多选 -->
        <NFormItem label="标签" path="tag">
          <NSelect :options="[{ label: '标签1', value: 'tag1' }, { label: '标签2', value: 'tag2' }]" multiple />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="排序" path="sort">
          <NInputNumber :min="0" :max="100" placeholder="请输入排序" />
        </NFormItem>
        <!-- 日期选择 -->
        <NFormItem label="开始时间" path="startTime">
          <NDatePicker type="datetime" />
        </NFormItem>
        <!-- 文件上传 -->
        <NFormItem label="图片" path="image">
          <NUpload>
            <NButton>上传图片</NButton>
          </NUpload>
        </NFormItem>
        <!-- MD文本 -->
        <NFormItem label="内容" path="content">
          <MDEditor model-value="456" />
        </NFormItem>
        <!-- 代码编辑器 -->
        <NFormItem label="代码" path="code">
          <CodeEditor language="javascript" model-value="789798" style="height: 200px;" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            关闭
          </NButton>
          <NButton type="primary" :loading="loading" @click="doSubmit">
            保存
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
