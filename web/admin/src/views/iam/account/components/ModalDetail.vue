<script setup lang="ts">
import { accountApi } from '@/api'
import { createTagColor, displayValue } from '@/utils'
import { dictTypeColor, dictTypeData } from '@/utils/dict'
import { computed, reactive } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const state = reactive({
  showModal: false,
  loading: false,
  account: {} as any,
})

const avatarAlt = computed(
  () => state.account?.nickname || state.account?.name || state.account?.account,
)

async function openModal(id: string) {
  state.account = {}
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await accountApi.detail({ id })
    state.account = response.data ?? {}
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
    :title="t('pages.iam.account.detailAccount')"
    style="width: 680px"
  >
    <NScrollbar class="max-h-[min(640px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NDescriptions label-placement="left" bordered :column="1">
          <NDescriptionsItem :label="t('pages.iam.account.id')">
            {{ displayValue(state.account.id) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.account')">
            {{ displayValue(state.account.account) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.accountType')">
            <NTag
              :color="createTagColor(dictTypeColor('ACCOUNT_TYPE', state.account.account_type))"
              :bordered="false"
            >
              {{ dictTypeData('ACCOUNT_TYPE', state.account.account_type) }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.accountStatus')">
            <NTag
              :color="createTagColor(dictTypeColor('ACCOUNT_STATUS', state.account.account_status))"
              :bordered="false"
            >
              {{ dictTypeData('ACCOUNT_STATUS', state.account.account_status) }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.name')">
            {{ displayValue(state.account.name) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.nickname')">
            {{ displayValue(state.account.nickname) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.avatar')">
            <NAvatar v-if="state.account.avatar" :src="state.account.avatar" :alt="avatarAlt" />
            <template v-else> - </template>
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.signature')">
            {{ displayValue(state.account.signature) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.phone')">
            {{ displayValue(state.account.phone) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.email')">
            {{ displayValue(state.account.email) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.isSuperuser')">
            {{
              state.account.is_superuser ? t('pages.iam.account.yes') : t('pages.iam.account.no')
            }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.cancelledAt')">
            {{ displayValue(state.account.cancelled_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.cancelledBy')">
            {{ displayValue(state.account.cancelled_by) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.cancelReason')">
            {{ displayValue(state.account.cancel_reason) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.lastLoginIp')">
            {{ displayValue(state.account.last_login_ip) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.lastLoginAddress')">
            {{ displayValue(state.account.last_login_address) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.lastLoginTime')">
            {{ displayValue(state.account.last_login_time) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.lastLoginDevice')">
            {{ displayValue(state.account.last_login_device) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.latestLoginIp')">
            {{ displayValue(state.account.latest_login_ip) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.latestLoginAddress')">
            {{ displayValue(state.account.latest_login_address) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.latestLoginTime')">
            {{ displayValue(state.account.latest_login_time) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('pages.iam.account.latestLoginDevice')">
            {{ displayValue(state.account.latest_login_device) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.createdAt')">
            {{ displayValue(state.account.created_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.createdBy')">
            {{ displayValue(state.account.created_by) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updatedAt')">
            {{ displayValue(state.account.updated_at) }}
          </NDescriptionsItem>
          <NDescriptionsItem :label="t('common.often.updatedBy')">
            {{ displayValue(state.account.updated_by) }}
          </NDescriptionsItem>
        </NDescriptions>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
