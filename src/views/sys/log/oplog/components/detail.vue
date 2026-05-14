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
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">执行状态</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">
              <a-tag :color="data.exe_status === 'SUCCESS' ? 'green' : 'red'">{{ data.exe_status || '-' }}</a-tag>
            </div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">请求IP</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.op_ip || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">IP来源</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.op_address || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">请求方式</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.req_method || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">请求地址</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.req_url || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">操作类</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.class_name || '-' }}</div>
          </a-col>
          <a-col :xs="24" :sm="12">
            <div class="text-[13px] text-[var(--text-secondary,#00000073)] mb-1">操作方法</div>
            <div class="text-sm text-[var(--header-text,#000000d9)]">{{ data.method_name || '-' }}</div>
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

      <a-card size="small" title="请求参数" class="mb-3">
        <pre class="text-xs bg-gray-50 p-3 rounded max-h-60 overflow-auto">{{ paramJson }}</pre>
      </a-card>

      <a-card size="small" :title="isException ? '异常信息' : '返回结果'">
        <pre class="text-xs bg-gray-50 p-3 rounded max-h-60 overflow-auto">{{ resultJson }}</pre>
      </a-card>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'OpLogDetail' })
import { ref, computed, watch } from 'vue'
import { fetchLogDetail } from '@/api/log'
import { useMobile } from '@/hooks/useMobile'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open'])

const loading = ref(false)
const data = ref<any>(null)
const paramJson = ref('')
const resultJson = ref('')

const { drawerWidth } = useMobile()

const isException = computed(() => data.value?.category === 'EXCEPTION')

async function doOpen(row: any) {
  if (!row?.id) return
  loading.value = true
  data.value = null
  const { data: detail } = await fetchLogDetail({ id: row.id })
  data.value = detail
  if (detail) {
    try {
      const parsed = JSON.parse(detail.param_json)
      paramJson.value = JSON.stringify(parsed, undefined, 2)
    } catch {
      paramJson.value = detail.param_json || '无'
    }
    if (detail.category === 'EXCEPTION') {
      resultJson.value = detail.exe_message || '无'
    } else {
      try {
        const parsed = JSON.parse(detail.result_json)
        resultJson.value = JSON.stringify(parsed, undefined, 2)
      } catch {
        resultJson.value = detail.result_json || '无'
      }
    }
  }
  loading.value = false
  emit('update:open', true)
}

watch(
  () => props.open,
  v => {
    if (!v) {
      data.value = null
      paramJson.value = ''
      resultJson.value = ''
    }
  }
)

function handleClose() {
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
