<template>
  <a-drawer :open="open" title="分配权限" width="480" @close="closeDrawer">
    <a-tree
      v-if="treeData.length"
      checkable
      :treeData="treeData"
      :checkedKeys="checkedKeys"
      @check="handleCheck"
    />
    <template #footer>
      <a-space>
        <a-button @click="closeDrawer">取消</a-button>
        <a-button type="primary" @click="handleSave" :loading="saving">保存</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { fetchRoleOwnPermission, fetchRoleGrantPermission } from '@/api/role'
import { fetchPermissionByModule } from '@/api/permission'

const props = defineProps<{ open: boolean; id: string }>()
const emit = defineEmits(['update:open', 'success'])

const treeData = ref<any[]>([])
const checkedKeys = ref<string[]>([])
const saving = ref(false)

watch(
  () => props.open,
  async v => {
    if (v && props.id) {
      const [modulesRes, ownRes] = await Promise.all([
        fetchPermissionByModule({}),
        fetchRoleOwnPermission({ role_id: props.id }),
      ])
      if (modulesRes.data) {
        treeData.value = modulesRes.data.map((m: any) => ({
          title: m.name,
          key: m.id,
          children: (m.permissions || []).map((p: any) => ({
            title: `${p.name} (${p.code})`,
            key: p.id,
          })),
        }))
      }
      if (ownRes.data) {
        checkedKeys.value = ownRes.data.map((p: any) => p.permission_id || p.id)
      }
    }
  }
)

function handleCheck(keys: any[]) {
  checkedKeys.value = keys
}

async function handleSave() {
  saving.value = true
  try {
    const { success } = await fetchRoleGrantPermission({
      role_id: props.id,
      permission_ids: checkedKeys.value,
    })
    if (success) {
      emit('success')
      closeDrawer()
    }
  } finally {
    saving.value = false
  }
}

function closeDrawer() {
  emit('update:open', false)
}
</script>
