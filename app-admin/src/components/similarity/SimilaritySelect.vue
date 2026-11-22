<script lang="ts" setup>
import { renderSafeAvatar } from '@/utils'
import { useDataLibraryFetch, useTaskSimilarityFetch } from '@/composables/v1'
import type { DataTableColumns, SelectOption } from 'naive-ui'
import { NAlert, NButton, NCard, NDatePicker, NDrawer, NDrawerContent, NForm, NInputNumber, NRadio, NRadioGroup, NSlider, NSpace, NTag, NText, NTreeSelect } from 'naive-ui'
const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()

const { dataLibraryiUserPage, dataLibraryiUserGroupList } = useDataLibraryFetch()
const defaultFormData = {
  // Base range
  moduleType: null,
  moduleId: null,
  problemId: null,
  language: null as any,

  startTime: null as number | null,
  endTime: null as number | null,

  timeRange: [Date.now() - (2 * 24 * 60 * 60 * 1000), Date.now()] as [number, number] | null,

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
    { required: true, message: 'Select Problem', trigger: ['input', 'blur'] },
  ],
  language: [
    { required: true, message: 'Select Language', trigger: ['input', 'blur'] },
  ],
}

const libraryBatchCount = ref({
  totalCount: '0',
  checkCount: '0',
  expectTime: '0',
  compareCount: '0',
})

const checkCountNum = computed(() => Number(libraryBatchCount.value.checkCount) || 0)
const canStartCheck = computed(() => checkCountNum.value > 1 && checkCountNum.value <= 200)

function doClose() {
  emit('close')
  show.value = false
  formData.value = {
    ...defaultFormData,
    timeRange: [Date.now() - (2 * 24 * 60 * 60 * 1000), Date.now()],
    userIds: [],
  }
  libraryBatchCount.value = {
    totalCount: '0',
    checkCount: '0',
    expectTime: '0',
    compareCount: '0',
  }
}

async function doSubmit() {
  if (!canStartCheck.value) {
    window.$message.warning(`Submission count ${libraryBatchCount.value.checkCount} is out of range and cannot be submitted`)
    return
  }

  syncTimeRangeToForm()
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      try {
        const { data } = await useTaskSimilarityFetch().taskSimilarityBatch(formData.value)
        if (data) {
          window.$message.success(`Check submitted; processing ${libraryBatchCount.value.checkCount} submissions`)
          emit('submit', data)
          doClose()
        }
      }
      // eslint-disable-next-line unused-imports/no-unused-vars
      catch (error) {
        window.$message.error('Failed to submit task')
      }
      finally {
        loading.value = false
      }
    }
  })
}

function handleConfirm() {
  window.$dialog.info({
    title: 'Notice',
    content: `Process ${libraryBatchCount.value.checkCount} submissions，View details later under Check Statistics`,
    positiveText: 'OK',
    negativeText: 'Cancel and close',
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
async function doOpen(row: any, moduleType: string) {
  show.value = true
  formData.value = {
    ...defaultFormData,
    moduleId: row.id as any,
    moduleType: moduleType as any,
    timeRange: [Date.now() - (2 * 24 * 60 * 60 * 1000), Date.now()],
    userIds: [],
  }
  syncTimeRangeToForm()

  const { data } = await useDataLibraryFetch().dataLibraryProblems({
    moduleType,
    moduleId: row.id as any,
  })
  setProblemOptions.value = data || []

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
    title: 'Nickname',
    key: 'nickname',
    render(row: any) {
      return h(
        NSpace,
        { align: 'center', size: 'small' },
        {
          default: () => [
            renderSafeAvatar(
              {
                size: 'small',
                round: true,
                src: row.avatar,
              },
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
    title: 'User Group',
    key: 'groupIdName',
  },
]

const pageLoading = ref(false)
async function loadUserData() {
  pageLoading.value = true
  try {
    const { data } = await dataLibraryiUserPage(formData.value)
    if (data) {
      pageData.value = data
    }
  }
  finally {
    pageLoading.value = false
  }
}
function handleUserGroupChange(value: any) {
  formData.value.groupId = value
  loadUserData()
  updateQcount()
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
    })) || []
    // 换题后清空语言，避免旧语言命中错误集合
    if (formData.value.language
      && !languageOptions.value.some((opt: any) => opt.value === formData.value.language)) {
      formData.value.language = null
    }
  },
  { immediate: true },
)

watch(
  () => formData.value.compareMode,
  () => {
    updateQcount()
  },
)

/**
 * 将 Time Range 同步到 start/end；清空时置 null，避免下标空指针。
 */
function syncTimeRangeToForm() {
  const range = formData.value.timeRange
  if (Array.isArray(range) && range.length >= 2 && range[0] != null && range[1] != null) {
    formData.value.startTime = range[0]
    formData.value.endTime = range[1]
  }
  else {
    formData.value.startTime = null
    formData.value.endTime = null
  }
}

function updateQcount() {
  syncTimeRangeToForm()
  if (!formData.value.problemId || !formData.value.language) {
    libraryBatchCount.value = {
      totalCount: '0',
      checkCount: '0',
      expectTime: '0',
      compareCount: '0',
    }
    return
  }
  useDataLibraryFetch().dataLibraryBatchQuery(formData.value).then(({ data }) => {
    if (data) {
      libraryBatchCount.value = data
    }
  })
}
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="Code Similarity Settings">
      <NAlert type="info" class="mb-4">
        System limit: process up to <NTag size="small" type="info">
          200
        </NTag> submissions
      </NAlert>

      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="130">
        <NCard title="Check Scope" size="small" class="mb-4">
          <NFormItem label="Problem" path="problemId">
            <NSelect
              v-model:value="formData.problemId"
              placeholder="Select a problem to check"
              label-field="title"
              value-field="id"
              :options="setProblemOptions"
              @update-value="() => {
                loadUserData()
                updateQcount()
              }"
            />
          </NFormItem>
          <NFormItem label="Language" path="language">
            <NSelect
              v-model:value="formData.language"
              placeholder="Select a language"
              :options="languageOptions"
              :disabled="!formData.problemId"
              @update-value="() => {
                loadUserData()
                updateQcount()
              }"
            />
          </NFormItem>
          <NFormItem label="Time Range">
            <NDatePicker
              v-model:value="formData.timeRange"
              type="datetimerange"
              clearable
              placeholder="Select a time range (optional)"
              @update-value="() => updateQcount()"
            />
          </NFormItem>
        </NCard>
        <NCard title="Check Strategy" size="small" class="mb-4">
          <NFormItem label="Comparison Mode">
            <NRadioGroup v-model:value="formData.compareMode">
              <NSpace>
                <NRadio value="PAIR_BY_PAIR">
                  Pairwise
                </NRadio>
                <NRadio value="MULTI_BY_MULTI">
                  Compare users
                </NRadio>
              </NSpace>
            </NRadioGroup>
          </NFormItem>
          <NFormItem v-if="formData.compareMode === 'MULTI_BY_MULTI'" label="Users">
            <NCard size="small" class="flex-1 mb-4">
              <NSpace vertical>
                <NSpace align="center" justify="space-between">
                  <NSpace align="center">
                    <NText>
                      Filter by user group
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
                      placeholder="Select a user group"
                      @update:value="(e) => {
                        handleUserGroupChange(e)
                      }"
                      @search="getGroupLists"
                    />
                  </NSpace>
                  <NP type="info" show-icon>
                    Current: {{ pageData?.records?.length || 0 }} records
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
                  @update:checked-row-keys="() => {
                    updateQcount()
                  }"
                />
              </NSpace>
              <template #action>
                <NSpace align="center" justify="space-between" class="w-full">
                  <NSpace align="center">
                    <NP type="info" show-icon>
                      Selected: {{ formData.userIds?.length }} rows
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
          <NFormItem v-if="formData.compareMode === 'PAIR_BY_PAIR'" label="Code Filter">
            <NRadioGroup v-model:value="formData.codeFilter" @update-value="() => updateQcount()">
              <NRadio value="VALID_SUBMIT">
                Valid submissions
              </NRadio>
              <NRadio value="TIME_WINDOW">
                Time window ({{ formData.codeFilterTimeWindow }} min)
              </NRadio>
            </NRadioGroup>
          </NFormItem>
          <n-grid v-if="formData.compareMode === 'PAIR_BY_PAIR' && formData.codeFilter === 'TIME_WINDOW'" :cols="12" :x-gap="12">
            <n-form-item-gi :span="8" label="Time window (minutes)">
              <NInputNumber v-model:value="formData.codeFilterTimeWindow" :min="1" :max="120" @update:value="updateQcount" />
            </n-form-item-gi>
          </n-grid>
        </NCard>
        <NCard title="Parameters" size="small" class="mb-4">
          <NFormItem label="Match sensitivity">
            <NSpace vertical class="w-full">
              <NSlider
                v-model:value="formData.minMatchLength"
                :step="1"
                :min="5"
                :max="15"
                :marks="{ 5: 'Lenient', 10: 'Standard', 15: 'Strict' }"
              />
            </NSpace>
          </NFormItem>
        </NCard>
        <NCard title="Preview" size="small">
          <NSpace vertical class="w-full">
            <NSpace vertical>
              <NSpace justify="space-between">
                <NText>Total submissions:</NText>
                <NTag>{{ libraryBatchCount.totalCount }}</NTag>
              </NSpace>
              <NSpace justify="space-between">
                <NText>Submissions checked:</NText>
                <NTag>
                  {{ libraryBatchCount.checkCount }}
                </NTag>
              </NSpace>
              <NSpace justify="space-between">
                <NText>Comparison pairs:</NText>
                <NTag type="info">
                  {{ libraryBatchCount.compareCount }}
                </NTag>
              </NSpace>
            </NSpace>
            <NAlert v-if="formData.problemId && formData.language && checkCountNum <= 1" type="warning" class="mt-2">
              Need at least 2 submissions with valid code tokens in scope before starting a check.
            </NAlert>
          </NSpace>
        </NCard>
      </NForm>

      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            Cancel
          </NButton>
          <NButton
            type="primary"
            :loading="loading"
            :disabled="!canStartCheck"
            @click="handleConfirm"
          >
            {{ `Start check (${libraryBatchCount.checkCount})` }}
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>
