<template>
  <a-drawer
    :open="open"
    title="通知详情"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <template v-if="data">
      <a-card size="small" title="基本信息" class="mb-3">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="24">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">通知标题</div>
            <div class="text-sm text-[var(--header-text,#000000d9)] font-medium">
              {{ data.title || '-' }}
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">通知类别</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ $dict.label('NOTICE_CATEGORY', data.category) }}
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">通知类型</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ $dict.label('NOTICE_TYPE', data.type) || '-' }}
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">通知级别</div>
            <div class="text-sm">
              <a-tag :color="$dict.color('NOTICE_LEVEL', data.level)">
                {{ $dict.label('NOTICE_LEVEL', data.level) }}
              </a-tag>
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">状态</div>
            <div class="text-sm">
              <a-tooltip title="禁用后仅不可被选择，不影响已绑定的数据">
                <a-tag :color="$dict.color('SYS_STATUS', data.status)">
                  {{ $dict.label('SYS_STATUS', data.status) }}
                </a-tag>
              </a-tooltip>
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">是否置顶</div>
            <div class="text-sm">
              <a-tag :color="$dict.color('SYS_YES_NO', data.is_top)">
                {{ $dict.label('SYS_YES_NO', data.is_top) }}
              </a-tag>
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">浏览次数</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ data.view_count ?? 0 }}
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">排序</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ data.sort_code ?? '-' }}
            </div>
          </a-col>
          <a-col v-if="data.position" :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">通知位置</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ $dict.label('NOTICE_POSITION', data.position) || '-' }}
            </div>
          </a-col>
          <a-col v-if="data.cover" :xs="24" :sm="24">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">封面图片</div>
            <img :src="data.cover" class="max-w-full max-h-40 object-contain mt-1 rounded" />
          </a-col>
          <a-col :xs="24" :sm="24">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">通知摘要</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.summary || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="24">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">通知内容</div>
            <!-- eslint-disable-next-line vue/no-v-html -->
            <div class="text-sm text-[var(--header-text,#000000d9)]" v-html="data.content || '-'" />
          </a-col>
        </a-row>
      </a-card>

      <a-card size="small" title="系统信息">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">创建时间</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ data.created_at || '-' }}
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">更新时间</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ data.updated_at || '-' }}
            </div>
          </a-col>
        </a-row>
      </a-card>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useMobile } from '@/hooks/useMobile'
import { fetchNoticeDetail } from '@/api/notice'
defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open'])

const loading = ref(false)
const data = ref<any>(null)

const { drawerWidth } = useMobile()

async function doOpen(row: any) {
  if (!row?.id) return
  loading.value = true
  data.value = null
  const { data: detail } = await fetchNoticeDetail({ id: row.id })
  data.value = detail
  loading.value = false
  emit('update:open', true)
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
