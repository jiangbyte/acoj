<template>
  <a-drawer
    :open="open"
    title="文件详情"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <template v-if="data">
      <a-card size="small" title="基本信息" class="mb-3">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="24">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">文件名称</div>
            <div class="text-sm text-[var(--header-text,#000000d9)] font-medium">{{ data.name || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">存储引擎</div>
            <div class="text-sm">
              <a-tag :color="engineColorMap[data.engine] || 'default'">
                {{ engineLabelMap[data.engine] || data.engine || '-' }}
              </a-tag>
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">存储桶</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.bucket || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">文件大小</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.size_info || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">文件后缀</div>
            <div class="text-sm">
              <a-tag>{{ data.suffix || '-' }}</a-tag>
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" v-if="isImage(data.suffix) && data.thumbnail">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">缩略图</div>
            <img :src="data.thumbnail" class="max-w-48 max-h-48 object-contain mt-1 rounded border" />
          </a-col>
        </a-row>
      </a-card>

      <a-card size="small" title="存储信息" class="mb-3">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="24">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">存储路径</div>
            <div class="text-sm text-[var(--header-text,#000000d9)] break-all">{{ data.storage_path || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="24">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">下载链接</div>
            <div class="text-sm text-[var(--header-text,#000000d9)] break-all">
              <a v-if="data.download_path" @click="handleDownload(data)">{{ data.download_path }}</a>
              <span v-else>-</span>
            </div>
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
        </a-row>
      </a-card>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { fetchFileDetail, fetchFileDownload } from '@/api/file'
import { downloadBlob } from '@/utils'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open'])

const loading = ref(false)
const data = ref<any>(null)

const engineLabelMap: Record<string, string> = {
  LOCAL: '本地',
  MINIO: 'MinIO',
  ALIYUN: '阿里云',
  TENCENT: '腾讯云',
  S3: 'S3',
}

const engineColorMap: Record<string, string> = {
  LOCAL: 'green',
  MINIO: 'blue',
  ALIYUN: 'orange',
  TENCENT: 'red',
  S3: 'purple',
}

function isImage(suffix: string | null | undefined): boolean {
  if (!suffix) return false
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg', 'ico'].includes(suffix.toLowerCase().replace('.', ''))
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

async function doOpen(row: any) {
  loading.value = true
  data.value = null
  emit('update:open', true)
  try {
    const res = await fetchFileDetail({ id: row.id })
    if (res.success) {
      data.value = res.data
    }
  } catch { /* ignore */ }
  loading.value = false
}

function handleClose() {
  emit('update:open', false)
}

async function handleDownload(row: any) {
  if (!row.download_path) return
  const blob = await fetchFileDownload(row.download_path)
  downloadBlob(blob, row.name || `file.${row.suffix || ''}`)
}

defineExpose({ doOpen })
</script>
