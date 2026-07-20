<script setup lang="ts">
import { ojProblemDatasetApi, ojProblemTestCaseApi } from '@/api'
import DictSelect from '@/components/common/DictSelect.vue'
import { ProCard } from 'pro-naive-ui'
import { computed, h, reactive, ref } from 'vue'
import { NButton, NDataTable, NDrawer, NDrawerContent, NFlex, NForm, NFormItem, NInput, NInputNumber, NModal, NSelect, NSpace, NSpin, NSwitch } from 'naive-ui'

const visible = ref(false)
const loading = ref(false)
const problemId = ref<string | null>(null)
const editingRow = ref<any>(null)
const showCaseEditor = ref(false)

const state = reactive({
  datasets: [] as any[],
  activeDatasetId: null as string | null,
  datasetForm: {
    name: '',
    version: '',
    is_active: false,
    data_zip_url: '',
    generator_url: '',
    checker: '',
    checker_args: '{}',
    output_prefix: null as number | null,
    output_limit: null as number | null,
    unicode_enabled: false,
    extra: '{}',
  },
  testCases: [] as any[],
  testCaseTotal: 0,
  testCasePage: 1,
  testCasePageSize: 50,
  caseForm: {
    case_no: 0,
    case_type: 'NORMAL',
    input_file: '',
    output_file: '',
    input_inline: '',
    output_inline: '',
    points: null as number | null,
    is_pretest: false,
    batch_no: null as number | null,
    batch_dependencies: '',
    time_limit_ms: null as number | null,
    memory_limit_kb: null as number | null,
    checker: '',
    checker_args: '{}',
    generator_args: '',
    sort: 0,
  },
})

const datasetOptions = computed(() =>
  state.datasets.map((d: any) => ({ label: `${d.name} (${d.version})`, value: d.id })),
)

function parseJson(value: string) {
  try { return JSON.parse(value || '{}') } catch { return {} }
}

async function openDrawer(id: string) {
  visible.value = true
  problemId.value = id
  await refreshDatasets()
}

async function refreshDatasets() {
  if (!problemId.value) return
  loading.value = true
  try {
    const resp = await ojProblemDatasetApi.page({ problem_id: problemId.value, size: 100 })
    state.datasets = resp.data?.records ?? []
    const active = state.datasets.find((d: any) => d.is_active) ?? state.datasets[0]
    if (active) {
      state.activeDatasetId = active.id
      await selectDataset(active.id)
    } else {
      state.activeDatasetId = null
      state.testCases = []
    }
  } finally {
    loading.value = false
  }
}

async function selectDataset(id: string) {
  state.activeDatasetId = id
  state.testCasePage = 1
  const ds = state.datasets.find((d: any) => d.id === id)
  if (!ds) return
  state.datasetForm = {
    name: ds.name ?? '',
    version: ds.version ?? '',
    is_active: ds.is_active ?? false,
    data_zip_url: ds.data_zip_url ?? '',
    generator_url: ds.generator_url ?? '',
    checker: ds.checker ?? '',
    checker_args: JSON.stringify(ds.checker_args ?? {}, null, 2),
    output_prefix: ds.output_prefix ?? null,
    output_limit: ds.output_limit ?? null,
    unicode_enabled: ds.unicode_enabled ?? false,
    extra: JSON.stringify(ds.extra ?? {}, null, 2),
  }
  await fetchTestCases()
}

async function fetchTestCases() {
  if (!state.activeDatasetId) return
  const resp = await ojProblemTestCaseApi.page({
    dataset_id: state.activeDatasetId,
    current: state.testCasePage,
    size: state.testCasePageSize,
  })
  state.testCases = resp.data?.records ?? []
  state.testCaseTotal = resp.data?.total ?? 0
}

function onTestCasePageChange(page: number) {
  state.testCasePage = page
  fetchTestCases()
}

async function saveDatasetConfig() {
  if (!problemId.value || !state.activeDatasetId) return
  const payload = {
    ...state.datasetForm,
    problem_id: problemId.value,
    checker_args: parseJson(state.datasetForm.checker_args),
    extra: parseJson(state.datasetForm.extra),
  }
  await ojProblemDatasetApi.update({ id: state.activeDatasetId, ...payload })
  window.$message.success('数据集配置已保存')
  await refreshDatasets()
}

async function addDataset() {
  if (!problemId.value) return
  const resp = await ojProblemDatasetApi.create({
    problem_id: problemId.value,
    name: `v${state.datasets.length + 1}`,
    version: `v${state.datasets.length + 1}`,
    is_active: state.datasets.length === 0,
    checker_args: {},
    extra: {},
  })
  state.activeDatasetId = resp.data?.id
  await refreshDatasets()
}

function confirmDeleteDataset() {
  if (!state.activeDatasetId) return
  window.$dialog?.warning({
    title: '删除数据集',
    content: '确定删除该数据集及其所有测试点?',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      await ojProblemDatasetApi.remove({ ids: [state.activeDatasetId!] })
      state.activeDatasetId = null
      await refreshDatasets()
      window.$message.success('已删除')
    },
  })
}

async function addTestCase() {
  if (!state.activeDatasetId) return
  const maxNo = Math.max(0, ...state.testCases.map((tc: any) => tc.case_no ?? 0))
  await ojProblemTestCaseApi.create({
    dataset_id: state.activeDatasetId,
    case_no: maxNo + 1,
    case_type: 'NORMAL',
    is_pretest: false,
    sort: state.testCases.length,
  })
  await selectDataset(state.activeDatasetId)
}

function openCaseEditor(row?: any) {
  if (row) {
    editingRow.value = row
    state.caseForm = {
      case_no: row.case_no ?? 0,
      case_type: row.case_type ?? 'NORMAL',
      input_file: row.input_file ?? '',
      output_file: row.output_file ?? '',
      input_inline: row.input_inline ?? '',
      output_inline: row.output_inline ?? '',
      points: row.points ?? null,
      is_pretest: row.is_pretest ?? false,
      batch_no: row.batch_no ?? null,
      batch_dependencies: row.batch_dependencies ? (Array.isArray(row.batch_dependencies) ? row.batch_dependencies.join(',') : String(row.batch_dependencies)) : '',
      time_limit_ms: row.time_limit_ms ?? null,
      memory_limit_kb: row.memory_limit_kb ?? null,
      checker: row.checker ?? '',
      checker_args: JSON.stringify(row.checker_args ?? {}, null, 2),
      generator_args: row.generator_args ?? '',
      sort: row.sort ?? 0,
    }
  } else {
    editingRow.value = null
    state.caseForm = {
      case_no: Math.max(0, ...state.testCases.map((tc: any) => tc.case_no ?? 0)) + 1,
      case_type: 'NORMAL',
      input_file: '', output_file: '', input_inline: '', output_inline: '',
      points: null, is_pretest: false, batch_no: null,
      batch_dependencies: '',
      time_limit_ms: null, memory_limit_kb: null,
      checker: '', checker_args: '{}', generator_args: '',
      sort: state.testCases.length,
    }
  }
  showCaseEditor.value = true
}

async function saveCase() {
  if (!state.activeDatasetId) return
  const payload = {
    ...state.caseForm,
    dataset_id: state.activeDatasetId,
    batch_dependencies: state.caseForm.batch_dependencies
      ? state.caseForm.batch_dependencies.split(',').map((s: string) => Number(s.trim())).filter((n: number) => !isNaN(n))
      : [],
    checker_args: parseJson(state.caseForm.checker_args),
  }
  if (editingRow.value) {
    await ojProblemTestCaseApi.update({ id: editingRow.value.id, ...payload })
  } else {
    await ojProblemTestCaseApi.create(payload)
  }
  showCaseEditor.value = false
  await selectDataset(state.activeDatasetId)
  window.$message.success(editingRow.value ? '测试点已更新' : '测试点已添加')
}

function confirmDeleteTestCase(row: any) {
  window.$dialog?.warning({
    title: '删除测试点',
    content: `确定删除测试点 #${row.case_no}?`,
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      await ojProblemTestCaseApi.remove({ ids: [row.id] })
      await selectDataset(state.activeDatasetId!)
      window.$message.success('已删除')
    },
  })
}

function closeDrawer() { visible.value = false }

defineExpose({ openDrawer })
</script>

<template>
  <NDrawer v-model:show="visible" :width="1100" mask-closable placement="right">
    <NDrawerContent title="数据管理" closable @close="closeDrawer">
      <NSpin :show="loading">
        <NFlex vertical :size="16">
          <!-- Dataset selector bar -->
          <NFlex align="center" justify="space-between">
            <NSelect v-model:value="state.activeDatasetId" :options="datasetOptions" style="width: 360px" placeholder="选择数据集" @update:value="selectDataset" />
            <NSpace>
              <NButton size="small" @click="addDataset">新建数据集</NButton>
              <NButton size="small" danger :disabled="!state.activeDatasetId" @click="confirmDeleteDataset">删除</NButton>
            </NSpace>
          </NFlex>

          <template v-if="state.activeDatasetId">
            <!-- Dataset config -->
            <ProCard title="数据集配置" :show-collapse="true" :collapsed="false">
              <NGrid :cols="3" :x-gap="16" :y-gap="12">
                <NGi><NFormItem label="名称"><NInput v-model:value="state.datasetForm.name" /></NFormItem></NGi>
                <NGi><NFormItem label="版本"><NInput v-model:value="state.datasetForm.version" /></NFormItem></NGi>
                <NGi><NFormItem label="激活"><NSwitch v-model:value="state.datasetForm.is_active" /></NFormItem></NGi>
              </NGrid>
              <NFormItem label="数据包 URL"><NInput v-model:value="state.datasetForm.data_zip_url" placeholder="测试数据 zip 文件 URL" /></NFormItem>
              <NFormItem label="生成器 URL"><NInput v-model:value="state.datasetForm.generator_url" /></NFormItem>
              <NGrid :cols="3" :x-gap="16" :y-gap="12">
                <NGi><NFormItem label="Check 器"><NInput v-model:value="state.datasetForm.checker" /></NFormItem></NGi>
                <NGi><NFormItem label="输出前缀"><NInputNumber v-model:value="state.datasetForm.output_prefix" class="w-full" clearable /></NFormItem></NGi>
                <NGi><NFormItem label="输出上限"><NInputNumber v-model:value="state.datasetForm.output_limit" class="w-full" clearable /></NFormItem></NGi>
              </NGrid>
              <NFlex align="center">
                <NFormItem label="Unicode"><NSwitch v-model:value="state.datasetForm.unicode_enabled" /></NFormItem>
                <NButton size="small" type="primary" @click="saveDatasetConfig">保存配置</NButton>
              </NFlex>
            </ProCard>

            <!-- Test Cases -->
            <ProCard title="测试点" :show-collapse="false">
              <template #header-extra>
                <NButton size="small" @click="addTestCase">新增测试点</NButton>
              </template>
              <NDataTable
                :columns="[
                  { title: '#', key: 'case_no', width: 60 },
                  { title: '类型', key: 'case_type', width: 100 },
                  { title: '输入文件', key: 'input_file', width: 160, ellipsis: { tooltip: true } },
                  { title: '输出文件', key: 'output_file', width: 160, ellipsis: { tooltip: true } },
                  { title: '分数', key: 'points', width: 70 },
                  { title: '预测试', key: 'is_pretest', width: 70,
                    render: (row: any) => row.is_pretest ? '预' : '-' },
                  { title: '批次', key: 'batch_no', width: 60 },
                  { title: '排序', key: 'sort', width: 60 },
                  { title: '操作', key: 'actions', width: 120, fixed: 'right',
                    render: (row: any) => h('span', [h(NButton, { text: true, size: 'tiny', type: 'primary', onClick: () => openCaseEditor(row) }, { default: () => '编辑' }), h(NButton, { text: true, size: 'tiny', type: 'error', onClick: () => confirmDeleteTestCase(row), style: 'margin-left:8px' }, { default: () => '删除' })]) },
                ]"
                :data="state.testCases"
                size="small"
                :bordered="false"
                :single-line="false"
                :scroll-x="900"
                :pagination="{
                  page: state.testCasePage,
                  pageSize: state.testCasePageSize,
                  itemCount: state.testCaseTotal,
                  showSizePicker: true,
                  pageSizes: [20, 50, 100],
                  onUpdatePage: onTestCasePageChange,
                  onUpdatePageSize: (size: number) => { state.testCasePageSize = size; state.testCasePage = 1; fetchTestCases() },
                }"
              ></NDataTable>
            </ProCard>
          </template>
        </NFlex>
      </NSpin>
    </NDrawerContent>
  </NDrawer>

  <!-- Test case editor modal -->
  <NModal v-model:show="showCaseEditor" preset="card" title="编辑测试点" style="width: 700px" mask-closable draggable>
    <NForm :model="state.caseForm" label-placement="left" label-width="140">
      <NGrid :cols="2" :x-gap="16" :y-gap="12">
        <NGi><NFormItem label="编号"><NInputNumber v-model:value="state.caseForm.case_no" class="w-full" /></NFormItem></NGi>
        <NGi><NFormItem label="类型"><DictSelect v-model:value="state.caseForm.case_type" dict-code="OJ_TEST_CASE_TYPE" /></NFormItem></NGi>
      </NGrid>
      <NGrid :cols="2" :x-gap="16" :y-gap="12">
        <NGi><NFormItem label="输入文件"><NInput v-model:value="state.caseForm.input_file" /></NFormItem></NGi>
        <NGi><NFormItem label="输出文件"><NInput v-model:value="state.caseForm.output_file" /></NFormItem></NGi>
      </NGrid>
      <NGrid :cols="2" :x-gap="16" :y-gap="12">
        <NGi><NFormItem label="输入 (内联)"><NInput v-model:value="state.caseForm.input_inline" type="textarea" :autosize="{ minRows: 2, maxRows: 6 }" /></NFormItem></NGi>
        <NGi><NFormItem label="输出 (内联)"><NInput v-model:value="state.caseForm.output_inline" type="textarea" :autosize="{ minRows: 2, maxRows: 6 }" /></NFormItem></NGi>
      </NGrid>
      <NGrid :cols="3" :x-gap="16" :y-gap="12">
        <NGi><NFormItem label="分数"><NInputNumber v-model:value="state.caseForm.points" class="w-full" clearable /></NFormItem></NGi>
        <NGi><NFormItem label="预测试"><NSwitch v-model:value="state.caseForm.is_pretest" /></NFormItem></NGi>
        <NGi><NFormItem label="批次号"><NInputNumber v-model:value="state.caseForm.batch_no" class="w-full" clearable /></NFormItem></NGi>
      </NGrid>
      <NGrid :cols="2" :x-gap="16" :y-gap="12">
        <NGi><NFormItem label="时间上限 (ms)"><NInputNumber v-model:value="state.caseForm.time_limit_ms" class="w-full" clearable /></NFormItem></NGi>
        <NGi><NFormItem label="内存上限 (KB)"><NInputNumber v-model:value="state.caseForm.memory_limit_kb" class="w-full" clearable /></NFormItem></NGi>
      </NGrid>
      <NGrid :cols="2" :x-gap="16" :y-gap="12">
        <NGi><NFormItem label="批次依赖"><NInput v-model:value="state.caseForm.batch_dependencies" placeholder="逗号分隔, 如 1,2" /></NFormItem></NGi>
        <NGi><NFormItem label="排序"><NInputNumber v-model:value="state.caseForm.sort" class="w-full" /></NFormItem></NGi>
      </NGrid>
      <NFormItem label="Check 器"><NInput v-model:value="state.caseForm.checker" /></NFormItem>
      <NFormItem label="Check 器参数"><NInput v-model:value="state.caseForm.checker_args" type="textarea" :autosize="{ minRows: 2, maxRows: 6 }" /></NFormItem>
      <NFormItem label="生成器参数"><NInput v-model:value="state.caseForm.generator_args" type="textarea" :autosize="{ minRows: 2, maxRows: 6 }" /></NFormItem>
    </NForm>
    <template #footer>
      <NSpace justify="end">
        <NButton @click="showCaseEditor = false">取消</NButton>
        <NButton type="primary" @click="saveCase">保存</NButton>
      </NSpace>
    </template>
  </NModal>
</template>
