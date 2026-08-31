<!--
  Author: Charlie

  OJ 题目详情页（仅题目信息，测例在独立页维护）。
-->
<script setup lang="ts">
import { MdPreview } from '@/components/editor'
import { ojProblemApi } from '@/api'
import {
  createTagColor,
  dictTypeColor,
  dictTypeData,
  displayValue,
  formatDateTime,
  hasPermission,
} from '@/utils'
import { computed, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const listPath = '/oj/problem'

const difficultyLabel: Record<string, string> = {
  EASY: '简单',
  MEDIUM: '中等',
  HARD: '困难',
}

const state = reactive({
  loading: false,
  detail: {} as any,
})

const dataId = computed(() => {
  const id = route.query.id
  return typeof id === 'string' ? id : ''
})

const languageLimitRows = computed(() => {
  const rows = state.detail.language_limits
  return Array.isArray(rows) ? rows : []
})

const problemTags = computed(() => {
  const tags = state.detail.tags
  return Array.isArray(tags) ? tags : []
})

const sampleList = computed(() => {
  const raw = state.detail.samples
  let list: unknown[] = []
  if (Array.isArray(raw)) {
    list = raw
  } else if (typeof raw === 'string' && raw.trim()) {
    try {
      const parsed = JSON.parse(raw)
      if (Array.isArray(parsed)) list = parsed
    } catch {
      list = []
    }
  }
  return list.filter((item) => item && typeof item === 'object') as Array<Record<string, unknown>>
})

async function fetchDetail(id: string) {
  if (!id) return
  state.loading = true
  try {
    const response = await ojProblemApi.detail({ id })
    state.detail = response.data ?? {}
  } finally {
    state.loading = false
  }
}

function sampleField(item: Record<string, unknown>, key: string) {
  const value = item[key]
  if (value === undefined || value === null || value === '') {
    return '—'
  }
  return String(value)
}

function goBack() {
  router.push(listPath)
}

function goEdit() {
  if (!dataId.value) return
  router.push({ path: '/oj/problem/edit', query: { id: dataId.value } })
}

function goCases() {
  if (!dataId.value) return
  router.push({ path: '/oj/problem/cases', query: { id: dataId.value } })
}

onMounted(() => {
  void fetchDetail(dataId.value)
})
watch(dataId, (id) => {
  void fetchDetail(id)
})
</script>

<template>
  <div class="h-full min-h-0">
    <NCard
      class="h-full min-h-0 overflow-auto"
      title="题目详情"
      :bordered="false"
    >
      <template #header-extra>
        <NSpace>
          <NButton @click="goBack">
            返回
          </NButton>
          <NButton
            v-if="hasPermission('oj:problem:update') && dataId"
            @click="goCases"
          >
            测例
          </NButton>
          <NButton
            v-if="hasPermission('oj:problem:update') && dataId"
            type="primary"
            @click="goEdit"
          >
            编辑
          </NButton>
        </NSpace>
      </template>
      <NSpin :show="state.loading">
        <div class="detail-page">
          <header class="detail-header">
            <h1 class="detail-title">
              {{ displayValue(state.detail.problem_key) }} · {{ displayValue(state.detail.title) }}
            </h1>
          </header>

          <section class="meta-section">
            <h2 class="section-label">
              基础信息
            </h2>
            <div class="meta-grid">
              <div class="meta-item">
                <div class="meta-key">
                  难度
                </div>
                <div class="meta-value">
                  {{
                    difficultyLabel[state.detail.difficulty] ||
                      displayValue(state.detail.difficulty)
                  }}
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  状态
                </div>
                <div class="meta-value">
                  <NTag
                    :color="createTagColor(dictTypeColor('OJ_PROBLEM_STATUS', state.detail.status))"
                    :bordered="false"
                  >
                    {{
                      dictTypeData('OJ_PROBLEM_STATUS', state.detail.status) ||
                        displayValue(state.detail.status)
                    }}
                  </NTag>
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  语言限额
                </div>
                <div class="meta-value">
                  <div
                    v-if="languageLimitRows.length"
                    class="space-y-1"
                  >
                    <div
                      v-for="row in languageLimitRows"
                      :key="row.id || row.language"
                    >
                      {{ displayValue(row.language) }}：
                      {{ displayValue(row.time_limit_ms) }} ms /
                      {{ displayValue(row.memory_limit_bytes) }} B
                    </div>
                  </div>
                  <template v-else>
                    —
                  </template>
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  测例版本
                </div>
                <div class="meta-value">
                  {{ displayValue(state.detail.case_version) }}
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  标签
                </div>
                <div class="meta-value">
                  <NSpace
                    v-if="problemTags.length"
                    :size="6"
                  >
                    <NTag
                      v-for="tag in problemTags"
                      :key="tag.id || tag.name"
                      size="small"
                      :bordered="false"
                    >
                      {{ tag.name || displayValue(tag.id) }}
                    </NTag>
                  </NSpace>
                  <template v-else>
                    —
                  </template>
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  来源
                </div>
                <div class="meta-value">
                  {{ displayValue(state.detail.source) }}
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  提交/AC
                </div>
                <div class="meta-value">
                  {{ displayValue(state.detail.submit_count) }} /
                  {{ displayValue(state.detail.accept_count) }}
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  更新时间
                </div>
                <div class="meta-value">
                  {{ formatDateTime(state.detail.updated_at) }}
                </div>
              </div>
            </div>
          </section>

          <section class="content-section content-section--full">
            <h2 class="section-label">
              题面
            </h2>
            <MdPreview
              class="statement-preview"
              :value="state.detail.statement_md ?? ''"
            />
          </section>

          <section class="meta-section">
            <h2 class="section-label">
              输入 / 输出格式
            </h2>
            <div class="meta-grid">
              <div class="meta-item">
                <div class="meta-key">
                  输入格式
                </div>
                <div class="meta-value whitespace-pre-wrap">
                  {{ displayValue(state.detail.input_format) }}
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  输出格式
                </div>
                <div class="meta-value whitespace-pre-wrap">
                  {{ displayValue(state.detail.output_format) }}
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  提示
                </div>
                <div class="meta-value whitespace-pre-wrap">
                  {{ displayValue(state.detail.hint) }}
                </div>
              </div>
            </div>
          </section>

          <section class="content-section">
            <h2 class="section-label">
              样例
            </h2>
            <div
              v-if="!sampleList.length"
              class="sample-empty"
            >
              暂无样例
            </div>
            <div
              v-for="(item, index) in sampleList"
              :key="index"
              class="sample-block"
            >
              <div class="sample-title">
                样例 {{ index + 1 }}
              </div>
              <div class="sample-field">
                <div class="meta-key">
                  输入
                </div>
                <pre class="sample-pre">{{ sampleField(item, 'input') }}</pre>
              </div>
              <div class="sample-field">
                <div class="meta-key">
                  输出
                </div>
                <pre class="sample-pre">{{ sampleField(item, 'output') }}</pre>
              </div>
              <div
                v-if="item.explanation !== undefined && item.explanation !== null && String(item.explanation).trim()"
                class="sample-field"
              >
                <div class="meta-key">
                  说明
                </div>
                <div class="meta-value whitespace-pre-wrap">
                  {{ sampleField(item, 'explanation') }}
                </div>
              </div>
            </div>
          </section>
        </div>
      </NSpin>
    </NCard>
  </div>
</template>

<style scoped>
.detail-page {
  width: 100%;
}

.statement-preview {
  width: 100%;
}

.content-section--full :deep(.editor-preview) {
  width: 100%;
}

.detail-header {
  margin-bottom: 28px;
}

.detail-title {
  margin: 0 0 14px;
  color: var(--text-color-1, #1f1f1f);
  font-size: 22px;
  font-weight: 650;
  line-height: 1.35;
}

.meta-section,
.content-section {
  margin-bottom: 28px;
}

.section-label {
  margin: 0 0 14px;
  color: var(--text-color-2, #666);
  font-size: 13px;
  font-weight: 600;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px 28px;
}

.meta-item {
  min-width: 0;
}

.meta-key {
  margin-bottom: 4px;
  color: var(--text-color-3, #999);
  font-size: 12px;
  line-height: 1.4;
}

.meta-value {
  color: var(--text-color-1, #333);
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.whitespace-pre-wrap {
  white-space: pre-wrap;
  word-break: break-word;
}

.sample-empty {
  color: var(--text-color-3, #999);
  font-size: 14px;
}

.sample-block {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--n-divider-color, rgba(0, 0, 0, 0.08));
}

.sample-block:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.sample-title {
  margin-bottom: 10px;
  color: var(--text-color-1, #333);
  font-size: 14px;
  font-weight: 600;
}

.sample-field {
  margin-bottom: 10px;
}

.sample-field:last-child {
  margin-bottom: 0;
}

.sample-pre {
  margin: 0;
  padding: 10px 12px;
  color: var(--text-color-1, #333);
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
  background: var(--n-color-embedded, rgba(0, 0, 0, 0.04));
  border-radius: 6px;
}

@media (max-width: 960px) {
  .meta-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .meta-grid {
    grid-template-columns: 1fr;
  }
}
</style>
