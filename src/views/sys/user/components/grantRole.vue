<template>
  <a-drawer
    :open="open"
    title="分配角色"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <a-spin :spinning="loading">
      <a-transfer
        v-model:target-keys="targetKeys"
        :data-source="dataSource"
        :titles="['可选角色', '已选角色']"
        :render="(item: any) => item.title"
        :row-key="(item: any) => item.key"
        :list-style="{ width: '100%', height: isMobile ? '300px' : '520px' }"
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
defineOptions({ name: 'UserGrantRole' })
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { useMobile } from '@/hooks/useMobile'
import { fetchUserGrantRole, fetchUserOwnRoles } from '@/api/user'
import { fetchRolePage } from '@/api/role'

defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const { isMobile, drawerWidth } = useMobile()

const currentUserId = ref('')
const loading = ref(false)
const submitLoading = ref(false)
const dataSource = ref<any[]>([])
const targetKeys = ref<string[]>([])

async function loadData() {
  loading.value = true
  const [rolesRes, ownRes] = await Promise.all([
    fetchRolePage({ size: 9999 }),
    fetchUserOwnRoles({ user_id: currentUserId.value }),
  ])
  dataSource.value = (rolesRes?.data?.records || []).map((r: any) => ({
    key: r.id,
    title: `${r.name} (${r.code})`,
  }))
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
  const { success } = await fetchUserGrantRole({
    user_id: currentUserId.value,
    role_ids: targetKeys.value,
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
