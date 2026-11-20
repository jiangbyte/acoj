<script lang="ts" setup>
import { NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput } from 'naive-ui'
import { useDataContestFetch, useSysDictFetch } from '@/composables/v1'
import MDEditor from '@/components/common/editor/md/MarkdownEditor.vue'

const emit = defineEmits(['close', 'submit'])
const show = ref(false)
const loading = ref(false)
const formRef = ref()
const { dataContestDefaultData, dataContestAdd, dataContestEdit } = useDataContestFetch()
const formData = ref<any>({ ...dataContestDefaultData })
const rules = {
  title: [
    { required: true, message: 'Enter Contest Title', trigger: ['input', 'blur'] },
  ],
  description: [
    { required: true, message: 'Enter Contest Description', trigger: ['input', 'blur'] },
  ],
  ruleType: [
    { required: true, message: 'Enter Rule Type', trigger: ['input', 'blur'] },
  ],
  category: [
    { required: true, message: 'Enter Category', trigger: ['input', 'blur'] },
  ],
  cover: [
    { required: true, message: 'Enter Cover', trigger: ['input', 'blur'] },
  ],
  maxTeamMembers: [
    { required: true, message: 'Enter Maximum Team Size', type: 'number', trigger: ['input', 'blur'] },
  ],
  password: [
    { required: true, message: 'Enter Access Password', trigger: ['input', 'blur'] },
  ],
  frozenTime: [
    { required: true, message: 'Enter Scoreboard Freeze (minutes)', type: 'number', trigger: ['input', 'blur'] },
  ],
  penaltyTime: [
    { required: true, message: 'Enter Penalty (minutes)', type: 'number', trigger: ['input', 'blur'] },
  ],
  allowedLanguages: [{
    type: 'array',
    required: true,
    trigger: ['blur', 'change'],
    message: 'Select Language',
  }],
  status: [
    { required: true, message: 'Enter Status', trigger: ['input', 'blur'] },
  ],
  sort: [
    { required: true, message: 'Enter Sort Order', type: 'number', trigger: ['input', 'blur'] },
  ],
}
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataContestDefaultData }
}

const isEdit = ref(false)
async function doSubmit() {
  formData.value.registerStartTime = formData.value.registerTime[0]
  formData.value.registerEndTime = formData.value.registerTime[1]
  formData.value.contestStartTime = formData.value.contestTime[0]
  formData.value.contestEndTime = formData.value.contestTime[1]
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      loading.value = true
      if (isEdit.value) {
        const { success } = await dataContestEdit(formData.value)
        if (success) {
          window.$message.success('Updated successfully')
        }
      }
      else {
        const { success } = await dataContestAdd(formData.value)
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

const allowLanguageOptions = ref()
const { sysDictOptions } = useSysDictFetch()
async function doOpen(row: any = null, edit: boolean = false) {
  show.value = true
  isEdit.value = edit
  formData.value = Object.assign(formData.value, row)
  formData.value.registerTime = [formData.value.registerStartTime, formData.value.registerEndTime]
  formData.value.contestTime = [formData.value.contestStartTime, formData.value.contestEndTime]

  const { data: allowLanguageData } = await sysDictOptions({ dictType: 'ALLOW_LANGUAGE' })
  if (allowLanguageData) {
    allowLanguageOptions.value = allowLanguageData
  }
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" :default-width="800" resizable @after-leave="doClose">
    <NDrawerContent :title="isEdit ? 'Edit' : 'Add'">
      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="auto">
        <!-- Text input -->
        <NFormItem v-if="isEdit" label="ID" path="id">
          <NInput v-model:value="formData.id" placeholder="Enter ID" :disabled="true" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Contest Title" path="title">
          <NInput v-model:value="formData.title" placeholder="Enter Contest Title" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Contest Description" path="description">
          <!-- <NInput v-model:value="formData.description" placeholder="Enter Contest Description" /> -->
          <MDEditor v-model="formData.description" />
        </NFormItem>
        <!-- Text input -->
        <!-- <NFormItem label="Rule Type" path="ruleType">
          <NInput v-model:value="formData.ruleType" placeholder="Enter Rule Type" />
        </NFormItem> -->
        <!-- Text input -->
        <NFormItem label="Category" path="category">
          <NInput v-model:value="formData.category" placeholder="Enter Category" />
        </NFormItem>
        <!-- Text input -->
        <NFormItem label="Cover" path="cover">
          <FileUpload v-model="formData.cover" :is-image="true" />
        </NFormItem>
        <!-- Number input -->
        <!-- <NFormItem label="Maximum Team Size" path="maxTeamMembers">
          <NInputNumber v-model:value="formData.maxTeamMembers" :min="0" :max="100" placeholder="Enter Maximum Team Size" />
        </NFormItem> -->
        <!-- Boolean selector -->
        <!-- <NFormItem label="Team Contest" path="isTeamContest">
          <NRadioGroup v-model:value="formData.isTeamContest">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </NFormItem> -->
        <!-- Boolean selector -->
        <NFormItem label="Public" path="isPublic">
          <NRadioGroup v-model:value="formData.isPublic">
            <NRadio :value="true">
              Yes
            </NRadio>
            <NRadio :value="false">
              No
            </NRadio>
          </NRadioGroup>
        </NFormItem>
        <!-- Text input -->
        <NFormItem v-if="!formData.isPublic" label="Access Password" path="password">
          <NInput v-model:value="formData.password" placeholder="Enter Access Password" />
        </NFormItem>
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
        <!-- Date selector -->
        <NFormItem label="Registration Period" path="registerTime">
          <NDatePicker v-model:value="formData.registerTime" type="datetimerange" />
        </NFormItem>
        <NFormItem label="Contest Period" path="contestTime">
          <NDatePicker v-model:value="formData.contestTime" type="datetimerange" />
        </NFormItem>
        <!-- <NFormItem label="Registration Starts" path="registerStartTime">
          <NDatePicker v-model:value="formData.registerStartTime" type="datetime" />
        </NFormItem>
        <NFormItem label="Registration Ends" path="registerEndTime">
          <NDatePicker v-model:value="formData.registerEndTime" type="datetime" />
        </NFormItem>
        <NFormItem label="Contest Starts" path="contestStartTime">
          <NDatePicker v-model:value="formData.contestStartTime" type="datetime" />
        </NFormItem>
        <NFormItem label="Contest Ends" path="contestEndTime">
          <NDatePicker v-model:value="formData.contestEndTime" type="datetime" />
        </NFormItem> -->
        <!-- Number input -->
        <!-- <NFormItem label="Scoreboard Freeze (minutes)" path="frozenTime">
          <NInputNumber v-model:value="formData.frozenTime" :min="0" :max="100" placeholder="Enter Scoreboard Freeze (minutes)" />
        </NFormItem> -->
        <!-- Number input -->
        <!-- <NFormItem label="Penalty (minutes)" path="penaltyTime">
          <NInputNumber v-model:value="formData.penaltyTime" :min="0" :max="100" placeholder="Enter Penalty (minutes)" />
        </NFormItem> -->
        <!-- Text input -->
        <!-- <NFormItem label="Allowed Languages" path="allowedLanguages">
          <NSelect
            v-model:value="formData.allowedLanguages"
            placeholder="Select Allowed Languages"
            :options="allowLanguageOptions"
            multiple
            clearable
            remote
          />
        </NFormItem> -->
        <!-- Text input -->
        <!--        <NFormItem label="Status" path="status"> -->
        <!--          <NInput v-model:value="formData.status" placeholder="Enter Status" /> -->
        <!--        </NFormItem> -->
        <!-- Number input -->
        <!--        <NFormItem label="Sort Order" path="sort"> -->
        <!--          <NInputNumber v-model:value="formData.sort" :min="0" :max="100" placeholder="Enter Sort Order" /> -->
        <!--        </NFormItem> -->
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
