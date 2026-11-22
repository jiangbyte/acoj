<script lang="ts" setup>
import { actionDeleteBtn, actionIconBtn, renderSafeAvatar } from '@/utils'
import type { DataTableColumns } from 'naive-ui'
import { NButton, NCard, NDataTable, NEllipsis, NPagination, NPopconfirm, NSpace, NTag, NTime } from 'naive-ui'
import { useSysConversationFetch } from '@/composables/v1'
import Detail from './detail.vue'

const detailRef = ref()
const columns: DataTableColumns<any> = [
  {
    type: 'selection',
  },
  {
    title: 'Conversation ID',
    key: 'conversationId',
    ellipsis: {
      tooltip: true,
    },
    minWidth: 220,
  },
  {
    title: 'User',
    key: 'user',
    width: 150,
    render(row: any) {
      return row.userId
        ? h(
            NSpace,
            { align: 'center', size: 'small', wrap: false },
            {
              default: () => [
                renderSafeAvatar(
                  {
                    size: 'small',
                    round: true,
                    src: row.userAvatar,
                  },
                ),
                h(
                  NEllipsis,
                  {
                    style: {
                      maxWidth: '90px',
                    },
                  },
                  { default: () => row.userIdName },
                ),
              ],
            },
          )
        : ''
    },
  },
  {
    title: 'Problem',
    key: 'problemIdName',
    ellipsis: {
      tooltip: true,
    },
    minWidth: 140,
  },
  {
    title: 'Messages',
    key: 'messageCount',
    width: 90,
  },
  {
    title: 'Last Type',
    key: 'lastMessageTypeName',
    width: 150,
    render(row: any) {
      return row.lastMessageTypeName || row.lastMessageType || ''
    },
  },
  {
    title: 'Status',
    key: 'status',
    width: 100,
    render(row: any) {
      const isError = row.status === 'ERROR'
      return h(NTag, { type: isError ? 'error' : 'success', size: 'small' }, { default: () => row.status || 'SUCCESS' })
    },
  },
  {
    title: 'Tokens',
    key: 'totalTokens',
    width: 90,
  },
  {
    title: 'Duration',
    key: 'totalDurationMs',
    width: 110,
    render(row: any) {
      const ms = Number(row.totalDurationMs || 0)
      return ms > 0 ? `${ms} ms` : '-'
    },
  },
  {
    title: 'Last Active',
    key: 'lastActiveAt',
    width: 170,
    render(row: any) {
      return row.lastActiveAt ? h(NTime, { time: Number(row.lastActiveAt) }) : ''
    },
  },
  {
    title: 'Preview',
    key: 'preview',
    ellipsis: {
      tooltip: true,
    },
    minWidth: 180,
  },
  {
    title: 'Actions',
    key: 'action',
    width: 100,
    fixed: 'right',
    render(row: any) {
      return h(NSpace, { align: 'center', wrap: false }, () => [
        actionIconBtn({ kind: 'detail', onClick: () => detailRef.value.doOpen(row) }),
        actionDeleteBtn(() => deleteHandle(row)),
      ])
    },
  },
]

const columnSortFieldOptions = computed<any[]>(() => {
  return [
    { label: 'Last Active', value: 'lastActiveAt' },
    { label: 'Started At', value: 'startedAt' },
  ]
})
const sortOrderOptions = computed(() => [
  { label: 'Ascending', value: 'ASCEND' },
  { label: 'Descending', value: 'DESCEND' },
])
const displayKey = ref<string[]>(
  columns
    .filter(col => 'key' in col && col.key !== 'selection' && col.key !== 'action')
    .map((col: any) => col.key as string),
)
const columnDisplayOptions = computed(() => {
  const filteredColumns = columns
    .filter(col => 'key' in col)
    .filter(col => col.key !== 'action' && col.key !== 'selection')

  return filteredColumns.map((col, index) => ({
    label: col.title as string,
    value: col.key as string,
    disabled: index === 0,
  }))
})

const filteredColumns = computed(() => {
  return columns.filter((col: any) => {
    if (col.type === 'selection' || col.key === 'action')
      return true
    return 'key' in col ? displayKey.value.includes(col.key as string) : false
  })
})

const pageData = ref()
const pageParam = ref({
  current: 1,
  size: 20,
  sortField: null as string | null,
  sortOrder: null as string | null,
  keyword: '',
  userId: '',
  problemId: '',
})

function resetHandle() {
  pageParam.value.keyword = ''
  pageParam.value.userId = ''
  pageParam.value.problemId = ''
  loadData()
}

const { sessionPage, sessionDelete, exportPrompts } = useSysConversationFetch()
const promptShow = ref(false)
const promptData = ref<any>(null)

async function openPrompt() {
  const { data } = await exportPrompts()
  promptData.value = data
  promptShow.value = true
}
const loading = ref(false)
async function loadData() {
  loading.value = true
  const { data } = await sessionPage(pageParam.value)
  if (data) {
    pageData.value = data
    loading.value = false
  }
  else {
    loading.value = false
  }
}

loadData()

async function deleteHandle(row: any) {
  const param = [{
    conversationId: row.conversationId,
  }]
  const { success } = await sessionDelete(param)
  if (success) {
    window.$message.success('Deleted successfully')
    await loadData()
  }
}
const checkedRowKeys = ref<string[]>([])
async function deleteBatchHandle() {
  const param = checkedRowKeys.value.map(conversationId => ({ conversationId }))
  const { success } = await sessionDelete(param)
  if (success) {
    window.$message.success('Deleted successfully')
    await loadData()
    checkedRowKeys.value = []
  }
}
</script>

<template>
  <div class="flex flex-col h-full w-full">
    <NCard size="small">
      <NSpace vertical>
        <NSpace align="center">
          <NSpace align="center">
            <NFormItem :show-feedback="false" label="Keywords" label-placement="left">
              <NInput v-model:value="pageParam.keyword" placeholder="Enter keywords" />
            </NFormItem>
            <NFormItem :show-feedback="false" label="User" label-placement="left">
              <NInput v-model:value="pageParam.userId" placeholder="User ID" clearable />
            </NFormItem>
            <NFormItem :show-feedback="false" label="Problem" label-placement="left">
              <NInput v-model:value="pageParam.problemId" placeholder="Problem ID" clearable />
            </NFormItem>
            <NSpace align="center">
              <NButton type="primary" @click="loadData">
                <template #icon>
                  <IconParkOutlineSearch />
                </template>
                Search
              </NButton>
              <NButton type="warning" @click="resetHandle">
                <template #icon>
                  <IconParkOutlineRefresh />
                </template>
                Reset
              </NButton>
            </NSpace>
          </NSpace>
        </NSpace>
        <NSpace align="center" justify="space-between">
          <NSpace align="center">
            <NButton @click="openPrompt">
              Prompt
            </NButton>
            <NPopconfirm v-if="checkedRowKeys.length > 0" @positive-click="deleteBatchHandle">
              <template #default>
                Confirm delete selected sessions
              </template>
              <template #trigger>
                <NButton type="error">
                  Delete
                </NButton>
              </template>
            </NPopconfirm>
          </NSpace>
          <NSpace align="center">
            <NFormItem :show-feedback="false" label-placement="left">
              <NButton text @click="loadData">
                <template #icon>
                  <IconParkOutlineRefresh />
                </template>
              </NButton>
            </NFormItem>
            <NFormItem :show-feedback="false" label-placement="left">
              <NPopselect
                v-model:value="displayKey"
                :options="columnDisplayOptions"
                multiple
                @update:value="loadData"
              >
                <NButton text>
                  <template #icon>
                    <IconParkOutlineEyes />
                  </template>
                </NButton>
              </NPopselect>
            </NFormItem>
            <NFormItem :show-feedback="false" label-placement="left">
              <NPopselect
                v-model:value="pageParam.sortField"
                :options="columnSortFieldOptions"
                @update:value="loadData"
              >
                <NButton text>
                  <template #icon>
                    <IconParkOutlineSortOne />
                  </template>
                </NButton>
              </NPopselect>
            </NFormItem>
            <NFormItem :show-feedback="false" label-placement="left">
              <NPopselect
                v-model:value="pageParam.sortOrder"
                :options="sortOrderOptions"
                @update:value="loadData"
              >
                <NButton text>
                  <template #icon>
                    <IconParkOutlineSort />
                  </template>
                </NButton>
              </NPopselect>
            </NFormItem>
          </NSpace>
        </NSpace>
      </NSpace>
    </NCard>
    <NCard size="small" class="flex-1">
      <NDataTable
        v-model:checked-row-keys="checkedRowKeys"
        :columns="filteredColumns"
        :data="pageData?.records"
        :bordered="false"
        :row-key="(row: any) => row.conversationId"
        :loading="loading"
        flex-height
        :scroll-x="1400"
        class="flex-1 h-full"
      />
      <template #action>
        <NSpace align="center" justify="space-between" class="w-full">
          <NSpace align="center">
            <NP type="info" show-icon>
              Current: {{ pageData?.records?.length || 0 }} sessions
            </NP>
            <NP type="info" show-icon>
              Selected: {{ checkedRowKeys.length }} sessions
            </NP>
          </NSpace>
          <NPagination
            v-model:page="pageParam.current"
            v-model:page-size="pageParam.size"
            class="flex justify-end"
            :page-count="pageData ? Number(pageData.pages) : 0"
            show-size-picker
            :page-sizes="Array.from({ length: 10 }, (_, i) => ({
              label: `${(i + 1) * 10} per page`,
              value: (i + 1) * 10,
            }))"
            :page-slot="5"
            @update:page="loadData"
            @update:page-size="loadData"
          />
        </NSpace>
      </template>
    </NCard>

    <Detail ref="detailRef" @submit="loadData" />

    <NModal v-model:show="promptShow" preset="card" title="Prompt" class="w-800px fixed-height-modal">
      <div class="modal-scroll-body">
        <NText>SHA-256: {{ promptData?.defaultPromptSha256 }}</NText>
        <NCode class="mt-2" :code="promptData?.defaultPrompt || ''" word-wrap />
      </div>
    </NModal>
  </div>
</template>

<style scoped>
.modal-scroll-body {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 4px;
}
</style>

<style>
.fixed-height-modal.n-card {
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.fixed-height-modal.n-card .n-card__content {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
</style>
