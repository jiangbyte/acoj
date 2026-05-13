<template>
  <a-drawer
    :open="open"
    title="授权权限"
    :width="drawerWidth"
    :loading="loading"
    destroy-on-close
    @close="handleClose"
  >
    <a-alert
      message="选择权限并为每个权限设置数据范围。数据范围更改后需重新登录方可生效。"
      type="warning"
      closable
      class="mb-3"
    />

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
            <a-checkbox
              :checked="allChecked"
              :indeterminate="indeterminate"
              @change="handleAllCheck"
            >
              权限编码
            </a-checkbox>
          </template>
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'code'">
            <a-checkbox
              :checked="record.checked"
              @change="(e: any) => handleCheck(record, e.target.checked)"
            >
              {{ record.code }}
            </a-checkbox>
          </template>
          <template v-else-if="column.key === 'name'">
            {{ record.name }}
          </template>
          <template v-else-if="column.key === 'scope'">
            <div v-if="record.checked" class="flex flex-col gap-1">
              <a-radio-group
                :value="record.scope"
                size="small"
                @change="
                  (e: any) => {
                    record.scope = e.target.value
                    record.customOrgIds = []
                    record.customGroupIds = []
                  }
                "
              >
                <a-radio-button value="ALL">全部</a-radio-button>
                <a-radio-button value="SELF">仅自己</a-radio-button>
                <a-radio-button value="ORG">本组织</a-radio-button>
                <a-radio-button value="ORG_AND_BELOW">组织及以下</a-radio-button>
                <a-radio-button value="GROUP">本用户组</a-radio-button>
                <a-radio-button value="GROUP_AND_BELOW">用户组及以下</a-radio-button>
                <a-radio-button value="CUSTOM_ORG">自定义组织</a-radio-button>
                <a-radio-button value="CUSTOM_GROUP">自定义用户组</a-radio-button>
              </a-radio-group>
              <!-- Custom Org selector -->
              <div v-if="record.scope === 'CUSTOM_ORG'" class="mt-1">
                <a-tag
                  v-for="id in record.customOrgIds"
                  :key="id"
                  closable
                  @close="removeOrgId(record, id)"
                >
                  {{ orgMap[id] || id }}
                </a-tag>
                <a-button size="small" @click="openOrgPicker(record)">选择组织</a-button>
              </div>
              <!-- Custom Group selector -->
              <div v-if="record.scope === 'CUSTOM_GROUP'" class="mt-1">
                <a-tag
                  v-for="id in record.customGroupIds"
                  :key="id"
                  closable
                  @close="removeGroupId(record, id)"
                >
                  {{ groupMap[id] || id }}
                </a-tag>
                <a-button size="small" @click="openGroupPicker(record)">选择用户组</a-button>
              </div>
            </div>
            <span v-else class="text-gray-400 text-xs">未选中</span>
          </template>
        </template>
      </a-table>
    </a-spin>

    <!-- Org tree picker modal -->
    <a-modal v-model:open="orgPickerVisible" title="选择组织" width="480" @ok="confirmOrgPicker">
      <a-tree
        ref="orgTreeRef"
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
      width="480"
      @ok="confirmGroupPicker"
    >
      <a-tree
        ref="groupTreeRef"
        v-model:checked-keys="groupPickerChecked"
        :tree-data="groupTreeData"
        checkable
        :field-names="{ children: 'children', title: 'name', key: 'id' }"
        default-expand-all
      />
    </a-modal>

    <template #footer>
      <a-space>
        <a-button @click="handleClose">取消</a-button>
        <a-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'UserGrantPermission' })
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { message } from 'ant-design-vue'
import { useAuthStore } from '@/store'
import { fetchUserGrantPermission, fetchUserOwnPermissionDetail } from '@/api/user'
import { fetchPermissionModules, fetchPermissionByModule } from '@/api/permission'
import { fetchOrgTree } from '@/api/org'
import { fetchGroupTree } from '@/api/group'

defineProps<{ open: boolean }>()
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
const drawerWidth = computed(() => (isMobile.value ? '100%' : 800))

const currentUserId = ref('')
const modules = ref<string[]>([])
const currentModule = ref<string | undefined>(undefined)
const tableData = ref<any[]>([])
const loading = ref(false)
const submitLoading = ref(false)

const allChecked = computed(
  () => tableData.value.length > 0 && tableData.value.every(p => p.checked)
)
const indeterminate = computed(() => {
  const checked = tableData.value.filter(p => p.checked)
  return checked.length > 0 && checked.length < tableData.value.length
})

const columns = [
  { key: 'code', title: '权限编码', dataIndex: 'code', width: 260 },
  { key: 'name', title: '权限名称', dataIndex: 'name', width: 160 },
  { key: 'scope', title: '数据范围', dataIndex: 'scope', width: 500 },
]

// ── Org / Group trees for pickers ──
const orgTreeData = ref<any[]>([])
const groupTreeData = ref<any[]>([])
const orgMap = ref<Record<string, string>>({})
const groupMap = ref<Record<string, string>>({})

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

// ── Org picker ──
const orgPickerVisible = ref(false)
const orgPickerRecord = ref<any>(null)
const orgPickerChecked = ref<string[]>([])
const orgTreeRef = ref()

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
const groupPickerVisible = ref(false)
const groupPickerRecord = ref<any>(null)
const groupPickerChecked = ref<string[]>([])
const groupTreeRef = ref()

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
      fetchUserOwnPermissionDetail({ user_id: currentUserId.value }),
    ])
    const ownMap: Record<string, any> = {}
    ;(ownRes?.data || []).forEach((item: any) => {
      ownMap[item.permission_code] = item
    })

    tableData.value = (permsRes?.data || []).map((p: any) => {
      const own = ownMap[p.code]
      return {
        code: p.code,
        name: p.name,
        checked: !!own,
        scope: own?.scope || 'ALL',
        customGroupIds: own?.custom_scope_group_ids ? JSON.parse(own.custom_scope_group_ids) : [],
        customOrgIds: own?.custom_scope_org_ids ? JSON.parse(own.custom_scope_org_ids) : [],
      }
    })
  } finally {
    loading.value = false
  }
}

function handleAllCheck(e: any) {
  const checked = e.target.checked
  tableData.value.forEach(p => {
    p.checked = checked
  })
}

function handleCheck(record: any, checked: boolean) {
  record.checked = checked
}

async function handleSubmit() {
  const checkedItems = tableData.value.filter(p => p.checked)
  submitLoading.value = true
  try {
    const permissions = checkedItems.map(p => ({
      permission_code: p.code,
      scope: p.scope,
      custom_scope_group_ids: p.customGroupIds?.length ? JSON.stringify(p.customGroupIds) : null,
      custom_scope_org_ids: p.customOrgIds?.length ? JSON.stringify(p.customOrgIds) : null,
    }))
    const { success } = await fetchUserGrantPermission({
      user_id: currentUserId.value,
      permissions,
    })
    if (success) {
      message.success('授权成功')
      useAuthStore().refreshPermissions()
      emit('success')
      handleClose()
    }
  } finally {
    submitLoading.value = false
  }
}

function doOpen(user: any) {
  currentUserId.value = user.id
  loadModules()
  loadTrees()
  emit('update:open', true)
}

function handleClose() {
  currentModule.value = undefined
  tableData.value = []
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
