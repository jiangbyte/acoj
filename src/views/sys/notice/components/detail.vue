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
            <div class="text-sm text-[var(--header-text,#000000d9)] font-medium">{{ data.title || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">通知类别</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ categoryMap[data.category] || data.category || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">通知类型</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.type || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">通知级别</div>
            <div class="text-sm">
              <a-tag :color="levelColorMap[data.level] || 'default'">
                {{ levelMap[data.level] || data.level || '-' }}
              </a-tag>
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">状态</div>
            <div class="text-sm">
              <a-tag :color="data.status === 'ENABLED' ? 'green' : 'red'">
                {{ data.status === 'ENABLED' ? '启用' : '禁用' }}
              </a-tag>
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">是否置顶</div>
            <div class="text-sm">
              <a-tag :color="data.is_top === 'YES' ? 'orange' : 'default'">
                {{ data.is_top === 'YES' ? '置顶' : '否' }}
              </a-tag>
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">浏览次数</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.view_count ?? 0 }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">排序</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.sort_code ?? '-' }}</div>
          </a-col>
          <a-col v-if="data.position" :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">通知位置</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.position }}</div>
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
            <div class="text-sm text-[var(--header-text,#000000d9)]" v-html="data.content || '-'" />
          </a-col>
        </a-row>
      </a-card>

      <a-card size="small" title="系统信息">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">创建人</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.created_by || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">创建时间</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.created_at || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">更新人</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.updated_by || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">更新时间</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.updated_at || '-' }}</div>
          </a-col>
        </a-row>
      </a-card>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open'])

const loading = ref(false)
const data = ref<any>(null)

const categoryMap: Record<string, string> = {
  NOTICE: '通知',
  NEWS: '新闻',
  MESSAGE: '消息',
}

const levelMap: Record<string, string> = {
  URGENT: '紧急',
  IMPORTANT: '重要',
  NORMAL: '普通',
}

const levelColorMap: Record<string, string> = {
  URGENT: 'red',
  IMPORTANT: 'orange',
  NORMAL: 'default',
}

const isMobile = ref(false)
onMounted(() => {
  const mql = window.matchMedia('(max-width: 767px)')
  isMobile.value = mql.matches
  const handler = (e: MediaQueryListEvent) => { isMobile.value = e.matches }
  mql.addEventListener('change', handler)
  onBeforeUnmount(() => mql.removeEventListener('change', handler))
})

const drawerWidth = computed(() => (isMobile.value ? '100%' : 640))

function doOpen(row: any) {
  loading.value = true
  data.value = null
  try {
    data.value = row
    emit('update:open', true)
  } finally {
    loading.value = false
  }
}

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
