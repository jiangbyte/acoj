<template>
  <a-drawer :open="open" title="用户详情" width="640" @close="closeDrawer">
    <a-descriptions v-if="data" :column="2" bordered size="small">
      <a-descriptions-item label="账号" :span="2">{{ data.account }}</a-descriptions-item>
      <a-descriptions-item label="昵称">{{ data.nickname }}</a-descriptions-item>
      <a-descriptions-item label="状态">{{ data.status === 'ACTIVE' ? '启用' : '禁用' }}</a-descriptions-item>
      <a-descriptions-item label="邮箱" :span="2">{{ data.email }}</a-descriptions-item>
      <a-descriptions-item label="手机" :span="2">{{ data.phone }}</a-descriptions-item>
      <a-descriptions-item label="创建时间" :span="2">{{ data.created_at }}</a-descriptions-item>
    </a-descriptions>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { fetchUserDetail } from '@/api/user'

const props = defineProps<{ open: boolean; id: string }>()
const emit = defineEmits(['update:open'])
const data = ref<any>(null)

watch(() => props.open, async (v) => {
  if (v && props.id) {
    const { data: d } = await fetchUserDetail({ id: props.id })
    data.value = d
  } else {
    data.value = null
  }
})

function closeDrawer() { emit('update:open', false) }
</script>
