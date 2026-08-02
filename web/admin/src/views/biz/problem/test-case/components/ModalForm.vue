<script setup lang="ts">
import { ojProblemTestCaseApi } from '@/api'
import FileUpload from '@/components/upload/FileUpload.vue'
import { computed, reactive, ref } from 'vue'

const props = defineProps<{
  problemId: string
}>()

const emit = defineEmits<{
  saved: []
}>()

const formInst = ref<any>(null)
const visible = ref(false)
const submitting = ref(false)
const editingId = ref<string | null>(null)
const inputFileRef = ref<HTMLInputElement | null>(null)
const outputFileRef = ref<HTMLInputElement | null>(null)

const form = reactive({
  case_no: 1,
  sort: 1,
  case_type: 'NORMAL',
  data_mode: 'inline' as 'inline' | 'file',
  points: 0,
  is_pretest: false,
  batch_no: null as number | null,
  time_limit_ms: null as number | null,
  memory_limit_kb: null as number | null,
  input_inline: '',
  output_inline: '',
  input_file: null as string | null,
  output_file: null as string | null,
})

const isInline = computed(() => form.data_mode === 'inline')
const modalTitle = computed(() => {
  const modeLabel = isInline.value ? 'inline' : 'file'
  return editingId.value ? `编辑测例（${modeLabel}）` : `新增测例（${modeLabel}）`
})

const dataModeOptions = [
  { label: 'Inline（文本）', value: 'inline' },
  { label: 'File（存储文件）', value: 'file' },
]

defineExpose({ openModal })

async function openModal(id?: string) {
  editingId.value = id ?? null
  if (id) {
    const response = await ojProblemTestCaseApi.detail(props.problemId, { id })
    const data = response.data ?? {}
    Object.assign(form, {
      case_no: data.case_no ?? 1,
      sort: data.sort ?? data.case_no ?? 1,
      case_type: data.case_type || 'NORMAL',
      data_mode: data.data_mode === 'file' ? 'file' : 'inline',
      points: data.points ?? 0,
      is_pretest: Boolean(data.is_pretest),
      batch_no: data.batch_no ?? null,
      time_limit_ms: data.time_limit_ms ?? null,
      memory_limit_kb: data.memory_limit_kb ?? null,
      input_inline: data.input_inline ?? '',
      output_inline: data.output_inline ?? '',
      input_file: data.input_file ?? null,
      output_file: data.output_file ?? null,
    })
  }
  else {
    Object.assign(form, {
      case_no: 1,
      sort: 1,
      case_type: 'NORMAL',
      data_mode: 'inline',
      points: 0,
      is_pretest: false,
      batch_no: null,
      time_limit_ms: null,
      memory_limit_kb: null,
      input_inline: '',
      output_inline: '',
      input_file: null,
      output_file: null,
    })
  }
  visible.value = true
}

function handleAfterLeave() {
  editingId.value = null
  formInst.value?.restoreValidation?.()
}

async function loadLocalText(kind: 'input' | 'output', event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) {
    return
  }
  try {
    const text = await file.text()
    if (kind === 'input') {
      form.input_inline = text
    }
    else {
      form.output_inline = text
    }
  }
  catch {
    window.$message.error('读取本地文件失败')
  }
}

async function handleSubmit() {
  await formInst.value?.validate()
  submitting.value = true
  try {
    const payload: Record<string, any> = {
      problem_id: props.problemId,
      case_no: Number(form.case_no),
      sort: Number(form.sort || form.case_no),
      case_type: form.case_type || 'NORMAL',
      data_mode: form.data_mode,
      points: Number(form.points ?? 0),
      is_pretest: Boolean(form.is_pretest),
      batch_no: form.batch_no == null ? null : Number(form.batch_no),
      time_limit_ms: form.time_limit_ms == null ? null : Number(form.time_limit_ms),
      memory_limit_kb: form.memory_limit_kb == null ? null : Number(form.memory_limit_kb),
      batch_depends: [],
      extra: {},
    }
    if (form.data_mode === 'inline') {
      payload.input_inline = form.input_inline ?? ''
      payload.output_inline = form.output_inline ?? ''
      payload.input_file = null
      payload.output_file = null
    }
    else {
      if (!form.input_file || !form.output_file) {
        window.$message.warning('file 模式需要输入/输出文件')
        return
      }
      payload.input_file = form.input_file
      payload.output_file = form.output_file
      payload.input_inline = null
      payload.output_inline = null
    }
    if (editingId.value) {
      await ojProblemTestCaseApi.update(props.problemId, { id: editingId.value, ...payload })
      window.$message.success('更新成功')
    }
    else {
      await ojProblemTestCaseApi.create(props.problemId, payload)
      window.$message.success('创建成功')
    }
    visible.value = false
    emit('saved')
  }
  finally {
    submitting.value = false
  }
}
</script>

<template>
  <NModal
    v-model:show="visible"
    preset="card"
    :title="modalTitle"
    class="w-920px"
    :mask-closable="false"
    :segmented="{ content: true, footer: true }"
    @after-leave="handleAfterLeave"
  >
    <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
      <NForm
        ref="formInst"
        :model="form"
        label-placement="left"
        label-width="88"
        require-mark-placement="right-hanging"
      >
        <NGrid :cols="24" :x-gap="12">
          <NFormItemGi :span="6" label="编号" path="case_no" :rule="{ type: 'number', required: true, message: '必填', trigger: ['input', 'blur'] }">
            <NInputNumber v-model:value="form.case_no" :min="1" class="w-full" />
          </NFormItemGi>
          <NFormItemGi :span="6" label="分值" path="points">
            <NInputNumber v-model:value="form.points" :min="0" class="w-full" />
          </NFormItemGi>
          <NFormItemGi :span="6" label="样例" path="is_pretest">
            <NSwitch v-model:value="form.is_pretest" />
          </NFormItemGi>
          <NFormItemGi :span="6" label="子任务" path="batch_no">
            <NInputNumber v-model:value="form.batch_no" :min="0" clearable class="w-full" placeholder="可选" />
          </NFormItemGi>
          <NFormItemGi :span="12" label="时间限制" path="time_limit_ms">
            <NInputNumber
              v-model:value="form.time_limit_ms"
              :min="1"
              clearable
              class="w-full"
              placeholder="空则回退语言/题目限制"
            >
              <template #suffix>
                ms
              </template>
            </NInputNumber>
          </NFormItemGi>
          <NFormItemGi :span="12" label="内存限制" path="memory_limit_kb">
            <NInputNumber
              v-model:value="form.memory_limit_kb"
              :min="1"
              clearable
              class="w-full"
              placeholder="空则回退语言/题目限制"
            >
              <template #suffix>
                KB
              </template>
            </NInputNumber>
          </NFormItemGi>

          <NFormItemGi :span="24" label="数据模式" path="data_mode" :rule="{ required: true, message: '必选', trigger: ['change', 'blur'] }">
            <NRadioGroup v-model:value="form.data_mode" size="small">
              <NRadioButton
                v-for="opt in dataModeOptions"
                :key="opt.value"
                :value="opt.value"
                :disabled="Boolean(editingId) && form.data_mode !== opt.value"
              >
                {{ opt.label }}
              </NRadioButton>
            </NRadioGroup>
          </NFormItemGi>

          <template v-if="isInline">
            <NFormItemGi :span="12" label="输入" path="input_inline" :rule="{ required: true, message: '请填写输入', trigger: ['input', 'blur'] }">
              <NFlex vertical :size="8" class="w-full">
                <NButton size="tiny" secondary @click="inputFileRef?.click()">
                  从本地文件载入输入
                </NButton>
                <input
                  ref="inputFileRef"
                  type="file"
                  class="hidden"
                  accept=".txt,.in,.dat,text/*"
                  @change="loadLocalText('input', $event)"
                >
                <NInput
                  v-model:value="form.input_inline"
                  type="textarea"
                  :rows="10"
                  placeholder="输入数据 (input)"
                  class="font-mono"
                />
              </NFlex>
            </NFormItemGi>
            <NFormItemGi :span="12" label="输出" path="output_inline">
              <NFlex vertical :size="8" class="w-full">
                <NButton size="tiny" secondary @click="outputFileRef?.click()">
                  从本地文件载入输出
                </NButton>
                <input
                  ref="outputFileRef"
                  type="file"
                  class="hidden"
                  accept=".txt,.out,.ans,.dat,text/*"
                  @change="loadLocalText('output', $event)"
                >
                <NInput
                  v-model:value="form.output_inline"
                  type="textarea"
                  :rows="10"
                  placeholder="期望输出 (output，交互题可留空)"
                  class="font-mono"
                />
              </NFlex>
            </NFormItemGi>
          </template>
          <template v-else>
            <NFormItemGi :span="12" label="输入文件" path="input_file" :rule="{ required: true, message: '必填', trigger: ['change', 'blur'] }">
              <FileUpload v-model:value="form.input_file" value-type="object_name" button-text="上传输入文件" />
            </NFormItemGi>
            <NFormItemGi :span="12" label="输出文件" path="output_file" :rule="{ required: true, message: '必填', trigger: ['change', 'blur'] }">
              <FileUpload v-model:value="form.output_file" value-type="object_name" button-text="上传输出文件" />
            </NFormItemGi>
          </template>
        </NGrid>
      </NForm>
    </NScrollbar>
    <template #footer>
      <NFlex justify="end">
        <NButton @click="visible = false">
          取消
        </NButton>
        <NButton type="primary" :loading="submitting" @click="handleSubmit">
          保存
        </NButton>
      </NFlex>
    </template>
  </NModal>
</template>
