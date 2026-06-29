<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { resourceApi, resourceModuleApi } from '@/api'
import { createRequiredRule, toNullableString } from '@/utils'
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const emit = defineEmits<{
  saved: []
}>()

const { t } = useI18n()
const formRef = ref<FormInst | null>(null)
const defaultFormData = {
  code: '',
  name: '',
  resource_type: '',
  parent_id: '',
  module_id: '',
  path: '',
  component: '',
  redirect: '',
  icon: '',
  href: '',
  sort: 0,
  is_visible: true,
  is_cache: false,
  is_affix: false,
  status: 'ENABLED',
  description: '',
  extra: {},
}
const state = reactive({
  showModal: false,
  loading: false,
  treeLoading: false,
  submitLoading: false,
  dataId: null as string | null,
  formModel: { ...defaultFormData },
  resourceTree: [] as any[],
  moduleOptions: [] as any[],
})

const modalTitle = computed(() =>
  state.dataId ? t('pages.iam.resource.editResource') : t('pages.iam.resource.addResource'),
)

const rules = computed<FormRules>(() => ({
  code: createRequiredRule(t, t('pages.iam.resource.code'), 'input'),
  name: createRequiredRule(t, t('pages.iam.resource.name'), 'input'),
  resource_type: createRequiredRule(t, t('pages.iam.resource.resourceType'), 'change'),
  module_id: createRequiredRule(t, t('pages.iam.resource.module'), 'change'),
  status: createRequiredRule(t, t('common.often.status'), 'change'),
}))

const parentTreeOptions = computed(() => {
  const excludedIds = new Set(
    state.dataId ? collectDescendantIds(state.resourceTree, state.dataId) : [],
  )
  return buildParentTreeOptions(state.resourceTree, excludedIds)
})

async function openModal(id?: string, parentId?: string) {
  state.dataId = id ?? null
  state.formModel = { ...defaultFormData, parent_id: parentId ?? '' }
  state.showModal = true
  await Promise.all([fetchResourceTree(), fetchModules()])

  if (id) {
    await fetchDetail(id)
  } else if (parentId) {
    const parent = findResourceNode(state.resourceTree, parentId)
    state.formModel.module_id = parent?.module_id ?? ''
  }
}

async function fetchResourceTree() {
  state.treeLoading = true
  try {
    const response = await resourceApi.tree()
    state.resourceTree = response.data ?? []
  } finally {
    state.treeLoading = false
  }
}

async function fetchModules() {
  const response = await resourceModuleApi.selector()
  state.moduleOptions = (response.data ?? []).map((item: any) => ({
    label: item.name,
    value: item.id,
  }))
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await resourceApi.detail({ id })
    state.formModel = Object.assign({}, defaultFormData, response.data, {
      parent_id: response.data?.parent_id ?? '',
      module_id: response.data?.module_id ?? '',
      path: response.data?.path ?? '',
      component: response.data?.component ?? '',
      redirect: response.data?.redirect ?? '',
      icon: response.data?.icon ?? '',
      href: response.data?.href ?? '',
      description: response.data?.description ?? '',
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
      code: state.formModel.code.trim(),
      name: state.formModel.name.trim(),
      parent_id: toNullableString(state.formModel.parent_id),
      module_id: toNullableString(state.formModel.module_id),
      path: toNullableString(state.formModel.path),
      component: toNullableString(state.formModel.component),
      redirect: toNullableString(state.formModel.redirect),
      icon: toNullableString(state.formModel.icon),
      href: toNullableString(state.formModel.href),
      sort: Number(state.formModel.sort ?? 0),
      is_visible: Boolean(state.formModel.is_visible),
      is_cache: Boolean(state.formModel.is_cache),
      is_affix: Boolean(state.formModel.is_affix),
      description: toNullableString(state.formModel.description),
      extra: state.formModel.extra ?? {},
    }

    if (state.dataId) {
      await resourceApi.update({
        ...payload,
        id: state.dataId,
      })
      window.$message.success(t('common.often.updateSuccess'))
    } else {
      await resourceApi.create(payload)
      window.$message.success(t('common.often.createSuccess'))
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

function buildParentTreeOptions(items: any[], excludedIds: Set<string>): any[] {
  return items
    .filter((item) => !excludedIds.has(item.id))
    .map((item) => ({
      id: item.id,
      name: `${item.name} (${item.code})`,
      children: buildParentTreeOptions(item.children ?? [], excludedIds),
    }))
}

function collectDescendantIds(items: any[], targetId: string) {
  const result = new Set<string>([targetId])
  const target = findResourceNode(items, targetId)
  const walk = (nodes: any[]) => {
    nodes.forEach((node) => {
      result.add(node.id)
      walk(node.children ?? [])
    })
  }
  if (target) {
    walk(target.children ?? [])
  }
  return Array.from(result)
}

function findResourceNode(items: any[], id: string): any | null {
  for (const item of items) {
    if (item.id === id) {
      return item
    }
    const child = findResourceNode(item.children ?? [], id)
    if (child) {
      return child
    }
  }
  return null
}
</script>

<template>
  <NModal
    v-model:show="state.showModal"
    preset="card"
    draggable
    :mask-closable="false"
    :title="modalTitle"
    style="width: 760px"
    :segmented="{ content: true, action: true }"
  >
    <NSpin :show="state.loading || state.treeLoading">
      <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
        <NForm
          ref="formRef"
          :model="state.formModel"
          :rules="rules"
          label-placement="left"
          label-width="110"
          :disabled="state.loading || state.treeLoading || state.submitLoading"
        >
          <NFormItem :label="t('pages.iam.resource.name')" path="name">
            <NInput v-model:value="state.formModel.name" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource.code')" path="code">
            <NInput v-model:value="state.formModel.code" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource.resourceType')" path="resource_type">
            <DictSelect v-model="state.formModel.resource_type" dict-code="RESOURCE_TYPE" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource.parentId')" path="parent_id">
            <NTreeSelect
              v-model:value="state.formModel.parent_id"
              clearable
              filterable
              :options="parentTreeOptions"
              :placeholder="t('pages.iam.resource.parentId')"
              key-field="id"
              label-field="name"
              children-field="children"
            />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource.module')" path="module_id">
            <NSelect
              v-model:value="state.formModel.module_id"
              filterable
              clearable
              :options="state.moduleOptions"
            />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource.path')" path="path">
            <NInput v-model:value="state.formModel.path" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource.component')" path="component">
            <NInput v-model:value="state.formModel.component" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource.redirect')" path="redirect">
            <NInput v-model:value="state.formModel.redirect" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource.icon')" path="icon">
            <NInput v-model:value="state.formModel.icon" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource.href')" path="href">
            <NInput v-model:value="state.formModel.href" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource.sort')" path="sort">
            <NInputNumber v-model:value="state.formModel.sort" class="w-full" :min="0" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource.isVisible')" path="is_visible">
            <NSwitch v-model:value="state.formModel.is_visible" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource.isCache')" path="is_cache">
            <NSwitch v-model:value="state.formModel.is_cache" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource.isAffix')" path="is_affix">
            <NSwitch v-model:value="state.formModel.is_affix" />
          </NFormItem>
          <NFormItem :label="t('common.often.status')" path="status">
            <DictSelect v-model="state.formModel.status" dict-code="COMMON_STATUS" type="radio" />
          </NFormItem>
          <NFormItem :label="t('pages.iam.resource.description')" path="description">
            <NInput
              v-model:value="state.formModel.description"
              type="textarea"
              :autosize="{ minRows: 3, maxRows: 5 }"
            />
          </NFormItem>
        </NForm>
      </NScrollbar>
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
