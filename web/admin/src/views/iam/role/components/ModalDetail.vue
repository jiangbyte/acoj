<script setup lang="ts">
import { roleApi } from '@/api'
import { createTagColor, displayValue } from '@/utils'
import { reactive } from 'vue'
import { dictTypeData, dictTypeColor } from '@/utils/dict'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const state = reactive({
  showModal: false,
  loading: false,
  role: {} as any,
})

async function openModal(id: string) {
  state.role = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await roleApi.detail({ id })
    state.role = response.data ?? {}
  } finally {
    state.loading = false
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
    :title="t('pages.iam.role.detailRole')"
    style="width: 680px"
  >
    <NScrollbar class="max-h-[min(640px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="t('pages.iam.role.id')">
            {{ displayValue(state.role.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.role.code')">
            {{ displayValue(state.role.code) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.role.name')">
            {{ displayValue(state.role.name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.role.category')">
            {{
              dictTypeData('SYS_BIZ_CATEGORY', state.role.category) ||
              displayValue(state.role.category)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.role.scopeType')">
            {{
              dictTypeData('ROLE_SCOPE_TYPE', state.role.scope_type) ||
              displayValue(state.role.scope_type)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.role.ownerDeptId')">
            {{ displayValue(state.role.owner_dept_id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.role.sort')">
            {{ displayValue(state.role.sort) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.role.isBuiltin')">
            {{ state.role.is_builtin ? t('pages.iam.role.yes') : t('pages.iam.role.no') }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.status')">
            <NTag
              :color="createTagColor(dictTypeColor('COMMON_STATUS', state.role.status))"
              :bordered="false"
            >
              {{
                dictTypeData('COMMON_STATUS', state.role.status) || displayValue(state.role.status)
              }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.role.description')">
            {{ displayValue(state.role.description) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.createdAt')">
            {{ displayValue(state.role.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.createdBy')">
            {{ displayValue(state.role.created_by) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updatedAt')">
            {{ displayValue(state.role.updated_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updatedBy')">
            {{ displayValue(state.role.updated_by) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
