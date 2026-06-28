<script setup lang="ts">
import { deptApi } from '@/api'
import { createTagColor, displayValue } from '@/utils'
import { reactive } from 'vue'
import { dictTypeData, dictTypeColor } from '@/utils/dict'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const state = reactive({
  showModal: false,
  loading: false,
  dept: {} as any,
})

async function openModal(id: string) {
  state.dept = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await deptApi.detail({ id })
    state.dept = response.data ?? {}
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
    :title="t('pages.iam.dept.detailDept')"
    style="width: 680px"
  >
    <NScrollbar class="max-h-[min(640px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="t('pages.iam.dept.id')">
            {{ displayValue(state.dept.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.dept.name')">
            {{ displayValue(state.dept.name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.dept.code')">
            {{ displayValue(state.dept.code) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.dept.category')">
            {{
              dictTypeData('DEPT_CATEGORY', state.dept.category) ||
              displayValue(state.dept.category)
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.dept.parentId')">
            {{ displayValue(state.dept.parent_id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.dept.masterId')">
            {{ displayValue(state.dept.master_id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.dept.deputyMasterId')">
            {{ displayValue(state.dept.deputy_master_id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.dept.sort')">
            {{ displayValue(state.dept.sort) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.dept.isVirtual')">
            {{ state.dept.is_virtual ? t('pages.iam.dept.yes') : t('pages.iam.dept.no') }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.status')">
            <NTag
              :color="createTagColor(dictTypeColor('COMMON_STATUS', state.dept.status))"
              :bordered="false"
            >
              {{
                dictTypeData('COMMON_STATUS', state.dept.status) || displayValue(state.dept.status)
              }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.createdAt')">
            {{ displayValue(state.dept.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.createdBy')">
            {{ displayValue(state.dept.created_by) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updatedAt')">
            {{ displayValue(state.dept.updated_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updatedBy')">
            {{ displayValue(state.dept.updated_by) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
