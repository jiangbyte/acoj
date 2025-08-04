<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NModal, NUpload, type UploadFileInfo } from 'naive-ui'
import { useProProblemFetch } from '@/composables'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { proProblemDefaultData, proProblemAdd, proProblemEdit } = useProProblemFetch()
const formData = ref<any>({ ...proProblemDefaultData })
const rules = {
  categoryId: [
    { required: true, message: '请输入分类', trigger: ['input', 'blur'] },
  ],
  title: [
    { required: true, message: '请输入标题', trigger: ['input', 'blur'] },
  ],
  source: [
    { required: true, message: '请输入来源', trigger: ['input', 'blur'] },
  ],
  url: [
    { required: true, message: '请输入链接', trigger: ['input', 'blur'] },
  ],
  maxTime: [
    { required: true, message: '请输入时间限制', type: 'number', trigger: ['input', 'blur'] },
  ],
  maxMemory: [
    { required: true, message: '请输入内存限制', type: 'number', trigger: ['input', 'blur'] },
  ],
  description: [
    { required: true, message: '请输入描述', trigger: ['input', 'blur'] },
  ],
  testCase: [
    { required: true, message: '请输入用例', trigger: ['input', 'blur'] },
  ],
  allowedLanguages: [
    { required: true, message: '请输入开放语言', trigger: ['input', 'blur'] },
  ],
  difficulty: [
    { required: true, message: '请输入难度', type: 'number', trigger: ['input', 'blur'] },
  ],
  codeTemplate: [
    { required: true, message: '请输入模板代码', trigger: ['input', 'blur'] },
  ],
  solved: [
    { required: true, message: '请输入解决', type: 'number', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...proProblemDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await proProblemEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await proProblemAdd(formData.value)
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
              <NFormItem label="分类" path="categoryId">
                <NInput v-model:value="formData.categoryId" placeholder="请输入分类" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="标题" path="title">
                <NInput v-model:value="formData.title" placeholder="请输入标题" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="来源" path="source">
                <NInput v-model:value="formData.source" placeholder="请输入来源" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="链接" path="url">
                <NInput v-model:value="formData.url" placeholder="请输入链接" />
              </NFormItem>
              <!-- 数字输入 -->
              <NFormItem label="时间限制" path="maxTime">
                <NInputNumber v-model:value="formData.maxTime" :min="0" :max="100" placeholder="请输入时间限制" />
              </NFormItem>
              <!-- 数字输入 -->
              <NFormItem label="内存限制" path="maxMemory">
                <NInputNumber v-model:value="formData.maxMemory" :min="0" :max="100" placeholder="请输入内存限制" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="描述" path="description">
                <NInput v-model:value="formData.description" placeholder="请输入描述" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="用例" path="testCase">
                <NInput v-model:value="formData.testCase" placeholder="请输入用例" />
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="开放语言" path="allowedLanguages">
                <NInput v-model:value="formData.allowedLanguages" placeholder="请输入开放语言" />
              </NFormItem>
              <!-- 数字输入 -->
              <NFormItem label="难度" path="difficulty">
                <NInputNumber v-model:value="formData.difficulty" :min="0" :max="100" placeholder="请输入难度" />
              </NFormItem>
              <!-- Boolean 选择框 -->
              <NFormItem label="使用模板" path="useTemplate">
                <NRadioGroup v-model:value="formData.useTemplate">
                  <NRadio :value="true">
                    是
                  </NRadio>
                  <NRadio :value="false">
                    否
                  </NRadio>
                </NRadioGroup>
              </NFormItem>
            <!-- 输入框 -->
              <NFormItem label="模板代码" path="codeTemplate">
                <NInput v-model:value="formData.codeTemplate" placeholder="请输入模板代码" />
              </NFormItem>
              <!-- 数字输入 -->
              <NFormItem label="解决" path="solved">
                <NInputNumber v-model:value="formData.solved" :min="0" :max="100" placeholder="请输入解决" />
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
