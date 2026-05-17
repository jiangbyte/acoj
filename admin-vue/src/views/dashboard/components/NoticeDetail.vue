<template>
  <a-drawer
    :open="open"
    title="通知详情"
    :width="560"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <template v-if="data">
      <div class="mb-6">
        <div class="flex items-center gap-2 mb-2">
          <a-tag :color="$dict.color('NOTICE_LEVEL', data.level)">
            {{ $dict.label('NOTICE_LEVEL', data.level) }}
          </a-tag>
          <span class="text-xs text-$text-color-secondary">{{ data.created_at }}</span>
        </div>
        <h2 class="text-base font-medium text-$text-color m-0">{{ data.title }}</h2>
      </div>

      <div v-if="data.summary" class="mb-4 p-3 bg-$background-color-light rounded text-sm text-$text-color-secondary">
        {{ data.summary }}
      </div>

      <!-- eslint-disable-next-line vue/no-v-html -->
      <div class="text-sm text-$text-color leading-6" v-html="data.content || '-'" />

      <a-divider />

      <div class="flex items-center gap-6 text-sm text-$text-color-secondary">
        <div class="flex items-center gap-1.5">
          <EyeOutlined />
          <span>{{ data.view_count ?? 0 }} 次阅读</span>
        </div>
        <div v-if="data.is_top === '1'" class="flex items-center gap-1.5">
          <PushpinOutlined class="text-$color-warning" />
          <span>置顶通知</span>
        </div>
      </div>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { EyeOutlined, PushpinOutlined } from '@ant-design/icons-vue'
import { fetchNoticeDetail } from '@/api/notice'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open'])

const loading = ref(false)
const data = ref<any>(null)

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
