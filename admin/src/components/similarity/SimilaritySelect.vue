<script lang="ts" setup>
import { useDataLibraryFetch, useTaskSimilarityFetch } from '@/composables/v1'
import type { DataTableColumns, SelectOption } from 'naive-ui'
import { NAlert, NAvatar, NButton, NCard, NDatePicker, NDrawer, NDrawerContent, NForm, NInputNumber, NRadio, NRadioGroup, NSlider, NSpace, NTag, NText, NTreeSelect } from 'naive-ui'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()

const { dataLibraryiUserPage, dataLibraryiUserGroupList } = useDataLibraryFetch()
const defaultFormData = {
  // 基础范围
  moduleType: null,
  moduleId: null,
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

  current: 1,
  size: 20,
  sortField: 'id',
  sortOrder: 'ASCEND',
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
function doOpen(row: any, moduleType: string) {
  show.value = true
  formData.value.moduleId = row.id as any
  formData.value.moduleType = moduleType as any

  // 仓库中有效的代码
  useDataLibraryFetch().dataLibraryProblems({
    moduleType,
    moduleId: row.id as any,
  }).then(({ data }) => {
    setProblemOptions.value = data
  })

  loadUserData()
  getGroupLists('')
}

function getGroupLists(query: string) {
  dataLibraryiUserGroupList({ keyword: query }).then(({ data }) => {
    groupOptions.value = data
  })
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
  () => formData.value.problemId,
  () => {
    const problem = setProblemOptions.value.find((problem: any) => problem.id === formData.value.problemId)
    languageOptions.value = problem?.allowedLanguages?.map((language: any) => ({
      label: language.charAt(0).toUpperCase() + language.slice(1),
      value: language,
    }))
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
      <NAlert type="info" class="mb-4">系统限制：最多处理 <NTag size="small" type="info">200</NTag>份提交</NAlert>

      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="130">
        <NCard title="检测范围" size="small" class="mb-4">
          <NFormItem label="题目选择" path="problemId">
            <NSelect
              v-model:value="formData.problemId"
              placeholder="请选择要检测的题目"
              label-field="title"
              value-field="id"
              :options="setProblemOptions"
              @update-value="(e) => {
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
        <NCard title="检测策略" size="small" class="mb-4">
          <NFormItem label="对比模式">
            <NRadioGroup v-model:value="formData.compareMode">
              <NSpace>
                <NRadio value="PAIR_BY_PAIR">
                  两两对比
                </NRadio>
                <NRadio value="MULTI_BY_MULTI">
                  用户对比
                </NRadio>
              </NSpace>
            </NRadioGroup>
          </NFormItem>
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
                    v-model:page="formData.current"
                    v-model:page-size="formData.size"
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
        <NCard title="参数控制" size="small" class="mb-4">
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
