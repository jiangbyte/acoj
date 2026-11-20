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
  // Base range
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

function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...defaultFormData }
}

async function doSubmit() {
  if (Number(libraryBatchCount.value.checkCount) <= 1 || Number(libraryBatchCount.value.checkCount) > 200) {
    window.$message.warning(`Submission count ${libraryBatchCount.value.checkCount} is out of range and cannot be submitted`)
    return
  }

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
  formData.value.moduleId = row.id as any
  formData.value.moduleType = moduleType as any

  console.log('=============')
  // Valid code in the repository
  // useDataLibraryFetch().dataLibraryProblems({
  //   moduleType,
  //   moduleId: row.id as any,
  // }).then(({ data }) => {
  //   setProblemOptions.value = data
  //   console.log(data)
  // })
  const { data } = await useDataLibraryFetch().dataLibraryProblems({
    moduleType,
    moduleId: row.id as any,
  })
  console.log(data)
  setProblemOptions.value = data

  console.log('----------')

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
    title: 'User Group',
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
  { immediate: true }, // Run once during initialization
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
    <NDrawerContent title="Code Similarity Settings">
      <NAlert type="info" class="mb-4">System limit: process up to <NTag size="small" type="info">200</NTag> submissions</NAlert>

      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="130">
        <NCard title="Check Scope" size="small" class="mb-4">
          <NFormItem label="Problem" path="problemId">
            <NSelect
              v-model:value="formData.problemId"
              placeholder="Select a problem to check"
              label-field="title"
              value-field="id"
              :options="setProblemOptions"
              @update-value="(e) => {
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
              @update-value="(e) => {
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
              @update-value="(e) => updateQcount()"
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
                        updateQcount()
                        handleUserGroupChange(e)
                      }"
                      @search="getGroupLists"
                    />
                  </NSpace>
                  <NP type="info" show-icon>
                    Current: {{ pageData?.records.length }} records
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
            <NRadioGroup v-model:value="formData.codeFilter" @update-value="(e) => updateQcount()">
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
            :disabled="Number(libraryBatchCount.checkCount) <= 1 || Number(libraryBatchCount.checkCount) > 200"
            @click="handleConfirm"
          >
            {{ `Start check (${libraryBatchCount.checkCount})` }}
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>
