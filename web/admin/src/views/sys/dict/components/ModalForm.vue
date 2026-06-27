<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import type { DictCategory, DictFormModel, SysDict } from '../types'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  categoryOptions,
  colorOptions,
  createEmptyDictForm,
  statusOptions,
} from '../constants'

const props = defineProps<{
  dicts: SysDict[]
}>()

const emit = defineEmits<{
  saved: [values: DictFormModel]
}>()

const { t } = useI18n()
const formRef = ref<FormInst | null>(null)
const showModal = ref(false)
const submitLoading = ref(false)
const dictId = ref<string | null>(null)
const formModel = ref<DictFormModel>(createEmptyDictForm('SYS'))

const isEdit = computed(() => Boolean(dictId.value))
const modalTitle = computed(() =>
  isEdit.value ? t('pages.sys.dict.editDict') : t('pages.sys.dict.addDict'),
)
const categorySelectOptions = computed(() => translateOptions(categoryOptions))
const statusSelectOptions = computed(() => translateOptions(statusOptions))
const colorSelectOptions = computed(() =>
  colorOptions.map((value) => ({
    label: value,
    value,
  })),
)
const parentTreeOptions = computed(() =>
  buildTreeOptions(
    props.dicts.filter((item) => item.category === formModel.value.category),
    dictId.value,
  ),
)

const rules = computed<FormRules>(() => ({
  code: [
    createRequiredRule(t('pages.sys.dict.code'), 'input'),
    {
      pattern: /^[A-Z0-9_]+$/,
      message: t('pages.sys.dict.codePattern'),
      trigger: ['input', 'blur'],
    },
  ],
  label: createRequiredRule(t('pages.sys.dict.label'), 'input'),
  category: createRequiredRule(t('pages.sys.dict.category'), 'change'),
  status: createRequiredRule(t('common.often.status'), 'change'),
}))

function openModal(id?: string, options?: { category?: DictCategory; parentId?: string | null }) {
  dictId.value = id ?? null

  if (id) {
    const current = props.dicts.find((item) => item.id === id)
    formModel.value = current ? toFormModel(current) : createEmptyDictForm(options?.category ?? 'SYS')
  } else {
    formModel.value = createEmptyDictForm(options?.category ?? 'SYS', options?.parentId)
  }

  showModal.value = true
}

function closeModal() {
  showModal.value = false
  submitLoading.value = false
}

async function submitForm() {
  await formRef.value?.validate()
  const payload = normalizePayload(formModel.value)
  if (isDuplicateCode(payload)) {
    window.$message.error(t('pages.sys.dict.codeExists'))
    return
  }

  submitLoading.value = true
  try {
    emit('saved', payload)
    closeModal()
  } finally {
    submitLoading.value = false
  }
}

function updateCode(value: string) {
  formModel.value.code = value.toUpperCase()
}

function updateCategory(value: DictCategory) {
  formModel.value.category = value
  formModel.value.parent_id = null
}

function toFormModel(data: SysDict): DictFormModel {
  return {
    id: data.id,
    code: data.code,
    label: data.label ?? '',
    value: data.value ?? '',
    color: data.color ?? 'default',
    category: data.category ?? 'SYS',
    parent_id: data.parent_id ?? null,
    status: data.status,
    sort: data.sort ?? 0,
  }
}

function normalizePayload(values: DictFormModel): DictFormModel {
  return {
    id: values.id,
    code: values.code.trim().toUpperCase(),
    label: toText(values.label),
    value: toText(values.value),
    color: values.color,
    category: values.category,
    parent_id: values.parent_id ?? null,
    status: values.status,
    sort: Number(values.sort ?? 0),
  }
}

function isDuplicateCode(values: DictFormModel) {
  return props.dicts.some((item) => item.code === values.code && item.id !== values.id)
}

function toText(value: unknown) {
  return String(value ?? '').trim()
}

function translateOptions(options: Array<{ labelKey: string; value: string }>) {
  return options.map((item) => ({
    label: t(item.labelKey),
    value: item.value,
  }))
}

function createRequiredRule(field: string, trigger: 'input' | 'change') {
  return {
    required: true,
    message: t('pages.sys.dict.required', { field }),
    trigger,
  }
}

function buildTreeOptions(items: SysDict[], excludeId?: string | null) {
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

  return roots
    .sort(sortTreeOptions)
    .map((node) => ({
      ...node,
      children: node.children.sort(sortTreeOptions),
      raw: undefined,
    }))
}

function collectChildIds(items: SysDict[], parentId: string) {
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

defineExpose({
  openModal,
})
</script>

<template>
  <NModal
    v-model:show="showModal"
    preset="card"
    draggable
    :mask-closable="false"
    :title="modalTitle"
    style="width: 640px"
    :segmented="{ content: true, action: true }"
  >
    <NForm
      ref="formRef"
      :model="formModel"
      :rules="rules"
      label-placement="left"
      label-width="100"
      :disabled="submitLoading"
    >
      <NFormItem :label="t('pages.sys.dict.category')" path="category">
        <NRadioGroup
          :value="formModel.category"
          :options="categorySelectOptions"
          @update:value="updateCategory"
        />
      </NFormItem>
      <NFormItem :label="t('pages.sys.dict.parent')" path="parent_id">
        <NTreeSelect
          v-model:value="formModel.parent_id"
          clearable
          filterable
          :options="parentTreeOptions"
          :placeholder="t('pages.sys.dict.topLevel')"
          key-field="key"
          label-field="label"
        />
      </NFormItem>
      <NFormItem :label="t('pages.sys.dict.code')" path="code">
        <NInput :value="formModel.code" @update:value="updateCode" />
      </NFormItem>
      <NFormItem :label="t('pages.sys.dict.label')" path="label">
        <NInput v-model:value="formModel.label" />
      </NFormItem>
      <NFormItem :label="t('pages.sys.dict.value')" path="value">
        <NInput v-model:value="formModel.value" />
      </NFormItem>
      <NFormItem :label="t('pages.sys.dict.color')" path="color">
        <NSelect v-model:value="formModel.color" clearable :options="colorSelectOptions" />
      </NFormItem>
      <NFormItem :label="t('pages.sys.dict.sort')" path="sort">
        <NInputNumber v-model:value="formModel.sort" class="w-full" :min="0" />
      </NFormItem>
      <NFormItem :label="t('common.often.status')" path="status">
        <NRadioGroup v-model:value="formModel.status" :options="statusSelectOptions" />
      </NFormItem>
    </NForm>

    <template #action>
      <NSpace justify="end" align="center">
        <NButton @click="closeModal">
          {{ t('common.cancel') }}
        </NButton>
        <NButton type="primary" :loading="submitLoading" @click="submitForm">
          {{ t('common.confirm') }}
        </NButton>
      </NSpace>
    </template>
  </NModal>
</template>
