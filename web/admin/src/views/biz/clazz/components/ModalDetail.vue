<script setup lang="tsx">
import { ojClazzApi } from '@/api'
import { displayValue, formatDateTime, hasPermission } from '@/utils'
import { NButton } from 'naive-ui'
import { reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const state = reactive({
  showModal: false,
  loading: false,
  classId: '',
  activeTab: 'basic',
  detail: {} as any,
  members: [] as any[],
  membersLoading: false,
  inviteRefreshing: false,
  addAccountIds: '',
  addLoading: false,
})

const memberColumns = [
  { title: '账户 ID', key: 'account_id' },
  { title: '角色', key: 'role', width: 100 },
  { title: '加入时间', key: 'joined_at', render: (row: any) => formatDateTime(row.joined_at) },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    render: (row: any) => hasPermission('biz:clazz:update')
      ? (
          <NButton type="error" size="small" text={true} onClick={() => removeMember(row.account_id)}>
            移除
          </NButton>
        )
      : null,
  },
]

async function openModal(id: string) {
  state.classId = id
  state.detail = {}
  state.members = []
  state.addAccountIds = ''
  state.activeTab = 'basic'
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await ojClazzApi.detail({ id })
    state.detail = response.data ?? {}
  } finally {
    state.loading = false
  }
}

async function fetchMembers() {
  if (!state.classId) return
  state.membersLoading = true
  try {
    const response = await ojClazzApi.members({ class_id: state.classId })
    state.members = response.data ?? []
  } finally {
    state.membersLoading = false
  }
}

function parseAccountIds(text: string) {
  return text.split(/[\s,;，；]+/).map(s => s.trim()).filter(Boolean)
}

async function refreshInvite() {
  state.inviteRefreshing = true
  try {
    const response = await ojClazzApi.refreshInvite({ id: state.classId })
    state.detail.invite_code = response.data?.invite_code ?? state.detail.invite_code
    window.$message.success('邀请码已刷新')
  } finally {
    state.inviteRefreshing = false
  }
}

async function addMembers() {
  const account_ids = parseAccountIds(state.addAccountIds)
  if (!account_ids.length) {
    window.$message.warning('请输入账户 ID')
    return
  }
  state.addLoading = true
  try {
    await ojClazzApi.memberAdd({ class_id: state.classId, account_ids })
    state.addAccountIds = ''
    window.$message.success('添加成功')
    await Promise.all([fetchMembers(), fetchDetail(state.classId)])
  } finally {
    state.addLoading = false
  }
}

async function removeMember(accountId: string) {
  await ojClazzApi.memberRemove({ class_id: state.classId, account_id: accountId })
  window.$message.success('已移除')
  await Promise.all([fetchMembers(), fetchDetail(state.classId)])
}

function goCourses() {
  router.push({ path: '/biz/course', query: { class_id: state.classId } })
}

function onTabChange(name: string) {
  state.activeTab = name
  if (name === 'members' && !state.members.length) {
    void fetchMembers()
  }
}

defineExpose({ openModal })
</script>

<template>
  <NModal v-model:show="state.showModal" preset="card" draggable :mask-closable="false" title="班级详情" style="width: 860px">
    <NSpin :show="state.loading">
      <NTabs :value="state.activeTab" type="line" @update:value="onTabChange">
        <NTabPane name="basic" tab="基本信息">
          <NDescriptions label-placement="left" bordered :column="1">
            <NDescriptionsItem label="主键">{{ displayValue(state.detail.id) }}</NDescriptionsItem>
            <NDescriptionsItem label="班级编码">{{ displayValue(state.detail.code) }}</NDescriptionsItem>
            <NDescriptionsItem label="班级名称">{{ displayValue(state.detail.name) }}</NDescriptionsItem>
            <NDescriptionsItem label="简介">
              <div class="whitespace-pre-wrap">{{ displayValue(state.detail.summary) }}</div>
            </NDescriptionsItem>
            <NDescriptionsItem label="状态">{{ displayValue(state.detail.status) }}</NDescriptionsItem>
            <NDescriptionsItem label="可见性">
              {{ state.detail.visibility === 'PUBLIC' ? '公开' : state.detail.visibility === 'PRIVATE' ? '私有' : displayValue(state.detail.visibility) }}
            </NDescriptionsItem>
            <NDescriptionsItem label="成员数">{{ displayValue(state.detail.member_count) }}</NDescriptionsItem>
            <NDescriptionsItem label="创建时间">{{ formatDateTime(state.detail.created_at) }}</NDescriptionsItem>
            <NDescriptionsItem label="更新时间">{{ formatDateTime(state.detail.updated_at) }}</NDescriptionsItem>
          </NDescriptions>
        </NTabPane>

        <NTabPane name="invite" tab="邀请码">
          <NFlex vertical :size="16">
            <NDescriptions label-placement="left" bordered :column="1">
              <NDescriptionsItem label="邀请码">{{ displayValue(state.detail.invite_code) }}</NDescriptionsItem>
            </NDescriptions>
            <NButton
              v-if="hasPermission('biz:clazz:update')"
              type="primary"
              :loading="state.inviteRefreshing"
              @click="refreshInvite"
            >
              刷新邀请码
            </NButton>
          </NFlex>
        </NTabPane>

        <NTabPane name="members" tab="成员">
          <NFlex vertical :size="12">
            <NFlex v-if="hasPermission('biz:clazz:update')" vertical :size="8">
              <NInput
                v-model:value="state.addAccountIds"
                type="textarea"
                placeholder="输入账户 ID，多个用逗号或换行分隔"
                :autosize="{ minRows: 2, maxRows: 4 }"
              />
              <NButton type="primary" :loading="state.addLoading" @click="addMembers">添加成员</NButton>
            </NFlex>
            <NSpin :show="state.membersLoading">
              <NDataTable :columns="memberColumns" :data="state.members" :bordered="false" size="small" />
            </NSpin>
          </NFlex>
        </NTabPane>

        <NTabPane name="courses" tab="课程">
          <NFlex vertical :size="12">
            <span class="text-gray-500">查看该班级下的课程列表</span>
            <NButton type="primary" @click="goCourses">前往课程管理</NButton>
          </NFlex>
        </NTabPane>
      </NTabs>
    </NSpin>
  </NModal>
</template>
