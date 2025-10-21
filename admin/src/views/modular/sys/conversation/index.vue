<script lang="ts" setup>
import type { DataTableColumns } from 'naive-ui'
import { NAvatar, NButton, NCard, NDataTable, NPagination, NPopconfirm, NSpace, NText } from 'naive-ui'
import { useSysConversationFetch } from '@/composables/v1'
import Form from './form.vue'
import Detail from './detail.vue'

const formRef = ref()
const detailRef = ref()
const columns: DataTableColumns<any> = [
  {
    type: 'selection',
  },
  {
    title: '对话ID',
    key: 'conversationId',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '消息角色',
    key: 'messageRoleName',
  },
  {
    title: '用户',
    key: 'user',
    width: 140,
    render(row: any) {
      return row.userId
        ? h(
            NSpace,
            { align: 'center', size: 'small' },
            {
              default: () => [
                h(
                  NAvatar,
                  {
                    size: 'small',
                    round: true,
                    src: row.userAvatar,
                  },
                  {},
                ),
                h(
                  NText,
                  {},
                  { default: () => row.userIdName },
                ),
              ],
            },
          )
        : ''
    },
  },
  {
    title: '题目',
    key: 'problemIdName',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '题集',
    key: 'setIdName',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '题集内对话',
    key: 'isSetName',
  },
  {
    title: '消息类型',
    key: 'messageTypeName',
  },
  // {
  //   title: '消息内容',
  //   key: 'messageContent',
  // },
  // {
  //   title: '用户代码',
  //   key: 'userCode',
  // },
  // {
  //   title: '代码语言',
  //   key: 'languageName',
  // },
  // {
  //   title: '提示Tokens',
  //   key: 'promptTokens',
  // },
  // {
  //   title: '完成Tokens',
  //   key: 'completionTokens',
  // },
  // {
  //   title: '总Tokens',
  //   key: 'totalTokens',
  // },
  // {
  //   title: '响应时间',
  //   key: 'responseTime',
  // },
  // {
  //   title: '流式传输总耗时',
  //   key: 'streamingDuration',
  // },
  // {
  //   title: '状态',
  //   key: 'status',
  // },
  // {
  //   title: '错误信息',
  //   key: 'errorMessage',
  // },
  {
    title: '用户平台',
    key: 'userPlatform',
  },
  // {
  //   title: 'IP地址',
  //   key: 'ipAddress',
  // },
  {
    title: '操作',
    key: 'action',
    width: 140,
    fixed: 'right',
    render(row: any) {
      return h(NSpace, { align: 'center' }, () => [
        // h(NButton, {
        //   type: 'primary',
        //   size: 'small',
        //   onClick: () => formRef.value.doOpen(row, true),
        // }, () => '编辑'),
        h(NButton, { size: 'small', onClick: () => detailRef.value.doOpen(row) }, () => '详情'),
        h(NPopconfirm, {
          onPositiveClick: () => deleteHandle(row),
        }, {
          default: () => '确认删除',
          trigger: () => h(NButton, { size: 'small', type: 'error' }, () => '删除'),
        }),
      ])
    },
  },
]

const columnSortFieldOptions = computed<any[]>(() => {
  return [
    { label: 'ID', value: 'id' },
    { label: '创建时间', value: 'createTime' },
    { label: '更新时间', value: 'updateTime' },
  ]
})
const sortOrderOptions = computed(() => [
  { label: '升序', value: 'ASCEND' },
  { label: '降序', value: 'DESCEND' },
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
  sortField: null,
  sortOrder: null,
  keyword: '',
})

function resetHandle() {
  pageParam.value.keyword = ''
  loadData()
}

const { sysConversationPage, sysConversationDelete } = useSysConversationFetch()
const loading = ref(false)
async function loadData() {
  loading.value = true
  const { data } = await sysConversationPage(pageParam.value)
  if (data) {
    pageData.value = data
    loading.value = false
  }
}

loadData()

async function deleteHandle(row: any) {
  const param = [{
    id: row.id,
  }]
  const { success } = await sysConversationDelete(param)
  if (success) {
    window.$message.success('删除成功')
    await loadData()
  }
}
const checkedRowKeys = ref<string[]>([])
async function deleteBatchHandle() {
  const param = checkedRowKeys.value.map(id => ({ id }))
  const { success } = await sysConversationDelete(param)
  if (success) {
    window.$message.success('删除成功')
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
            <NFormItem :show-feedback="false" label="关键词" label-placement="left">
              <NInput v-model:value="pageParam.keyword" placeholder="请输入关键词" />
            </NFormItem>
            <NSpace align="center">
              <NButton type="primary" @click="loadData">
                <template #icon>
                  <IconParkOutlineSearch />
                </template>
                搜索
              </NButton>
              <NButton type="warning" @click="resetHandle">
                <template #icon>
                  <IconParkOutlineRefresh />
                </template>
                重置
              </NButton>
            </NSpace>
          </NSpace>
        </NSpace>
        <NSpace align="center" justify="space-between">
          <NSpace align="center">
            <!-- <NButton type="primary" @click="formRef.doOpen(null, false)">
              <template #icon>
                <IconParkOutlinePlus />
              </template>
              创建
            </NButton> -->
            <!-- <NButton type="primary">
              对话内存
            </NButton> -->
            <NPopconfirm v-if="checkedRowKeys.length > 0" @positive-click="deleteBatchHandle">
              <template #default>
                确认删除
              </template>
              <template #trigger>
                <NButton type="error">
                  删除
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
        :row-key="(row: any) => row.id"
        :loading="loading"
        flex-height
        :scroll-x="1400"
        class="flex-1 h-full"
      />
      <template #action>
        <NSpace align="center" justify="space-between" class="w-full">
          <NSpace align="center">
            <NP type="info" show-icon>
              当前数据 {{ pageData?.records.length }} 条
            </NP>
            <NP type="info" show-icon>
              选中了 {{ checkedRowKeys.length }} 行
            </NP>
          </NSpace>
          <NPagination
            v-model:page="pageParam.current"
            v-model:page-size="pageParam.size"
            class="flex justify-end"
            :page-count="pageData ? Number(pageData.pages) : 0"
            @update:page="loadData"
            @update:page-size="loadData"
          />
        </NSpace>
      </template>
    </NCard>

    <Form ref="formRef" @submit="loadData" />
    <Detail ref="detailRef" @submit="loadData" />
  </div>
</template>

<style scoped>

</style>
