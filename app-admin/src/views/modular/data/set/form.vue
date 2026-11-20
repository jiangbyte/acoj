<script lang="ts" setup>
import type { DataTableColumns } from 'naive-ui'
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NTag } from 'naive-ui'
import { useDataProblemFetch, useDataSetFetch, useSysCategoryFetch, useSysDictFetch } from '@/composables/v1'
import MDEditor from '@/components/common/editor/md/MarkdownEditor.vue'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { dataSetDefaultData, dataSetAdd, dataSetEdit } = useDataSetFetch()
const formData = ref<any>({ ...dataSetDefaultData })
const rules = {
  setType: [
    { required: true, message: 'Enter Set Type', type: 'number', trigger: ['input', 'blur'] },
  ],
  title: [
    { required: true, message: 'Enter Title', trigger: ['input', 'blur'] },
  ],
  cover: [
    { required: true, message: 'Enter Cover', trigger: ['input', 'blur'] },
  ],
  description: [
    { required: true, message: 'Enter Description', trigger: ['input', 'blur'] },
  ],
  categoryId: [
    { required: true, message: 'Enter Category', trigger: ['input', 'blur'] },
  ],
  difficulty: [
    { required: true, message: 'Enter Difficulty', type: 'number', trigger: ['input', 'blur'] },
  ],
}

const isEdit = ref(false)
const problemKeyValue = ref([])
async function doSubmit() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await dataSetEdit(formData.value)
        if (success) {
          window.$message.success('Updated successfully')
        }
      }
      else {
        const { success } = await dataSetAdd(formData.value)
        if (success) {
          window.$message.success('Created successfully')
        }
      }
      emit('submit', true)
      doClose()
      show.value = false
      loading.value = false
    }
    else {
      //
    }
  })
}

const difficultyOptions = ref()
const categoryOptions = ref()
const setTypeOptions = ref()
const { sysDictOptions } = useSysDictFetch()
const { sysCategoryOptions } = useSysCategoryFetch()

const problemsPageParam = ref({
  current: 1,
  size: 10,
  sortField: null,
  sortOrder: null,
  keyword: '',
})
const problemsPageData = ref()
const { dataProblemSetPage, dataProblemListIds } = useDataProblemFetch()
async function loadData() {
  formData.value.startTime = formData.value.time[0]
  formData.value.endTime = formData.value.time[1]
  const { data } = await dataProblemSetPage(problemsPageParam.value)
  if (data) {
    problemsPageData.value = data
  }
}

function clearProblemsPageParamHandle() {
  problemsPageParam.value.keyword = ''
  loadData()
}
async function doOpen(row: any = null, edit: boolean = false) {
  show.value = true
  isEdit.value = edit
  formData.value = Object.assign(formData.value, row)
  formData.value.time = [formData.value.startTime, formData.value.endTime]
  loadData()
  const param = formData.value.problemIds.map((id: any) => ({ id }))
  if (param.length > 0) {
    loadProblems(param)
  }
  else {
    problemKeyValue.value = []
  }
  // Load dropdown options
  const { data: difficultyData } = await sysDictOptions({ dictType: 'PROBLEM_DIFFICULTY' })
  if (difficultyData) {
    // Convert value to a number and keep the other fields
    difficultyOptions.value = difficultyData.map((item: any) => {
      return {
        value: Number(item.value),
        label: item.label,
      }
    })
  }

  const { data: catgoryData } = await sysCategoryOptions({})
  if (catgoryData) {
    categoryOptions.value = catgoryData
  }

  const { data: setTypeData } = await sysDictOptions({ dictType: 'PROBLEM_SET_TYPE' })
  if (setTypeData) {
    setTypeOptions.value = setTypeData.map((item: any) => ({
      value: Number(item.value),
      label: item.label,
    }))
  }
}
defineExpose({
  doOpen,
})

const problemColumns: DataTableColumns<any> = [
  {
    type: 'selection',
  },
  {
    title: 'Source',
    key: 'source',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Title',
    key: 'title',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Category',
    key: 'categoryName',
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Difficulty',
    key: 'difficultyName',
  },
  {
    title: 'Threshold',
    key: 'threshold',
  },
  {
    title: 'Public',
    key: 'isPublicName',
    render: (row) => {
      return h(NTag, {
        type: row.isPublic ? 'primary' : 'error',
      }, () => row.isPublicName)
    },
  },
]
async function selectedProblems(keys: Array<string | number>) {
  formData.value.problemIds = keys
  const param = keys.map((id: any) => ({ id }))
  console.log(param)
  if (param.length > 0) {
    loadProblems(param)
  }
  else {
    problemKeyValue.value = []
  }
}
async function loadProblems(param: any) {
  const { data } = await dataProblemListIds(param)
  if (data) {
    console.log(data)
    problemKeyValue.value = data.map((item: { id: any, title: any }) => ({
      key: item.id,
      value: item.title,
    }))
  }
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataSetDefaultData }
  problemKeyValue.value = []
  problemsPageParam.value = {
    current: 1,
    size: 10,
    sortField: null,
    sortOrder: null,
    keyword: '',
  }
}
function removeProblem(index: string | number) {
  console.log(index)
  // Find the key for the index in problemKeyValue
  const removedItem = problemKeyValue.value.find((_, i) => i === index)
  if (!removedItem)
    return

  const removedKey = removedItem.key

  // Remove the indexed item from problemKeyValue
  problemKeyValue.value = problemKeyValue.value.filter((_, i) => i !== index)

  // Remove the corresponding key from formData.problemIds
  const problemIds = formData.value.problemIds.filter((id: string | number) => id !== removedKey)

  // Call selectedProblems again to refresh data if needed
  // Comment out the following line if no API reload is needed
  selectedProblems(problemIds)
}
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" :default-width="800" resizable @after-leave="doClose">
    <NDrawerContent :title="isEdit ? 'Edit' : 'Add'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- Text input -->
        <NFormItem v-if="isEdit" label="ID" path="id">
          <NInput v-model:value="formData.id" placeholder="Enter ID" :disabled="true" />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Set Type" path="setType">
          <!-- <NInputNumber v-model:value="formData.setType" :min="0" :max="100" placeholder="Enter Set Type" /> -->
          <NSelect
            v-model:value="formData.setType"
            placeholder="Select Category"
            :options="setTypeOptions"
            clearable
            remote
          />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Title" path="title">
          <NInput v-model:value="formData.title" placeholder="Enter Title" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Cover" path="cover">
          <!-- <NInput v-model:value="formData.cover" placeholder="Enter Cover" /> -->
          <FileUpload v-model="formData.cover" :is-image="true" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Description" path="description">
          <!-- <NInput v-model:value="formData.description" placeholder="Enter Description" /> -->
          <MDEditor v-model="formData.description" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Category" path="categoryId">
          <!-- <NInput v-model:value="formData.categoryId" placeholder="Enter Category" /> -->
          <NSelect
            v-model:value="formData.categoryId"
            placeholder="Select Category"
            :options="categoryOptions"
            clearable
            remote
          />
        </NFormItem>
        <!-- Number input -->
        <NFormItem label="Difficulty" path="difficulty">
          <!-- <NInputNumber v-model:value="formData.difficulty" :min="0" :max="100" placeholder="Enter Difficulty" /> -->
          <NSelect
            v-model:value="formData.difficulty"
            placeholder="Select Difficulty"
            :options="difficultyOptions"
            clearable
            remote
          />
        </NFormItem>
        <!-- <NFormItem v-if="formData.setType === 2" label="Starts At" path="startTime">
          <NDatePicker v-model:value="formData.startTime" type="datetime" />
        </NFormItem>
        <NFormItem v-if="formData.setType === 2" label="Ends At" path="endTime">
          <NDatePicker v-model:value="formData.endTime" type="datetime" />
        </NFormItem> -->
        <NFormItem v-if="formData.setType === 2" label="Time Limit" path="time">
          <NDatePicker v-model:value="formData.time" type="datetimerange" />
        </NFormItem>
        <!-- Boolean selector -->
        <NFormItem label="Published" path="isVisible">
          <NRadioGroup v-model:value="formData.isVisible">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- Boolean selector -->
        <!-- <NFormItem label="Problem" path="problems">
          <NSpace vertical class="w-full">
            <NSpace class="w-full">
              <NInput v-model:value="problemsPageParam.keyword" placeholder="Enter keywords" clearable @clear="clearProblemsPageParamHandle" />
              <NButton @click="loadData">
                Search
              </NButton>
            </NSpace>
            <NDataTable
              :checked-row-keys="formData.problemIds"
              :columns="problemColumns"
              :bordered="false"
              :data="problemsPageData?.records"
              :row-key="(row: any) => row.id"
              class="flex-1 h-full"
              @update:checked-row-keys="selectedProblems"
            />
            <NSpace align="center" justify="space-between" class="w-full">
              <NSpace align="center">
                <NP type="info" show-icon>
                  Current: {{ problemsPageData?.records.length }} records
                </NP>
                <NP type="info" show-icon>
                  Selected: {{ formData.problemIds.length }} rows
                </NP>
              </NSpace>
              <NPagination
                v-model:page="problemsPageParam.current"
                v-model:page-size="problemsPageParam.size"
                class="flex justify-end"
                :page-count="problemsPageData ? Number(problemsPageData.pages) : 0"
                show-size-picker
                :page-sizes="Array.from({ length: 10 }, (_, i) => ({
                  label: `${(i + 1) * 10} per page`,
                  value: (i + 1) * 10,
                }))"
                :page-slot="5"
                @update:page="loadData"
                @update:page-size="loadData"
              />
            </NSpace>
            <n-dynamic-input
              v-model:value="problemKeyValue"
              @remove="removeProblem"
            >
              <template #create-button-default>
                Select Problem
              </template>
              <template #default="{ value, index }">
                <div style="display: flex; align-items: center; width: 100%; gap: 10px;">
                  <NTag type="warning">
                    # {{ index + 1 }}
                  </NTag>
                  <NInput v-model:value="value.value" style="width: 100%" disabled />
                </div>
              </template>
            </n-dynamic-input>
          </NSpace>
        </NFormItem> -->
        <!-- Boolean selector -->
        <NFormItem label="Use AI" path="useAi">
          <NRadioGroup v-model:value="formData.useAi">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            Close
          </NButton>
          <NButton type="primary" :loading="loading" @click="doSubmit">
            <template #icon>
              <IconParkOutlineSave />
            </template>
            Save
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
