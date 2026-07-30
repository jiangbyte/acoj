<script setup lang="tsx">
import type { DataTableColumns } from 'naive-ui'
import { ojProblemApi } from '@/api'
import { NButton } from 'naive-ui'
import { computed, reactive, watch } from 'vue'

export interface ProblemOption {
  id: string
  code: string
  name: string
}

const props = withDefaults(
  defineProps<{
    visible: boolean
    title?: string
  }>(),
  {
    title: '选择题目',
  },
)

const emit = defineEmits<{
  'update:visible': [value: boolean]
  select: [problem: ProblemOption]
}>()

const state = reactive({
  loading: false,
  code: '',
  name: '',
  options: [] as ProblemOption[],
  total: 0,
  page: 1,
  pageSize: 20,
})

const columns = computed<DataTableColumns<ProblemOption>>(() => [
  { title: '编码', key: 'code', width: 140, ellipsis: { tooltip: true } },
  { title: '标题', key: 'name', minWidth: 180, ellipsis: { tooltip: true } },
  {
    title: '操作',
    key: 'action',
    width: 70,
    align: 'center',
    render: (row) => (
      <NButton text type="primary" size="small" onClick={() => handleSelect(row)}>
        选择
      </NButton>
    ),
  },
])

watch(
  () => props.visible,
  (val) => {
    if (val) {
      state.code = ''
      state.name = ''
      state.page = 1
      void loadOptions()
    }
  },
)

watch(
  () => [state.page, state.pageSize],
  () => {
    if (props.visible) {
      void loadOptions()
    }
  },
)

async function loadOptions() {
  state.loading = true
  try {
    const params: Record<string, string | number> = {
      current: state.page,
      size: state.pageSize,
      status: 'published',
    }
    const code = state.code.trim()
    const name = state.name.trim()
    if (code) {
      params.code = code
    }
    if (name) {
      params.name = name
    }
    const res = await ojProblemApi.page(params)
    const records = res?.data?.records ?? []
    state.options = records.map((item: any) => ({
      id: String(item.id),
      code: item.code || '',
      name: item.name || '',
    }))
    state.total = res?.data?.total ?? 0
  } catch {
    state.options = []
    state.total = 0
  } finally {
    state.loading = false
  }
}

function handleSelect(problem: ProblemOption) {
  emit('select', problem)
  close()
}

function doSearch() {
  state.page = 1
  void loadOptions()
}

function resetSearch() {
  state.code = ''
  state.name = ''
  state.page = 1
  void loadOptions()
}

function close() {
  emit('update:visible', false)
}
</script>

<template>
  <NDrawer
    :show="visible"
    :default-width="720"
    placement="right"
    :mask-closable="false"
    @update:show="(val) => emit('update:visible', val)"
  >
    <NDrawerContent :title="title" closable :native-scrollbar="false">
      <NSpace vertical>
        <NInputGroup>
          <NInput
            v-model:value="state.code"
            clearable
            placeholder="题目编码"
            @keyup.enter="doSearch"
          />
          <NInput
            v-model:value="state.name"
            clearable
            placeholder="题目标题"
            @keyup.enter="doSearch"
          />
          <NButton type="primary" @click="doSearch">
            搜索
          </NButton>
          <NButton @click="resetSearch">
            重置
          </NButton>
        </NInputGroup>
        <NDataTable
          :row-key="(row: ProblemOption) => row.id"
          :columns="columns"
          :data="state.options"
          :loading="state.loading"
          :bordered="true"
          :single-line="false"
          max-height="calc(100vh - 290px)"
        />
        <NPagination
          v-model:page="state.page"
          v-model:page-size="state.pageSize"
          show-size-picker
          :item-count="state.total"
          :page-sizes="[10, 20, 50, 100]"
        />
      </NSpace>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="close">
            关闭
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>
