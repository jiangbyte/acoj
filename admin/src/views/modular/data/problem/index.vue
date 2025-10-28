<script lang="ts" setup>
import type { DataTableColumns } from 'naive-ui'
import { NButton, NCard, NDataTable, NPagination, NPopconfirm, NSpace, NTag } from 'naive-ui'
import { useDataProblemFetch } from '@/composables/v1'
import Form from './form.vue'
import Detail from './detail.vue'
import TestCasePage from './testcase.vue'

const formRef = ref()
const detailRef = ref()
const similaritySelectFormRef = ref()
const testcaseIndexRef = ref()
const columns: DataTableColumns<any> = [
  {
    type: 'selection',
  },
  // {
  //   title: '展示ID',
  //   key: 'displayId',
  // },
  {
    title: '分类',
    key: 'categoryName',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '标题',
    key: 'title',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: '来源',
    key: 'source',
    ellipsis: {
      tooltip: true,
    },
  },
  // {
  //   title: '链接',
  //   key: 'url',
  // },
  // {
  //   title: '时间限制',
  //   key: 'maxTime',
  //   width: 110,
  // },
  // {
  //   title: '内存限制',
  //   key: 'maxMemory',
  //   width: 110,
  // },
  // {
  //   title: '描述',
  //   key: 'description',
  // },
  // {
  //   title: '用例',
  //   key: 'testCase',
  // },
  // {
  //   title: '开放语言',
  //   key: 'allowedLanguages',
  // },
  {
    title: '难度',
    key: 'difficultyName',
  },
  {
    title: '阈值',
    key: 'threshold',
  },
  // {
  //   title: '使用模板',
  //   key: 'useTemplateName',
  // },
  // {
  //   title: '模板代码',
  //   key: 'codeTemplate',
  // },
  {
    title: '公开',
    key: 'isPublicName',
    render: (row) => {
      return h(NTag, {
        type: row.isPublic ? 'primary' : 'error',
      }, () => row.isPublicName)
    },
  },
  {
    title: '上架',
    key: 'isVisibleName',
    render: (row) => {
      return h(NTag, {
        type: row.isVisible ? 'primary' : 'error',
      }, () => row.isVisibleName)
    },
  },
  {
    title: 'AI',
    key: 'useAiName',
    render: (row) => {
      return h(NTag, {
        type: row.useAi ? 'primary' : 'error',
      }, () => row.useAiName)
    },
  },
  {
    title: '解决',
    key: 'solved',
  },
  {
    title: '操作',
    key: 'action',
    width: 280,
    fixed: 'right',
    render(row: any) {
      return h(NSpace, { align: 'center' }, () => [
        h(NButton, {
          type: 'primary',
          size: 'small',
          onClick: () => formRef.value.doOpen(row, true),
        }, () => '编辑'),
        h(NButton, { size: 'small', onClick: () => detailRef.value.doOpen(row) }, () => '详情'),
        h(NButton, {
          type: 'primary',
          size: 'small',
          onClick: () => testcaseIndexRef.value.doOpen(row, true),
        }, () => '用例数据'),
        // h(NButton, {
        //   type: 'primary',
        //   size: 'small',
        //   onClick: () => similaritySelectFormRef.value.doOpen(null, row.id, false),
        // }, () => '相似检测'),
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
  sortField: 'id',
  sortOrder: 'DESCEND',
  keyword: '',
})

function resetHandle() {
  pageParam.value.keyword = ''
  loadData()
}

const { dataProblemPage, dataProblemDelete } = useDataProblemFetch()
const loading = ref(false)
async function loadData() {
  loading.value = true
  const { data } = await dataProblemPage(pageParam.value)
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
  const { success } = await dataProblemDelete(param)
  if (success) {
    window.$message.success('删除成功')
    await loadData()
  }
}
const checkedRowKeys = ref<string[]>([])
async function deleteBatchHandle() {
  const param = checkedRowKeys.value.map(id => ({ id }))
  const { success } = await dataProblemDelete(param)
  if (success) {
    window.$message.success('删除成功')
    await loadData()
    checkedRowKeys.value = []
  }
}
const problemImportRef = ref()
</script>

<template>
  <div class="flex flex-col h-full w-full">
    <NCard size="small">
      <NSpace vertical>
        <NSpace align="center">
          <NSpace align="center">
            <NFormItem :show-feedback="false" label="关键词" label-placement="left">
              <NInput v-model:value="pageParam.keyword" placeholder="请输入关键词" clearable @clear="resetHandle" />
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
            <NButton type="primary" @click="formRef.doOpen(null, false)">
              <template #icon>
                <IconParkOutlinePlus />
              </template>
              创建
            </NButton>
            <!-- <NButton type="primary">
              <template #icon>
                <IconParkOutlinePlus />
              </template>
              导入
            </NButton> -->
            <!-- <ProblemImport model-value="" buttontext="导入" @success="loadData()" /> -->
            <NButton type="primary" @click="problemImportRef.doOpen()">
              <template #icon>
                <IconParkOutlinePlus />
              </template>
              导入
            </NButton>
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
            show-size-picker
            :page-sizes="Array.from({ length: 10 }, (_, i) => ({
              label: `${(i + 1) * 10} 每页`,
              value: (i + 1) * 10,
            }))"
            :page-slot="5"
            @update:page="loadData"
            @update:page-size="loadData"
          />
        </NSpace>
      </template>
    </NCard>

    <Form ref="formRef" @submit="loadData" />
    <Detail ref="detailRef" @submit="loadData" />
    <SimilaritySelect ref="similaritySelectFormRef" />
    <TestCasePage ref="testcaseIndexRef" />
    <ProblemImport ref="problemImportRef" @success="loadData" />
  </div>
</template>

<style scoped>

</style>
