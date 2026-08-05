<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import type { ClassOption } from '@/components/selector/ClassSelector.vue'
import { ojClazzApi, ojCourseApi } from '@/api'
import ClassSelector from '@/components/selector/ClassSelector.vue'
import { createRequiredRule } from '@/utils'
import { computed, reactive, ref } from 'vue'

const emit = defineEmits<{
  saved: []
}>()

const formRef = ref<FormInst | null>(null)
const defaultFormData: Record<string, any> = {
  name: '',
  summary: '',
  access_scope: 'CLASS',
  visibility: 'PRIVATE',
  binding_mode: 'SHARED',
  sort: 0,
}
const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  formModel: { ...defaultFormData },
  selectedClasses: [] as ClassOption[],
  showClassSelector: false,
})

const modalTitle = computed(() => state.dataId ? '编辑课程' : '新增课程')
const selectedClassText = computed(() => {
  if (!state.selectedClasses.length) return ''
  return state.selectedClasses.map(item => `${item.name}（${item.code}）`).join('、')
})
const isOpenCourse = computed(() => state.formModel.access_scope === 'OPEN')
const bindingModeHint = computed(() => {
  if (state.formModel.binding_mode === 'PER_CLASS') {
    return '分班开课：每个班级各创建一门独立课程（任务/时间可不同）'
  }
  return '合班上课：多个班级共用同一门课程与课内小组'
})

const rules = computed<FormRules>(() => ({
  class_ids: [{
    key: 'class_ids',
    required: !isOpenCourse.value,
    validator: () => {
      if (isOpenCourse.value) return true
      if (!state.selectedClasses.length) {
        return new Error('请选择班级')
      }
      if (state.dataId && state.formModel.binding_mode === 'PER_CLASS' && state.selectedClasses.length > 1) {
        return new Error('分班课程只能关联一个班级')
      }
      return true
    },
    trigger: ['change', 'blur'],
  }],
  name: [createRequiredRule('课程名称', 'input')],
  sort: [{
    validator: () => typeof state.formModel.sort === 'number' && Number.isFinite(state.formModel.sort),
    message: '请输入排序',
    trigger: ['input', 'blur'],
  }],
}))

async function openModal(id?: string, defaults: Partial<{ class_id: string } & typeof defaultFormData> = {}) {
  state.dataId = id ?? null
  state.formModel = { ...defaultFormData }
  state.selectedClasses = []
  state.showModal = true

  if (defaults.name) state.formModel.name = defaults.name
  if (defaults.summary !== undefined) state.formModel.summary = defaults.summary
  if (defaults.visibility) state.formModel.visibility = defaults.visibility
  if (defaults.binding_mode) state.formModel.binding_mode = defaults.binding_mode
  if (typeof defaults.sort === 'number') state.formModel.sort = defaults.sort

  if (id) {
    await fetchDetail(id)
    return
  }

  if (defaults.class_id) {
    await preloadClass(defaults.class_id)
  }
}

async function resolveClassOption(classId: string): Promise<ClassOption> {
  try {
    const response = await ojClazzApi.detail({ id: classId })
    const data = response.data ?? {}
    return {
      id: String(data.id ?? classId),
      code: data.code || '',
      name: data.name || classId,
      status: data.status,
      visibility: data.visibility,
      member_count: data.member_count,
    }
  }
  catch {
    return { id: classId, code: '', name: classId }
  }
}

async function preloadClass(classId: string) {
  state.selectedClasses = [await resolveClassOption(classId)]
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await ojCourseApi.detail({ id })
    const data = response.data ?? {}
    state.formModel = {
      name: data.name ?? '',
      summary: data.summary ?? '',
      access_scope: data.access_scope ?? 'CLASS',
      visibility: data.visibility ?? 'PRIVATE',
      binding_mode: data.binding_mode ?? 'PER_CLASS',
      sort: data.sort ?? 0,
    }
    const classes = Array.isArray(data.classes) ? data.classes : []
    if (classes.length) {
      state.selectedClasses = classes.map((item: any) => ({
        id: String(item.id),
        code: item.code || '',
        name: item.name || item.id,
      }))
    }
    else if (Array.isArray(data.class_ids) && data.class_ids.length) {
      const options: ClassOption[] = []
      for (const classId of data.class_ids) {
        options.push(await resolveClassOption(String(classId)))
      }
      state.selectedClasses = options
    }
  }
  finally {
    state.loading = false
  }
}

function closeModal() {
  state.showModal = false
  state.submitLoading = false
  state.showClassSelector = false
}

function handleClassConfirm(classes: ClassOption[]) {
  state.selectedClasses = classes
  state.showClassSelector = false
  formRef.value?.validate(['class_ids']).catch(() => {})
}

function clearClasses() {
  state.selectedClasses = []
}

function removeClass(id: string) {
  state.selectedClasses = state.selectedClasses.filter(item => item.id !== id)
}

async function submitForm() {
  await formRef.value?.validate()
  state.submitLoading = true
  try {
    const class_ids = isOpenCourse.value ? [] : state.selectedClasses.map(item => item.id)
    const payload = {
      name: state.formModel.name,
      summary: state.formModel.summary || null,
      access_scope: state.formModel.access_scope,
      visibility: isOpenCourse.value ? 'PUBLIC' : state.formModel.visibility,
      sort: state.formModel.sort,
      class_ids,
    }
    if (state.dataId) {
      await ojCourseApi.update({ ...payload, id: state.dataId })
      window.$message.success('更新成功')
    }
    else {
      const res = await ojCourseApi.create({
        ...payload,
        binding_mode: isOpenCourse.value ? 'SHARED' : state.formModel.binding_mode,
      })
      const ids = res.data?.ids ?? (res.data?.id ? [res.data.id] : [])
      if (!isOpenCourse.value && state.formModel.binding_mode === 'PER_CLASS' && ids.length > 1) {
        window.$message.success(`已为 ${ids.length} 个班级分别创建课程`)
      }
      else {
        window.$message.success('创建成功')
      }
    }
    emit('saved')
    closeModal()
  }
  finally {
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
    :title="modalTitle"
    style="width: 720px"
    :segmented="{ content: true, action: true }"
  >
    <NSpin :show="state.loading">
      <NScrollbar class="max-h-[min(620px,calc(100vh-300px))] pr-16px">
        <NForm ref="formRef" :model="state.formModel" :rules="rules" label-placement="left" label-width="110" :disabled="state.loading || state.submitLoading">
          <NFormItem label="课程类型" path="access_scope">
            <NSpace vertical class="w-full">
              <NRadioGroup v-model:value="state.formModel.access_scope" :disabled="!!state.dataId">
                <NSpace>
                  <NRadio value="OPEN">
                    公开课
                  </NRadio>
                  <NRadio value="CLASS">
                    私有课
                  </NRadio>
                </NSpace>
              </NRadioGroup>
              <NText depth="3" class="text-12px">
                {{ isOpenCourse ? '所有人可浏览学习；交作业/进小组需登录' : '仅关联班级成员可学习' }}
              </NText>
            </NSpace>
          </NFormItem>
          <template v-if="!isOpenCourse">
            <NFormItem v-if="!state.dataId" label="开课模式" path="binding_mode">
              <NSpace vertical class="w-full">
                <NRadioGroup v-model:value="state.formModel.binding_mode">
                  <NSpace>
                    <NRadio value="SHARED">
                      合班上课
                    </NRadio>
                    <NRadio value="PER_CLASS">
                      分班开课
                    </NRadio>
                  </NSpace>
                </NRadioGroup>
                <NText depth="3" class="text-12px">
                  {{ bindingModeHint }}
                </NText>
              </NSpace>
            </NFormItem>
            <NFormItem v-else label="开课模式">
              <NTag :type="state.formModel.binding_mode === 'SHARED' ? 'success' : 'info'">
                {{ state.formModel.binding_mode === 'SHARED' ? '合班上课' : '分班开课' }}
              </NTag>
            </NFormItem>
            <NFormItem label="所属班级" path="class_ids">
              <NSpace vertical class="w-full">
                <NInputGroup>
                  <NInput
                    :value="selectedClassText"
                    readonly
                    :placeholder="state.formModel.binding_mode === 'SHARED' ? '请选择班级（可多选，共用一门课）' : '请选择班级（可多选，每班一门课）'"
                  />
                  <NButton type="primary" @click="state.showClassSelector = true">
                    选择
                  </NButton>
                  <NButton :disabled="!state.selectedClasses.length" @click="clearClasses">
                    清除
                  </NButton>
                </NInputGroup>
                <NSpace v-if="state.selectedClasses.length" :size="8">
                  <NTag
                    v-for="item in state.selectedClasses"
                    :key="item.id"
                    closable
                    @close="removeClass(item.id)"
                  >
                    {{ item.name }}（{{ item.code }}）
                  </NTag>
                </NSpace>
              </NSpace>
            </NFormItem>
          </template>
          <NFormItem label="课程名称" path="name">
            <NInput v-model:value="state.formModel.name" />
          </NFormItem>
          <NFormItem label="简介" path="summary">
            <NInput v-model:value="state.formModel.summary" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" />
          </NFormItem>
          <NFormItem v-if="!isOpenCourse" label="可见性" path="visibility">
            <NSelect
              v-model:value="state.formModel.visibility"
              :options="[
                { label: '公开', value: 'PUBLIC' },
                { label: '私有', value: 'PRIVATE' },
              ]"
            />
          </NFormItem>
          <NFormItem label="排序" path="sort">
            <NInputNumber v-model:value="state.formModel.sort" class="w-full" />
          </NFormItem>
        </NForm>
      </NScrollbar>
    </NSpin>
    <template #action>
      <NSpace justify="end">
        <NButton @click="closeModal">
          取消
        </NButton>
        <NButton type="primary" :loading="state.submitLoading" @click="submitForm">
          确认
        </NButton>
      </NSpace>
    </template>
  </NModal>

  <ClassSelector
    v-model:visible="state.showClassSelector"
    mode="multiple"
    :selected="state.selectedClasses"
    @confirm="handleClassConfirm"
  />
</template>
