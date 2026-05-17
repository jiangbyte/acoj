<template>
  <a-drawer
    :open="open"
    title="日志详情"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <template v-if="data">
      <a-card size="small" title="基本信息" class="mb-3">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">日志名称</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.name || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">IP地址</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.op_ip || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">地址</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.op_address || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">浏览器</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.op_browser || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">操作系统</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.op_os || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">操作时间</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.op_time || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">操作人</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.op_user || '-' }}</div>
          </a-col>
        </a-row>
      </a-card>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'VisLogDetail' })
import { ref, watch } from 'vue'
import { fetchLogDetail } from '@/api/log'
import { useMobile } from '@/hooks/useMobile'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open'])

const loading = ref(false)
const data = ref<any>(null)

const { drawerWidth } = useMobile()

async function doOpen(row: any) {
  if (!row?.id) return
  loading.value = true
  data.value = null
  const { data: detail } = await fetchLogDetail({ id: row.id })
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
