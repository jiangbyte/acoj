import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import { confirmDelete } from '@/utils'

interface UseCrudOptions {
  name: string
  deleteApi: (params: { ids: string[] }) => Promise<{ success: boolean }>
  onSuccess?: () => void
}

export function useCrud(options: UseCrudOptions) {
  const tableRef = ref()
  const selectedKeys = ref<string[]>([])

  const rowSelection = computed(() => ({
    selectedRowKeys: selectedKeys.value,
    onChange: (keys: string[]) => { selectedKeys.value = keys },
  }))

  function handleSearch() {
    tableRef.value?.refresh(true)
  }

  function resetSearch(form: Record<string, any>, extra?: Record<string, any>) {
    Object.keys(form).forEach(k => { form[k] = undefined })
    if (extra) {
      Object.entries(extra).forEach(([k, v]) => { form[k] = v })
    }
    tableRef.value?.refresh(true)
  }

  async function handleDelete(id: string) {
    const { success } = await options.deleteApi({ ids: [id] })
    if (success) {
      message.success('删除成功')
      tableRef.value?.refresh()
      options.onSuccess?.()
    }
  }

  function handleBatchDelete() {
    confirmDelete({
      name: options.name,
      selectedKeys: selectedKeys.value,
      deleteApi: options.deleteApi,
      onSuccess: () => {
        selectedKeys.value = []
        tableRef.value?.refresh()
        options.onSuccess?.()
      },
    })
  }

  function handleFormSuccess() {
    tableRef.value?.refresh()
    options.onSuccess?.()
  }

  return {
    tableRef,
    selectedKeys,
    rowSelection,
    handleSearch,
    resetSearch,
    handleDelete,
    handleBatchDelete,
    handleFormSuccess,
  }
}
