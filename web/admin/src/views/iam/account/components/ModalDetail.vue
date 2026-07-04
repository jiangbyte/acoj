<script setup lang="ts">
import { accountApi } from '@/api'
import { createTagColor, displayValue, resolveFileUrl } from '@/utils'
import { computed, reactive } from 'vue'
import { dictTypeData, dictTypeColor } from '@/utils/dict'

const state = reactive({
  showModal: false,
  loading: false,
  account: {} as any,
})

const avatarAlt = computed(
  () => state.account?.nickname || 'Account avatar',
)
const avatarUrl = computed(() => resolveFileUrl(state.account?.avatar))
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
    :title="'Account Detail'"
    style="width: 680px"
  >
    <NScrollbar class="max-h-[min(640px,calc(100vh-300px))] pr-16px">
      <NSpin :show="state.loading">
        <NTabs type="line" animated>
          <NTabPane name="account" :tab="'Account Info'">
            <NDescriptions label-placement="left" bordered :column="1">
              <NDescriptionsItem :label="'Account ID'">
                {{ displayValue(state.account.id) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Account Type'">
                <NTag
                  :color="createTagColor(dictTypeColor('ACCOUNT_TYPE', state.account.account_type))"
                  :bordered="false"
                >
                  {{ dictTypeData('ACCOUNT_TYPE', state.account.account_type) }}
                </NTag>
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Account Status'">
                <NTag
                  :color="
                    createTagColor(dictTypeColor('ACCOUNT_STATUS', state.account.account_status))
                  "
                  :bordered="false"
                >
                  {{ dictTypeData('ACCOUNT_STATUS', state.account.account_status) }}
                </NTag>
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Cancelled At'">
                {{ displayValue(state.account.cancelled_at) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Cancelled By'">
                {{ displayValue(state.account.cancelled_by) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Cancel Reason'">
                {{ displayValue(state.account.cancel_reason) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Last Login IP'">
                {{ displayValue(state.account.last_login_ip) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Last Login Address'">
                {{ displayValue(state.account.last_login_address) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Last Login Time'">
                {{ displayValue(state.account.last_login_time) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Last Login Device'">
                {{ displayValue(state.account.last_login_device) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Latest Login IP'">
                {{ displayValue(state.account.latest_login_ip) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Latest Login Address'">
                {{ displayValue(state.account.latest_login_address) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Latest Login Time'">
                {{ displayValue(state.account.latest_login_time) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Latest Login Device'">
                {{ displayValue(state.account.latest_login_device) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Created At'">
                {{ displayValue(state.account.created_at) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Updated At'">
                {{ displayValue(state.account.updated_at) }}
              </NDescriptionsItem>
            </NDescriptions>
          </NTabPane>

          <NTabPane name="identity" :tab="'Login Identity'">
            <NDescriptions label-placement="left" bordered :column="1">
              <NDescriptionsItem :label="'Account'">
                {{ displayValue(state.account.account) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Email Login Identity'">
                {{ displayValue(state.account.email_identity) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Email Login Enabled'">
                {{ state.account.email_login_enabled ? 'Yes' : 'No' }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Email Verified'">
                {{
                  state.account.email_identity_verified
                    ? 'Yes'
                    : 'No'
                }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Email Bind Status'">
                {{
                  dictTypeData(
                    'ACCOUNT_IDENTITY_BIND_STATUS',
                    state.account.email_identity_bind_status,
                  ) || displayValue(state.account.email_identity_bind_status)
                }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Phone Login Identity'">
                {{ displayValue(state.account.phone_identity) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Phone Login Enabled'">
                {{ state.account.phone_login_enabled ? 'Yes' : 'No' }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Phone Verified'">
                {{
                  state.account.phone_identity_verified
                    ? 'Yes'
                    : 'No'
                }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Phone Bind Status'">
                {{
                  dictTypeData(
                    'ACCOUNT_IDENTITY_BIND_STATUS',
                    state.account.phone_identity_bind_status,
                  ) || displayValue(state.account.phone_identity_bind_status)
                }}
              </NDescriptionsItem>
            </NDescriptions>
          </NTabPane>

          <NTabPane name="profile" :tab="'User Profile'">
            <NDescriptions label-placement="left" bordered :column="1">
              <NDescriptionsItem :label="'Name'">
                {{ displayValue(state.account.name) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Nickname'">
                {{ displayValue(state.account.nickname) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Avatar'">
                <NAvatar
                  v-if="avatarUrl"
                  :src="avatarUrl"
                  :alt="avatarAlt"
                  :img-props="avatarImgProps"
                />
                <template v-else> - </template>
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Signature'">
                {{ displayValue(state.account.signature) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Phone'">
                {{ displayValue(state.account.phone) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Email'">
                {{ displayValue(state.account.email) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Employee No.'">
                {{ displayValue(state.account.employee_no) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Title'">
                {{ displayValue(state.account.title) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Level'">
                {{ displayValue(state.account.level) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Bio'">
                {{ displayValue(state.account.bio) }}
              </NDescriptionsItem>
              <NDescriptionsItem :label="'Remark'">
                {{ displayValue(state.account.remark) }}
              </NDescriptionsItem>
            </NDescriptions>
          </NTabPane>
        </NTabs>
      </NSpin>
    </NScrollbar>
  </NModal>
</template>
