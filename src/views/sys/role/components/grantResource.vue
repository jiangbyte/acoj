<template>
  <a-drawer
    :open="open"
    title="授权资源"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <a-spin :spinning="loading">
      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="false"
        size="small"
        bordered
        :scroll="{ x: 'max-content' }"
        :row-key="(r: any) => r.id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <a-checkbox
              v-if="record.type === 'MENU' || record.type === 'DIRECTORY'"
              :checked="record.checked"
              :indeterminate="isIndeterminate(record)"
              @change="(e: any) => handleMenuCheck(record, e.target.checked)"
            >
              {{ record.name }}
            </a-checkbox>
            <span v-else>{{ record.name }}</span>
          </template>
          <template v-else-if="column.key === 'button'">
            <template v-if="record.buttonList?.length">
              <a-checkbox
                v-for="btn in record.buttonList"
                :key="btn.id"
                :checked="btn.checked"
                class="mr-2"
                @change="(e: any) => btn.checked = e.target.checked"
              >
                {{ btn.name }}
              </a-checkbox>
            </template>
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
defineOptions({ name: 'RoleGrantResource' })
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { message } from 'ant-design-vue'
import { fetchRoleGrantResource, fetchRoleOwnResource } from '@/api/role'
import { fetchResourceTree } from '@/api/resource'

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
const loading = ref(false)
const submitLoading = ref(false)
const treeData = ref<any[]>([])
const tableData = ref<any[]>([])

const columns = [
  { key: 'name', title: '菜单/目录', dataIndex: 'name', width: 300 },
  { key: 'button', title: '按钮权限', dataIndex: 'button', width: 400 },
]

function flattenTree(nodes: any[], ownIds: string[]): any[] {
  const result: any[] = []
  const traverse = (items: any[]): any[] => {
    const children: any[] = []
    items?.forEach((node: any) => {
      const buttons = node.children?.filter((c: any) => c.type === 'BUTTON') || []
      const menuChildren = node.children?.filter((c: any) => c.type !== 'BUTTON') || []
      const row: any = {
        id: node.id,
        name: node.name,
        type: node.type,
        checked: ownIds.includes(node.id),
        buttonList: buttons.map((b: any) => ({ id: b.id, name: b.name, checked: ownIds.includes(b.id) })),
        children: menuChildren.length > 0 ? traverse(menuChildren) : [],
      }
      result.push(row)
      children.push(row)
    })
    return children
  }
  traverse(nodes)
  return result
}

function isIndeterminate(record: any): boolean {
  if (!record.children?.length) return false
  const all = getAllDescendants(record)
  const checked = all.filter((n: any) => n.checked)
  return checked.length > 0 && checked.length < all.length
}

function getAllDescendants(node: any): any[] {
  const result: any[] = []
  const walk = (n: any) => {
    n.children?.forEach((child: any) => {
      result.push(child)
      walk(child)
    })
  }
  walk(node)
  return result
}

function handleMenuCheck(record: any, checked: boolean) {
  record.checked = checked
  const descendants = getAllDescendants(record)
  descendants.forEach((n: any) => { n.checked = checked })
  if (record.buttonList) {
    record.buttonList.forEach((b: any) => { b.checked = checked })
  }
}

async function loadData() {
  loading.value = true
  try {
    const [treeRes, ownRes] = await Promise.all([
      fetchResourceTree(),
      fetchRoleOwnResource({ role_id: currentRoleId.value }),
    ])
    const ownIds: string[] = ownRes?.data || []
    tableData.value = flattenTree(treeRes?.data || [], ownIds)
  } finally {
    loading.value = false
  }
}

function doOpen(role: any) {
  currentRoleId.value = role.id
  loadData()
  emit('update:open', true)
}

async function handleSubmit() {
  const allIds: string[] = []
  const collect = (items: any[]) => {
    items?.forEach((item: any) => {
      if (item.checked) allIds.push(item.id)
      if (item.buttonList) {
        item.buttonList.forEach((b: any) => {
          if (b.checked) allIds.push(b.id)
        })
      }
      collect(item.children)
    })
  }
  collect(tableData.value)

  submitLoading.value = true
  try {
    const { success } = await fetchRoleGrantResource({
      role_id: currentRoleId.value,
      resource_ids: allIds,
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

function handleClose() {
  tableData.value = []
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
