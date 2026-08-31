<!--
  Author: Charlie

  OJ 提交详情页。
-->
<script setup lang="ts">
import { MonacoPreview } from '@/components/editor'
import { ojSubmissionApi } from '@/api'
import { displayValue, formatDateTime, mapOjLanguageToMonaco } from '@/utils'
import { computed, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const listPath = '/oj/submission'

const state = reactive({
  loading: false,
  detail: {} as any,
})

const dataId = computed(() => {
  const id = route.query.id
  return typeof id === 'string' ? id : ''
})

const sourceMonacoLanguage = computed(() => mapOjLanguageToMonaco(state.detail.language))

async function fetchDetail(id: string) {
  if (!id) return
  state.loading = true
  try {
    const response = await ojSubmissionApi.detail({ id })
    state.detail = response.data ?? {}
  } finally {
    state.loading = false
  }
}

function formatJsonValue(value: unknown) {
  if (value === undefined || value === null || value === '') {
    return Array.isArray(value) ? '[]' : '{}'
  }
  if (typeof value === 'string') {
    try {
      return JSON.stringify(JSON.parse(value), null, 2)
    } catch {
      return value
    }
  }
  return JSON.stringify(value, null, 2)
}

function goBack() {
  router.push(listPath)
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
      title="提交详情"
      :bordered="false"
    >
      <template #header-extra>
        <NSpace>
          <NButton @click="goBack">
            返回
          </NButton>
        </NSpace>
      </template>
      <NSpin :show="state.loading">
        <div class="detail-page">
          <header class="detail-header">
            <h1 class="detail-title">
              {{ displayValue(state.detail.id) }}
            </h1>
          </header>

          <section class="meta-section">
            <h2 class="section-label">
              基础信息
            </h2>
            <div class="meta-grid">
              <div class="meta-item">
                <div class="meta-key">
                  状态
                </div>
                <div class="meta-value">
                  {{ displayValue(state.detail.status) }}
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  语言
                </div>
                <div class="meta-value">
                  {{ displayValue(state.detail.language) }}
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  得分
                </div>
                <div class="meta-value">
                  {{ displayValue(state.detail.score) }}
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  题目ID
                </div>
                <div class="meta-value">
                  {{ displayValue(state.detail.problem_id) }}
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  账户ID
                </div>
                <div class="meta-value">
                  {{ displayValue(state.detail.account_id) }}
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
                  耗时
                </div>
                <div class="meta-value">
                  {{ displayValue(state.detail.time_ms) }} ms
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  内存
                </div>
                <div class="meta-value">
                  {{ displayValue(state.detail.memory_bytes) }} 字节
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  提交时间
                </div>
                <div class="meta-value">
                  {{ formatDateTime(state.detail.created_at) }}
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  判题时间
                </div>
                <div class="meta-value">
                  {{ formatDateTime(state.detail.judged_at) }}
                </div>
              </div>
              <div class="meta-item">
                <div class="meta-key">
                  执行机
                </div>
                <div class="meta-value">
                  {{ displayValue(state.detail.judge_node_id) }}
                </div>
              </div>
            </div>
          </section>

          <section class="content-section">
            <h2 class="section-label">
              判题说明
            </h2>
            <div class="detail-content whitespace-pre-wrap">
              {{ displayValue(state.detail.judge_message) }}
            </div>
          </section>

          <section class="content-section">
            <h2 class="section-label">
              编译输出
            </h2>
            <div class="detail-content whitespace-pre-wrap code-block">
              {{ displayValue(state.detail.compile_output) }}
            </div>
          </section>

          <section class="content-section">
            <h2 class="section-label">
              测点结果
            </h2>
            <NCode
              :code="formatJsonValue(state.detail.case_results ?? [])"
              language="json"
              word-wrap
            />
          </section>

          <section class="content-section">
            <h2 class="section-label">
              源代码
            </h2>
            <MonacoPreview
              :value="state.detail.source_code ?? ''"
              :language="sourceMonacoLanguage"
              :height="360"
            />
          </section>

          <section
            v-if="state.detail.last_dispatch_error"
            class="content-section"
          >
            <h2 class="section-label">
              最近调度错误
            </h2>
            <div class="detail-content whitespace-pre-wrap">
              {{ displayValue(state.detail.last_dispatch_error) }}
            </div>
          </section>
        </div>
      </NSpin>
    </NCard>
  </div>
</template>

<style scoped>
.detail-page {
  max-width: 880px;
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
  word-break: break-all;
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

.detail-content {
  min-height: 40px;
  color: var(--text-color-1, #333);
  font-size: 15px;
  line-height: 1.75;
}

.code-block {
  padding: 12px 14px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 13px;
  background: var(--n-color-embedded, rgba(0, 0, 0, 0.04));
  border-radius: 6px;
}

.whitespace-pre-wrap {
  white-space: pre-wrap;
  word-break: break-word;
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
