<script setup lang="ts">
import { accountApi } from '@/api'
import { createTagColor, displayValue } from '@/utils'
import { computed, reactive } from 'vue'
import { dictTypeData, dictTypeColor } from '@/utils/dict'
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
const avatarImgProps = { referrerPolicy: 'no-referrer' } as any

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
    :title="t('resource.iam.account.detail_account')"
    style="width: 680px"
  >
    <NScrollbar class="max-h-[min(640px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NTabs type="line" animated>
          <NTabPane name="account" :tab="t('resource.iam.account.account_info')">
            <NDescriptions label-placement="left" bordered :column="1">
              <NDescriptionsItem :label="t('resource.iam.account.id')">
                {{ displayValue(state.account.id) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.account_type')">
                <NTag
                  :color="createTagColor(dictTypeColor('ACCOUNT_TYPE', state.account.account_type))"
                  :bordered="false"
                >
                  {{ dictTypeData('ACCOUNT_TYPE', state.account.account_type) }}
                </NTag>
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.account_status')">
                <NTag
                  :color="
                    createTagColor(dictTypeColor('ACCOUNT_STATUS', state.account.account_status))
                  "
                  :bordered="false"
                >
                  {{ dictTypeData('ACCOUNT_STATUS', state.account.account_status) }}
                </NTag>
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.cancelled_at')">
                {{ displayValue(state.account.cancelled_at) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.cancelled_by')">
                {{ displayValue(state.account.cancelled_by) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.cancel_reason')">
                {{ displayValue(state.account.cancel_reason) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.last_login_ip')">
                {{ displayValue(state.account.last_login_ip) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.last_login_address')">
                {{ displayValue(state.account.last_login_address) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.last_login_time')">
                {{ displayValue(state.account.last_login_time) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.last_login_device')">
                {{ displayValue(state.account.last_login_device) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.latest_login_ip')">
                {{ displayValue(state.account.latest_login_ip) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.latest_login_address')">
                {{ displayValue(state.account.latest_login_address) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.latest_login_time')">
                {{ displayValue(state.account.latest_login_time) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.latest_login_device')">
                {{ displayValue(state.account.latest_login_device) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('common.often.created_at')">
                {{ displayValue(state.account.created_at) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('common.often.updated_at')">
                {{ displayValue(state.account.updated_at) }}
              </NDescriptionsItem>
            </NDescriptions>
          </NTabPane>

          <NTabPane name="identity" :tab="t('resource.iam.account.login_identity')">
            <NDescriptions label-placement="left" bordered :column="1">
              <NDescriptionsItem :label="t('resource.iam.account.account')">
                {{ displayValue(state.account.account) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.email_identity')">
                {{ displayValue(state.account.email_identity) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.email_identity_verified')">
                {{
                  state.account.email_identity_verified
                    ? t('resource.iam.account.yes')
                    : t('resource.iam.account.no')
                }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.email_identity_bind_status')">
                {{
                  dictTypeData(
                    'ACCOUNT_IDENTITY_BIND_STATUS',
                    state.account.email_identity_bind_status,
                  ) || displayValue(state.account.email_identity_bind_status)
                }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.phone_identity')">
                {{ displayValue(state.account.phone_identity) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.phone_identity_verified')">
                {{
                  state.account.phone_identity_verified
                    ? t('resource.iam.account.yes')
                    : t('resource.iam.account.no')
                }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.phone_identity_bind_status')">
                {{
                  dictTypeData(
                    'ACCOUNT_IDENTITY_BIND_STATUS',
                    state.account.phone_identity_bind_status,
                  ) || displayValue(state.account.phone_identity_bind_status)
                }}
              </NDescriptionsItem>
            </NDescriptions>
          </NTabPane>

          <NTabPane name="profile" :tab="t('resource.iam.account.profile_info')">
            <NDescriptions label-placement="left" bordered :column="1">
              <NDescriptionsItem :label="t('resource.iam.account.name')">
                {{ displayValue(state.account.name) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.nickname')">
                {{ displayValue(state.account.nickname) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.avatar')">
                <NAvatar
                  v-if="state.account.avatar"
                  :src="state.account.avatar"
                  :alt="avatarAlt"
                  :img-props="avatarImgProps"
                />
                <template v-else> - </template>
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.signature')">
                {{ displayValue(state.account.signature) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.phone')">
                {{ displayValue(state.account.phone) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.email')">
                {{ displayValue(state.account.email) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.employee_no')">
                {{ displayValue(state.account.employee_no) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.title_name')">
                {{ displayValue(state.account.title) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="t('resource.iam.account.remark')">
                {{ displayValue(state.account.remark) }}
              </NDescriptionsItem>
            </NDescriptions>
          </NTabPane>
        </NTabs>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
