<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { ojTeamApi } from '@/api'
import { createRequiredRule } from '@/utils'
import { computed, reactive, ref } from 'vue'

const emit = defineEmits<{
  saved: []
}>()

const formRef = ref<FormInst | null>(null)
const defaultFormData: Record<string, any> = {
  course_id: '',
  name: '',
  description: '',
  visibility: 'PRIVATE',
  max_members: 50,
  member_account_ids: '',
}
const state = reactive({
  showModal: false,
  submitLoading: false,
  formModel: { ...defaultFormData },
})

const rules = computed<FormRules>(() => ({
  course_id: [createRequiredRule('课程 ID', 'input')],
  name: [createRequiredRule('小组名称', 'input')],
}))

function parseAccountIds(text: string) {
  return text.split(/[\s,;，；]+/).map(s => s.trim()).filter(Boolean)
}

function openModal(defaults: Partial<typeof defaultFormData> = {}) {
  state.formModel = { ...defaultFormData, ...defaults }
  state.showModal = true
}

function closeModal() {
  state.showModal = false
  state.submitLoading = false
}

async function submitForm() {
  await formRef.value?.validate()
  state.submitLoading = true
  try {
    await ojTeamApi.createCourseTeam({
      course_id: state.formModel.course_id,
      name: state.formModel.name,
      description: state.formModel.description || null,
      visibility: state.formModel.visibility,
      max_members: state.formModel.max_members,
      member_account_ids: parseAccountIds(state.formModel.member_account_ids),
    })
    window.$message.success('创建成功')
    emit('saved')
    closeModal()
  } finally {
    state.submitLoading = false
  }
}

defineExpose({ openModal })
</script>

<template>
  <NModal
    v-model:show="state.showModal"
    preset="card"
    draggable
    :mask-closable="false"
    title="创建课程小组"
    style="width: 720px"
    :segmented="{ content: true, action: true }"
  >
    <NForm ref="formRef" :model="state.formModel" :rules="rules" label-placement="left" label-width="120" :disabled="state.submitLoading">
      <NFormItem label="课程 ID" path="course_id">
        <NInput v-model:value="state.formModel.course_id" />
      </NFormItem>
      <NFormItem label="小组名称" path="name">
        <NInput v-model:value="state.formModel.name" />
      </NFormItem>
      <NFormItem label="描述" path="description">
        <NInput v-model:value="state.formModel.description" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
      </NFormItem>
      <NFormItem label="可见性" path="visibility">
        <NSelect
          v-model:value="state.formModel.visibility"
          :options="[
            { label: '公开', value: 'PUBLIC' },
            { label: '私有', value: 'PRIVATE' },
          ]"
        />
      </NFormItem>
      <NFormItem label="最大成员数" path="max_members">
        <NInputNumber v-model:value="state.formModel.max_members" class="w-full" :min="2" :max="500" />
      </NFormItem>
      <NFormItem label="初始成员" path="member_account_ids">
        <NInput v-model:value="state.formModel.member_account_ids" type="textarea" placeholder="账户 ID，多个用逗号或换行分隔" :autosize="{ minRows: 2, maxRows: 4 }" />
      </NFormItem>
    </NForm>
    <template #action>
      <NSpace justify="end">
        <NButton @click="closeModal">取消</NButton>
        <NButton type="primary" :loading="state.submitLoading" @click="submitForm">确认</NButton>
      </NSpace>
    </template>
  </NModal>
</template>
