<template>
  <a-drawer
    :open="open"
    title="授权权限"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <a-alert message="选择权限并为每个权限设置数据范围。数据范围更改后需重新登录方可生效。" type="warning" closable class="mb-3" />

    <a-spin :spinning="loading">
      <!-- Module selector -->
      <a-select
        v-model:value="currentModule"
        placeholder="选择模块"
        allow-clear
        style="width: 240px"
        class="mb-3"
        @change="loadPermissions"
      >
        <a-select-option v-for="m in modules" :key="m" :value="m">{{ m }}</a-select-option>
      </a-select>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="false"
        size="small"
        bordered
        :scroll="{ x: 'max-content' }"
      >
        <template #headerCell="{ column }">
          <template v-if="column.key === 'code'">
            <a-checkbox :checked="allChecked" :indeterminate="indeterminate" @change="handleAllCheck">
              权限编码
            </a-checkbox>
          </template>
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'code'">
            <a-checkbox :checked="record.checked" @change="(e: any) => handleCheck(record, e.target.checked)">
              {{ record.code }}
            </a-checkbox>
          </template>
          <template v-else-if="column.key === 'name'">
            {{ record.name }}
          </template>
          <template v-else-if="column.key === 'scope'">
            <a-radio-group
              v-if="record.checked"
              :value="record.scope"
              size="small"
              @change="(e: any) => record.scope = e.target.value"
            >
              <a-radio-button value="ALL">全部</a-radio-button>
              <a-radio-button value="SELF">仅自己</a-radio-button>
              <a-radio-button value="ORG">所属组织</a-radio-button>
              <a-radio-button value="ORG_AND_BELOW">组织及以下</a-radio-button>
            </a-radio-group>
            <span v-else class="text-gray-400 text-xs">未选中</span>
          </template>
        </template>
      </a-table>
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
defineOptions({ name: 'RoleGrantPermission' })
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { message } from 'ant-design-vue'
import { fetchRoleGrantPermission, fetchRoleOwnPermission } from '@/api/role'
import { fetchPermissionModules, fetchPermissionByModule } from '@/api/permission'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isMobile = ref(false)
onMounted(() => {
  const mql = window.matchMedia('(max-width: 767px)')
  isMobile.value = mql.matches
  const handler = (e: MediaQueryListEvent) => { isMobile.value = e.matches }
  mql.addEventListener('change', handler)
  onBeforeUnmount(() => mql.removeEventListener('change', handler))
})
const drawerWidth = computed(() => (isMobile.value ? '100%' : 800))

const currentRoleId = ref('')
const modules = ref<string[]>([])
const currentModule = ref<string | undefined>(undefined)
const allPermissions = ref<any[]>([])
const tableData = ref<any[]>([])
const loading = ref(false)
const submitLoading = ref(false)

const allChecked = computed(() => tableData.value.length > 0 && tableData.value.every(p => p.checked))
const indeterminate = computed(() => {
  const checked = tableData.value.filter(p => p.checked)
  return checked.length > 0 && checked.length < tableData.value.length
})

const columns = [
  { key: 'code', title: '权限编码', dataIndex: 'code', width: 280 },
  { key: 'name', title: '权限名称', dataIndex: 'name', width: 180 },
  { key: 'scope', title: '数据范围', dataIndex: 'scope', width: 400 },
]

async function loadModules() {
  const { data } = await fetchPermissionModules()
  modules.value = data || []
}

async function loadPermissions() {
  if (!currentModule.value) {
    tableData.value = []
    return
  }
  loading.value = true
  try {
    const [permsRes, ownRes] = await Promise.all([
      fetchPermissionByModule({ module: currentModule.value }),
      fetchRoleOwnPermission({ role_id: currentRoleId.value }),
    ])
    const ownIds: string[] = ownRes?.data || []

    tableData.value = (permsRes?.data || []).map((p: any) => ({
      id: p.id,
      code: p.code,
      name: p.name,
      checked: ownIds.includes(p.id),
      scope: 'ALL',
    }))
  } finally {
    loading.value = false
  }
}

function handleAllCheck(e: any) {
  const checked = e.target.checked
  tableData.value.forEach(p => { p.checked = checked })
}

function handleCheck(record: any, checked: boolean) {
  record.checked = checked
}

async function handleSubmit() {
  const checkedItems = tableData.value.filter(p => p.checked)
  submitLoading.value = true
  try {
    const { success } = await fetchRoleGrantPermission({
      role_id: currentRoleId.value,
      permission_ids: checkedItems.map(p => p.id),
    })
    if (success) {
      message.success('授权成功')
      emit('success')
      handleClose()
    }
  } finally {
    submitLoading.value = false
  }
}

function doOpen(role: any) {
  currentRoleId.value = role.id
  loadModules()
  emit('update:open', true)
}

function handleClose() {
  currentModule.value = undefined
  tableData.value = []
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
