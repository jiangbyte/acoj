<script lang="ts" setup>
import { NUpload } from 'naive-ui'
import type { UploadCustomRequestOptions, UploadFileInfo } from 'naive-ui'
import { useDataProblemFetch, useSysCategoryFetch, useSysDictFetch, useSysTagFetch } from '@/composables/v1'

const emit = defineEmits<{
  (e: 'success', url: string): void
  (e: 'error', error?: Error): void
}>()

// 上传状态枚举
enum UploadStatus {
  IDLE = 'idle',
  UPLOADING = 'uploading',
  SUCCESS = 'success',
  ERROR = 'error',
}

const uploadStatus = ref<UploadStatus>(UploadStatus.IDLE)
const uploadProgress = ref(0)
const { dataProblemImport } = useDataProblemFetch()
const formRef = ref()

const defaultFormData = {
  file: new File([], ''),
  threshold: 0.5,
  useAi: false,
  isPublic: false,
  isVisible: true,
  categoryId: null,
  difficulty: null,
  tagIds: [],
  allowedLanguages: [],
}
const formData = ref({ ...defaultFormData })

const fileListLengthRef = ref(0)

function handleChange(options: { fileList: UploadFileInfo[] }) {
  fileListLengthRef.value = options.fileList.length
  // 当文件列表变化时重置状态（除非正在上传中）
  if (uploadStatus.value !== UploadStatus.UPLOADING) {
    uploadStatus.value = UploadStatus.IDLE
    uploadProgress.value = 0
  }
}
const isLoading = ref(false)
async function FileUploadRequest({
  file,
  onFinish,
  onError,
  onProgress,
}: UploadCustomRequestOptions) {
  try {
    uploadStatus.value = UploadStatus.UPLOADING
    uploadProgress.value = 0
    isLoading.value = true

    // 模拟上传进度
    const progressInterval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += 10
        onProgress?.({ percent: uploadProgress.value })
      }
    }, 200)

    // 创建 FormData 对象
    const submitData = new FormData()

    // 添加文件
    submitData.append('file', file.file as File)

    // 添加其他参数
    submitData.append('threshold', formData.value.threshold.toString())
    submitData.append('useAi', formData.value.useAi.toString())
    submitData.append('isPublic', formData.value.isPublic.toString())
    submitData.append('isVisible', formData.value.isVisible.toString())

    if (formData.value.categoryId) {
      submitData.append('categoryId', formData.value.categoryId)
    }
    if (formData.value.difficulty) {
      submitData.append('difficulty', formData.value.difficulty)
    }
    if (formData.value.tagIds && formData.value.tagIds.length > 0) {
      formData.value.tagIds.forEach((tagId) => {
        submitData.append('tagIds', tagId)
      })
    }
    if (formData.value.allowedLanguages && formData.value.allowedLanguages.length > 0) {
      formData.value.allowedLanguages.forEach((language) => {
        submitData.append('allowedLanguages', language)
      })
    }

    window.$message.info('文件上传中，请稍候...')

    const { data, success } = await dataProblemImport(submitData)

    // 清除进度定时器并设置完成
    clearInterval(progressInterval)
    uploadProgress.value = 100
    onProgress?.({ percent: 100 })

    if (success) {
      uploadStatus.value = UploadStatus.SUCCESS
      window.$message.success(`导入成功！成功导入 ${data?.successCount || 0}/${data?.totalCount || 0} 道题目`)

      // 如果有错误信息，显示警告
      if (data?.errors && data.errors.length > 0) {
        window.$message.warning(`部分题目导入失败：${data.errors.join('; ')}`)
      }

      emit('success', 'OK')
      // 上传成功后关闭模态框
      setTimeout(() => {
        handleClose()
      }, 1500)
    }
    else {
      throw new Error(data?.message || '导入失败')
    }

    isLoading.value = false
    onFinish()
  }
  catch (error) {
    console.error('Upload failed:', error)
    uploadStatus.value = UploadStatus.ERROR
    const errorMessage = error instanceof Error ? error.message : '上传失败，请重试'
    window.$message.error(errorMessage)
    emit('error', error instanceof Error ? error : new Error('Upload failed'))
    onError()
  }
}

function handleFinish({
  file,
  event,
}: {
  file: UploadFileInfo
  event?: ProgressEvent
}) {
  console.log('Upload finished:', file, event)
}

function handleError({
  file,
  event,
}: {
  file: UploadFileInfo
  event?: ProgressEvent
}) {
  console.error('Upload error:', file, event)
  uploadStatus.value = UploadStatus.ERROR
  emit('error', new Error('Upload failed'))
}

const { sysDictOptions } = useSysDictFetch()
const { sysCategoryOptions } = useSysCategoryFetch()
const { sysTagOptions } = useSysTagFetch()
const showImportModal = ref(false)
const difficultyOptions = ref()
const allowLanguageOptions = ref()
const categoryOptions = ref()
const tagOptions = ref()

async function doOpen() {
  // 重置状态
  uploadStatus.value = UploadStatus.IDLE
  uploadProgress.value = 0
  fileListLengthRef.value = 0

  // 获取下拉列表数据
  const { data: difficultyData } = await sysDictOptions({ dictType: 'PROBLEM_DIFFICULTY' })
  if (difficultyData) {
    difficultyOptions.value = difficultyData.map((item: any) => {
      return {
        value: Number(item.value),
        label: item.label,
      }
    })
  }

  const { data: allowLanguageData } = await sysDictOptions({ dictType: 'ALLOW_LANGUAGE' })
  if (allowLanguageData) {
    allowLanguageOptions.value = allowLanguageData
  }

  const { data: catgoryData } = await sysCategoryOptions({})
  if (catgoryData) {
    categoryOptions.value = catgoryData
  }

  const { data: tagData } = await sysTagOptions({})
  if (tagData) {
    tagOptions.value = tagData
  }

  showImportModal.value = true
}

defineExpose({
  doOpen,
})

const rules = {
  categoryId: [
    { required: true, message: '请输入分类', trigger: ['input', 'blur'] },
  ],
  allowedLanguages: [{
    type: 'array',
    required: true,
    trigger: ['blur', 'change'],
    message: '请选择语言',
  }],
  tagIds: [{
    type: 'array',
    required: true,
    trigger: ['blur', 'change'],
    message: '请选择标签',
  }],
  difficulty: [
    { required: true, message: '请输入难度', type: 'number', trigger: ['input', 'blur'] },
  ],
  threshold: [
    { required: true, message: '请输入阈值', type: 'number', trigger: ['input', 'blur'] },
  ],
}

// 添加一个 ref 来访问上传组件
const uploadRef = ref()

function handleSubmit() {
  // 如果正在上传中，不允许重复提交
  if (uploadStatus.value === UploadStatus.UPLOADING) {
    window.$message.warning('文件正在上传中，请稍候...')
    return
  }

  // 验证表单
  formRef.value?.validate((errors) => {
    if (!errors) {
      // 表单验证通过，触发文件上传
      if (uploadRef.value && fileListLengthRef.value > 0) {
        uploadStatus.value = UploadStatus.UPLOADING
        uploadProgress.value = 0
        // 手动触发上传
        uploadRef.value.submit()
      }
      else {
        window.$message.error('请先选择要上传的文件')
      }
    }
    else {
      window.$message.error('请完善表单信息')
    }
  })
}

function handleClose() {
  // 如果正在上传中，提示用户
  if (uploadStatus.value === UploadStatus.UPLOADING) {
    window.$message.warning('文件正在上传中，关闭窗口将取消上传')
  }
  showImportModal.value = false
  // 重置状态
  setTimeout(() => {
    uploadStatus.value = UploadStatus.IDLE
    uploadProgress.value = 0
  }, 300)
  formData.value = { ...defaultFormData }
}

// 根据上传状态获取按钮文本
const getSubmitButtonText = computed(() => {
  switch (uploadStatus.value) {
    case UploadStatus.UPLOADING:
      return `上传中 ${uploadProgress.value}%`
    case UploadStatus.SUCCESS:
      return '上传成功'
    case UploadStatus.ERROR:
      return '上传失败，重试'
    default:
      return '执行'
  }
})

// 根据上传状态获取按钮类型
const getSubmitButtonType = computed(() => {
  switch (uploadStatus.value) {
    case UploadStatus.SUCCESS:
      return 'success'
    case UploadStatus.ERROR:
      return 'error'
    default:
      return 'primary'
  }
})

// 根据上传状态判断按钮是否禁用
const isSubmitButtonDisabled = computed(() => {
  return !fileListLengthRef.value || uploadStatus.value === UploadStatus.UPLOADING
})

function handleTagUpdate(newValue: string[], option: any) {
  if (newValue.length > 2) {
    window.$message.warning(`最多只能选择 ${2} 个标签`)
    // 截断
    formData.value.tagIds = formData.value.tagIds.slice(0, 2)
    return
  }
  formData.value.tagIds = newValue
}
</script>

<template>
  <n-modal
    v-model:show="showImportModal"
    preset="card"
    class="w-200"
    title="题目导入"
    :bordered="false"
    :mask-closable="false"
    @close="handleClose"
  >
    <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
      <n-grid :cols="24" :x-gap="24">
        <n-form-item-gi :span="12" label="分类" path="categoryId">
          <NSelect
            v-model:value="formData.categoryId"
            placeholder="请选择分类"
            :options="categoryOptions"
            clearable
            remote
          />
        </n-form-item-gi>
        <n-form-item-gi :span="12" label="难度" path="difficulty">
          <NSelect
            v-model:value="formData.difficulty"
            placeholder="请选择难度"
            :options="difficultyOptions"
            clearable
            remote
          />
        </n-form-item-gi>
        <n-form-item-gi :span="12" label="开放语言" path="allowedLanguages">
          <NSelect
            v-model:value="formData.allowedLanguages"
            placeholder="请选择开放语言"
            :options="allowLanguageOptions"
            multiple
            clearable
            remote
          />
        </n-form-item-gi>
        <n-form-item-gi :span="12" label="标签" path="tagIds">
          <NSelect
            v-model:value="formData.tagIds"
            placeholder="请选择标签"
            :options="tagOptions"
            clearable
            multiple
            remote
            @update:value="handleTagUpdate"
          />
        </n-form-item-gi>
        <n-form-item-gi :span="12" label="阈值" path="threshold">
          <NInputNumber
            v-model:value="formData.threshold"
            :min="0"
            :max="1.0"
            placeholder="请输入阈值"
            :step="0.1"
            :precision="1"
          />
        </n-form-item-gi>
        <n-form-item-gi :span="12" label="使用AI" path="useAi">
          <NRadioGroup v-model:value="formData.useAi">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </n-form-item-gi>
        <n-form-item-gi :span="12" label="是否公开" path="isPublic">
          <NRadioGroup v-model:value="formData.isPublic">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </n-form-item-gi>
        <n-form-item-gi :span="12" label="是否上架" path="isVisible">
          <NRadioGroup v-model:value="formData.isVisible">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </n-form-item-gi>
        <n-gi :span="24" class="mb-4">
          <n-text>
            <n-tag type="primary" size="small">
              公开
            </n-tag> 与 <n-tag type="primary" size="small">
              上架
            </n-tag> 处理 Web 题库显示，均开启时显示该题。<n-tag type="primary" size="small">
              公开
            </n-tag> 标记是否为题集中私有题目。题集组题时受是否<n-tag type="primary" size="small">
              上架
            </n-tag> 标记限制。
          </n-text>
        </n-gi>
      </n-grid>
    </NForm>

    <!-- 上传状态提示 -->
    <div v-if="uploadStatus !== UploadStatus.IDLE" class="mb-4">
      <n-alert
        :type="uploadStatus === UploadStatus.SUCCESS ? 'success'
          : uploadStatus === UploadStatus.ERROR ? 'error' : 'info'"
        :show-icon="true"
      >
        <template #icon>
          <n-icon v-if="uploadStatus === UploadStatus.UPLOADING" class="spin">
            <svg viewBox="0 0 24 24">
              <path d="M12,4V2A10,10 0 0,0 2,12H4A8,8 0 0,1 12,4Z" fill="currentColor" />
            </svg>
          </n-icon>
        </template>
        <div class="flex items-center justify-between">
          <span>
            {{
              uploadStatus === UploadStatus.UPLOADING ? '文件上传中，请稍候...'
              : uploadStatus === UploadStatus.SUCCESS ? '文件上传成功！'
                : '文件上传失败，请重试'
            }}
          </span>
          <n-progress
            v-if="uploadStatus === UploadStatus.UPLOADING"
            type="line"
            :percentage="uploadProgress"
            indicator-placement="inside"
            processing
            class="w-20"
          />
        </div>
      </n-alert>
    </div>

    <n-space vertical>
      <n-space align="center">
        <n-p depth="3" style="margin: 0">
          请上传有效的fps文件(xml格式), 支持单个xml文件上传和Zip压缩包上传
        </n-p>
        <n-p v-if="uploadStatus === UploadStatus.UPLOADING" depth="3" style="margin: 0">
          上传进度: {{ uploadProgress }}%
        </n-p>
      </n-space>
      <NUpload
        ref="uploadRef"
        :multiple="false"
        :default-upload="false"
        :max="1"
        :disabled="uploadStatus === UploadStatus.UPLOADING"
        :custom-request="FileUploadRequest"
        @finish="handleFinish"
        @error="handleError"
        @change="handleChange"
      >
        <n-button type="primary">
          <template #icon>
            <IconParkOutlinePlus />
          </template>
          上传
        </n-button>
      </NUpload>
    </n-space>

    <template #footer>
      <n-space align="center" justify="end">
        <n-button :disabled="uploadStatus === UploadStatus.UPLOADING" @click="handleClose">
          取消
        </n-button>
        <n-button
          :type="getSubmitButtonType"
          :disabled="isSubmitButtonDisabled"
          :loading="uploadStatus === UploadStatus.UPLOADING"
          @click="handleSubmit"
        >
          {{ getSubmitButtonText }}
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>
.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.w-20 {
  width: 80px;
}
</style>
