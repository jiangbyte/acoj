<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NSpin } from 'naive-ui'
import { useDataJudgeCaseFetch } from '@/composables/v1'

const emit = defineEmits(['close', 'submit'])

// 懒加载重量级组件
const CodeEditor = defineAsyncComponent({
  loader: () => import('@/components/common/editor/code/CodeEditor.vue'),
  // loader: () =>
  //   new Promise((resolve) => {
  //     // 模拟 3 秒延迟
  //     setTimeout(() => {
  //       resolve(import('@/components/common/editor/code/CodeEditor.vue'))
  //     }, 3000)
  //   }),
  loadingComponent: {
    setup() {
      return () => h('div', {
        class: 'h-full p-4',
        style: {
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        },
      }, [
        h(NSpin, { size: 'small', description: '编辑器加载中...' }, { }),
      ])
    },
  },
  delay: 200,
  timeout: 10000,
})

const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { dataJudgeCaseDefaultData, dataJudgeCaseAdd, dataJudgeCaseEdit } = useDataJudgeCaseFetch()
const formData = ref<any>({ ...dataJudgeCaseDefaultData })
const rules = {
  submitId: [
    { required: true, message: '请输入提交ID', trigger: ['input', 'blur'] },
  ],
  inputData: [
    { required: true, message: '请输入输入数据', trigger: ['input', 'blur'] },
  ],
  outputData: [
    { required: true, message: '请输入输出数据', trigger: ['input', 'blur'] },
  ],
  expectedOutput: [
    { required: true, message: '请输入期望输出', trigger: ['input', 'blur'] },
  ],
  inputFilePath: [
    { required: true, message: '请输入输入文件路径', trigger: ['input', 'blur'] },
  ],
  inputFileSize: [
    { required: true, message: '请输入输入文件大小', type: 'number', trigger: ['input', 'blur'] },
  ],
  outputFilePath: [
    { required: true, message: '请输入输出文件路径', trigger: ['input', 'blur'] },
  ],
  outputFileSize: [
    { required: true, message: '请输入输出文件大小', type: 'number', trigger: ['input', 'blur'] },
  ],
  maxTime: [
    { required: true, message: '请输入最大耗时', type: 'number', trigger: ['input', 'blur'] },
  ],
  maxMemory: [
    { required: true, message: '请输入最大内存使用', type: 'number', trigger: ['input', 'blur'] },
  ],
  score: [
    { required: true, message: '请输入分值', type: 'number', trigger: ['input', 'blur'] },
  ],
  status: [
    { required: true, message: '请输入执行状态', trigger: ['input', 'blur'] },
  ],
  exitCode: [
    { required: true, message: '请输入退出码', type: 'number', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataJudgeCaseDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await dataJudgeCaseEdit(formData.value)
        if (success) {
          window.$message.success('修改成功')
        }
      }
      else {
        const { success } = await dataJudgeCaseAdd(formData.value)
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

function doOpen(row: any = null, edit: boolean = false, submitIdStr: string) {
  show.value = true
  isEdit.value = edit
  formData.value = Object.assign(formData.value, row)
  formData.value.submitId = submitIdStr
  console.log('Form 题目ID-->', formData.value.submitId, submitIdStr)
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent :title="isEdit ? '编辑' : '新增'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- 输入框 -->
        <NFormItem v-if="isEdit" label="主键" path="id">
          <NInput v-model:value="formData.id" placeholder="请输入主键" :disabled="true" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="提交ID" path="submitId">
          <NInput v-model:value="formData.submitId" placeholder="请输入提交ID" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="输入数据" path="inputData">
          <!-- <NInput v-model:value="formData.inputData" placeholder="请输入输入数据" /> -->
          <CodeEditor v-model="formData.inputData" height="200px" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="输出数据" path="outputData">
          <!-- <NInput v-model:value="formData.outputData" placeholder="请输入输出数据" /> -->
          <CodeEditor v-model="formData.outputData" height="200px" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="期望输出" path="expectedOutput">
          <!-- <NInput v-model:value="formData.expectedOutput" placeholder="请输入期望输出" /> -->
          <CodeEditor v-model="formData.expectedOutput" height="200px" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="输入文件路径" path="inputFilePath">
          <NInput v-model:value="formData.inputFilePath" placeholder="请输入输入文件路径" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="输入文件大小" path="inputFileSize">
          <NInputNumber v-model:value="formData.inputFileSize" :min="0" :max="100" placeholder="请输入输入文件大小" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="输出文件路径" path="outputFilePath">
          <NInput v-model:value="formData.outputFilePath" placeholder="请输入输出文件路径" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="输出文件大小" path="outputFileSize">
          <NInputNumber v-model:value="formData.outputFileSize" :min="0" :max="100" placeholder="请输入输出文件大小" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="最大耗时" path="maxTime">
          <NInputNumber v-model:value="formData.maxTime" :min="0" :max="100" placeholder="请输入最大耗时" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="最大内存使用" path="maxMemory">
          <NInputNumber v-model:value="formData.maxMemory" :min="0" :max="100" placeholder="请输入最大内存使用" />
        </NFormItem>
        <!-- Boolean 选择框 -->
        <NFormItem label="是否样例判题" path="isSample">
          <NRadioGroup v-model:value="formData.isSample">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="分值" path="score">
          <NInputNumber v-model:value="formData.score" :min="0" :max="100" placeholder="请输入分值" />
        </NFormItem>
        <!-- 输入框 -->
        <NFormItem label="执行状态" path="status">
          <NInput v-model:value="formData.status" placeholder="请输入执行状态" />
        </NFormItem>
        <!-- 数字输入 -->
        <NFormItem label="退出码" path="exitCode">
          <NInputNumber v-model:value="formData.exitCode" :min="0" :max="100" placeholder="请输入退出码" />
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
