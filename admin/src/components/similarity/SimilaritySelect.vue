<script lang="ts" setup>
import { useDataProblemFetch, useDataSetFetch, useSysGroupFetch, useSysUserFetch, useTaskSimilarityFetch } from '@/composables/v1'
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
  setId: '',
  language: null,
  isSet: false,
  userIds: [],
  taskId: '',
  isGroup: false,
  groupId: null,
  batchTaskId: '',
  minMatchLength: 5,
  threshold: 0.5
}

const formData = ref({ ...defaultFormData })
const rules = {
  problemIds: [
    // { required: true, message: '请选择题目', trigger: ['input', 'blur'] },
  ],
  setId: [
  ],
  language: [
    { required: true, message: '请选择语言', trigger: ['input', 'blur'] },
  ],
  minMatchLength: [
    // { required: true, message: '请输入敏感度', trigger: ['input', 'blur'] },
  ],
  threshold: [
    // { required: true, message: '请输入阈值', trigger: ['input', 'blur'] },
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
          loading.value = true
          useTaskSimilarityFetch().taskSimilarityBatch(formData.value).then(({ data }) => {
            if (data) {
              console.log(data)
              useTaskSimilarityFetch().taskSimilarityProgress(data).then(({ data }) => {
                console.log(data)
              })
              window.$message.success('提交成功')
            }
          })
          emit('submit', true)
          doClose()
          console.log(formData.value)
          
          show.value = false
          loading.value = false
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
]
const pageData = ref()
const pageParam = ref({
  current: 1,
  size: 20,
  sortField: 'id',
  sortOrder: 'ASCEND',
  keyword: '',
  groupId: '',
})
const { sysUserPage } = useSysUserFetch()
async function loadUserData() {
  loading.value = true
  const { data } = await sysUserPage(pageParam.value)
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
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- 输入框 -->
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
          />
        </NFormItem>
        <NFormItem label="匹配敏感度" path="minMatchLength">
          <NInputNumber v-model:value="formData.minMatchLength" placeholder="请输入匹配敏感度" />
        </NFormItem>
        <NFormItem label="阈值" path="threshold">
          <NInputNumber v-model:value="formData.threshold" placeholder="请输入检测阈值" />
        </NFormItem>
        <NFormItem label="检测组" path="isGroup">
          <NRadioGroup v-model:value="formData.isGroup">
            <NRadio :value="true">
              是
            </NRadio>
            <NRadio :value="false">
              否
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <NFormItem label="用户组" path="groupId">
          <n-tree-select
            v-model:value="formData.groupId"
            :options="groupOptions"
            label-field="name"
            key-field="id"
            :indent="12"
            @update:value="handleUserGroupChange"
          />
        </NFormItem>
      </NForm>
      <NCard v-if="!formData.isGroup" size="small" class="flex-1 mb-4">
        <NDataTable
          v-model:checked-row-keys="formData.userIds"
          :columns="columns"
          :data="pageData?.records"
          :bordered="false"
          :row-key="(row: any) => row.id"
          :loading="loading"
          flex-height
          class="h-90"
        />
        <template #action>
          <NSpace align="center" justify="space-between" class="w-full">
            <NSpace align="center">
              <NP type="info" show-icon>
                当前数据 {{ pageData?.records.length }} 条
              </NP>
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
      <n-alert type="warning" class="mb-4">
        检测范围最多为近期
        <n-tag type="info" size="small">
          1000
        </n-tag>
        条有效数据。
      </n-alert>
      <n-alert v-if="isSetRef" type="warning" class="mb-4">
        题集检测语言为题目中允许的语言交集
      </n-alert>
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

</style>
