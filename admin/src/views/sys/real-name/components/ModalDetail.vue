<!--
  Author: Charlie

  实名认证审核详情。
-->
<script setup lang="ts">
import { realNameApi } from '@/api'
import { createTagColor, displayValue, formatDateTime, hasPermission, isImageFile } from '@/utils'
import { buildAdminFileDownloadUrl } from '@/utils/file'
import { computed, reactive } from 'vue'

const emit = defineEmits<{
  changed: []
}>()

const DOCUMENT_TYPE_LABELS: Record<string, string> = {
  ID_CARD: '身份证',
  PASSPORT: '护照',
  EID_CARD: '电子身份证',
  EID: '电子身份证',
}

const CASE_STATUS_LABELS: Record<string, string> = {
  PENDING: '审核中',
  APPROVED: '已通过',
  REJECTED: '已驳回',
}

const VERIFY_CHANNEL_LABELS: Record<string, string> = {
  MANUAL: '人工审核',
  THIRD_PARTY: '第三方认证',
}

const BUSINESS_TYPE_LABELS: Record<string, string> = {
  ACCOUNT_VERIFY: '账号实名认证',
  ACCOUNT_RECOVERY: '实名找回账号',
}

const state = reactive({
  showModal: false,
  loading: false,
  detail: null as any,
  approveShow: false,
  rejectShow: false,
  approveLoading: false,
  rejectLoading: false,
  rejectReason: '',
  activeCaseId: '',
})

function labelOf(map: Record<string, string>, value?: string | null) {
  if (!value) return '-'
  return map[value] || value
}

function statusColor(status?: string | null) {
  if (status === 'APPROVED') return '#52c41a'
  if (status === 'PENDING') return '#1677ff'
  if (status === 'REJECTED') return '#ff4d4f'
  return '#d9d9d9'
}

function attachmentUrl(item: any) {
  const direct = String(item?.url ?? '').trim()
  if (direct && /^(https?:|data:|blob:)/i.test(direct)) {
    return direct
  }
  return buildAdminFileDownloadUrl(item?.id) || direct || undefined
}

const attachments = computed(() => {
  const list = state.detail?.attachments
  return Array.isArray(list) ? list : []
})

async function openModal(caseId: string) {
  state.detail = null
  state.activeCaseId = caseId
  state.showModal = true
  await fetchDetail(caseId)
}

async function fetchDetail(caseId: string) {
  state.loading = true
  try {
    const response = await realNameApi.reviewDetail(caseId)
    state.detail = response.data ?? null
  } finally {
    state.loading = false
  }
}

function openApprove() {
  state.approveShow = true
}

function openReject() {
  state.rejectReason = ''
  state.rejectShow = true
}

async function confirmApprove() {
  state.approveLoading = true
  try {
    await realNameApi.approveCase({ id: state.activeCaseId })
    window.$message?.success?.('已通过实名认证申请')
    state.approveShow = false
    await fetchDetail(state.activeCaseId)
    emit('changed')
  } finally {
    state.approveLoading = false
  }
}

async function confirmReject() {
  const reason = state.rejectReason.trim()
  if (!reason) {
    window.$message?.warning?.('请输入驳回原因')
    return false
  }
  state.rejectLoading = true
  try {
    await realNameApi.rejectCase({ id: state.activeCaseId, reject_reason: reason })
    window.$message?.success?.('已驳回实名认证申请')
    state.rejectShow = false
    await fetchDetail(state.activeCaseId)
    emit('changed')
  } finally {
    state.rejectLoading = false
  }
}

defineExpose({
  openModal,
})
</script>

<template>
  <HeiDetailContainer
    v-model:show="state.showModal"
    title="实名认证详情"
    :width="680"
    :mask-closable="false"
  >
    <NSpin :show="state.loading">
      <template v-if="state.detail">
        <NDescriptions
          label-placement="left"
          bordered
          :column="1"
          label-style="min-width: 120px"
        >
          <NDescriptionsItem label="工单号">
            {{ displayValue(state.detail.case_id ?? state.detail.caseId) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="账号 ID">
            {{ displayValue(state.detail.account_id ?? state.detail.accountId) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="业务类型">
            {{ labelOf(BUSINESS_TYPE_LABELS, state.detail.business_type ?? state.detail.businessType) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="状态">
            <NTag
              size="small"
              :color="createTagColor(statusColor(state.detail.status))"
              :bordered="false"
            >
              {{ labelOf(CASE_STATUS_LABELS, state.detail.status) }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem label="证件类型">
            {{ labelOf(DOCUMENT_TYPE_LABELS, state.detail.document_type ?? state.detail.documentType) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="姓名">
            {{ displayValue(state.detail.real_name_masked ?? state.detail.realNameMasked) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="证件号码">
            {{ displayValue(state.detail.document_no_masked ?? state.detail.documentNoMasked) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="认证方式">
            {{ labelOf(VERIFY_CHANNEL_LABELS, state.detail.verify_channel ?? state.detail.verifyChannel) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="提交时间">
            {{ formatDateTime(state.detail.created_at ?? state.detail.createdAt) }}
          </NDescriptionsItem>
          <NDescriptionsItem
            v-if="state.detail.reject_reason || state.detail.rejectReason"
            label="驳回原因"
          >
            {{ displayValue(state.detail.reject_reason ?? state.detail.rejectReason) }}
          </NDescriptionsItem>
        </NDescriptions>

        <div class="mt-4">
          <div class="mb-2 text-sm font-medium">
            附件
          </div>
          <NFlex v-if="attachments.length">
            <template
              v-for="item in attachments"
              :key="item.id ?? item.object_name ?? item.objectName"
            >
              <NImage
                v-if="isImageFile(item) && attachmentUrl(item)"
                width="96"
                height="96"
                object-fit="cover"
                :src="attachmentUrl(item)"
                :alt="item.original_name ?? item.originalName ?? 'attachment'"
              />
              <NButton
                v-else-if="attachmentUrl(item)"
                tag="a"
                text
                type="primary"
                :href="attachmentUrl(item)"
                target="_blank"
                rel="noopener"
              >
                {{ item.original_name ?? item.originalName ?? item.object_name ?? item.objectName ?? '查看附件' }}
              </NButton>
            </template>
          </NFlex>
          <span
            v-else
            class="text-sm text-gray-500"
          >
            暂无附件
          </span>
        </div>

        <NFlex
          v-if="state.detail.status === 'PENDING' && hasPermission('sys:realname:verify')"
          class="mt-4"
          justify="end"
        >
          <NButton
            type="success"
            @click="openApprove"
          >
            通过
          </NButton>
          <NButton
            type="error"
            @click="openReject"
          >
            驳回
          </NButton>
        </NFlex>
      </template>
    </NSpin>
  </HeiDetailContainer>

  <NModal
    v-model:show="state.approveShow"
    preset="dialog"
    title="通过实名认证"
    positive-text="确认通过"
    negative-text="取消"
    :loading="state.approveLoading"
    @positive-click="confirmApprove"
  >
    确认通过该实名认证申请？
  </NModal>

  <NModal
    v-model:show="state.rejectShow"
    preset="dialog"
    title="驳回实名认证"
    positive-text="确认驳回"
    negative-text="取消"
    :loading="state.rejectLoading"
    @positive-click="confirmReject"
  >
    <NInput
      v-model:value="state.rejectReason"
      type="textarea"
      :rows="4"
      placeholder="请输入驳回原因"
    />
  </NModal>
</template>
