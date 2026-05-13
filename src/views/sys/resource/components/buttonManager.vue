<template>
  <a-drawer
    :open="open"
    :title="`权限按钮管理 - ${parentResource?.name || ''}`"
    :width="drawerWidth"
    destroy-on-close
    @close="handleClose"
  >
    <a-spin :spinning="loading">
      <div class="mb-3">
        <a-button type="primary" @click="openCreate">
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
            <a-tag
              :color="record.permission_code ? 'blue' : undefined"
              style="cursor: pointer"
              @click="openPermissionPicker(record)"
            >
              {{ record.permission_code || '未绑定' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'sort_code'">
            {{ record.sort_code ?? 0 }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openManageModal(record)">管理</a-button>
              <a-button type="link" danger size="small" @click="openDeleteConfirm(record)">
                删除
              </a-button>
            </a-space>
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
        <a-button type="primary" :loading="submitLoading" @click="handleSubmit">
          保存权限绑定
        </a-button>
      </a-space>
    </template>

    <!-- Create button modal -->
    <a-modal
      v-model:open="createModalVisible"
      title="新增按钮"
      :confirm-loading="createLoading"
      :width="createModalWidth"
      @ok="confirmCreate"
    >
      <a-form :model="createForm" layout="vertical">
        <a-form-item label="按钮名称" required>
          <a-input v-model:value="createForm.name" placeholder="如 新增用户" />
        </a-form-item>
        <a-form-item label="按钮编码" required>
          <a-input v-model:value="createForm.code" placeholder="如 sys:user:create" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number
            v-model:value="createForm.sort_code"
            :min="0"
            :max="9999"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Permission picker modal -->
    <a-modal
      :open="permPickerVisible"
      title="选择关联权限"
      :width="permModalWidth"
      destroy-on-close
      ok-text="确认"
      cancel-text="取消"
      @ok="confirmPermissionPicker"
      @cancel="permPickerVisible = false"
    >
      <a-select
        v-model:value="permCurrentModule"
        placeholder="选择模块"
        allow-clear
        style="width: 100%"
        class="mb-3"
        @change="loadModulePermissions"
      >
        <a-select-option v-for="m in permModules" :key="m" :value="m">{{ m }}</a-select-option>
      </a-select>

      <a-table
        :data-source="permList"
        :columns="permColumns"
        :pagination="false"
        size="small"
        bordered
        row-key="code"
        :scroll="{ y: 360 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'code'">
            <a-radio
              :checked="permSelectedCode === record.code"
              @change="permSelectedCode = record.code"
            >
              {{ record.code }}
            </a-radio>
          </template>
        </template>
      </a-table>
      <div v-if="!permList.length && permCurrentModule" class="text-center text-gray-400 py-4">
        该模块下没有权限
      </div>
    </a-modal>

    <!-- Button management modal -->
    <a-modal
      :open="manageModalVisible"
      :title="`按钮管理 - ${manageTarget?.name || ''}`"
      :width="manageModalWidth"
      destroy-on-close
      ok-text="保存"
      cancel-text="取消"
      @ok="confirmManage"
      @cancel="manageModalVisible = false"
    >
      <a-form layout="vertical">
        <a-form-item label="按钮名称">
          <span class="text-gray-600">{{ manageTarget?.name }}</span>
        </a-form-item>
        <a-form-item label="按钮编码">
          <span class="text-gray-600">{{ manageTarget?.code }}</span>
        </a-form-item>
        <a-form-item label="关联权限">
          <div class="flex items-center gap-2">
            <a-tag :color="manageTarget?.permission_code ? 'blue' : undefined">
              {{ manageTarget?.permission_code || '未绑定' }}
            </a-tag>
            <a-button size="small" @click="openPermissionPicker(manageTarget)">选择</a-button>
            <a-button
              v-if="manageTarget?.permission_code"
              size="small"
              danger
              @click="clearPermission"
            >
              清除
            </a-button>
          </div>
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number
            v-model:value="manageTarget.sort_code"
            :min="0"
            :max="9999"
            style="width: 100%"
          />
        </a-form-item>
        <a-divider />
      </a-form>
    </a-modal>

    <!-- Delete confirmation modal -->
    <a-modal
      :open="deleteModalVisible"
      title="确认删除"
      ok-text="确定删除"
      ok-danger
      cancel-text="取消"
      @ok="confirmDelete"
      @cancel="deleteModalVisible = false"
    >
      <p>确定删除按钮「{{ deleteTarget?.name }}」？删除后不可恢复。</p>
    </a-modal>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'ResourceButtonManager' })
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import {
  fetchResourceTree,
  fetchResourceCreate,
  fetchResourceModify,
  fetchResourceRemove,
} from '@/api/resource'
import { fetchPermissionModules, fetchPermissionByModule } from '@/api/permission'

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
const drawerWidth = computed(() => (isMobile.value ? '100%' : 760))
const createModalWidth = computed(() => (isMobile.value ? '100%' : 480))
const permModalWidth = computed(() => (isMobile.value ? '100%' : 480))
const manageModalWidth = computed(() => (isMobile.value ? '100%' : 520))

const loading = ref(false)
const submitLoading = ref(false)
const parentResource = ref<any>(null)
const buttonList = ref<any[]>([])

const columns = [
  { title: '按钮名称', dataIndex: 'name', key: 'name', width: 140 },
  { title: '按钮编码', dataIndex: 'code', key: 'code', width: 140 },
  { title: '关联权限', key: 'permission', width: 240 },
  { title: '排序', dataIndex: 'sort_code', key: 'sort_code', width: 70 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' },
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

async function loadButtons(resourceId: string) {
  const { data: tree } = await fetchResourceTree()
  const node = findNode(tree || [], resourceId)
  const buttons = (node?.children || []).filter((c: any) => c.type === 'BUTTON')
  buttonList.value = buttons.map((b: any) => {
    let permission_code: string | undefined
    try {
      const extra = JSON.parse(b.extra || '{}')
      permission_code = extra.permission_code || undefined
    } catch {
      permission_code = undefined
    }
    return { ...b, permission_code }
  })
}

async function loadData(resource: any) {
  await loadButtons(resource.id)
}

function doOpen(resource: any) {
  parentResource.value = resource
  loading.value = true
  loadData(resource).finally(() => {
    loading.value = false
  })
  emit('update:open', true)
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    await Promise.all(
      buttonList.value.map(b =>
        fetchResourceModify({
          id: b.id,
          extra: JSON.stringify({ permission_code: b.permission_code || null }),
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

// ── Button management modal ──
const manageModalVisible = ref(false)
const manageTarget = ref<any>(null)

function openManageModal(record: any) {
  manageTarget.value = record
  manageModalVisible.value = true
}

function confirmManage() {
  manageModalVisible.value = false
}

// ── Permission picker modal ──
const permPickerVisible = ref(false)
const permTarget = ref<any>(null)
const permModules = ref<string[]>([])
const permCurrentModule = ref<string | undefined>(undefined)
const permList = ref<any[]>([])
const permSelectedCode = ref<string>('')
const permColumns = [
  { key: 'code', title: '权限编码', dataIndex: 'code', width: 260 },
  { key: 'name', title: '权限名称', dataIndex: 'name', width: 160 },
]

async function openPermissionPicker(record: any) {
  permTarget.value = record
  permSelectedCode.value = record.permission_code || ''
  const { data: modules } = await fetchPermissionModules()
  permModules.value = modules || []
  permCurrentModule.value = undefined
  permList.value = []
  permPickerVisible.value = true
}

async function loadModulePermissions() {
  if (!permCurrentModule.value) {
    permList.value = []
    return
  }
  const { data } = await fetchPermissionByModule({ module: permCurrentModule.value })
  permList.value = data || []
}

function confirmPermissionPicker() {
  if (permTarget.value) {
    permTarget.value.permission_code = permSelectedCode.value || undefined
  }
  permPickerVisible.value = false
}

function clearPermission() {
  if (manageTarget.value) {
    manageTarget.value.permission_code = undefined
  }
}

// ── Delete confirmation modal ──
const deleteModalVisible = ref(false)
const deleteTarget = ref<any>(null)

function openDeleteConfirm(record: any) {
  deleteTarget.value = record
  deleteModalVisible.value = true
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  try {
    await fetchResourceRemove({ ids: [deleteTarget.value.id] })
    message.success('删除成功')
    buttonList.value = buttonList.value.filter(b => b.id !== deleteTarget.value.id)
    manageModalVisible.value = false
  } catch {
    message.error('删除失败')
  } finally {
    deleteModalVisible.value = false
    deleteTarget.value = null
  }
}

function handleClose() {
  buttonList.value = []
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
