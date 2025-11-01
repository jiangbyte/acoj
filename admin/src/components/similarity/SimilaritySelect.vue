<!-- <script lang="ts" setup>
import { useDataLibraryFetch, useDataProblemFetch, useDataSetFetch, useSysGroupFetch, useTaskSimilarityFetch } from '@/composables/v1'
import type { DataTableColumns, SelectOption } from 'naive-ui'
import { NAvatar, NButton, NDrawer, NDrawerContent, NForm, NInputNumber, NSpace, NText } from 'naive-ui'
import { v4 as uuidv4 } from 'uuid'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const groupOptionsLoading = ref(false)
const groupOptions = ref<SelectOption[]>([])

const defaultFormData = {
  problemIds: [],
  setId: null,
  language: null,
  isSet: false,
  userIds: [],
  taskId: '',
  isGroup: false,
  groupId: null,
  batchTaskId: '',
  minMatchLength: 5,
  threshold: 0.5,

  current: 1,
  size: 20,
  sortField: 'id',
  sortOrder: 'ASCEND',
  keyword: '',
}

const formData = ref({ ...defaultFormData })
const rules = {
  problemIds: [{
    type: 'array',
    required: true,
    trigger: ['blur', 'change'],
    message: '请选择题目',
  }],
  setId: [
  ],
  language: [
    { required: true, message: '请选择语言', trigger: ['input', 'blur'] },
  ],
  minMatchLength: [
    { type: 'number', required: true, trigger: ['blur', 'change'], message: '请输入匹配敏感度' },
  ],
  threshold: [
    { type: 'number', required: true, trigger: ['blur', 'change'], message: '请输入阈值' },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...defaultFormData }
}

async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      if (formData.value.userIds.length <= 1) {
        window.$message.error('请选择至少两个用户')
        return
      }
      loading.value = true
      useTaskSimilarityFetch().taskSimilarityBatch(formData.value).then(async ({ data }) => {
        if (data) {
          console.log(data)
          // 等待2秒后查询
          // await new Promise(resolve => setTimeout(resolve, 2000))
          // useTaskSimilarityFetch().taskSimilarityProgress(data).then(({ data }) => {
          //   console.log(data)
          // })
          window.$message.success('提交成功，在后台执行中，请稍后查看结果')
        }
      })
      // emit('submit', true)
      // doClose()
      // console.log(formData.value)

      // show.value = false
      // loading.value = false
    }
    else {
      //
    }
  })
}

const isSetRef = ref(false)

const setProblemOptions = ref([])
const languageOptions = ref([])
function doOpen(sId: string = '', pid: string = '', isSet: boolean) {
  show.value = true
  isSetRef.value = isSet
  formData.value.isSet = isSet
  formData.value.batchTaskId = `task-${uuidv4()}`
  if (isSet) {
    formData.value.setId = sId
    useDataSetFetch().dataSetProblemWithSearch({ id: sId, keyword: '' }).then(({ data }) => {
      setProblemOptions.value = data
      console.log(data)
    })
    useDataSetFetch().dataProblemGetSetProblemLanguages({ id: sId, problemIds: formData.value.problemIds }).then(({ data }) => {
      if (data) {
        // 直接使用API返回的对象结构
        languageOptions.value = data.map((item: any) => ({
          label: item.label.charAt(0).toUpperCase() + item.label.slice(1),
          value: item.value,
        }))
      }
    })
  }
  else {
    formData.value.problemIds = [pid]
    useDataProblemFetch().dataProblemDetail({ id: pid }).then(({ data }) => {
      languageOptions.value = data.allowedLanguages.map((language: any) => ({
        label: language.charAt(0).toUpperCase() + language.slice(1),
        value: language,
      }))
    })
  }

  useSysGroupFetch().sysGroupAuthTree({ keyword: '' }).then(({ data }) => {
    groupOptions.value = data
    groupOptionsLoading.value = false
  })
  loadUserData()
}
defineExpose({
  doOpen,
})

function updateProblemIds(value: any) {
  console.log(value)
  useDataSetFetch().dataProblemGetSetProblemLanguages({ id: formData.value.setId, problemIds: value }).then(({ data }) => {
    if (data) {
      // 直接使用API返回的对象结构
      languageOptions.value = data.map((item: any) => ({
        label: item.label.charAt(0).toUpperCase() + item.label.slice(1),
        value: item.value,
      }))
    }
  })
  loadUserData()
}

const columns: DataTableColumns<any> = [
  {
    type: 'selection',
  },
  {
    title: '昵称',
    key: 'nickname',
    render(row: any) {
      return h(
        NSpace,
        { align: 'center', size: 'small' },
        {
          default: () => [
            h(
              NAvatar,
              {
                size: 'small',
                round: true,
                src: row.avatar,
              },
              {},
            ),
            h(
              NText,
              {},
              { default: () => row.nickname },
            ),
          ],
        },
      )
    },
  },
  {
    title: '用户组',
    key: 'groupIdName',
  },
]
const pageData = ref()
const pageParam = ref({
  current: 1,
  size: 20,
  sortField: 'id',
  sortOrder: 'ASCEND',
  keyword: '',
  groupId: '',
  problemIds: [],
  setId: '',
  language: '',
})
const { dataLibraryiUserPage } = useDataLibraryFetch()
async function loadUserData() {
  loading.value = true
  const { data } = await dataLibraryiUserPage(formData.value)
  if (data) {
    pageData.value = data
    loading.value = false
    console.log(data)
  }
}
function handleUserGroupChange(value: any) {
  pageParam.value.groupId = value
  loadUserData()
}
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="报告参数配置">
      <n-alert type="warning" class="mb-4">
        检测范围最多为近期
        <n-tag type="info" size="small">
          1000
        </n-tag>
        条有效数据。题集检测语言为题目中允许的语言交集
      </n-alert>
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <NFormItem v-if="isSetRef" label="题目" path="problemIds">
          <NSelect
            v-model:value="formData.problemIds"
            placeholder="请选择题目"
            label-field="title"
            value-field="id"
            filterable
            clearable
            multiple
            :options="setProblemOptions"
            @update:value="updateProblemIds"
          />
        </NFormItem>
        <NFormItem label="检测语言" path="language">
          <NSelect
            v-model:value="formData.language"
            placeholder="请选择开放语言"
            :options="languageOptions"
            clearable
            @update:value="loadUserData"
          />
        </NFormItem>
        <n-grid :cols="24" :x-gap="24">
          <n-form-item-gi :span="12" label="匹配敏感度" path="minMatchLength">
            <NInputNumber v-model:value="formData.minMatchLength" placeholder="请输入匹配敏感度" />
          </n-form-item-gi>
          <n-form-item-gi :span="12" label="阈值" path="threshold">
            <NInputNumber v-model:value="formData.threshold" placeholder="请输入检测阈值" />
          </n-form-item-gi>
        </n-grid>
      </NForm>
      <NCard size="small" class="flex-1 mb-4">
        <NSpace vertical>
          <NSpace align="center" justify="space-between">
            <NSpace align="center">
              <NText>
                用户组筛选
              </NText>
              <n-tree-select
                v-model:value="formData.groupId"
                :options="groupOptions"
                label-field="name"
                key-field="id"
                :indent="12"
                class="w-60"
                placeholder="请选择用户组"
                @update:value="handleUserGroupChange"
              />
            </NSpace>
            <NP type="info" show-icon>
              当前数据 {{ pageData?.records.length }} 条
            </NP>
          </NSpace>
          <NDataTable
            v-model:checked-row-keys="formData.userIds"
            :columns="columns"
            :data="pageData?.records"
            :bordered="false"
            :row-key="(row: any) => row.id"
            :loading="!pageData"
            flex-height
            class="h-90"
          />
        </NSpace>
        <template #action>
          <NSpace align="center" justify="space-between" class="w-full">
            <NSpace align="center">
              <NP type="info" show-icon>
                选中了 {{ formData.userIds?.length }} 行
              </NP>
            </NSpace>
            <NPagination
              v-model:page="pageParam.current"
              v-model:page-size="pageParam.size"
              class="flex justify-end"
              :page-count="pageData ? Number(pageData.pages) : 0"
              @update:page="loadUserData"
              @update:page-size="loadUserData"
            />
          </NSpace>
        </template>
      </NCard>
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            取消
          </NButton>
          <NButton type="primary" :loading="loading" @click="doSubmit">
            <template #icon>
              <IconParkOutlineSave />
            </template>
            开始检测
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style> -->




<script lang="ts" setup>
import { useDataLibraryFetch, useDataProblemFetch, useDataSetFetch, useSysGroupFetch, useTaskSimilarityFetch } from '@/composables/v1'
import type { SelectOption } from 'naive-ui'
import { NButton, NDrawer, NDrawerContent, NForm, NInputNumber, NSpace, NAlert, NTag, NCard, NTreeSelect, NRadioGroup, NRadio, NDatePicker, NProgress, NSlider } from 'naive-ui'
import { v4 as uuidv4 } from 'uuid'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()

// 系统限制常量
const SYSTEM_LIMITS = {
  MAX_SUBMISSIONS: 5000, // 最大提交数量
  WARNING_THRESHOLD: 1000, // 警告阈值
  OPTIMAL_RANGE: 500, // 最优范围
  TIMEOUT_MS: 300000, // 5分钟超时
  BATCH_SIZE: 1000 // 分批处理大小
}

const defaultFormData = {
  // 基础范围
  problemIds: [],
  setId: null,
  language: null,
  isSet: false,
  
  // 高级筛选策略
  compareStrategy: 'ac_only', // 默认只检测AC代码
  timeRange: null,
  timeWindow: 10, // 分钟
  sameErrorPattern: false,
  acOnly: true,
  
  // 对比方式
  compareMode: 'pairwise',
  seedSubmissionId: null,
  groupId: null,
  
  // 算法参数
  minMatchLength: 8,
  threshold: 0.8,
  
  // 性能控制
  sampleRate: 100, // 采样率百分比
  useSampling: false,
  
  taskId: '',
  batchTaskId: '',
}

const formData = ref({ ...defaultFormData })

// 估算数据
const submissionEstimate = ref({
  total: 0,
  filtered: 0,
  actual: 0, // 实际检测数量（考虑采样）
  riskLevel: 'low', // low, medium, high
  estimatedTime: 0, // 秒
  isOverLimit: false
})

const rules = {
  problemIds: [{
    type: 'array',
    required: true,
    trigger: ['blur', 'change'],
    message: '请选择题目',
  }],
  language: [
    { required: true, message: '请选择语言', trigger: ['input', 'blur'] },
  ]
}

function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...defaultFormData }
}

async function doSubmit() {
  if (submissionEstimate.value.isOverLimit) {
    window.$message.error(`检测数量超过系统限制（${SYSTEM_LIMITS.MAX_SUBMISSIONS}），请调整筛选条件`)
    return
  }

  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      
      const taskParams = {
        ...formData.value,
        batchTaskId: `task-${uuidv4()}`,
        estimatedCount: submissionEstimate.value.actual,
        riskLevel: submissionEstimate.value.riskLevel,
        systemLimits: SYSTEM_LIMITS
      }
      
      try {
        const { data } = await useTaskSimilarityFetch().taskSimilarityBatch(taskParams)
        if (data) {
          window.$message.success(`检测任务已提交，将处理 ${submissionEstimate.value.actual} 份提交`)
          emit('submit', data)
          doClose()
        }
      } catch (error) {
        window.$message.error('任务提交失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 估算提交数量
async function estimateSubmissionCount(): Promise<number> {
  // 模拟估算逻辑 - 实际中应该调用API
  let baseCount = formData.value.isSet ? 1200 : 400
  
  // 应用筛选条件
  if (formData.value.acOnly) baseCount = Math.floor(baseCount * 0.7)
  if (formData.value.compareStrategy === 'time_window') baseCount = Math.floor(baseCount * 0.4)
  if (formData.value.timeRange) baseCount = Math.floor(baseCount * 0.6)
  if (formData.value.language) baseCount = Math.floor(baseCount * 0.8)
  
  return baseCount
}

// 计算风险等级
function calculateRiskLevel(count: number): string {
  if (count > SYSTEM_LIMITS.MAX_SUBMISSIONS) return 'blocked'
  if (count > SYSTEM_LIMITS.WARNING_THRESHOLD) return 'high'
  if (count > SYSTEM_LIMITS.OPTIMAL_RANGE) return 'medium'
  return 'low'
}

// 计算预计时间
function calculateEstimatedTime(count: number): number {
  const baseTimePerSubmission = 0.1 // 秒
  let time = count * baseTimePerSubmission
  
  // 复杂策略增加时间
  if (formData.value.compareStrategy === 'same_error') time *= 1.5
  if (formData.value.minMatchLength < 6) time *= 1.2
  
  return Math.ceil(time)
}

// 更新估算信息
async function updateEstimate() {
  const total = await estimateSubmissionCount()
  const actual = formData.value.useSampling 
    ? Math.floor(total * formData.value.sampleRate / 100)
    : total
  
  const riskLevel = calculateRiskLevel(actual)
  const estimatedTime = calculateEstimatedTime(actual)
  
  submissionEstimate.value = {
    total,
    filtered: total,
    actual,
    riskLevel,
    estimatedTime,
    isOverLimit: actual > SYSTEM_LIMITS.MAX_SUBMISSIONS
  }
}

const isSetRef = ref(false)
const setProblemOptions = ref<SelectOption[]>([])
const languageOptions = ref<SelectOption[]>([])
const groupOptions = ref<SelectOption[]>([])

function doOpen(sId: string = '', pid: string = '', isSet: boolean) {
  show.value = true
  isSetRef.value = isSet
  formData.value.isSet = isSet
  
  // 初始化选项数据
  initializeOptions(isSet, sId, pid)
  updateEstimate()
}

defineExpose({
  doOpen,
})

function initializeOptions(isSet: boolean, sId: string, pid: string) {
  if (isSet) {
    formData.value.setId = sId
    setProblemOptions.value = [
      { label: '两数之和', value: '1' },
      { label: '反转链表', value: '2' },
      { label: '二叉树遍历', value: '3' }
    ]
    languageOptions.value = [
      { label: 'C++', value: 'cpp' },
      { label: 'Java', value: 'java' },
      { label: 'Python', value: 'python' }
    ]
  } else {
    formData.value.problemIds = [pid]
    languageOptions.value = [
      { label: 'C++', value: 'cpp' },
      { label: 'Java', value: 'java' },
      { label: 'Python', value: 'python' }
    ]
  }

  groupOptions.value = [
    { label: '计算机科学与技术1班', value: '1' },
    { label: '计算机科学与技术2班', value: '2' },
    { label: '软件工程1班', value: '3' }
  ]
}

function updateProblemIds(value: any) {
  formData.value.problemIds = value
  updateEstimate()
}

// 风险等级颜色映射
const riskLevelConfig = {
  low: { color: 'success', text: '低风险' },
  medium: { color: 'warning', text: '中等风险' },
  high: { color: 'error', text: '高风险' },
  blocked: { color: 'error', text: '已超限' }
}

// 自动启用采样当数据量过大
watch(() => submissionEstimate.value.filtered, (newVal) => {
  if (newVal > SYSTEM_LIMITS.WARNING_THRESHOLD && !formData.value.useSampling) {
    formData.value.useSampling = true
    formData.value.sampleRate = Math.floor(SYSTEM_LIMITS.OPTIMAL_RANGE / newVal * 100)
  }
})

// 监听所有影响估算的参数变化
watch(() => [
  formData.value.problemIds,
  formData.value.language,
  formData.value.compareStrategy,
  formData.value.acOnly,
  formData.value.timeRange,
  formData.value.useSampling,
  formData.value.sampleRate
], updateEstimate, { deep: true })
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="代码相似度检测配置">
      <NAlert type="info" class="mb-4">
        系统限制：最多处理 <NTag size="small" type="info">{{ SYSTEM_LIMITS.MAX_SUBMISSIONS }}</NTag> 份提交
      </NAlert>

      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="140">
        <!-- 基础检测范围 -->
        <NCard title="📊 检测范围" size="small" class="mb-4">
          <NFormItem v-if="isSetRef" label="题目选择" path="problemIds">
            <NSelect
              v-model:value="formData.problemIds"
              placeholder="请选择要检测的题目"
              label-field="label"
              value-field="value"
              filterable
              clearable
              multiple
              :max-tag-count="2"
              :options="setProblemOptions"
              @update:value="updateProblemIds"
            />
          </NFormItem>
          
          <NFormItem label="编程语言" path="language">
            <NSelect
              v-model:value="formData.language"
              placeholder="请选择检测语言"
              :options="languageOptions"
              clearable
            />
          </NFormItem>

          <NFormItem label="时间范围">
            <NDatePicker
              v-model:value="formData.timeRange"
              type="datetimerange"
              clearable
              placeholder="选择时间范围（可选）"
            />
          </NFormItem>
        </NCard>

        <!-- 检测策略 -->
        <NCard title="🎯 检测策略" size="small" class="mb-4">
          <NFormItem label="对比模式">
            <NRadioGroup v-model:value="formData.compareMode">
              <NSpace>
                <NRadio value="pairwise">两两对比</NRadio>
                <NRadio value="group_internal">组内对比</NRadio>
              </NSpace>
            </NRadioGroup>
          </NFormItem>

          <NFormItem v-if="formData.compareMode === 'group_internal'" label="用户组">
            <NTreeSelect
              v-model:value="formData.groupId"
              :options="groupOptions"
              label-field="label"
              key-field="value"
              placeholder="选择用户组"
              clearable
            />
          </NFormItem>

          <NFormItem label="代码筛选">
            <NRadioGroup v-model:value="formData.compareStrategy">
              <NSpace vertical>
                <NRadio value="ac_only">仅AC代码（推荐）</NRadio>
                <NRadio value="time_window">时间窗口（{{ formData.timeWindow }}分钟内）</NRadio>
                <NRadio value="all">全部提交</NRadio>
              </NSpace>
            </NRadioGroup>
          </NFormItem>

          <n-grid v-if="formData.compareStrategy === 'time_window'" :cols="12" :x-gap="12">
            <n-form-item-gi :span="8" label="时间窗口（分钟）">
              <NInputNumber v-model:value="formData.timeWindow" :min="1" :max="120" />
            </n-form-item-gi>
          </n-grid>
        </NCard>

        <!-- 性能控制 -->
        <NCard title="⚡ 性能控制" size="small" class="mb-4">
          <NFormItem label="启用采样">
            <NRadioGroup v-model:value="formData.useSampling">
              <NSpace>
                <NRadio :value="true">是</NRadio>
                <NRadio :value="false">否</NRadio>
              </NSpace>
            </NRadioGroup>
          </NFormItem>

          <NFormItem v-if="formData.useSampling" label="采样比例">
            <NSpace vertical class="w-full">
              <NSlider
                v-model:value="formData.sampleRate"
                :step="10"
                :min="10"
                :max="100"
                :marks="{10: '10%', 50: '50%', 100: '100%'}"
              />
              <NSpace justify="space-between">
                <NText depth="3">较低精度</NText>
                <NText depth="3">较高精度</NText>
              </NSpace>
            </NSpace>
          </NFormItem>

          <NFormItem label="匹配敏感度">
            <NSpace vertical class="w-full">
              <NSlider
                v-model:value="formData.minMatchLength"
                :step="1"
                :min="5"
                :max="15"
                :marks="{5: '宽松', 10: '标准', 15: '严格'}"
              />
            </NSpace>
          </NFormItem>
        </NCard>

        <!-- 估算信息 -->
        <NCard title="📈 检测预览" size="small">
          <NSpace vertical class="w-full">
            <!-- 风险提示 -->
            <NAlert v-if="submissionEstimate.riskLevel !== 'low'" 
                   :type="submissionEstimate.riskLevel === 'blocked' ? 'error' : 'warning'">
              <NSpace vertical>
                <NText>风险等级：{{ riskLevelConfig[submissionEstimate.riskLevel].text }}</NText>
                <NProgress
                  v-if="submissionEstimate.riskLevel !== 'blocked'"
                  type="line"
                  :percentage="Math.min(submissionEstimate.actual / SYSTEM_LIMITS.WARNING_THRESHOLD * 100, 100)"
                  :height="8"
                  :border-radius="4"
                  :color="submissionEstimate.riskLevel === 'high' ? '#ff4d4f' : '#faad14'"
                />
              </NSpace>
            </NAlert>

            <!-- 数据统计 -->
            <NSpace vertical>
              <NSpace justify="space-between">
                <NText>总提交数量：</NText>
                <NTag>{{ submissionEstimate.total.toLocaleString() }}</NTag>
              </NSpace>
              <NSpace justify="space-between">
                <NText>实际检测数量：</NText>
                <NTag :type="submissionEstimate.isOverLimit ? 'error' : 'info'">
                  {{ submissionEstimate.actual.toLocaleString() }}
                  <span v-if="formData.useSampling">(采样{{ formData.sampleRate }}%)</span>
                </NTag>
              </NSpace>
              <NSpace justify="space-between">
                <NText>预计处理时间：</NText>
                <NTag :type="submissionEstimate.estimatedTime > 60 ? 'warning' : 'success'">
                  {{ submissionEstimate.estimatedTime }}秒
                </NTag>
              </NSpace>
              <NSpace justify="space-between">
                <NText>对比组合数：</NText>
                <NTag type="info">
                  {{ Math.floor(submissionEstimate.actual * (submissionEstimate.actual - 1) / 2).toLocaleString() }}
                </NTag>
              </NSpace>
            </NSpace>

            <!-- 建议 -->
            <NAlert v-if="submissionEstimate.riskLevel === 'high'" type="warning" size="small">
              建议：启用采样或增加筛选条件以减少检测数量
            </NAlert>
          </NSpace>
        </NCard>
      </NForm>

      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">取消</NButton>
          <NButton 
            type="primary" 
            :loading="loading" 
            :disabled="submissionEstimate.isOverLimit || submissionEstimate.actual === 0"
            @click="doSubmit"
          >
            <template #icon>
              <!-- <IconParkOutlinePlayCircle /> -->
            </template>
            {{
              submissionEstimate.isOverLimit ? '数据量超限' : 
              `开始检测 (${submissionEstimate.actual}份)`
            }}
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>