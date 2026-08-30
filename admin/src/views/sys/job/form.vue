<!--
  Author: Charlie

  任务新建/编辑页。
-->
<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { jobApi } from '@/api'
import { createRequiredRule, toNullableString, wireBool, wireInt } from '@/utils'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInst | null>(null)
const listPath = '/sys/job'

const defaultFormData: Record<string, any> = {
  name: '',
  handler: '',
  trigger_type: 'FIXED',
  trigger_config: '60',
  paramText: '',
  description: '',
  sort: 0,
  enabled: true,
}

const state = reactive({
  loading: false,
  submitLoading: false,
  dataId: null as string | null,
  formModel: { ...defaultFormData },
})

const pageTitle = computed(() => (state.dataId ? '编辑任务' : '新增任务'))

const rules = computed<FormRules>(() => ({
  name: createRequiredRule('任务名称', 'input'),
  handler: createRequiredRule('处理器', 'input'),
  trigger_type: createRequiredRule('触发类型', 'change'),
  trigger_config: createRequiredRule('触发配置', 'input'),
}))

const triggerPlaceholder = computed(() =>
  state.formModel.trigger_type === 'CRON'
    ? '秒 分 时 日 月 周，如：10 15 0/1 * * *'
    : '固定间隔秒数，如：60',
)

function resolveQueryId() {
  const id = route.query.id
  return typeof id === 'string' && id ? id : null
}

async function initPage() {
  const id = resolveQueryId()
  state.dataId = id
  state.formModel = { ...defaultFormData }
  if (id) {
    await fetchDetail(id)
  }
}

async function fetchDetail(id: string) {
  state.loading = true
  try {
    const response = await jobApi.detail({ id })
    const data = response.data ?? {}
    const param =
      data.params && typeof data.params === 'object'
        ? JSON.stringify(data.params, null, 2)
        : ''
    state.formModel = {
      ...defaultFormData,
      ...data,
      enabled: wireBool(data.enabled ?? true),
      sort: data.sort !== undefined && data.sort !== null && data.sort !== ''
        ? wireInt(String(data.sort))
        : 0,
      paramText: param,
    }
  } finally {
    state.loading = false
  }
}

function goBack() {
  router.push(listPath)
}

async function submitForm() {
  await formRef.value?.validate()
  let params: Record<string, any> | undefined
  if (state.formModel.paramText && state.formModel.paramText.trim()) {
    try {
      params = JSON.parse(state.formModel.paramText)
    } catch {
      window.$message.error('任务参数必须是合法的 JSON')
      return
    }
  }
  state.submitLoading = true
  try {
    const payload: Record<string, any> = {
      name: state.formModel.name,
      handler: state.formModel.handler,
      trigger_type: state.formModel.trigger_type,
      trigger_config: state.formModel.trigger_config,
      params,
      description: toNullableString(state.formModel.description),
      sort: Number(state.formModel.sort ?? 0),
      enabled: state.formModel.enabled,
    }
    delete payload.paramText

    if (state.dataId) {
      await jobApi.update({ ...payload, id: state.dataId })
      window.$message.success('更新成功')
    } else {
      await jobApi.create(payload)
      window.$message.success('创建成功')
    }
    goBack()
  } finally {
    state.submitLoading = false
  }
}

onMounted(initPage)
watch(
  () => route.query.id,
  () => {
    void initPage()
  },
)
</script>

<template>
  <div class="h-full min-h-0">
    <NCard
      class="h-full min-h-0 overflow-auto"
      :title="pageTitle"
      :bordered="false"
    >
      <template #header-extra>
        <NSpace>
          <NButton @click="goBack">
            返回
          </NButton>
          <NButton
            type="primary"
            :loading="state.submitLoading"
            @click="submitForm"
          >
            保存
          </NButton>
        </NSpace>
      </template>
      <NSpin :show="state.loading">
        <NForm
          ref="formRef"
          class="job-page-form"
          :model="state.formModel"
          :rules="rules"
          label-placement="left"
          label-width="108"
          :disabled="state.loading || state.submitLoading"
        >
          <div class="form-section-title">
            基本信息
          </div>
          <NGrid
            :cols="2"
            :x-gap="24"
            :y-gap="4"
          >
            <NGi :span="2">
              <NFormItem
                label="任务名称"
                path="name"
              >
                <NInput
                  v-model:value="state.formModel.name"
                  placeholder="请输入任务名称"
                />
              </NFormItem>
            </NGi>
            <NGi :span="2">
              <NFormItem
                label="任务描述"
                path="description"
              >
                <NInput
                  v-model:value="state.formModel.description"
                  type="textarea"
                  :autosize="{ minRows: 2, maxRows: 4 }"
                  placeholder="可选"
                />
              </NFormItem>
            </NGi>
            <NGi>
              <NFormItem
                label="排序"
                path="sort"
              >
                <NInputNumber
                  v-model:value="state.formModel.sort"
                  class="w-full"
                  :min="0"
                />
              </NFormItem>
            </NGi>
            <NGi>
              <NFormItem
                label="是否开启"
                path="enabled"
              >
                <NSwitch v-model:value="state.formModel.enabled" />
              </NFormItem>
            </NGi>
          </NGrid>

          <div class="form-section-title">
            调度配置
          </div>
          <NGrid
            :cols="2"
            :x-gap="24"
            :y-gap="4"
          >
            <NGi :span="2">
              <NFormItem
                label="处理器"
                path="handler"
              >
                <NInput
                  v-model:value="state.formModel.handler"
                  placeholder="JobHandler 全限定类名，如：github.jiangbyte.io.sys.modules.job.sample.SysJobSample"
                />
              </NFormItem>
            </NGi>
            <NGi>
              <NFormItem
                label="触发类型"
                path="trigger_type"
              >
                <NRadioGroup v-model:value="state.formModel.trigger_type">
                  <NSpace>
                    <NRadio value="CRON">
                      CRON 表达式
                    </NRadio>
                    <NRadio value="FIXED">
                      固定间隔
                    </NRadio>
                  </NSpace>
                </NRadioGroup>
              </NFormItem>
            </NGi>
            <NGi>
              <NFormItem
                label="触发配置"
                path="trigger_config"
              >
                <NInput
                  v-model:value="state.formModel.trigger_config"
                  :placeholder="triggerPlaceholder"
                />
              </NFormItem>
            </NGi>
            <NGi :span="2">
              <NFormItem
                label="任务参数"
                path="paramText"
              >
                <NInput
                  v-model:value="state.formModel.paramText"
                  type="textarea"
                  :autosize="{ minRows: 4, maxRows: 10 }"
                  placeholder="JSON 对象，如：{&quot;retentionDays&quot;: 15}"
                />
              </NFormItem>
            </NGi>
          </NGrid>
        </NForm>
      </NSpin>
    </NCard>
  </div>
</template>

<style scoped>
.job-page-form {
  max-width: 960px;
}

.form-section-title {
  margin: 4px 0 14px;
  padding-bottom: 8px;
  color: var(--text-color-2, #666);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.02em;
  border-bottom: 1px solid var(--n-divider-color, rgba(0, 0, 0, 0.08));
}

.form-section-title:not(:first-child) {
  margin-top: 12px;
}
</style>
