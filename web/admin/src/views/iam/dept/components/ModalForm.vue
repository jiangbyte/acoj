<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { deptApi } from '@/api'
import { createRequiredRule, toNullableString } from '@/utils'
import { computed, reactive, ref } from 'vue'

const emit = defineEmits<{
  saved: []
}>()

const formRef = ref<FormInst | null>(null)
const defaultFormData = {
  name: '',
  code: '',
  category: null as string | null,
  parent_id: '',
  master_id: '',
  deputy_master_id: '',
  sort: 0,
  is_virtual: false,
  status: 'ENABLED',
  extra: {},
}
const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  formModel: { ...defaultFormData },
})

const modalTitle = computed(() =>
  state.dataId ? '编辑 部门' : '新增 部门',
)

const rules = computed<FormRules>(() => ({
  name: createRequiredRule('部门名称', 'input'),
  code: createRequiredRule('部门编码', 'input'),
  category: createRequiredRule('部门分类', 'change'),
  status: createRequiredRule('状态', 'change'),
}))

async function openModal(id?: string) {
  state.dataId = id ?? null
  state.formModel = { ...defaultFormData }
  state.showModal = true

  if (id) {
    await fetchDetail(id)
  }
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await deptApi.detail({ id })
    state.formModel = Object.assign({}, defaultFormData, response.data, {
      parent_id: response.data?.parent_id ?? '',
      master_id: response.data?.master_id ?? '',
      deputy_master_id: response.data?.deputy_master_id ?? '',
      extra: response.data?.extra ?? {},
    })
  } finally {
    state.loading = false
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
    const payload = {
      ...state.formModel,
      name: state.formModel.name.trim(),
      code: state.formModel.code.trim(),
      parent_id: toNullableString(state.formModel.parent_id),
      master_id: toNullableString(state.formModel.master_id),
      deputy_master_id: toNullableString(state.formModel.deputy_master_id),
      sort: Number(state.formModel.sort ?? 0),
      is_virtual: Boolean(state.formModel.is_virtual),
      extra: state.formModel.extra ?? {},
    }

    if (state.dataId) {
      await deptApi.update({
        ...payload,
        id: state.dataId,
      })
      window.$message.success('更新成功')
    } else {
      await deptApi.create(payload)
      window.$message.success('创建成功')
    }

    closeModal()
    emit('saved')
  } finally {
    state.submitLoading = false
  }
}

defineExpose({
  openModal,
})
</script>

<template>
  <NModal
    v-model:show="state.showModal"
    preset="card"
    draggable
    :mask-closable="false"
    :title="modalTitle"
    style="width: 720px"
    :segmented="{ content: true, action: true }"
  >
    <NSpin :show="state.loading">
      <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
        <NForm
          ref="formRef"
          :model="state.formModel"
          :rules="rules"
          label-placement="left"
          label-width="110"
          :disabled="state.loading || state.submitLoading"
        >
          <NFormItem :label="'部门名称'" path="name">
            <NInput v-model:value="state.formModel.name" />
          </NFormItem>
          <NFormItem :label="'部门编码'" path="code">
            <NInput v-model:value="state.formModel.code" />
          </NFormItem>
          <NFormItem :label="'部门分类'" path="category">
            <DictSelect v-model="state.formModel.category" dict-code="DEPT_CATEGORY" />
          </NFormItem>
          <NFormItem :label="'父级部门ID'" path="parent_id">
            <NInput v-model:value="state.formModel.parent_id" />
          </NFormItem>
          <NFormItem :label="'负责人ID'" path="master_id">
            <NInput v-model:value="state.formModel.master_id" />
          </NFormItem>
          <NFormItem :label="'副负责人ID'" path="deputy_master_id">
            <NInput v-model:value="state.formModel.deputy_master_id" />
          </NFormItem>
          <NFormItem :label="'排序'" path="sort">
            <NInputNumber v-model:value="state.formModel.sort" class="w-full" :min="0" />
          </NFormItem>
          <NFormItem :label="'虚拟部门'" path="is_virtual">
            <NSwitch v-model:value="state.formModel.is_virtual" />
          </NFormItem>
          <NFormItem :label="'状态'" path="status">
            <DictSelect v-model="state.formModel.status" dict-code="COMMON_STATUS" type="radio" />
          </NFormItem>
        </NForm>
      </NScrollbar>
    </NSpin>

    <template #action>
      <NSpace justify="end" align="center">
        <NButton @click="closeModal">
          取消
        </NButton>
        <NButton type="primary" :loading="state.submitLoading" @click="submitForm">
          确认
        </NButton>
      </NSpace>
    </template>
  </NModal>
</template>
