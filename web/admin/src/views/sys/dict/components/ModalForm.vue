<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { dictApi } from '@/api'
import CommonColorPicker from '@/components/common/CommonColorPicker.vue'
import { createRequiredRule, isHexColor, toNullableString } from '@/utils'
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  dicts: any[]
}>()

const emit = defineEmits<{
  saved: []
}>()

const { t } = useI18n()
const formRef = ref<FormInst | null>(null)
const defaultFormData = {
  code: '',
  label: '',
  value: '',
  color: '',
  category: 'SYS',
  parent_id: null as string | null,
  status: 'ENABLED',
  sort: 99,
}
const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  formModel: { ...defaultFormData },
})

const modalTitle = computed(() =>
  state.dataId ? t('pages.sys.dict.editDict') : t('pages.sys.dict.addDict'),
)
const parentTreeOptions = computed(() =>
  buildTreeOptions(
    props.dicts.filter((item) => item.category === state.formModel.category),
    state.dataId,
  ),
)

const rules = computed<FormRules>(() => ({
  code: [
    createRequiredRule(t, t('pages.sys.dict.code'), 'input'),
    {
      pattern: /^[A-Z0-9_]+$/,
      message: t('pages.sys.dict.codePattern'),
      trigger: ['input', 'blur'],
    },
  ],
  label: createRequiredRule(t, t('pages.sys.dict.label'), 'input'),
  color: [
    {
      validator: (_rule, value) => isHexColor(value),
      message: t('pages.sys.dict.colorPattern'),
      trigger: ['change', 'blur'],
    },
  ],
  category: createRequiredRule(t, t('pages.sys.dict.category'), 'change'),
  status: createRequiredRule(t, t('common.often.status'), 'change'),
}))

async function openModal(id?: string, options?: { category?: string; parentId?: string | null }) {
  state.dataId = id ?? null
  state.formModel = {
    ...defaultFormData,
    category: options?.category ?? 'SYS',
    parent_id: options?.parentId ?? null,
  }
  state.showModal = true

  if (id) {
    await fetchDetail(id)
  }
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await dictApi.detail({ id })
    const data = response.data ?? {}
    state.formModel = Object.assign({}, defaultFormData, data, {
      value: data.value ?? '',
      color: data.color ?? '',
      category: data.category ?? 'SYS',
      parent_id: data.parent_id ?? null,
      status: data.status ?? 'ENABLED',
      sort: data.sort ?? 0,
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
  const payload = {
    ...state.formModel,
    code: state.formModel.code.trim().toUpperCase(),
    label: String(state.formModel.label ?? '').trim(),
    value: toNullableString(state.formModel.value),
    color: toNullableString(state.formModel.color),
    parent_id: state.formModel.parent_id ?? null,
    sort: Number(state.formModel.sort ?? 0),
  }

  state.submitLoading = true
  try {
    if (state.dataId) {
      await dictApi.update({
        ...payload,
        id: state.dataId,
      })
      window.$message.success(t('common.often.updateSuccess'))
    } else {
      await dictApi.create(payload)
      window.$message.success(t('common.often.createSuccess'))
    }

    emit('saved')
    closeModal()
  } finally {
    state.submitLoading = false
  }
}

function updateCode(value: string) {
  state.formModel.code = value.toUpperCase()
}

function updateCategory(value: string) {
  state.formModel.category = value
  state.formModel.parent_id = null
}

function buildTreeOptions(items: any[], excludeId?: string | null) {
  const excludeIds = excludeId ? collectChildIds(items, excludeId) : new Set<string>()
  if (excludeId) {
    excludeIds.add(excludeId)
  }

  const nodeMap = new Map(
    items
      .filter((item) => !excludeIds.has(item.id))
      .map((item) => [
        item.id,
        {
          key: item.id,
          label: item.label ? `${item.label} (${item.code})` : item.code,
          children: [] as any[],
          raw: item,
        },
      ]),
  )
  const roots: any[] = []

  nodeMap.forEach((node) => {
    const parentId = node.raw.parent_id
    const parent = parentId ? nodeMap.get(parentId) : null
    if (parent) {
      parent.children.push(node)
    } else {
      roots.push(node)
    }
  })

  return normalizeTreeOptions(roots)
}

function collectChildIds(items: any[], parentId: string) {
  const result = new Set<string>()
  const walk = (id: string) => {
    items
      .filter((item) => item.parent_id === id)
      .forEach((item) => {
        result.add(item.id)
        walk(item.id)
      })
  }
  walk(parentId)
  return result
}

function sortTreeOptions(a: any, b: any) {
  return (a.raw?.sort ?? 0) - (b.raw?.sort ?? 0)
}

function normalizeTreeOptions(nodes: any[]): any[] {
  return nodes.sort(sortTreeOptions).map((node) => ({
    key: node.key,
    label: node.label,
    children: normalizeTreeOptions(node.children),
  }))
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
    style="width: 640px"
    :segmented="{ content: true, action: true }"
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
        <NFormItem :label="t('pages.sys.dict.category')" path="category">
          <DictSelect
            v-model="state.formModel.category"
            dict-code="DICT_CATEGORY"
            type="radio"
            @change="updateCategory"
          />
        </NFormItem>
        <NFormItem :label="t('pages.sys.dict.parent')" path="parent_id">
          <NTreeSelect
            v-model:value="state.formModel.parent_id"
            clearable
            filterable
            :options="parentTreeOptions"
            :placeholder="t('pages.sys.dict.topLevel')"
            key-field="key"
            label-field="label"
          />
        </NFormItem>
        <NFormItem :label="t('pages.sys.dict.code')" path="code">
          <NInput :value="state.formModel.code" @update:value="updateCode" />
        </NFormItem>
        <NFormItem :label="t('pages.sys.dict.label')" path="label">
          <NInput v-model:value="state.formModel.label" />
        </NFormItem>
        <NFormItem :label="t('pages.sys.dict.value')" path="value">
          <NInput v-model:value="state.formModel.value" />
        </NFormItem>
        <NFormItem :label="t('pages.sys.dict.color')" path="color">
          <CommonColorPicker
            v-model="state.formModel.color"
            :disabled="state.loading || state.submitLoading"
          />
        </NFormItem>
        <NFormItem :label="t('pages.sys.dict.sort')" path="sort">
          <NInputNumber v-model:value="state.formModel.sort" class="w-full" :min="0" />
        </NFormItem>
        <NFormItem :label="t('common.often.status')" path="status">
          <DictSelect v-model="state.formModel.status" dict-code="COMMON_STATUS" type="radio" />
        </NFormItem>
      </NForm>
    </NSpin>

    <template #action>
      <NSpace justify="end" align="center">
        <NButton @click="closeModal">
          {{ t('common.cancel') }}
        </NButton>
        <NButton type="primary" :loading="state.submitLoading" @click="submitForm">
          {{ t('common.confirm') }}
        </NButton>
      </NSpace>
    </template>
  </NModal>
</template>
