<template>
  <a-drawer
    :open="open"
    title="授权资源"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <a-alert message="数据范围更改后需重新登录方可生效。" type="warning" closable class="mb-2" />

    <a-spin :spinning="loading">
      <a-table
        size="middle"
        :columns="columns"
        :data-source="tableData"
        :pagination="false"
        bordered
        :scroll="{ x: 'max-content' }"
        :row-key="(r: any) => r._key"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'parentName'">
            <a-checkbox
              :checked="record.parentCheck"
              @change="(e: any) => changeParent(record, e.target.checked)"
            >
              {{ record.parentName }}
            </a-checkbox>
          </template>

          <template v-if="column.dataIndex === 'menuName'">
            <a-checkbox
              :checked="record.nameCheck"
              @change="(e: any) => changeSub(record, e.target.checked)"
            >
              {{ record.menuName }}
            </a-checkbox>
          </template>

          <template v-if="column.dataIndex === 'btnName'">
            <template v-if="record.btnId">
              <a-checkbox
                :checked="record.btnCheck"
                @change="(e: any) => (record.btnCheck = e.target.checked)"
              >
                {{ record.btnName }}
              </a-checkbox>
            </template>
            <span v-else class="text-gray-400 text-xs">-</span>
          </template>

          <template v-if="column.dataIndex === 'scope'">
            <template v-if="record.btnId && record.btnCheck && record.permission_code">
              <a-select
                v-model:value="record.scope"
                size="small"
                :style="{ width: '130px' }"
                @change="
                  () => {
                    record.customOrgIds = []
                    record.customGroupIds = []
                  }
                "
              >
                <a-select-option value="ALL">全部</a-select-option>
                <a-select-option value="SELF">仅自己</a-select-option>
                <a-select-option value="ORG">本组织</a-select-option>
                <a-select-option value="ORG_AND_BELOW">组织及以下</a-select-option>
                <a-select-option value="GROUP">本用户组</a-select-option>
                <a-select-option value="GROUP_AND_BELOW">用户组及以下</a-select-option>
                <a-select-option value="CUSTOM_ORG">自定义组织</a-select-option>
                <a-select-option value="CUSTOM_GROUP">自定义用户组</a-select-option>
              </a-select>
              <template v-if="record.scope === 'CUSTOM_ORG'">
                <div class="mt-1 flex flex-wrap items-center gap-1">
                  <a-tag
                    v-for="id in record.customOrgIds"
                    :key="id"
                    closable
                    @close="removeOrgId(record, id)"
                  >
                    {{ orgMap[id] || id }}
                  </a-tag>
                  <a-button size="small" type="link" @click="openOrgPicker(record)">选择</a-button>
                </div>
              </template>
              <template v-else-if="record.scope === 'CUSTOM_GROUP'">
                <div class="mt-1 flex flex-wrap items-center gap-1">
                  <a-tag
                    v-for="id in record.customGroupIds"
                    :key="id"
                    closable
                    @close="removeGroupId(record, id)"
                  >
                    {{ groupMap[id] || id }}
                  </a-tag>
                  <a-button size="small" type="link" @click="openGroupPicker(record)">
                    选择
                  </a-button>
                </div>
              </template>
            </template>
            <span v-else class="text-gray-400 text-xs">-</span>
          </template>
        </template>
      </a-table>
    </a-spin>

    <!-- Org tree picker modal -->
    <a-modal v-model:open="orgPickerVisible" title="选择组织" @ok="confirmOrgPicker" width="480">
      <a-tree
        v-model:checked-keys="orgPickerChecked"
        :tree-data="orgTreeData"
        checkable
        :field-names="{ children: 'children', title: 'name', key: 'id' }"
        default-expand-all
      />
    </a-modal>

    <!-- Group tree picker modal -->
    <a-modal
      v-model:open="groupPickerVisible"
      title="选择用户组"
      @ok="confirmGroupPicker"
      width="480"
    >
      <a-tree
        v-model:checked-keys="groupPickerChecked"
        :tree-data="groupTreeData"
        checkable
        :field-names="{ children: 'children', title: 'name', key: 'id' }"
        default-expand-all
      />
    </a-modal>

    <template #footer>
      <a-space>
        <a-button @click="handleClose">关闭</a-button>
        <a-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'RoleGrantResource' })
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { message } from 'ant-design-vue'
import {
  fetchRoleGrantResource,
  fetchRoleOwnResource,
  fetchRoleOwnPermissionDetail,
} from '@/api/role'
import { fetchResourceTree } from '@/api/resource'
import { fetchOrgTree } from '@/api/org'
import { fetchGroupTree } from '@/api/group'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const isMobile = ref(false)
onMounted(() => {
  const mql = window.matchMedia('(max-width: 767px)')
  isMobile.value = mql.matches
  const handler = (e: MediaQueryListEvent) => {
    isMobile.value = e.matches
  }
  mql.addEventListener('change', handler)
  onBeforeUnmount(() => mql.removeEventListener('change', handler))
})
const drawerWidth = computed(() => (isMobile.value ? '100%' : 1100))

const currentRoleId = ref('')
const loading = ref(false)
const submitLoading = ref(false)

// ── Columns: 一级目录 | 菜单 | 按钮授权 | 数据范围 ──
const parentRowSpanMap = ref<Record<string, { start: number; count: number }>>({})
const menuRowSpanMap = ref<Record<string, { start: number; count: number }>>({})
const tableData = ref<any[]>([])

const columns = [
  {
    key: 'parentName',
    title: '一级目录',
    dataIndex: 'parentName',
    width: 180,
    fixed: 'left' as const,
    customCell: (row: any, index: number) => {
      const info = parentRowSpanMap.value[row.parentName]
      if (info && index === info.start) {
        return { rowSpan: info.count }
      }
      return { rowSpan: 0 }
    },
  },
  {
    key: 'menuName',
    title: '菜单',
    dataIndex: 'menuName',
    width: 180,
    customCell: (row: any, index: number) => {
      const info = menuRowSpanMap.value[row.menuId]
      if (info && index === info.start) {
        return { rowSpan: info.count }
      }
      return { rowSpan: 0 }
    },
  },
  { key: 'btnName', title: '按钮授权', dataIndex: 'btnName', width: 200 },
  { key: 'scope', title: '数据范围', dataIndex: 'scope' },
]

// ── Org / Group trees for pickers ──
const orgTreeData = ref<any[]>([])
const groupTreeData = ref<any[]>([])
const orgMap = ref<Record<string, string>>({})
const groupMap = ref<Record<string, string>>({})

const orgPickerVisible = ref(false)
const orgPickerRecord = ref<any>(null)
const orgPickerChecked = ref<string[]>([])

const groupPickerVisible = ref(false)
const groupPickerRecord = ref<any>(null)
const groupPickerChecked = ref<string[]>([])

// ── Build button rows from resource tree ──

function expandToRows(
  nodes: any[],
  ownIds: string[],
  scopeMap: Record<string, any>
): {
  rows: any[]
  parentMap: Record<string, { start: number; count: number }>
  menuMap: Record<string, { start: number; count: number }>
} {
  const rows: any[] = []
  const parentMap: Record<string, { start: number; count: number }> = {}
  const menuMap: Record<string, { start: number; count: number }> = {}

  const traverse = (items: any[]) => {
    items?.forEach((node: any) => {
      if (node.type === 'DIRECTORY') {
        const menus = (node.children || []).filter((c: any) => c.type === 'MENU')
        const dirStart = rows.length

        menus.forEach((menu: any) => {
          const buttons = (menu.children || []).filter((c: any) => c.type === 'BUTTON')
          const menuStart = rows.length

          if (buttons.length > 0) {
            buttons.forEach((b: any) => {
              let permission_code: string | undefined
              try {
                const extra = JSON.parse(b.extra || '{}')
                permission_code = extra.permission_code || undefined
              } catch {
                permission_code = undefined
              }
              const sd = permission_code ? scopeMap[permission_code] : undefined

              rows.push({
                _key: `${menu.id}_${b.id}`,
                parentName: node.name,
                parentId: node.id,
                parentCheck: ownIds.includes(node.id),
                menuId: menu.id,
                menuName: menu.name,
                nameCheck: ownIds.includes(menu.id),
                btnId: b.id,
                btnName: b.name,
                btnCheck: ownIds.includes(b.id),
                permission_code,
                scope: sd?.scope || 'ALL',
                customOrgIds: sd?.customOrgIds || [],
                customGroupIds: sd?.customGroupIds || [],
              })
            })
          } else {
            rows.push({
              _key: `${menu.id}_nobtn`,
              parentName: node.name,
              parentId: node.id,
              parentCheck: ownIds.includes(node.id),
              menuId: menu.id,
              menuName: menu.name,
              nameCheck: ownIds.includes(menu.id),
              btnId: null,
              btnName: null,
              btnCheck: false,
              permission_code: null,
              scope: 'ALL',
              customOrgIds: [],
              customGroupIds: [],
            })
          }

          menuMap[menu.id] = { start: menuStart, count: rows.length - menuStart }
        })

        parentMap[node.name] = { start: dirStart, count: rows.length - dirStart }
        traverse(node.children)
      }
    })
  }
  traverse(nodes)
  return { rows, parentMap, menuMap }
}

// ── Cascading check logic ──

function changeParent(record: any, val: boolean) {
  const info = parentRowSpanMap.value[record.parentName]
  if (!info) return
  for (let i = info.start; i < info.start + info.count; i++) {
    const row = tableData.value[i]
    if (row) {
      row.parentCheck = val
      row.nameCheck = val
      row.btnCheck = val
    }
  }
}

function changeSub(record: any, val: boolean) {
  const info = menuRowSpanMap.value[record.menuId]
  if (!info) return
  for (let i = info.start; i < info.start + info.count; i++) {
    const row = tableData.value[i]
    if (row) {
      row.nameCheck = val
      row.btnCheck = val
    }
  }
}

// ── Org picker ──
function openOrgPicker(record: any) {
  orgPickerRecord.value = record
  orgPickerChecked.value = [...(record.customOrgIds || [])]
  orgPickerVisible.value = true
}
function confirmOrgPicker() {
  if (orgPickerRecord.value) {
    orgPickerRecord.value.customOrgIds = [...orgPickerChecked.value]
  }
  orgPickerVisible.value = false
}
function removeOrgId(record: any, id: string) {
  record.customOrgIds = record.customOrgIds.filter((i: string) => i !== id)
}

// ── Group picker ──
function openGroupPicker(record: any) {
  groupPickerRecord.value = record
  groupPickerChecked.value = [...(record.customGroupIds || [])]
  groupPickerVisible.value = true
}
function confirmGroupPicker() {
  if (groupPickerRecord.value) {
    groupPickerRecord.value.customGroupIds = [...groupPickerChecked.value]
  }
  groupPickerVisible.value = false
}
function removeGroupId(record: any, id: string) {
  record.customGroupIds = record.customGroupIds.filter((i: string) => i !== id)
}

// ── Data loading ──

async function loadTrees() {
  const [orgRes, groupRes] = await Promise.all([fetchOrgTree({}), fetchGroupTree({})])
  orgTreeData.value = orgRes?.data || []
  groupTreeData.value = groupRes?.data || []
  const buildMap = (nodes: any[], map: Record<string, string>) => {
    nodes?.forEach((n: any) => {
      map[n.id] = n.name
      if (n.children) buildMap(n.children, map)
    })
  }
  buildMap(orgTreeData.value, orgMap.value)
  buildMap(groupTreeData.value, groupMap.value)
}

async function loadData() {
  loading.value = true
  try {
    const [treeRes, ownRes, detailRes] = await Promise.all([
      fetchResourceTree(),
      fetchRoleOwnResource({ role_id: currentRoleId.value }),
      fetchRoleOwnPermissionDetail({ role_id: currentRoleId.value }),
    ])
    const ownIds: string[] = ownRes?.data || []

    // Build permission_code → scope mapping (互通 with grant-permission)
    const scopeMap: Record<string, any> = {}
    ;(detailRes?.data || []).forEach((item: any) => {
      if (item.permission_code) {
        scopeMap[item.permission_code] = {
          scope: item.scope || 'ALL',
          customOrgIds: item.custom_scope_org_ids ? JSON.parse(item.custom_scope_org_ids) : [],
          customGroupIds: item.custom_scope_group_ids
            ? JSON.parse(item.custom_scope_group_ids)
            : [],
        }
      }
    })

    const { rows, parentMap, menuMap } = expandToRows(treeRes?.data || [], ownIds, scopeMap)
    tableData.value = rows
    parentRowSpanMap.value = parentMap
    menuRowSpanMap.value = menuMap
  } finally {
    loading.value = false
  }
}

// ── Submit ──

async function handleSubmit() {
  const allIds: string[] = []
  const permissionItems: any[] = []
  const seen = new Set<string>()

  tableData.value.forEach((row: any) => {
    if (row.btnCheck && row.btnId) {
      if (!seen.has(row.btnId)) {
        allIds.push(row.btnId)
        seen.add(row.btnId)
      }
      if (row.permission_code) {
        permissionItems.push({
          permission_code: row.permission_code,
          scope: row.scope,
          custom_scope_group_ids: row.customGroupIds?.length
            ? JSON.stringify(row.customGroupIds)
            : null,
          custom_scope_org_ids: row.customOrgIds?.length ? JSON.stringify(row.customOrgIds) : null,
        })
      }
    }
    if (row.nameCheck && !seen.has(row.menuId)) {
      allIds.push(row.menuId)
      seen.add(row.menuId)
    }
    if (row.parentCheck && !seen.has(row.parentId)) {
      allIds.push(row.parentId)
      seen.add(row.parentId)
    }
  })

  submitLoading.value = true
  try {
    const { success } = await fetchRoleGrantResource({
      role_id: currentRoleId.value,
      resource_ids: allIds,
      permissions: permissionItems,
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
  loadTrees()
  loadData()
  emit('update:open', true)
}

function handleClose() {
  tableData.value = []
  parentRowSpanMap.value = {}
  menuRowSpanMap.value = {}
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>

<style scoped>
.ant-checkbox-wrapper {
  margin-left: 0 !important;
  padding-top: 2px !important;
  padding-bottom: 2px !important;
}
</style>
