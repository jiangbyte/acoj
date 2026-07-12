<script setup lang="tsx">
import { accountApi, deptApi } from '@/api'
import { computed, reactive } from 'vue'

const emit = defineEmits<{
  saved: []
}>()

const state = reactive({
  showModal: false,
  loading: false,
  submitLoading: false,
  account: {} as any,
  deptTree: [] as any[],
  deptIds: [] as string[],
  primaryDeptId: null as string | null,
})

const modalTitle = computed(() =>
  state.account?.name
    ? `${'分配部门'} - ${state.account.name}`
    : '分配部门',
)
const primaryOptions = computed(() =>
  buildDeptOptions(state.deptTree).filter((item) => state.deptIds.includes(item.value)),
)

async function openModal(account: any) {
  state.account = account ?? {}
  state.deptTree = []
  state.deptIds = []
  state.primaryDeptId = null
  state.showModal = true
  await fetchGrant()
}

async function fetchGrant() {
  if (!state.account?.id) {
    return
  }
  state.loading = true
  try {
    const [deptResponse, grantResponse] = await Promise.all([
      deptApi.tree().catch(() => ({ data: [] })),
      accountApi.ownDepts(state.account.id),
    ])
    const grant_info_list = grantResponse.data?.grant_info_list ?? []
    state.deptTree = deptResponse.data ?? []
    state.deptIds = grant_info_list.map((item: any) => String(item.dept_id))
    const primaryDeptId = grant_info_list.find((item: any) => item.is_primary)?.dept_id
    state.primaryDeptId = primaryDeptId ? String(primaryDeptId) : (state.deptIds[0] ?? null)
  } finally {
    state.loading = false
  }
}

async function submitGrant() {
  state.submitLoading = true
  try {
    const selectedDeptIds = state.deptIds.map(String)
    const primaryDeptId =
      state.primaryDeptId && selectedDeptIds.includes(state.primaryDeptId)
        ? state.primaryDeptId
        : selectedDeptIds[0]
    await accountApi.grantDepts({
      id: state.account.id,
      grant_info_list: selectedDeptIds.map((deptId) => ({
        dept_id: deptId,
        is_primary: deptId === primaryDeptId,
      })),
    })
    window.$message.success('授权保存成功')
    closeModal()
    emit('saved')
  } finally {
    state.submitLoading = false
  }
}

function closeModal() {
  state.deptTree = []
  state.deptIds = []
  state.primaryDeptId = null
  state.showModal = false
  state.submitLoading = false
}

function buildDeptOptions(nodes: any[]): any[] {
  return nodes.flatMap((node) => [
    {
      label: node.name,
      value: String(node.id),
    },
    ...buildDeptOptions(node.children ?? []),
  ])
}

defineExpose({
  openModal,
})
</script>

<template>
  <NDrawer
    v-model:show="state.showModal"
    :default-width="520"
    placement="right"
    resizable
    :mask-closable="false"
  >
    <NDrawerContent :title="modalTitle" closable :native-scrollbar="false">
      <NSpin :show="state.loading">
        <NSpace vertical>
          <NFormItem :label="'分配部门'">
            <NTreeSelect
              v-model:value="state.deptIds"
              multiple
              cascade
              checkable
              clearable
              filterable
              :options="state.deptTree"
              key-field="id"
              label-field="name"
              children-field="children"
            />
          </NFormItem>
          <NFormItem :label="'主部门'">
            <NSelect
              v-model:value="state.primaryDeptId"
              clearable
              :disabled="!state.deptIds.length"
              :options="primaryOptions"
            />
          </NFormItem>
        </NSpace>
      </NSpin>

      <template #footer>
        <NSpace justify="end" align="center">
          <NButton @click="closeModal">
            关闭
          </NButton>
          <NButton type="primary" :loading="state.submitLoading" @click="submitGrant">
            保存
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>
