<template>
  <a-drawer :open="open" title="配置详情" placement="right" :width="480" @close="handleClose">
    <a-spin :spinning="loading">
      <template v-if="data">
        <a-card title="基本信息" size="small" class="mb-3">
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="配置键">{{ data.config_key || '-' }}</a-descriptions-item>
            <a-descriptions-item label="配置值">
              <div class="whitespace-pre-wrap break-all">{{ data.config_value || '-' }}</div>
            </a-descriptions-item>
            <a-descriptions-item label="分类">{{ data.category || '-' }}</a-descriptions-item>
            <a-descriptions-item label="备注">{{ data.remark || '-' }}</a-descriptions-item>
            <a-descriptions-item label="排序">{{ data.sort_code ?? '-' }}</a-descriptions-item>
          </a-descriptions>
        </a-card>
        <a-card title="系统信息" size="small">
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="创建时间">{{ data.created_at || '-' }}</a-descriptions-item>
            <a-descriptions-item label="更新时间">{{ data.updated_at || '-' }}</a-descriptions-item>
          </a-descriptions>
        </a-card>
      </template>
    </a-spin>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { fetchConfigDetail } from '@/api/config'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open'])

const loading = ref(false)
const data = ref<any>(null)

async function doOpen(row: any) {
  if (!row?.id) return
  loading.value = true
  try {
    const res = await fetchConfigDetail({ id: row.id })
    data.value = res?.data || null
  } finally {
    loading.value = false
  }
  emit('update:open', true)
}

function handleClose() { emit('update:open', false) }

defineExpose({ doOpen })
</script>
