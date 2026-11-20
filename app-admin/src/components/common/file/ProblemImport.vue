<script lang="ts" setup>
import { NUpload } from 'naive-ui'
import type { UploadCustomRequestOptions, UploadFileInfo } from 'naive-ui'
import { useDataProblemFetch, useSysCategoryFetch, useSysDictFetch, useSysTagFetch } from '@/composables/v1'

const emit = defineEmits<{
  (e: 'success', url: string): void
  (e: 'error', error?: Error): void
}>()

// Upload status enum
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
  // Reset status when the file list changes unless an upload is active
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

    // Simulate upload progress
    const progressInterval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += 10
        onProgress?.({ percent: uploadProgress.value })
      }
    }, 200)

    // Create a FormData object
    const submitData = new FormData()

    // Add the file
    submitData.append('file', file.file as File)

    // Add other parameters
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

    window.$message.info('Uploading file. Please wait...')

    const { data, success } = await dataProblemImport(submitData)

    // Clear the progress timer and mark complete
    clearInterval(progressInterval)
    uploadProgress.value = 100
    onProgress?.({ percent: 100 })

    if (success) {
      uploadStatus.value = UploadStatus.SUCCESS
      window.$message.success(`Imported: ${data?.successCount || 0}/${data?.totalCount || 0}  problems`)

      // Show a warning when errors exist
      if (data?.errors && data.errors.length > 0) {
        window.$message.warning(`Some problems failed to import: ${data.errors.join('; ')}`)
      }

      emit('success', 'OK')
      // Close the modal after upload
      setTimeout(() => {
        handleClose()
      }, 1500)
    }
    else {
      throw new Error(data?.message || 'Import failed')
    }

    isLoading.value = false
    onFinish()
  }
  catch (error) {
    console.error('Upload failed:', error)
    uploadStatus.value = UploadStatus.ERROR
    const errorMessage = error instanceof Error ? error.message : 'Upload failed. Please try again'
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
  // Reset status
  uploadStatus.value = UploadStatus.IDLE
  uploadProgress.value = 0
  fileListLengthRef.value = 0

  // Load dropdown options
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
    { required: true, message: 'Enter Category', trigger: ['input', 'blur'] },
  ],
  allowedLanguages: [{
    type: 'array',
    required: true,
    trigger: ['blur', 'change'],
    message: 'Select Language',
  }],
  tagIds: [{
    type: 'array',
    required: true,
    trigger: ['blur', 'change'],
    message: 'Select Tag',
  }],
  difficulty: [
    { required: true, message: 'Enter Difficulty', type: 'number', trigger: ['input', 'blur'] },
  ],
  threshold: [
    { required: true, message: 'Enter Threshold', type: 'number', trigger: ['input', 'blur'] },
  ],
}

// Add a ref for the upload component
const uploadRef = ref()

function handleSubmit() {
  // Prevent duplicate submission while uploading
  if (uploadStatus.value === UploadStatus.UPLOADING) {
    window.$message.warning('A file is uploading，Please wait...')
    return
  }

  // Validate the form
  formRef.value?.validate((errors) => {
    if (!errors) {
      // Upload after form validation succeeds
      if (uploadRef.value && fileListLengthRef.value > 0) {
        uploadStatus.value = UploadStatus.UPLOADING
        uploadProgress.value = 0
        // Trigger upload manually
        uploadRef.value.submit()
      }
      else {
        window.$message.error('Select a file to upload first')
      }
    }
    else {
      window.$message.error('Complete the form')
    }
  })
}

function handleClose() {
  // Warn the user if an upload is in progress
  if (uploadStatus.value === UploadStatus.UPLOADING) {
    window.$message.warning('A file is uploading，Closing the window will cancel the upload')
  }
  showImportModal.value = false
  // Reset status
  setTimeout(() => {
    uploadStatus.value = UploadStatus.IDLE
    uploadProgress.value = 0
  }, 300)
  formData.value = { ...defaultFormData }
}

// Get button text for the upload status
const getSubmitButtonText = computed(() => {
  switch (uploadStatus.value) {
    case UploadStatus.UPLOADING:
      return `Uploading ${uploadProgress.value}%`
    case UploadStatus.SUCCESS:
      return 'Uploaded successfully'
    case UploadStatus.ERROR:
      return 'Upload failed，Retry'
    default:
      return 'Run'
  }
})

// Get button type for the upload status
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

// Determine whether the button is disabled from upload status
const isSubmitButtonDisabled = computed(() => {
  return !fileListLengthRef.value || uploadStatus.value === UploadStatus.UPLOADING
})

function handleTagUpdate(newValue: string[], option: any) {
  if (newValue.length > 2) {
    window.$message.warning(`You can select up to ${2} tags`)
    // Truncate
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
    title="Import Problems"
    :bordered="false"
    :mask-closable="false"
    @close="handleClose"
  >
    <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
      <n-grid :cols="24" :x-gap="24">
        <n-form-item-gi :span="12" label="Category" path="categoryId">
          <NSelect
            v-model:value="formData.categoryId"
            placeholder="Select Category"
            :options="categoryOptions"
            clearable
            remote
          />
        </n-form-item-gi>
        <n-form-item-gi :span="12" label="Difficulty" path="difficulty">
          <NSelect
            v-model:value="formData.difficulty"
            placeholder="Select Difficulty"
            :options="difficultyOptions"
            clearable
            remote
          />
        </n-form-item-gi>
        <n-form-item-gi :span="12" label="Allowed Languages" path="allowedLanguages">
          <NSelect
            v-model:value="formData.allowedLanguages"
            placeholder="Select Allowed Languages"
            :options="allowLanguageOptions"
            multiple
            clearable
            remote
          />
        </n-form-item-gi>
        <n-form-item-gi :span="12" label="Tag" path="tagIds">
          <NSelect
            v-model:value="formData.tagIds"
            placeholder="Select Tag"
            :options="tagOptions"
            clearable
            multiple
            remote
            @update:value="handleTagUpdate"
          />
        </n-form-item-gi>
        <n-form-item-gi :span="12" label="Threshold" path="threshold">
          <!-- <NInputNumber
            v-model:value="formData.threshold"
            :min="0"
            :max="1.0"
            placeholder="Enter Threshold"
            :step="0.1"
            :precision="1"
          /> -->
          <NSlider
            v-model:value="formData.threshold"
            :step="0.1"
            :min="0.5"
            :max="1.0"
            :marks="{ 0.5: '0.5', 0.8: '0.8', 1.0: '1.0' }"
          />
        </n-form-item-gi>
        <n-form-item-gi :span="12" label="Use AI" path="useAi">
          <NRadioGroup v-model:value="formData.useAi">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </n-form-item-gi>
        <n-form-item-gi :span="12" label="Public" path="isPublic">
          <NRadioGroup v-model:value="formData.isPublic">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </n-form-item-gi>
        <n-form-item-gi :span="12" label="Published" path="isVisible">
          <NRadioGroup v-model:value="formData.isVisible">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </n-form-item-gi>
        <n-gi :span="24" class="mb-4">
          <n-text>
            <n-tag type="primary" size="small">
              Public
            </n-tag> and <n-tag type="primary" size="small">
              Published
            </n-tag> control display in the web problem library. The problem appears when both are enabled.<n-tag type="primary" size="small">
              Public
            </n-tag> marks a problem as private to a set. Set composition is restricted by the<n-tag type="primary" size="small">
              Published
            </n-tag> setting.
          </n-text>
        </n-gi>
      </n-grid>
    </NForm>

    <!-- UploadStatusNotice -->
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
              uploadStatus === UploadStatus.UPLOADING ? 'Uploading file. Please wait...'
              : uploadStatus === UploadStatus.SUCCESS ? 'File uploaded'
                : 'Upload failed. Please try again'
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
          Upload a valid FPS file (XML), either as a single XML file or a ZIP archive
        </n-p>
        <n-p v-if="uploadStatus === UploadStatus.UPLOADING" depth="3" style="margin: 0">
          Upload progress: {{ uploadProgress }}%
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
          Upload
        </n-button>
      </NUpload>
    </n-space>

    <template #footer>
      <n-space align="center" justify="end">
        <n-button :disabled="uploadStatus === UploadStatus.UPLOADING" @click="handleClose">
          Cancel
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
