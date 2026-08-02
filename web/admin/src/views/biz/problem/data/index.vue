<script setup lang="ts">
import type { SelectOption } from 'naive-ui'
import { ojProblemDataApi, ojProblemLanguageApi } from '@/api'
import JudgeSourceEditor from '@/components/editor/JudgeSourceEditor.vue'
import FileUpload from '@/components/upload/FileUpload.vue'
import { createRequiredRule } from '@/utils'
import { monacoLanguageFromExtension } from '../shared/monacoLanguage'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const formRef = ref<any>(null)

const props = withDefaults(defineProps<{
  problemId?: string
  embedded?: boolean
  /** 嵌入时必填：judge=配置，import=导入 */
  mode?: 'judge' | 'import'
}>(), {
  embedded: false,
})

const problemId = computed(() => String(props.problemId ?? route.query.id ?? ''))

const judgeModeOptions = [
  { label: '标准比对 (STANDARD)', value: 'STANDARD' },
  { label: '特殊评测 SPJ (SPECIAL_JUDGE)', value: 'SPECIAL_JUDGE' },
  { label: '交互题 (INTERACTIVE)', value: 'INTERACTIVE' },
]

const workerLanguageOptions = ref<SelectOption[]>([])
const extByKey = ref<Record<string, string>>({})

const defaultFormData = {
  judge_mode: 'STANDARD',
  spj_source: '',
  interactor_source: '',
  interactor_language_key: 'cpp17',
}

const state = reactive({
  loading: false,
  submitLoading: false,
  importing: false,
  dataId: null as string | null,
  formModel: { ...defaultFormData },
  zipFileKey: '',
  replaceCases: true,
  lastImportCount: null as number | null,
})

const interactorMonacoLanguage = computed(() =>
  monacoLanguageFromExtension(extByKey.value[state.formModel.interactor_language_key]),
)

const rules = computed(() => {
  const base: Record<string, any> = {
    judge_mode: [createRequiredRule('判题模式', 'select')],
  }
  if (state.formModel.judge_mode === 'SPECIAL_JUDGE') {
    base.spj_source = [{ required: true, message: '请输入 SPJ 源码', trigger: ['input', 'blur'] }]
  }
  if (state.formModel.judge_mode === 'INTERACTIVE') {
    base.interactor_language_key = [createRequiredRule('交互器语言', 'select')]
    base.interactor_source = [{ required: true, message: '请输入交互器源码', trigger: ['input', 'blur'] }]
  }
  return base
})

watch(problemId, () => {
  void loadConfig()
})

onMounted(async () => {
  await Promise.all([loadWorkerLanguages(), loadConfig()])
})

async function loadWorkerLanguages() {
  try {
    const response = await ojProblemLanguageApi.options()
    const items = response.data ?? []
    extByKey.value = Object.fromEntries(items.map((item: any) => [item.key, item.extension]))
    workerLanguageOptions.value = items.map((item: any) => ({
      label: `${item.label} (${item.key})`,
      value: item.key,
    }))
  }
  catch {
    workerLanguageOptions.value = []
  }
}

async function loadConfig() {
  if (!problemId.value) {
    return
  }
  state.loading = true
  try {
    const response = await ojProblemDataApi.page(problemId.value, { current: 1, size: 1 })
    const record = response.data?.records?.[0]
    if (record) {
      state.dataId = record.id
      state.formModel = {
        judge_mode: record.judge_mode || 'STANDARD',
        spj_source: record.spj_source ?? '',
        interactor_source: record.interactor_source ?? '',
        interactor_language_key: record.interactor_language_key || 'cpp17',
      }
    }
    else {
      state.dataId = null
      state.formModel = { ...defaultFormData }
    }
  }
  finally {
    state.loading = false
  }
}

async function saveConfig() {
  if (!problemId.value) {
    return
  }
  try {
    await formRef.value?.validate()
  }
  catch {
    return
  }

  const isSpj = state.formModel.judge_mode === 'SPECIAL_JUDGE'
  const isInteractive = state.formModel.judge_mode === 'INTERACTIVE'

  state.submitLoading = true
  try {
    const payload: Record<string, any> = {
      problem_id: problemId.value,
      judge_mode: state.formModel.judge_mode,
      spj_source: isSpj ? state.formModel.spj_source : null,
      interactor_source: isInteractive ? state.formModel.interactor_source : null,
      interactor_language_key: isInteractive ? state.formModel.interactor_language_key : null,
    }

    if (state.dataId) {
      await ojProblemDataApi.update(problemId.value, { ...payload, id: state.dataId })
      window.$message.success('更新成功')
    }
    else {
      await ojProblemDataApi.create(problemId.value, payload)
      window.$message.success('创建成功')
    }
    await loadConfig()
  }
  finally {
    state.submitLoading = false
  }
}

async function importZip() {
  if (!problemId.value) {
    return
  }
  if (!state.zipFileKey) {
    window.$message.warning('请先上传 zip 文件')
    return
  }
  state.importing = true
  try {
    const response = await ojProblemDataApi.importZip(problemId.value, {
      zip_file_key: state.zipFileKey,
      replace: state.replaceCases,
    })
    const data = response.data ?? {}
    state.lastImportCount = Number(data.imported ?? 0)
    window.$message.success(
      `已展开为 ${state.lastImportCount} 条测试用例（写入测试用例表，判题按行发送）`,
    )
    await loadConfig()
  }
  finally {
    state.importing = false
  }
}
</script>

<template>
  <NFlex class="h-full min-h-0" vertical :size="12">
    <NSpin :show="state.loading" class="min-h-0 flex-1">
      <div v-if="props.mode === 'judge' || (!props.embedded && !props.mode)">
        <NFlex v-if="props.embedded" justify="end" class="mb-12px">
          <NButton type="primary" :loading="state.submitLoading" @click="saveConfig">
            保存配置
          </NButton>
        </NFlex>
        <NForm ref="formRef" :model="state.formModel" :rules="rules" label-placement="left" label-width="150">
          <NGrid :cols="2" :x-gap="16">
            <NFormItemGi label="判题模式" path="judge_mode" :span="2">
              <NSelect v-model:value="state.formModel.judge_mode" :options="judgeModeOptions" />
            </NFormItemGi>

            <template v-if="state.formModel.judge_mode === 'STANDARD'">
              <NFormItemGi :span="2">
                <NAlert type="info" :bordered="false">
                  Worker STANDARD：去掉末尾空白后逐测例比对输出。ACM（不允许部分分）首错即停；OI（允许部分分）累加得分。
                </NAlert>
              </NFormItemGi>
            </template>

            <template v-else-if="state.formModel.judge_mode === 'SPECIAL_JUDGE'">
              <NFormItemGi label="SPJ 源码（C++17 / testlib）" path="spj_source" :span="2">
                <JudgeSourceEditor
                  v-model:value="state.formModel.spj_source"
                  language="cpp"
                  height="360px"
                  load-button-text="从本地文件载入 SPJ（.cpp）"
                />
              </NFormItemGi>
              <NFormItemGi :span="2">
                <NAlert type="warning" :bordered="false">
                  以 worker 为准：SPECIAL_JUDGE 固定用 testlib_checker_language（g++ -std=c++17），不支持多语言；payload 中的 spj.language 会被忽略。请按 testlib 约定编写 checker（参数：input / user_out / answer）。
                </NAlert>
              </NFormItemGi>
            </template>

            <template v-else-if="state.formModel.judge_mode === 'INTERACTIVE'">
              <NFormItemGi label="交互器语言" path="interactor_language_key">
                <NSelect
                  v-model:value="state.formModel.interactor_language_key"
                  :options="workerLanguageOptions"
                  filterable
                  placeholder="选择 worker 支持的语言"
                />
              </NFormItemGi>
              <NFormItemGi label="交互器源码" path="interactor_source" :span="2">
                <JudgeSourceEditor
                  v-model:value="state.formModel.interactor_source"
                  :language="interactorMonacoLanguage"
                  height="360px"
                  load-button-text="从本地文件载入交互器"
                />
              </NFormItemGi>
              <NFormItemGi :span="2">
                <NAlert type="info" :bordered="false">
                  以 worker 为准：INTERACTIVE 使用 payload.interactor.language 编译运行（与用户题解语言相互独立）。可选语言与题目语言相同，均来自 /language/options（worker 镜像显式启用）；用户程序与交互器经 FIFO 通信。
                </NAlert>
              </NFormItemGi>
            </template>
          </NGrid>
        </NForm>
      </div>

      <div v-else-if="props.mode === 'import'">
        <NAlert type="info" class="mb-12px" :bordered="false">
          Zip 只用于批量导入。导入后写入「测例」表；试判发给 worker 的是这些行，不会发送压缩包本身。
        </NAlert>
        <NForm label-placement="left" label-width="140">
          <NFormItem label="Zip 文件">
            <FileUpload
              v-model:value="state.zipFileKey"
              value-type="object_name"
              accept=".zip,application/zip"
              button-text="上传 zip"
            />
          </NFormItem>
          <NFormItem label="替换已有测例">
            <NSwitch v-model:value="state.replaceCases" />
            <span class="ml-8px text-gray-500 text-sm">开启后先清空本题测试用例表再写入</span>
          </NFormItem>
          <NFormItem>
            <NFlex :size="8">
              <NButton type="primary" :loading="state.importing" @click="importZip">
                展开写入测试用例表
              </NButton>
              <NTag v-if="state.lastImportCount != null" type="success">
                已写入 {{ state.lastImportCount }} 条
              </NTag>
            </NFlex>
          </NFormItem>
        </NForm>
      </div>
    </NSpin>
  </NFlex>
</template>
