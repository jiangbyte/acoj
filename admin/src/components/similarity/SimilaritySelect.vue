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
import { useDataLibraryFetch, useDataSetFetch, useTaskSimilarityFetch } from '@/composables/v1'
import type { DataTableColumns, SelectOption } from 'naive-ui'
import { NAlert, NAvatar, NButton, NCard, NDatePicker, NDrawer, NDrawerContent, NForm, NInputNumber, NRadio, NRadioGroup, NSlider, NSpace, NTag, NText, NTreeSelect } from 'naive-ui'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()

const { dataLibraryiUserPage, dataLibraryiUserGroupList } = useDataLibraryFetch()
const defaultFormData = {
  // 基础范围
  setId: null,
  problemId: null,
  language: null as any,

  startTime: 0,
  endTime: 0,

  timeRange: [Date.now() - (2 * 24 * 60 * 60 * 1000), Date.now()],

  compareMode: 'PAIR_BY_PAIR',

  groupId: null,
  userIds: [],

  codeFilter: 'VALID_SUBMIT',

  codeFilterTimeWindow: 10,

  enableSampling: true,
  samplingRatio: 0.1,

  minMatchLength: 8,
}

const formData = ref({ ...defaultFormData })

const rules = {
  problemId: [
    { required: true, message: '请选择题目', trigger: ['input', 'blur'] },
  ],
  language: [
    { required: true, message: '请选择语言', trigger: ['input', 'blur'] },
  ],
}

const libraryBatchCount = ref({
  totalCount: '0',
  checkCount: '0',
  expectTime: '0',
  compareCount: '0',
})

function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...defaultFormData }
}

async function doSubmit() {
  if (Number(libraryBatchCount.value.checkCount) <= 1 || Number(libraryBatchCount.value.checkCount) > 200) {
    window.$message.warning(`检测份数 ${libraryBatchCount.value.checkCount} 为边界值，无法提交`)
    return
  }

  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      try {
        const { data } = await useTaskSimilarityFetch().taskSimilarityBatch(formData.value)
        if (data) {
          window.$message.success(`检测任务已提交，将处理 ${libraryBatchCount.value.checkCount} 份提交`)
          emit('submit', data)
          doClose()
        }
      }
      // eslint-disable-next-line unused-imports/no-unused-vars
      catch (error) {
        window.$message.error('任务提交失败')
      }
      finally {
        loading.value = false
      }
    }
  })
}

function handleConfirm() {
  window.$dialog.info({
    title: '提示',
    content: `将处理 ${libraryBatchCount.value.checkCount} 份提交，稍后可在【检测统计】中查看详情`,
    positiveText: '确定',
    negativeText: '取消（该操作将关闭检测窗口）',
    onPositiveClick: () => {
      doSubmit()
    },
    onNegativeClick: () => {
      doClose()
    },
  })
}

const setProblemOptions = ref<SelectOption[]>([])
const languageOptions = ref<SelectOption[]>([])
const groupOptions = ref<SelectOption[]>([])
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

function doOpen(sId: string = '', _pid: string = '', _isSet: boolean) {
  show.value = true
  formData.value.setId = sId as any
  pageParam.value.setId = sId as any

  loadProblems('')
  loadLanguages('')
  loadUserData()
  getGroupLists('')
}

function getGroupLists(query: string) {
  dataLibraryiUserGroupList({ keyword: query }).then(({ data }) => {
    groupOptions.value = data
  })
}

function loadProblems(query: string) {
  useDataSetFetch().dataSetProblemWithSearch({ id: formData.value.setId, keyword: query }).then(({ data }) => {
    console.log(data)
    setProblemOptions.value = data
  })
}
function loadLanguages(query: string) {
  if (formData.value.problemId) {
    useDataSetFetch().dataProblemGetSetProblemLanguages({ id: formData.value.setId, problemIds: [formData.value.problemId], keyword: query }).then(({ data }) => {
      languageOptions.value = data.map((item: any) => {
        item.label = item.label.charAt(0).toUpperCase() + item.label.slice(1)
        return item
      })
    })
  }
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

const pageLoading = ref(false)
async function loadUserData() {
  pageLoading.value = true
  const { data } = await dataLibraryiUserPage(formData.value)
  if (data) {
    pageData.value = data
    pageLoading.value = false
  }
}
function handleUserGroupChange(value: any) {
  formData.value.groupId = value
  loadUserData()
}

defineExpose({
  doOpen,
})

watch(
  () => formData.value.problemId, // 更推荐使用函数形式监听深层属性
  () => {
    loadLanguages('')
  },
  { immediate: true }, // 初始化时执行一次
)

function updateQcount() {
  formData.value.startTime = formData.value.timeRange[0]
  formData.value.endTime = formData.value.timeRange[1]
  useDataLibraryFetch().dataLibraryBatchQuery(formData.value).then(({ data }) => {
    libraryBatchCount.value = data
    console.log(data)
  })
}
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="代码相似度检测配置">
      <NAlert type="info" class="mb-4">
        系统限制：最多处理 <NTag size="small" type="info">
          200
        </NTag> 份提交
      </NAlert>

      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="130">
        <!-- 基础检测范围 -->
        <NCard title="检测范围" size="small" class="mb-4">
          <NFormItem label="题目选择" path="problemId">
            <NSelect
              v-model:value="formData.problemId"
              placeholder="请选择要检测的题目"
              label-field="title"
              value-field="id"
              filterable
              :options="setProblemOptions"
              @search="loadProblems"
              @update-value="(e) => {
                formData.problemIds = [e]
                loadUserData()
                updateQcount()
              }"
            />
          </NFormItem>

          <NFormItem label="编程语言" path="language">
            <NSelect
              v-model:value="formData.language"
              placeholder="请选择检测语言"
              :options="languageOptions"
              :disabled="!formData.problemId"
              @update-value="(e) => {
                formData.language = e
                loadUserData()
                updateQcount()
              }"
            />
          </NFormItem>

          <NFormItem label="时间范围">
            <NDatePicker
              v-model:value="formData.timeRange"
              type="datetimerange"
              clearable
              placeholder="选择时间范围（可选）"
              @update-value="(e) => updateQcount()"
            />
          </NFormItem>
        </NCard>

        <!-- 检测策略 -->
        <NCard title="检测策略" size="small" class="mb-4">
          <NFormItem label="对比模式">
            <NRadioGroup v-model:value="formData.compareMode">
              <NSpace>
                <NRadio value="PAIR_BY_PAIR">
                  两两对比
                </NRadio>
                <!-- <NRadio value="GROUP_BY_GROUP">
                  组内对比
                </NRadio> -->
                <NRadio value="MULTI_BY_MULTI">
                  用户对比
                </NRadio>
              </NSpace>
            </NRadioGroup>
          </NFormItem>

          <!-- <NFormItem v-if="formData.compareMode === 'GROUP_BY_GROUP'" label="用户组">
            <NTreeSelect
              v-model:value="formData.groupId"
              :options="groupOptions"
              label-field="label"
              key-field="value"
              placeholder="选择用户组"
              clearable
            />
          </NFormItem> -->

          <NFormItem v-if="formData.compareMode === 'MULTI_BY_MULTI'" label="用户列表">
            <NCard size="small" class="flex-1 mb-4">
              <NSpace vertical>
                <NSpace align="center" justify="space-between">
                  <NSpace align="center">
                    <NText>
                      用户组筛选
                    </NText>
                    <NTreeSelect
                      v-model:value="formData.groupId"
                      :options="groupOptions"
                      label-field="name"
                      key-field="id"
                      filterable
                      :indent="12"
                      class="w-60"
                      clearable
                      placeholder="请选择用户组"
                      @update:value="(e) => {
                        updateQcount()
                        handleUserGroupChange(e)
                      }"
                      @search="getGroupLists"
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
                  :loading="pageLoading"
                  flex-height
                  class="h-90"
                  @update:checked-row-keys="(e) => {
                    updateQcount()
                  }"
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
          </NFormItem>

          <NFormItem v-if="formData.compareMode === 'PAIR_BY_PAIR'" label="代码筛选">
            <NRadioGroup v-model:value="formData.codeFilter" @update-value="(e) => updateQcount()">
              <NRadio value="VALID_SUBMIT">
                有效提交
              </NRadio>
              <NRadio value="TIME_WINDOW">
                时间窗口（{{ formData.codeFilterTimeWindow }}分钟内）
              </NRadio>
            </NRadioGroup>
          </NFormItem>

          <n-grid v-if="formData.compareMode === 'PAIR_BY_PAIR' && formData.codeFilter === 'TIME_WINDOW'" :cols="12" :x-gap="12">
            <n-form-item-gi :span="8" label="时间窗口（分钟）">
              <NInputNumber v-model:value="formData.codeFilterTimeWindow" :min="1" :max="120" @update:value="updateQcount" />
            </n-form-item-gi>
          </n-grid>
        </NCard>

        <!-- 性能控制 -->
        <NCard title="参数控制" size="small" class="mb-4">
          <!-- <NFormItem label="启用采样">
            <NRadioGroup v-model:value="formData.enableSampling">
              <NSpace>
                <NRadio :value="true">
                  是
                </NRadio>
                <NRadio :value="false">
                  否
                </NRadio>
              </NSpace>
            </NRadioGroup>
          </NFormItem>

          <NFormItem v-if="formData.enableSampling" label="采样比例">
            <NSpace vertical class="w-full">
              <NSlider
                v-model:value="formData.samplingRatio"
                :step="0.1"
                :min="0.1"
                :max="1.0"
                :marks="{ 0.1: '10%', 0.5: '50%', 1.0: '100%' }"
              />
              <NSpace justify="space-between">
                <NText depth="3">
                  较低精度
                </NText>
                <NText depth="3">
                  较高精度
                </NText>
              </NSpace>
            </NSpace>
          </NFormItem> -->

          <NFormItem label="匹配敏感度">
            <NSpace vertical class="w-full">
              <NSlider
                v-model:value="formData.minMatchLength"
                :step="1"
                :min="5"
                :max="15"
                :marks="{ 5: '宽松', 10: '标准', 15: '严格' }"
              />
            </NSpace>
          </NFormItem>
        </NCard>

        <!-- 估算信息 -->
        <NCard title="检测预览" size="small">
          <NSpace vertical class="w-full">
            <NSpace vertical>
              <NSpace justify="space-between">
                <NText>总提交数量：</NText>
                <NTag>{{ libraryBatchCount.totalCount }}</NTag>
              </NSpace>
              <NSpace justify="space-between">
                <NText>实际检测数量：</NText>
                <NTag>
                  {{ libraryBatchCount.checkCount }}
                </NTag>
              </NSpace>
              <!-- <NSpace justify="space-between">
                <NText>预计处理时间：</NText>
                <NTag>
                  {{ libraryBatchCount.expectTime }}秒
                </NTag>
              </NSpace> -->
              <NSpace justify="space-between">
                <NText>对比组合数：</NText>
                <NTag type="info">
                  {{ libraryBatchCount.compareCount }}
                </NTag>
              </NSpace>
            </NSpace>
          </NSpace>
        </NCard>
      </NForm>

      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            取消
          </NButton>
          <NButton
            type="primary"
            :loading="loading"
            :disabled="Number(libraryBatchCount.checkCount) <= 1 || Number(libraryBatchCount.checkCount) > 200"
            @click="handleConfirm"
          >
            {{ `开始检测 (${libraryBatchCount.checkCount}份)` }}
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>
