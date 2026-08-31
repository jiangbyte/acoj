<!--

  Author: Charlie



  单条测例新增/编辑弹窗（人工逐条维护）。

-->

<script setup lang="ts">

import type { FormInst, FormRules, SelectOption } from 'naive-ui'

import FileUpload from '@/components/upload/FileUpload.vue'

import { ojProblemCaseApi } from '@/api'

import { createRequiredRule } from '@/utils'

import { wireFields } from '@/utils/wire'

import { computed, reactive, ref, watch } from 'vue'



const emit = defineEmits<{

  saved: []

}>()



const formRef = ref<FormInst | null>(null)

const storageOptions: SelectOption[] = [

  { label: '内联文本', value: 'INLINE' },

  { label: '对象存储', value: 'OBJECT' },

]



const defaultFormData: Record<string, any> = {

  case_key: '',

  sort_no: 0,

  is_sample: false,

  score: 0,

  input_storage: 'INLINE',

  output_storage: 'INLINE',

  input_text: '',

  output_text: '',

  input_object_key: '',

  output_object_key: '',

  status: 'ENABLED',

}



const state = reactive({

  showModal: false,

  loading: false,

  submitLoading: false,

  dataId: null as string | null,

  problemId: '',

  caseVersion: 1,

  formModel: normalizeFormData(),

  uploadResetKey: 0,

})



const modalTitle = computed(() => (state.dataId ? '编辑测例' : '新增测例'))



const rules = computed<FormRules>(() => ({

  case_key: [createRequiredRule('测例号', 'input')],

  input_storage: [createRequiredRule('输入存储', 'change')],

  output_storage: [createRequiredRule('输出存储', 'change')],

  status: [createRequiredRule('状态', 'change')],

  sort_no: [

    {

      validator: () =>

        typeof state.formModel.sort_no === 'number' && Number.isFinite(state.formModel.sort_no),

      message: '请输入排序',

      trigger: ['input', 'blur'],

    },

  ],

  input_object_key: [

    {

      validator: () =>

        state.formModel.input_storage !== 'OBJECT'

        || !!String(state.formModel.input_object_key || '').trim(),

      message: '请上传输入文件',

      trigger: ['change', 'blur'],

    },

  ],

  output_object_key: [

    {

      validator: () =>

        state.formModel.output_storage !== 'OBJECT'

        || !!String(state.formModel.output_object_key || '').trim(),

      message: '请上传期望输出文件',

      trigger: ['change', 'blur'],

    },

  ],

}))



watch(

  () => state.formModel.input_storage,

  (value) => {

    if (value === 'INLINE') {

      state.formModel.input_object_key = ''

    } else {

      state.formModel.input_text = ''

    }

  },

)



watch(

  () => state.formModel.output_storage,

  (value) => {

    if (value === 'INLINE') {

      state.formModel.output_object_key = ''

    } else {

      state.formModel.output_text = ''

    }

  },

)



async function openModal(options: {

  problemId: string

  caseVersion: number

  id?: string

  nextSortNo?: number

}) {

  state.problemId = options.problemId

  state.caseVersion = options.caseVersion

  state.dataId = options.id ?? null

  state.formModel = normalizeFormData({

    sort_no: options.nextSortNo ?? 0,

  })

  state.uploadResetKey += 1

  state.showModal = true

  if (options.id) {

    await fetchDetail(options.id)

  }

}



async function fetchDetail(id: string) {

  state.loading = true

  try {

    const response = await ojProblemCaseApi.detail({ id })

    state.formModel = normalizeFormData(response.data ?? {})

    state.uploadResetKey += 1

  } finally {

    state.loading = false

  }

}



function normalizeFormData(data: Record<string, any> = {}): Record<string, any> {

  return {

    ...defaultFormData,

    ...data,

    ...wireFields(

      data,

      {

        sort_no: 'int',

        score: 'int',

        is_sample: 'bool',

      },

      defaultFormData,

    ),

    input_storage: data.input_storage || defaultFormData.input_storage,

    output_storage: data.output_storage || defaultFormData.output_storage,

    status: data.status || defaultFormData.status,

    input_text: data.input_text ?? '',

    output_text: data.output_text ?? '',

    input_object_key: data.input_object_key ?? '',

    output_object_key: data.output_object_key ?? '',

  }

}



function sanitizeCaseExtra(extra: unknown) {

  if (!extra || typeof extra !== 'object' || Array.isArray(extra)) {

    return undefined

  }

  const copy = { ...(extra as Record<string, unknown>) }

  delete copy.input_original_name

  delete copy.output_original_name

  return Object.keys(copy).length ? copy : undefined

}



function normalizeSubmitData(data: Record<string, any>) {

  const inputInline = data.input_storage === 'INLINE'

  const outputInline = data.output_storage === 'INLINE'

  return {

    problem_id: state.problemId,

    case_version: state.caseVersion,

    case_key: data.case_key,

    sort_no: data.sort_no,

    is_sample: !!data.is_sample,

    score: data.score ?? 0,

    input_storage: data.input_storage,

    output_storage: data.output_storage,

    input_text: inputInline ? data.input_text ?? '' : null,

    output_text: outputInline ? data.output_text ?? '' : null,

    input_object_key: inputInline ? null : data.input_object_key || null,

    output_object_key: outputInline ? null : data.output_object_key || null,

    status: data.status,

    extra: sanitizeCaseExtra(data.extra),

  }

}



function closeModal() {

  state.showModal = false

  state.submitLoading = false

}



async function submitForm() {

  await formRef.value?.validate()

  state.submitLoading = true

  try {

    const payload = normalizeSubmitData(state.formModel)

    if (state.dataId) {

      await ojProblemCaseApi.update({ ...payload, id: state.dataId })

      window.$message.success('更新成功')

    } else {

      await ojProblemCaseApi.create(payload)

      window.$message.success('创建成功')

    }

    emit('saved')

    closeModal()

  } finally {

    state.submitLoading = false

  }

}



defineExpose({

  openModal,

})

</script>



<template>

  <HeiFormContainer

    v-model:show="state.showModal"

    :title="modalTitle"

    :width="720"

    :mask-closable="false"

  >

    <NSpin :show="state.loading">

      <NForm

        ref="formRef"

        :model="state.formModel"

        :rules="rules"

        label-placement="left"

        label-width="100"

        :disabled="state.loading || state.submitLoading"

      >

        <NFormItem

          label="测例号"

          path="case_key"

        >

          <NInput

            v-model:value="state.formModel.case_key"

            placeholder="如 1、sample1"

            :disabled="!!state.dataId"

          />

        </NFormItem>

        <NFormItem

          label="排序"

          path="sort_no"

        >

          <NInputNumber

            v-model:value="state.formModel.sort_no"

            class="w-full"

          />

        </NFormItem>

        <NFormItem

          label="分值"

          path="score"

        >

          <NInputNumber

            v-model:value="state.formModel.score"

            class="w-full"

          />

        </NFormItem>

        <NFormItem

          label="作为样例"

          path="is_sample"

        >

          <NSwitch v-model:value="state.formModel.is_sample" />

        </NFormItem>

        <NFormItem

          label="状态"

          path="status"

        >

          <DictSelect

            v-model="state.formModel.status"

            dict-code="COMMON_STATUS"

            :clearable="false"

          />

        </NFormItem>

        <NFormItem

          label="输入存储"

          path="input_storage"

        >

          <NSelect

            v-model:value="state.formModel.input_storage"

            :options="storageOptions"

          />

        </NFormItem>

        <NFormItem

          v-if="state.formModel.input_storage === 'INLINE'"

          label="输入内容"

          path="input_text"

        >

          <NInput

            v-model:value="state.formModel.input_text"

            type="textarea"

            :autosize="{ minRows: 4, maxRows: 16 }"

            placeholder="stdin 内容"

          />

        </NFormItem>

        <NFormItem

          v-else

          label="输入文件"

          path="input_object_key"

        >

          <FileUpload

            :key="`input-${state.uploadResetKey}`"

            v-model:value="state.formModel.input_object_key"

            mode="upload"

            upload-variant="dragger"

            value-type="object_name"

            accept=".in,.txt,text/*"

            button-text="上传输入文件（.in / .txt）"

          />

        </NFormItem>

        <NFormItem

          label="输出存储"

          path="output_storage"

        >

          <NSelect

            v-model:value="state.formModel.output_storage"

            :options="storageOptions"

          />

        </NFormItem>

        <NFormItem

          v-if="state.formModel.output_storage === 'INLINE'"

          label="期望输出"

          path="output_text"

        >

          <NInput

            v-model:value="state.formModel.output_text"

            type="textarea"

            :autosize="{ minRows: 4, maxRows: 16 }"

            placeholder="期望 stdout"

          />

        </NFormItem>

        <NFormItem

          v-else

          label="输出文件"

          path="output_object_key"

        >

          <FileUpload

            :key="`output-${state.uploadResetKey}`"

            v-model:value="state.formModel.output_object_key"

            mode="upload"

            upload-variant="dragger"

            value-type="object_name"

            accept=".out,.txt,text/*"

            button-text="上传期望输出文件（.out / .txt）"

          />

        </NFormItem>

      </NForm>

    </NSpin>



    <template #action>

      <NSpace justify="end">

        <NButton @click="closeModal">

          取消

        </NButton>

        <NButton

          type="primary"

          :loading="state.submitLoading"

          @click="submitForm"

        >

          确认

        </NButton>

      </NSpace>

    </template>

  </HeiFormContainer>

</template>


