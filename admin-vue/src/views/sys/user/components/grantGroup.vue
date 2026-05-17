<template>
  <a-drawer
    :open="open"
    title="分配用户组"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <a-spin :spinning="loading">
      <a-transfer
        v-model:target-keys="targetKeys"
        :data-source="dataSource"
        :titles="['可选用户组', '已选用户组']"
        :render="(item: any) => item.title"
        :row-key="(item: any) => item.key"
        :list-style="{ width: '100%', height: 420 }"
        show-search
        :filter-option="(inputValue: string, item: any) => item.title.includes(inputValue)"
      />
    </a-spin>

    <template #footer>
      <a-space>
        <a-button @click="handleClose">取消</a-button>
        <a-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'UserGrantGroup' })
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { useMobile } from '@/hooks/useMobile'
import { fetchUserGrantGroup, fetchUserOwnGroups } from '@/api/user'
import { fetchGroupTree } from '@/api/group'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const { drawerWidth } = useMobile()

const currentUserId = ref('')
const loading = ref(false)
const submitLoading = ref(false)
const dataSource = ref<any[]>([])
const targetKeys = ref<string[]>([])

function flattenTree(nodes: any[]): any[] {
  const result: any[] = []
  const walk = (items: any[]) => {
    items?.forEach((n: any) => {
      result.push({ key: n.id, title: n.name })
      if (n.children) walk(n.children)
    })
  }
  walk(nodes)
  return result
}

async function loadData() {
  loading.value = true
  const [groupRes, ownRes] = await Promise.all([
    fetchGroupTree({}),
    fetchUserOwnGroups({ user_id: currentUserId.value }),
  ])
  dataSource.value = flattenTree(groupRes?.data || [])
  targetKeys.value = ownRes?.data || []
  loading.value = false
}

function doOpen(user: any) {
  currentUserId.value = user.id
  loadData()
  emit('update:open', true)
}

async function handleSubmit() {
  submitLoading.value = true
  const { success } = await fetchUserGrantGroup({
    user_id: currentUserId.value,
    group_ids: targetKeys.value,
  })
  if (success) {
    message.success('分配成功')
    emit('success')
    handleClose()
  }
  submitLoading.value = false
}

function handleClose() {
  dataSource.value = []
  targetKeys.value = []
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
