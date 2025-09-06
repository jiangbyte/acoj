<script lang="ts" setup>
// 定义 props 和 emits
const props = defineProps<{
  value?: string // 接收父组件传递的值
  title?: string
}>()

const emit = defineEmits<{
  (e: 'update:value', value: string): void // 定义更新事件
  (e: 'update:select', value: string): void // 定义更新事件
  // 更新标题
  (e: 'update:title', value: string): void
}>()

const showModal = ref(false)
const current = ref<number | undefined>(1)
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

const loading = ref(false)
const checkedRowKeys = ref<string[]>([])
async function loadData() {
  loading.value = true
//   const { data } = await proProblemPage(pageParam.value)
//   if (data) {
//     pageData.value = data
//     loading.value = false
//   }
}

loadData()

const rowData = ref()
const problemReportConfigData = ref({
  problemId: '',
  useProblemThreshold: true,
  threshold: 0.0,
  useSingleLanguage: true,
  languages: [],
  useSingleSubmitUser: true,
  useGlobalSubmitUser: true,
  submitUsers: [],
  sampleCount: 0,
})
function doOpen(row: any = null) {
  showModal.value = true
  //   loadData()
  console.log(row)
  rowData.value = row
  problemReportConfigData.value.problemId = row.id
}
// 获取语言选项
const languageOptions = computed(() => {
  // if (!detailData.value?.codeTemplate)
  //   return []
  return rowData.value?.allowedLanguages.map((language: any) => ({
    label: language.toUpperCase(),
    value: language,
  }))
})
defineExpose({
  doOpen,
})
function toggleShow(show: boolean) {
  showModal.value = show
  if (!show) {
    checkedRowKeys.value = []
    current.value = 1
    problemReportConfigData.value = {
      problemId: '',
      useProblemThreshold: true,
      threshold: 0.0,
      useSingleLanguage: true,
      languages: [],
      useSingleSubmitUser: true,
      useGlobalSubmitUser: true,
      submitUsers: [],
      sampleCount: 0,
    }
  }
}

function handleSubmit() {
  if (current.value === 3) {
    return
  }
  current.value++
//   if (checkedRowKeys.value.length > 0) {
//     emit('update:value', checkedRowKeys.value[0]) // 发送更新事件
//     emit('update:select', checkedRowKeys.value[0]) // 发送更新事件
//     // 更新标题
//     const selectedItem = pageData.value?.records.find((item: any) => item.id === checkedRowKeys.value[0])
//     emit('update:title', selectedItem?.title)
//   }
//   toggleShow(false)
}

// 添加监听，当选择变化时立即更新
watch(checkedRowKeys, (newVal) => {
  if (newVal.length > 0) {
    emit('update:value', newVal[0])
    // emit('update:select', newVal[0])
  }
}, { deep: true })

function handlePrev() {
  if (current.value === 1) {
    return
  }
  current.value--
}
function handleNext() {
  if (current.value === 3) {
    return
  }
  current.value++
}
</script>

<template>
  <n-modal
    v-model:show="showModal"
    class="w-250 h-160"
    :mask-closable="false"
    preset="card"
    title="相似报告生成"
    @update:show="toggleShow"
  >
    <n-steps :current="current" class="w-full">
      <n-step
        title="第一步：检测配置"
        description="设置选择阈值、样本集范围等参数"
      />
      <n-step
        title="第二步：任务执行"
        description="生成检测任务执行中"
      />
      <n-step
        title="第三步：完成生成"
        description="查看生成的检测报告"
      />
    </n-steps>
    <n-form
      v-if="current === 1"
      ref="formRef"
      label-placement="top"
      class="mt-4"
      :model="problemReportConfigData"
    >
      <n-grid :cols="24" :x-gap="24">
        <n-form-item-gi :span="24" label="题目信息" path="problemId">
          <n-space vertical>
            <n-text>
              题目名称：{{ rowData.title }}
            </n-text>
            <n-text>
              题目ID：{{ rowData.id }}
            </n-text>
          </n-space>
          <!-- <n-input :disabled="true" :value="rowData.id" placeholder="请输入题目ID" /> -->
        </n-form-item-gi>
        <n-form-item-gi :span="12" label="是否使用题目自身阈值" path="useProblemThreshold">
          <n-switch v-model:value="problemReportConfigData.useProblemThreshold" />
        </n-form-item-gi>
        <n-form-item-gi :span="12" label="阈值" path="threshold">
          <n-input-number v-if="!problemReportConfigData.useProblemThreshold" v-model:value="problemReportConfigData.threshold" :min="0" :max="1.0" placeholder="请输入阈值" :step="0.1" :precision="1" />

          <n-input-number v-if="problemReportConfigData.useProblemThreshold" :value="rowData.threshold" :disabled="true" />
        </n-form-item-gi>
        <n-form-item-gi :span="12" label="使用单种编程语言" path="useSingleLanguage">
          <n-switch v-model:value="problemReportConfigData.useSingleLanguage" />
        </n-form-item-gi>
        <n-form-item-gi :span="12" label="编程语言" path="languages">
          <n-select v-model:value="problemReportConfigData.languages" :options="languageOptions" :multiple="!problemReportConfigData.useSingleLanguage" placeholder="请选择编程语言" />
        </n-form-item-gi>
        <n-form-item-gi :span="6" label="是否对全局用户进行检测" path="useGlobalSubmitUser">
          <n-switch v-model:value="problemReportConfigData.useGlobalSubmitUser" />
        </n-form-item-gi>
        <n-form-item-gi v-if="!problemReportConfigData.useGlobalSubmitUser" :span="6" label="使用单种提交用户" path="useSingleSubmitUser">
          <n-switch v-model:value="problemReportConfigData.useSingleSubmitUser" />
        </n-form-item-gi>
        <n-form-item-gi v-if="!problemReportConfigData.useGlobalSubmitUser" :span="12" label="通过提交的用户" path="submitUsers">
          <n-select v-model:value="problemReportConfigData.submitUsers" :multiple="!problemReportConfigData.useSingleSubmitUser && !problemReportConfigData.useGlobalSubmitUser" placeholder="请选择编程语言" />
        </n-form-item-gi>
        <n-form-item-gi :span="24" label="样本数" path="sampleCount">
          <n-input-number v-model:value="problemReportConfigData.sampleCount" placeholder="样本数" />
        </n-form-item-gi>
      </n-grid>
    </n-form>
    <n-empty v-if="current === 2" class="flex items-center justify-center h-full w-full">
      <template #extra>
        <n-button type="primary" @click="$router.push('/report')">
          报告列表
        </n-button>
      </template>
      执行中，任务ID：154044687656314674，可在检测报告列表中查询到
      <template #icon>
        <icon-park-outline-loading class="rotating-icon" />
      </template>
    </n-empty>
    <template #action>
      <NSpace align="center" justify="space-between" class="w-full">
        <NSpace align="center" />
        <NSpace align="center">
          <!-- <n-button type="primary" round :disabled="current === 1" @click="handlePrev">
            上一步
          </n-button>
          <n-button type="primary" round :disabled="current === 3" @click="handleNext">
            下一步
          </n-button> -->
        </NSpace>

        <n-space align="center" justify="end">
          <n-button v-if="current === 1" @click="toggleShow(false)">
            取消
          </n-button>
          <n-button v-if="current === 2" @click="toggleShow(false)">
            关闭
          </n-button>
          <n-button v-if="current === 1" type="primary" @click="handleSubmit">
            下一步（执行）
          </n-button>
        </n-space>
      </NSpace>
    </template>
  </n-modal>
</template>

<style scoped>
.rotating-icon {
    animation: rotate 1.5s linear infinite;
    filter: drop-shadow(0 0 0 rgba(85, 15, 215, 1));
}

@keyframes rotate {
    from {
        transform: rotate(0deg);
    }
    to {
        transform: rotate(360deg);
    }
}
</style>
