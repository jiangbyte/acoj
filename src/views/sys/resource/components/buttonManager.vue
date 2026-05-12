<template>
  <a-drawer
    :open="open"
    :title="`权限按钮管理 - ${parentResource?.name || ''}`"
    :width="760"
    destroy-on-close
    @close="handleClose"
  >
    <a-spin :spinning="loading">
      <div class="mb-3">
        <a-button type="primary" size="small" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          新增按钮
        </a-button>
      </div>

      <a-table
        :columns="columns"
        :data-source="buttonList"
        :pagination="false"
        size="small"
        bordered
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            {{ record.name }}
          </template>
          <template v-else-if="column.key === 'code'">
            {{ record.code }}
          </template>
          <template v-else-if="column.key === 'permission'">
            <a-select
              v-model:value="record.permission_id"
              placeholder="选择关联权限"
              allow-clear
              show-search
              :filter-option="(input: string, option: any) => option.label?.includes(input)"
              style="width: 100%"
            >
              <a-select-opt-group v-for="group in permissionGroups" :key="group.module" :label="group.module">
                <a-select-option
                  v-for="p in group.permissions"
                  :key="p.id"
                  :value="p.id"
                  :label="`${p.code} - ${p.name}`"
                >
                  {{ p.code }} - {{ p.name }}
                </a-select-option>
              </a-select-opt-group>
            </a-select>
          </template>
          <template v-else-if="column.key === 'sort_code'">
            <a-input-number
              v-model:value="record.sort_code"
              :min="0"
              :max="9999"
              size="small"
              style="width: 100%"
              @change="handleSortChange(record)"
            />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-popconfirm title="确定删除该按钮？" @confirm="handleDelete(record)">
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
      <div v-if="!buttonList.length && !loading" class="text-center text-gray-400 py-8">
        该资源下没有按钮类子资源
      </div>
    </a-spin>

    <template #footer>
      <a-space>
        <a-button @click="handleClose">取消</a-button>
        <a-button type="primary" :loading="submitLoading" @click="handleSubmit">保存权限绑定</a-button>
      </a-space>
    </template>

    <!-- Create button modal -->
    <a-modal v-model:open="createModalVisible" title="新增按钮" @ok="confirmCreate" :confirm-loading="createLoading" width="480">
      <a-form :model="createForm" layout="vertical">
        <a-form-item label="按钮名称" required>
          <a-input v-model:value="createForm.name" placeholder="如 新增用户" />
        </a-form-item>
        <a-form-item label="按钮编码" required>
          <a-input v-model:value="createForm.code" placeholder="如 sys:user:create" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="createForm.sort_code" :min="0" :max="9999" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'ResourceButtonManager' })
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import {
  fetchResourceTree,
  fetchResourceCreate,
  fetchResourceModify,
  fetchResourceRemove,
  fetchResourceOwnPermissions,
  fetchResourceBindPermissions,
} from '@/api/resource'
import { fetchPermissionModules, fetchPermissionByModule } from '@/api/permission'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const loading = ref(false)
const submitLoading = ref(false)
const parentResource = ref<any>(null)
const buttonList = ref<any[]>([])
const permissionGroups = ref<Array<{ module: string; permissions: any[] }>>([])

const columns = [
  { title: '按钮名称', dataIndex: 'name', key: 'name', width: 140 },
  { title: '按钮编码', dataIndex: 'code', key: 'code', width: 140 },
  { title: '关联权限', key: 'permission', width: 240 },
  { title: '排序', dataIndex: 'sort_code', key: 'sort_code', width: 80 },
  { title: '操作', key: 'action', width: 60, fixed: 'right' },
]

function findNode(nodes: any[], id: string): any | null {
  for (const n of nodes) {
    if (n.id === id) return n
    if (n.children) {
      const found = findNode(n.children, id)
      if (found) return found
    }
  }
  return null
}

async function loadPermissions() {
  const { data: modules } = await fetchPermissionModules()
  const modList: string[] = modules || []
  const groups = await Promise.all(
    modList.map(async (mod) => {
      const { data } = await fetchPermissionByModule({ module: mod })
      return { module: mod, permissions: data || [] }
    })
  )
  permissionGroups.value = groups.filter((g) => g.permissions.length > 0)
}

async function loadButtons(resourceId: string) {
  const { data: tree } = await fetchResourceTree()
  const node = findNode(tree || [], resourceId)
  const buttons = (node?.children || []).filter((c: any) => c.type === 'BUTTON')
  buttonList.value = buttons.map((b: any) => ({ ...b, permission_id: undefined as string | undefined }))

  if (buttonList.value.length > 0) {
    const results = await Promise.all(
      buttonList.value.map((b) =>
        fetchResourceOwnPermissions({ resource_id: b.id }).then((r) => ({
          id: b.id,
          ids: ((r as any)?.data || []) as string[],
        }))
      )
    )
    const map = new Map(results.map((r) => [r.id, r.ids]))
    buttonList.value.forEach((b) => {
      const ids = map.get(b.id)
      b.permission_id = ids?.length ? ids[0] : undefined
    })
  }
}

async function loadData(resource: any) {
  await Promise.all([loadPermissions(), loadButtons(resource.id)])
}

function doOpen(resource: any) {
  parentResource.value = resource
  loading.value = true
  loadData(resource).finally(() => { loading.value = false })
  emit('update:open', true)
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    await Promise.all(
      buttonList.value.map((b) =>
        fetchResourceBindPermissions({
          resource_id: b.id,
          permission_ids: b.permission_id ? [b.permission_id] : [],
        })
      )
    )
    message.success('保存成功')
    emit('success')
    handleClose()
  } finally {
    submitLoading.value = false
  }
}

// ── Sort (auto-update) ──
let sortTimer: ReturnType<typeof setTimeout> | null = null
async function handleSortChange(record: any) {
  if (sortTimer) clearTimeout(sortTimer)
  sortTimer = setTimeout(async () => {
    try {
      await fetchResourceModify({ id: record.id, sort_code: record.sort_code })
    } catch {
      message.error('排序更新失败')
    }
  }, 500)
}

// ── Create button ──
const createModalVisible = ref(false)
const createLoading = ref(false)
const createForm = ref({ name: '', code: '', sort_code: 0 })

function openCreate() {
  createForm.value = { name: '', code: '', sort_code: 0 }
  createModalVisible.value = true
}

async function confirmCreate() {
  if (!createForm.value.name || !createForm.value.code) {
    message.warning('请填写按钮名称和编码')
    return
  }
  createLoading.value = true
  try {
    const parent = parentResource.value
    await fetchResourceCreate({
      name: createForm.value.name,
      code: createForm.value.code,
      sort_code: createForm.value.sort_code,
      category: parent?.category,
      type: 'BUTTON',
      parent_id: parent?.id,
      status: 'ENABLED',
    })
    message.success('新增成功')
    createModalVisible.value = false
    await loadButtons(parent?.id)
  } finally {
    createLoading.value = false
  }
}

// ── Delete button ──
async function handleDelete(record: any) {
  try {
    await fetchResourceRemove({ ids: [record.id] })
    message.success('删除成功')
    buttonList.value = buttonList.value.filter((b) => b.id !== record.id)
  } catch {
    message.error('删除失败')
  }
}

function handleClose() {
  buttonList.value = []
  permissionGroups.value = []
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
