<template>
  <a-drawer
    :open="open"
    title="轮播图详情"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <template v-if="data">
      <a-card size="small" title="基本信息" class="mb-3">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">标题</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.title || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">类别</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ $dict.label('BANNER_CATEGORY', data.category) || '-' }}
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">类型</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ $dict.label('BANNER_TYPE', data.type) || '-' }}
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">展示位置</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ $dict.label('BANNER_POSITION', data.position) || '-' }}
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">排序</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ data.sort_code ?? '-' }}
            </div>
          </a-col>
          <a-col v-if="data.image" :xs="24" :sm="24">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">图片</div>
            <img :src="data.image" class="max-w-full max-h-40 rounded mt-1" />
          </a-col>
        </a-row>
      </a-card>

      <a-card size="small" title="链接信息" class="mb-3">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">跳转地址</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.url || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">链接类型</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ $dict.label('LINK_TYPE', data.link_type) || '-' }}
            </div>
          </a-col>
          <a-col v-if="data.summary" :xs="24">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">摘要</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.summary }}</div>
          </a-col>
          <a-col v-if="data.description" :xs="24">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">描述</div>
            <div class="text-sm text-[var(--header-text,#000000d9)] whitespace-pre-wrap">
              {{ data.description }}
            </div>
          </a-col>
        </a-row>
      </a-card>

      <a-card size="small" title="统计信息" class="mb-3">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">浏览次数</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ data.view_count ?? 0 }}
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">点击次数</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              {{ data.click_count ?? 0 }}
            </div>
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
import { ref, watch } from 'vue'
import { useMobile } from '@/hooks/useMobile'
import { fetchBannerDetail } from '@/api/banner'
const props = defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open'])

const loading = ref(false)
const data = ref<any>(null)

const { drawerWidth } = useMobile()

async function doOpen(row: any) {
  if (!row?.id) return
  loading.value = true
  data.value = null
  const { data: detail } = await fetchBannerDetail({ id: row.id })
  data.value = detail
  loading.value = false
  emit('update:open', true)
}

watch(
  () => props.open,
  v => {
    if (!v) data.value = null
  }
)

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
