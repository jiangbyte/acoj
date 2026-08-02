<script setup lang="ts">
import type { FormInst, FormRules, SelectOption } from 'naive-ui'
import { Icon } from '@iconify/vue/offline'
import { ojProblemApi, ojProblemLanguageApi } from '@/api'
import { createRequiredRule, hasPermission } from '@/utils'
import { ProCard } from 'pro-naive-ui'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

interface LanguageItem {
  /** server id, or local draft id `draft-xxx` */
  uid: string
  id: string | null
  language_key: string | null
  status: 'ENABLED' | 'DISABLED'
  time_limit_ms: number | null
  memory_limit_kb: number | null
  dirty: boolean
  saving: boolean
}

const route = useRoute()
const router = useRouter()
const props = defineProps<{ problemId?: string, embedded?: boolean }>()
const problemId = computed(() => String(props.problemId ?? route.query.id ?? ''))
const parentTitle = ref('')

const formRefs = ref<Record<string, FormInst | null>>({})
const workerOptions = ref<Array<{ key: string, label: string, extension: string }>>([])
const expandedNames = ref<string[]>([])
const state = reactive({
  loading: false,
  items: [] as LanguageItem[],
})

const canCreate = computed(() => hasPermission('biz:problem:language:create'))
const canUpdate = computed(() => hasPermission('biz:problem:language:update'))
const canDelete = computed(() => hasPermission('biz:problem:language:delete'))

const labelByKey = computed(() =>
  Object.fromEntries(workerOptions.value.map(item => [item.key, item.label])),
)

const rules = computed<FormRules>(() => ({
  language_key: [createRequiredRule('语言', 'select')],
}))

watch(problemId, () => {
  if (!props.embedded) {
    void loadParentTitle()
  }
  void loadAll()
})

onMounted(async () => {
  const tasks: Promise<unknown>[] = [loadWorkerOptions(), loadAll()]
  if (!props.embedded) {
    tasks.push(loadParentTitle())
  }
  await Promise.all(tasks)
})

function setFormRef(uid: string, inst: FormInst | null) {
  formRefs.value[uid] = inst
}

function makeDraft(): LanguageItem {
  return {
    uid: `draft-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`,
    id: null,
    language_key: null,
    status: 'ENABLED',
    time_limit_ms: null,
    memory_limit_kb: null,
    dirty: true,
    saving: false,
  }
}

function isEnabled(item: LanguageItem) {
  return item.status === 'ENABLED'
}

function setEnabled(item: LanguageItem, value: boolean) {
  item.status = value ? 'ENABLED' : 'DISABLED'
  markDirty(item)
}

function itemTitle(item: LanguageItem) {
  if (!item.language_key) {
    return item.id ? '未命名语言' : '新建语言'
  }
  const label = labelByKey.value[item.language_key] || item.language_key
  return `${label} (${item.language_key})`
}

function itemSummary(item: LanguageItem) {
  const status = isEnabled(item) ? '已开启' : '已关闭'
  const time = item.time_limit_ms == null ? '时间默认' : `${item.time_limit_ms} ms`
  const mem = item.memory_limit_kb == null ? '内存默认' : `${item.memory_limit_kb} KB`
  return `${status} · ${time} · ${mem}`
}

function optionsForItem(item: LanguageItem): SelectOption[] {
  const used = new Set(
    state.items
      .filter(row => row.uid !== item.uid && row.language_key)
      .map(row => String(row.language_key)),
  )
  return workerOptions.value
    .filter(opt => !used.has(opt.key) || opt.key === item.language_key)
    .map(opt => ({
      label: `${opt.label} (${opt.key})`,
      value: opt.key,
    }))
}

function markDirty(item: LanguageItem) {
  item.dirty = true
}

function expandAll() {
  expandedNames.value = state.items.map(item => item.uid)
}

function collapseAll() {
  expandedNames.value = []
}

function goBack() {
  router.push('/biz/problem/problem')
}

async function loadParentTitle() {
  if (!problemId.value) {
    parentTitle.value = ''
    return
  }
  try {
    const response = await ojProblemApi.detail({ id: problemId.value })
    const data = response.data ?? {}
    parentTitle.value = data.name ?? data.key ?? problemId.value
  }
  catch {
    parentTitle.value = problemId.value
  }
}

async function loadWorkerOptions() {
  try {
    const response = await ojProblemLanguageApi.options()
    workerOptions.value = response.data ?? []
  }
  catch {
    workerOptions.value = []
  }
}

async function loadAll() {
  if (!problemId.value) {
    state.items = []
    return
  }
  state.loading = true
  try {
    const response = await ojProblemLanguageApi.page(problemId.value, { current: 1, size: 100 })
    const records = response.data?.records ?? []
    state.items = records.map((row: any) => ({
      uid: String(row.id),
      id: String(row.id),
      language_key: row.language_key ?? null,
      status: row.status === 'DISABLED' ? 'DISABLED' : 'ENABLED',
      time_limit_ms: row.time_limit_ms ?? null,
      memory_limit_kb: row.memory_limit_kb ?? null,
      dirty: false,
      saving: false,
    }))
    // default collapsed; only keep still-valid expanded panels (e.g. unsaved drafts)
    const valid = new Set(state.items.map(item => item.uid))
    expandedNames.value = expandedNames.value.filter(name => valid.has(name))
  }
  finally {
    state.loading = false
  }
}

function addLanguage() {
  if (!canCreate.value) {
    return
  }
  if (!workerOptions.value.length) {
    window.$message.warning('暂无可用 worker 语言')
    return
  }
  const used = new Set(state.items.map(item => item.language_key).filter(Boolean))
  const nextKey = workerOptions.value.find(opt => !used.has(opt.key))?.key ?? null
  if (!nextKey && used.size >= workerOptions.value.length) {
    window.$message.warning('所有 worker 语言均已添加')
    return
  }
  const draft = makeDraft()
  draft.language_key = nextKey
  state.items = [...state.items, draft]
  expandedNames.value = [...expandedNames.value, draft.uid]
}

async function saveItem(item: LanguageItem) {
  if (!problemId.value) {
    window.$message.error('缺少题目 ID')
    return
  }
  const form = formRefs.value[item.uid]
  await form?.validate()
  if (!item.language_key) {
    return
  }
  if (item.id && !canUpdate.value) {
    return
  }
  if (!item.id && !canCreate.value) {
    return
  }

  item.saving = true
  try {
    const payload = {
      problem_id: problemId.value,
      language_key: item.language_key,
      status: item.status,
      time_limit_ms: item.time_limit_ms,
      memory_limit_kb: item.memory_limit_kb,
    }
    if (item.id) {
      await ojProblemLanguageApi.update(problemId.value, { ...payload, id: item.id })
      window.$message.success('已保存')
    }
    else {
      await ojProblemLanguageApi.create(problemId.value, payload)
      window.$message.success('已添加')
    }
    item.dirty = false
    await loadAll()
  }
  finally {
    item.saving = false
  }
}

function confirmRemove(item: LanguageItem) {
  if (!item.id) {
    state.items = state.items.filter(row => row.uid !== item.uid)
    expandedNames.value = expandedNames.value.filter(name => name !== item.uid)
    return
  }
  if (!canDelete.value) {
    return
  }
  window.$dialog.warning({
    title: '删除语言',
    content: `确定删除 ${itemTitle(item)}？`,
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: () => removeItem(item),
  })
}

async function removeItem(item: LanguageItem) {
  if (!item.id || !problemId.value) {
    return
  }
  await ojProblemLanguageApi.remove(problemId.value, { ids: [item.id] })
  window.$message.success('已删除')
  await loadAll()
}
</script>

<template>
  <NFlex :class="props.embedded ? 'min-h-0' : 'h-full min-h-0'" vertical :size="12">
    <ProCard v-if="!props.embedded">
      <NFlex align="center" :size="12">
        <NButton text @click="goBack">
          返回题目列表
        </NButton>
        <span class="font-medium">题目语言</span>
        <span v-if="parentTitle" class="text-gray-500">{{ parentTitle }}</span>
      </NFlex>
    </ProCard>

    <ProCard content-class="min-h-0">
      <NSpin :show="state.loading">
        <NFlex vertical :size="12">
          <NFlex align="center" justify="space-between" :wrap="true" :size="8">
            <NFlex align="center" :size="8">
              <span class="font-medium">已启用语言</span>
              <NTag size="small" :bordered="false">
                {{ state.items.length }}
              </NTag>
              <span class="text-gray-400 text-sm">选项来自 /language/options（worker 镜像显式启用），可单独覆盖时限/内存</span>
            </NFlex>
            <NFlex :size="8">
              <NButton text size="small" @click="expandAll">
                全部展开
              </NButton>
              <NButton text size="small" @click="collapseAll">
                全部收起
              </NButton>
              <NButton text size="small" :loading="state.loading" @click="loadAll">
                <template #icon>
                  <NIcon><Icon icon="icon-park-outline:refresh" /></NIcon>
                </template>
                刷新
              </NButton>
              <NButton
                v-if="canCreate"
                type="primary"
                size="small"
                @click="addLanguage"
              >
                <template #icon>
                  <NIcon><Icon icon="icon-park-outline:plus" /></NIcon>
                </template>
                添加语言
              </NButton>
            </NFlex>
          </NFlex>

          <NEmpty v-if="!state.loading && !state.items.length" description="尚未配置题目语言">
            <template #extra>
              <NButton v-if="canCreate" type="primary" @click="addLanguage">
                添加第一门语言
              </NButton>
            </template>
          </NEmpty>

          <NCollapse
            v-else
            v-model:expanded-names="expandedNames"
            display-directive="show"
            arrow-placement="right"
          >
            <NCollapseItem
              v-for="item in state.items"
              :key="item.uid"
              :name="item.uid"
            >
              <template #header>
                <NFlex align="center" :size="10" class="pr-8px">
                  <span class="font-medium">{{ itemTitle(item) }}</span>
                  <NTag v-if="!item.id" size="tiny" type="warning" :bordered="false">
                    未保存
                  </NTag>
                  <NTag v-else-if="item.dirty" size="tiny" type="info" :bordered="false">
                    已修改
                  </NTag>
                  <NTag
                    size="tiny"
                    :type="isEnabled(item) ? 'success' : 'default'"
                    :bordered="false"
                  >
                    {{ isEnabled(item) ? '开启' : '关闭' }}
                  </NTag>
                  <span class="text-gray-400 text-sm">{{ itemSummary(item) }}</span>
                </NFlex>
              </template>
              <template #header-extra>
                <NFlex :size="4" align="center" @click.stop>
                  <NSwitch
                    v-if="item.id && canUpdate"
                    :value="isEnabled(item)"
                    size="small"
                    :disabled="item.saving"
                    @update:value="(value: boolean) => { setEnabled(item, value); void saveItem(item) }"
                  />
                  <NButton
                    v-if="canDelete || !item.id"
                    size="tiny"
                    type="error"
                    quaternary
                    @click="confirmRemove(item)"
                  >
                    删除
                  </NButton>
                </NFlex>
              </template>

              <NForm
                :ref="(inst: any) => setFormRef(item.uid, inst)"
                :model="item"
                :rules="rules"
                label-placement="left"
                label-width="120"
                class="pt-8px"
                :disabled="item.saving"
              >
                <NGrid :cols="2" :x-gap="16">
                  <NFormItemGi label="语言" path="language_key" :span="2">
                    <NSelect
                      v-model:value="item.language_key"
                      filterable
                      :options="optionsForItem(item)"
                      placeholder="选择 worker 语言"
                      :disabled="Boolean(item.id)"
                      @update:value="markDirty(item)"
                    />
                  </NFormItemGi>
                  <NFormItemGi label="状态" path="status" :span="2">
                    <NFlex align="center" :size="10">
                      <NSwitch
                        :value="isEnabled(item)"
                        :disabled="item.saving || (Boolean(item.id) && !canUpdate)"
                        @update:value="(value: boolean) => setEnabled(item, value)"
                      />
                      <span class="text-gray-500 text-sm">
                        {{ isEnabled(item) ? 'ENABLED：提交/试测可选' : 'DISABLED：保留配置但不开放' }}
                      </span>
                    </NFlex>
                  </NFormItemGi>
                  <NFormItemGi label="时间限制（毫秒）" path="time_limit_ms">
                    <NInputNumber
                      v-model:value="item.time_limit_ms"
                      class="w-full"
                      clearable
                      placeholder="空则用题目默认"
                      @update:value="markDirty(item)"
                    />
                  </NFormItemGi>
                  <NFormItemGi label="内存限制（KB）" path="memory_limit_kb">
                    <NInputNumber
                      v-model:value="item.memory_limit_kb"
                      class="w-full"
                      clearable
                      placeholder="空则用题目默认"
                      @update:value="markDirty(item)"
                    />
                  </NFormItemGi>
                </NGrid>

                <NFlex justify="end" :size="8" class="mt-4px">
                  <NButton
                    v-if="(item.id && canUpdate) || (!item.id && canCreate)"
                    type="primary"
                    size="small"
                    :loading="item.saving"
                    :disabled="item.id ? !item.dirty : false"
                    @click="saveItem(item)"
                  >
                    {{ item.id ? '保存' : '创建' }}
                  </NButton>
                </NFlex>
              </NForm>
            </NCollapseItem>
          </NCollapse>

          <NButton
            v-if="canCreate && state.items.length"
            dashed
            block
            @click="addLanguage"
          >
            <template #icon>
              <NIcon><Icon icon="icon-park-outline:plus" /></NIcon>
            </template>
            添加语言
          </NButton>
        </NFlex>
      </NSpin>
    </ProCard>
  </NFlex>
</template>
