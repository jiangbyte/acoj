<!-- Author: Charlie -->

<script setup lang="ts">
import { fileApi, realNameApi } from '@/api'
import { useAuthStore } from '@/stores'
import { formatDateTime } from '@/utils'
import { normalizeUploadedFile } from '@/utils/file'
import { useElementSize } from '@vueuse/core'
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import type { UploadCustomRequestOptions, UploadFileInfo } from 'naive-ui'
import '../profile.css'

const DOCUMENT_TYPE_LABELS: Record<string, string> = {
  ID_CARD: '身份证',
  PASSPORT: '护照',
  EID: '电子身份证',
}

type FlowPhase = 'form' | 'pending' | 'success' | 'failed' | 'revoked'

const authStore = useAuthStore()
const pollTimer = ref<number | null>(null)
const objectNameByUid = new Map<string, string>()

const state = reactive({
  loading: false,
  submitting: false,
  status: null as any,
  options: null as any,
  latestReject: null as any,
  fileList: [] as UploadFileInfo[],
  wizardStep: 0,
  flowPhase: 'form' as FlowPhase,
  form: {
    document_type: '',
    real_name: '',
    document_no: '',
    applicant_contact: '',
  },
})

const forceBindIdentity = computed(() => Boolean(authStore.userInfo?.forceBindIdentity))

const businessType = computed(() => {
  const items = state.options?.business_types ?? state.options?.businessTypes ?? []
  return items[0]?.business_type ?? items[0]?.businessType ?? 'ACCOUNT_VERIFY'
})

const thirdPartyAvailable = computed(() => {
  const items = state.options?.business_types ?? state.options?.businessTypes ?? []
  const current = items.find(
    (item: any) => (item.business_type ?? item.businessType) === businessType.value,
  )
  const channels: string[] = current?.channels ?? []
  return channels.includes('THIRD_PARTY')
})

const documentTypeOptions = computed(() => {
  const types: string[] = state.options?.document_types ?? state.options?.documentTypes ?? []
  return types.map((value) => ({
    label: labelOf(DOCUMENT_TYPE_LABELS, value),
    value,
  }))
})

const pendingCase = computed(() => state.status?.pending_case ?? state.status?.pendingCase)

const wizardRef = ref<HTMLElement | null>(null)
const { width: wizardWidth } = useElementSize(wizardRef)
const stepsVertical = computed(() => wizardWidth.value > 0 && wizardWidth.value < 560)
const stepsCompact = computed(() => wizardWidth.value > 0 && wizardWidth.value < 680)

const stepCurrent = computed(() => (state.flowPhase === 'form' ? state.wizardStep : 2))

function stepStatus(index: number): 'process' | 'finish' | 'error' | 'wait' {
  if (state.flowPhase === 'success') return 'finish'
  if (state.flowPhase === 'failed') {
    if (index < 2) return 'finish'
    if (index === 2) return 'error'
    return 'wait'
  }
  if (state.flowPhase === 'pending' || state.flowPhase === 'revoked') {
    if (index < 2) return 'finish'
    if (index === 2) return 'process'
    return 'wait'
  }
  if (index < state.wizardStep) return 'finish'
  if (index === state.wizardStep) return 'process'
  return 'wait'
}

const rejectReason = computed(
  () =>
    state.latestReject?.reject_reason ??
    state.latestReject?.rejectReason ??
    pendingCase.value?.reject_reason ??
    pendingCase.value?.rejectReason,
)

function labelOf(map: Record<string, string>, value?: string | null) {
  if (!value) return '-'
  return map[value] || value
}

function applyFlowPhase(nextStatus: any, rejectRow: any | null) {
  const identityStatus = nextStatus?.status
  const pending = nextStatus?.pending_case ?? nextStatus?.pendingCase
  if (identityStatus === 'VERIFIED') {
    state.flowPhase = 'success'
    return
  }
  if (identityStatus === 'REVOKED') {
    state.flowPhase = 'revoked'
    return
  }
  if (pending?.status === 'PENDING') {
    state.flowPhase = 'pending'
    return
  }
  if (rejectRow?.status === 'REJECTED') {
    state.flowPhase = 'failed'
    return
  }
  state.flowPhase = 'form'
}

function clearPollTimer() {
  if (pollTimer.value != null) {
    window.clearInterval(pollTimer.value)
    pollTimer.value = null
  }
}

async function loadStatus() {
  const response = await realNameApi.getIdentityStatus()
  state.status = response.data
  return response.data
}

async function loadLatestCase() {
  const response = await realNameApi.myCasePage({ current: 1, size: 1 })
  const page = response.data
  const rows = page?.records ?? page?.items ?? []
  const latest = rows[0] ?? null
  state.latestReject = latest?.status === 'REJECTED' ? latest : null
  return latest
}

async function refresh() {
  state.loading = true
  try {
    const [nextStatus, latest] = await Promise.all([
      loadStatus(),
      loadLatestCase(),
      authStore.refreshUserInfo(),
    ])
    applyFlowPhase(nextStatus, latest?.status === 'REJECTED' ? latest : null)
  } finally {
    state.loading = false
  }
}

function startPolling() {
  clearPollTimer()
  let attempts = 0
  pollTimer.value = window.setInterval(() => {
    attempts += 1
    void (async () => {
      const next = await loadStatus()
      const latest = await loadLatestCase()
      await authStore.refreshUserInfo()
      applyFlowPhase(next, latest?.status === 'REJECTED' ? latest : null)
      const pending = next?.pending_case ?? next?.pendingCase
      const identityStatus = next?.status
      if (!pending && identityStatus !== 'UNVERIFIED') {
        clearPollTimer()
      }
      if (attempts >= 40) {
        clearPollTimer()
      }
    })()
  }, 3000)
}

watch(
  () => state.flowPhase,
  (phase) => {
    if (phase === 'pending') {
      startPolling()
    } else {
      clearPollTimer()
    }
  },
)

async function customRequest(options: UploadCustomRequestOptions) {
  const { file, onFinish, onError } = options
  try {
    const raw = file.file
    if (!raw) {
      throw new Error('empty file')
    }
    const res = await fileApi.upload(raw)
    const normalized = normalizeUploadedFile(res.data, raw, 'object_name')
    file.url = normalized.url
    file.name = normalized.name || file.name
    objectNameByUid.set(file.id, normalized.objectName)
    onFinish()
  } catch (error) {
    onError()
    throw error
  }
}

function handleFileListUpdate(list: UploadFileInfo[]) {
  const keep = new Set(list.map((item) => item.id))
  for (const uid of [...objectNameByUid.keys()]) {
    if (!keep.has(uid)) {
      objectNameByUid.delete(uid)
    }
  }
  state.fileList = list
}

function resolveObjectName(item: UploadFileInfo) {
  return String(objectNameByUid.get(item.id) || '').trim()
}

function handleNextStep() {
  if (!state.form.document_type || !state.form.real_name.trim() || !state.form.document_no.trim()) {
    window.$message?.warning?.('请完整填写证件信息')
    return
  }
  state.wizardStep = 1
}

function handleRestart() {
  state.wizardStep = 0
  state.fileList = []
  objectNameByUid.clear()
  state.latestReject = null
  state.flowPhase = 'form'
}

async function submitVerification() {
  if (state.submitting) {
    return
  }
  if (!state.form.document_type || !state.form.real_name.trim() || !state.form.document_no.trim()) {
    window.$message?.warning?.('请完整填写证件信息')
    return
  }
  if (state.fileList.some((item) => item.status === 'uploading')) {
    window.$message?.warning?.('请等待附件上传完成')
    return
  }
  if (state.fileList.some((item) => item.status === 'error')) {
    window.$message?.warning?.('请移除上传失败的附件后再提交')
    return
  }

  if (thirdPartyAvailable.value && !state.fileList.length) {
    state.submitting = true
    try {
      const response = await realNameApi.initThirdParty({
        business_type: businessType.value,
        document_type: state.form.document_type,
        real_name: state.form.real_name.trim(),
        document_no: state.form.document_no.trim(),
      })
      const redirectUrl = response.data?.redirect_url ?? response.data?.redirectUrl
      if (redirectUrl) {
        window.location.assign(String(redirectUrl))
        return
      }
    } finally {
      state.submitting = false
    }
  }

  const attachmentIds = state.fileList.map(resolveObjectName).filter(Boolean)
  if (!attachmentIds.length) {
    window.$message?.warning?.('请至少上传一张证件照片')
    return
  }

  state.submitting = true
  try {
    await realNameApi.submitCase({
      business_type: businessType.value,
      document_type: state.form.document_type,
      real_name: state.form.real_name.trim(),
      document_no: state.form.document_no.trim(),
      attachment_ids: attachmentIds,
      applicant_contact: state.form.applicant_contact.trim() || null,
    })
    window.$message?.success?.('实名认证申请已提交')
    state.fileList = []
    objectNameByUid.clear()
    state.wizardStep = 0
    const nextStatus = await loadStatus()
    applyFlowPhase(nextStatus, null)
    await authStore.refreshUserInfo()
  } finally {
    state.submitting = false
  }
}

onMounted(async () => {
  state.loading = true
  try {
    const optionsResponse = await realNameApi.getCaseOptions()
    state.options = optionsResponse.data
    const types: string[] =
      optionsResponse.data?.document_types ?? optionsResponse.data?.documentTypes ?? []
    if (types.length) {
      state.form.document_type = types[0]
    }
    const nextStatus = await loadStatus()
    const latest = await loadLatestCase()
    applyFlowPhase(nextStatus, latest?.status === 'REJECTED' ? latest : null)
  } finally {
    state.loading = false
  }
})

onBeforeUnmount(() => {
  clearPollTimer()
})

defineExpose({ refresh })
</script>

<template>
  <NSpin :show="state.loading">
    <div
      ref="wizardRef"
      class="profile-identity-wizard"
    >
      <NAlert
        v-if="forceBindIdentity"
        type="warning"
        class="mb-4"
        title="请先完成实名认证后再使用其他功能。"
      />

      <h3 class="profile-identity-wizard__title">
        个人实名认证
      </h3>

      <NSteps
        :current="stepCurrent"
        size="small"
        :vertical="stepsVertical"
        class="profile-identity-wizard__steps"
        :class="{
          'profile-identity-wizard__steps--vertical': stepsVertical,
          'profile-identity-wizard__steps--compact': stepsCompact && !stepsVertical,
        }"
      >
        <NStep
          title="填写基本信息"
          description="证件与姓名"
          :status="stepStatus(0)"
        />
        <NStep
          title="上传证件材料"
          description="照片或说明"
          :status="stepStatus(1)"
        />
        <NStep
          title="认证结果"
          description="等待或完成"
          :status="stepStatus(2)"
        />
      </NSteps>

      <NDivider style="margin: 16px 0 24px" />

      <template v-if="state.flowPhase === 'form' && state.wizardStep === 0">
        <div class="profile-identity-wizard__form">
          <h4 class="profile-identity-wizard__section-title">
            填写基本信息
          </h4>
          <NForm label-placement="top">
            <NFormItem label="证件类型">
              <NSelect
                v-model:value="state.form.document_type"
                :options="documentTypeOptions"
                placeholder="请选择证件类型"
              />
            </NFormItem>
            <NFormItem label="真实姓名">
              <NInput
                v-model:value="state.form.real_name"
                placeholder="请输入与证件一致的姓名"
              />
            </NFormItem>
            <NFormItem label="证件号码">
              <NInput
                v-model:value="state.form.document_no"
                placeholder="请输入证件号码"
              />
            </NFormItem>
            <NFormItem label="补充说明">
              <NInput
                v-model:value="state.form.applicant_contact"
                placeholder="可选，便于审核沟通"
              />
            </NFormItem>
          </NForm>
          <div class="profile-identity-wizard__actions">
            <NButton
              type="primary"
              size="large"
              @click="handleNextStep"
            >
              下一步
            </NButton>
          </div>
        </div>
      </template>

      <template v-else-if="state.flowPhase === 'form' && state.wizardStep === 1">
        <div class="profile-identity-wizard__upload">
          <h4 class="profile-identity-wizard__section-title">
            上传证件材料
          </h4>
          <p class="profile-identity-wizard__hint">
            请上传证件正反面或手持证件照，照片需完整清晰。支持 JPG、PNG，单张不超过 5MB。
            <template v-if="thirdPartyAvailable">
              未上传材料时将尝试在线核验。
            </template>
            <template v-else>
              请至少上传一张证件照片。
            </template>
          </p>
          <NUpload
            multiple
            directory-dnd
            :file-list="state.fileList"
            :custom-request="customRequest"
            @update:file-list="handleFileListUpdate"
          >
            <NUploadDragger>
              <div class="text-sm text-gray-500">
                点击或拖拽上传证件照片
              </div>
            </NUploadDragger>
          </NUpload>
          <div class="profile-identity-wizard__actions">
            <NButton
              size="large"
              @click="state.wizardStep = 0"
            >
              上一步
            </NButton>
            <NButton
              type="primary"
              size="large"
              :loading="state.submitting"
              @click="submitVerification"
            >
              提交认证
            </NButton>
          </div>
          <p class="profile-identity-wizard__agreement">
            提交即表示同意平台实名认证与隐私相关协议
          </p>
        </div>
      </template>

      <template v-else-if="state.flowPhase === 'pending'">
        <NResult
          status="info"
          title="实名认证审核中"
          description="您的申请已提交，请耐心等待审核结果。"
        >
          <template #footer>
            <div class="profile-identity-wizard__result-sub">
              <span v-if="pendingCase">
                提交时间：{{ formatDateTime(pendingCase.created_at ?? pendingCase.createdAt) }}
              </span>
            </div>
          </template>
        </NResult>
      </template>

      <template v-else-if="state.flowPhase === 'success'">
        <NResult
          status="success"
          title="实名认证成功"
        >
          <template #footer>
            <div class="profile-identity-wizard__result-sub">
              <span>
                证件类型：{{ labelOf(DOCUMENT_TYPE_LABELS, state.status?.document_type ?? state.status?.documentType) }}
              </span>
              <span>姓名：{{ state.status?.real_name_masked ?? state.status?.realNameMasked ?? '-' }}</span>
              <span>证件号码：{{ state.status?.document_no_masked ?? state.status?.documentNoMasked ?? '-' }}</span>
              <span>认证时间：{{ formatDateTime(state.status?.verified_at ?? state.status?.verifiedAt) }}</span>
            </div>
          </template>
        </NResult>
      </template>

      <template v-else-if="state.flowPhase === 'failed'">
        <NResult
          status="error"
          title="实名认证未通过"
          description="对不起，本次实名认证未通过审核。"
        >
          <template #footer>
            <div class="profile-identity-wizard__result-sub">
              <p
                v-if="rejectReason"
                class="profile-identity-wizard__fail-reason"
              >
                失败原因：{{ rejectReason }}
              </p>
              <span v-else>请核对信息后重新提交。</span>
              <span v-if="state.latestReject">
                审核时间：{{
                  formatDateTime(
                    state.latestReject.reviewed_at
                      ?? state.latestReject.reviewedAt
                      ?? state.latestReject.created_at
                      ?? state.latestReject.createdAt,
                  )
                }}
              </span>
              <NButton
                type="primary"
                size="large"
                class="mt-4"
                @click="handleRestart"
              >
                重新认证
              </NButton>
            </div>
          </template>
        </NResult>
      </template>

      <template v-else-if="state.flowPhase === 'revoked'">
        <NResult
          status="warning"
          title="实名认证已撤销"
          description="您的实名状态已被撤销，如需继续使用相关功能，请重新完成认证。"
        >
          <template #footer>
            <NButton
              type="primary"
              size="large"
              @click="handleRestart"
            >
              重新认证
            </NButton>
          </template>
        </NResult>
      </template>
    </div>
  </NSpin>
</template>
