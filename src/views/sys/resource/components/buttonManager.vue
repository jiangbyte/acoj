<template>
  <a-drawer
    :open="open"
    :title="`权限按钮管理 - ${parentResource?.name || ''}`"
    :width="700"
    destroy-on-close
    @close="handleClose"
  >
    <a-spin :spinning="loading">
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
              <a-select-option
                v-for="p in permissions"
                :key="p.id"
                :value="p.id"
                :label="`${p.code} - ${p.name}`"
              >
                {{ p.code }} - {{ p.name }}
              </a-select-option>
            </a-select>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 'ENABLED' ? 'green' : 'red'">
              {{ record.status === 'ENABLED' ? '启用' : '禁用' }}
            </a-tag>
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
        <a-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
defineOptions({ name: 'ResourceButtonManager' })
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { fetchResourceTree, fetchResourceOwnPermissions, fetchResourceBindPermissions } from '@/api/resource'
import { fetchPermissionList } from '@/api/permission'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits(['update:open', 'success'])

const loading = ref(false)
const submitLoading = ref(false)
const parentResource = ref<any>(null)
const buttonList = ref<any[]>([])
const permissions = ref<any[]>([])

const columns = [
  { title: '按钮名称', dataIndex: 'name', key: 'name', width: 150 },
  { title: '按钮编码', dataIndex: 'code', key: 'code', width: 150 },
  { title: '关联权限', key: 'permission', width: 290 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
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

async function loadData(resource: any) {
  const [treeRes, permRes] = await Promise.all([
    fetchResourceTree(),
    fetchPermissionList(),
  ])
  permissions.value = permRes?.data || []

  const node = findNode(treeRes?.data || [], resource.id)
  const buttons = (node?.children || []).filter((c: any) => c.type === 'BUTTON')
  buttonList.value = buttons.map((b: any) => ({ ...b, permission_id: undefined as string | undefined }))

  // Load existing permission bindings for each button
  if (buttonList.value.length > 0) {
    const results = await Promise.all(
      buttonList.value.map((b) =>
        fetchResourceOwnPermissions({ resource_id: b.id }).then((r) => ({
          id: b.id,
          ids: (r as any)?.data || [],
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

function handleClose() {
  buttonList.value = []
  emit('update:open', false)
}

defineExpose({ doOpen })
</script>
