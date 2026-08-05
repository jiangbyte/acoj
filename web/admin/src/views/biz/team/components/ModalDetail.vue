<script setup lang="tsx">
import { ojTeamApi } from '@/api'
import { displayValue, formatDateTime, hasPermission } from '@/utils'
import { NButton, NFlex } from 'naive-ui'
import { reactive } from 'vue'

const emit = defineEmits<{
  saved: []
}>()

const state = reactive({
  showModal: false,
  loading: false,
  teamId: '',
  detail: {} as any,
  members: [] as any[],
  membersLoading: false,
  addAccountIds: '',
  addLoading: false,
  editShowModal: false,
  editSubmitLoading: false,
  editForm: { name: '', description: '', max_members: 50, visibility: 'PRIVATE' },
})

const memberColumns = [
  { title: '账户 ID', key: 'account_id' },
  { title: '角色', key: 'role', width: 100 },
  { title: '加入时间', key: 'joined_at', render: (row: any) => formatDateTime(row.joined_at) },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    render: (row: any) => hasPermission('biz:team:update')
      ? (
          <NButton type="error" size="small" text={true} onClick={() => removeMember(row.account_id)}>
            移除
          </NButton>
        )
      : null,
  },
]

async function openModal(id: string) {
  state.teamId = id
  state.detail = {}
  state.members = []
  state.addAccountIds = ''
  state.showModal = true
  await fetchDetail(id)
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await ojTeamApi.detail({ id })
    state.detail = response.data ?? {}
    state.editForm = {
      name: state.detail.name ?? '',
      description: state.detail.description ?? '',
      max_members: state.detail.max_members ?? 50,
      visibility: state.detail.visibility ?? 'PRIVATE',
    }
  } finally {
    state.loading = false
  }
  await fetchMembers()
}

async function fetchMembers() {
  if (!state.teamId) return
  state.membersLoading = true
  try {
    const response = await ojTeamApi.members({ team_id: state.teamId })
    state.members = response.data ?? []
  } finally {
    state.membersLoading = false
  }
}

function parseAccountIds(text: string) {
  return text.split(/[\s,;，；]+/).map(s => s.trim()).filter(Boolean)
}

async function addMembers() {
  const account_ids = parseAccountIds(state.addAccountIds)
  if (!account_ids.length) {
    window.$message.warning('请输入账户 ID')
    return
  }
  state.addLoading = true
  try {
    await ojTeamApi.memberAdd({ team_id: state.teamId, account_ids })
    state.addAccountIds = ''
    window.$message.success('添加成功')
    await fetchMembers()
    await fetchDetail(state.teamId)
    emit('saved')
  } finally {
    state.addLoading = false
  }
}

async function removeMember(accountId: string) {
  await ojTeamApi.memberRemove({ team_id: state.teamId, account_id: accountId })
  window.$message.success('已移除')
  await fetchMembers()
  await fetchDetail(state.teamId)
  emit('saved')
}

async function submitEdit() {
  state.editSubmitLoading = true
  try {
    await ojTeamApi.update({
      id: state.teamId,
      name: state.editForm.name,
      description: state.editForm.description || null,
      max_members: state.editForm.max_members,
      visibility: state.editForm.visibility,
    })
    window.$message.success('更新成功')
    state.editShowModal = false
    await fetchDetail(state.teamId)
    emit('saved')
  } finally {
    state.editSubmitLoading = false
  }
}

async function disableTeam() {
  await ojTeamApi.disable({ id: state.teamId })
  window.$message.success('已禁用')
  await fetchDetail(state.teamId)
  emit('saved')
}

async function dissolveTeam() {
  window.$dialog.warning({
    title: '解散小组',
    content: '确认解散该小组？',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      await ojTeamApi.dissolve({ id: state.teamId })
      window.$message.success('已解散')
      state.showModal = false
      emit('saved')
    },
  })
}

defineExpose({ openModal })
</script>

<template>
  <NModal v-model:show="state.showModal" preset="card" draggable :mask-closable="false" title="小组详情" style="width: 860px">
    <NSpin :show="state.loading">
      <NFlex vertical :size="16">
        <NDescriptions label-placement="left" bordered :column="2">
          <NDescriptionsItem label="主键">{{ displayValue(state.detail.id) }}</NDescriptionsItem>
          <NDescriptionsItem label="范围">{{ displayValue(state.detail.scope) }}</NDescriptionsItem>
          <NDescriptionsItem label="名称">{{ displayValue(state.detail.name) }}</NDescriptionsItem>
          <NDescriptionsItem label="状态">{{ displayValue(state.detail.status) }}</NDescriptionsItem>
          <NDescriptionsItem label="可见性">
            {{ state.detail.visibility === 'PUBLIC' ? '公开' : state.detail.visibility === 'PRIVATE' ? '私有' : displayValue(state.detail.visibility) }}
          </NDescriptionsItem>
          <NDescriptionsItem label="课程 ID">{{ displayValue(state.detail.course_id) }}</NDescriptionsItem>
          <NDescriptionsItem label="邀请码">{{ displayValue(state.detail.invite_code) }}</NDescriptionsItem>
          <NDescriptionsItem label="成员数">{{ displayValue(state.detail.member_count) }} / {{ displayValue(state.detail.max_members) }}</NDescriptionsItem>
          <NDescriptionsItem label="负责人">{{ displayValue(state.detail.owner_id) }}</NDescriptionsItem>
          <NDescriptionsItem label="描述" :span="2">
            <div class="whitespace-pre-wrap">{{ displayValue(state.detail.description) }}</div>
          </NDescriptionsItem>
        </NDescriptions>

        <NFlex v-if="hasPermission('biz:team:update')" :size="8">
          <NButton type="primary" size="small" @click="state.editShowModal = true">编辑</NButton>
          <NButton v-if="state.detail.status === 'ENABLED'" type="warning" size="small" @click="disableTeam">禁用</NButton>
          <NButton v-if="state.detail.status !== 'DISSOLVED'" type="error" size="small" @click="dissolveTeam">解散</NButton>
        </NFlex>

        <NDivider>成员管理</NDivider>
        <NFlex v-if="hasPermission('biz:team:update')" vertical :size="8">
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
    </NSpin>
  </NModal>

  <NModal v-model:show="state.editShowModal" preset="card" title="编辑小组" style="width: 640px">
    <NForm label-placement="left" label-width="100">
      <NFormItem label="名称">
        <NInput v-model:value="state.editForm.name" />
      </NFormItem>
      <NFormItem label="描述">
        <NInput v-model:value="state.editForm.description" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
      </NFormItem>
      <NFormItem label="可见性">
        <NSelect
          v-model:value="state.editForm.visibility"
          :options="[
            { label: '公开', value: 'PUBLIC' },
            { label: '私有', value: 'PRIVATE' },
          ]"
        />
      </NFormItem>
      <NFormItem label="最大成员">
        <NInputNumber v-model:value="state.editForm.max_members" class="w-full" :min="2" :max="500" />
      </NFormItem>
    </NForm>
    <template #action>
      <NSpace justify="end">
        <NButton @click="state.editShowModal = false">取消</NButton>
        <NButton type="primary" :loading="state.editSubmitLoading" @click="submitEdit">确认</NButton>
      </NSpace>
    </template>
  </NModal>
</template>
