import { Modal, message } from 'ant-design-vue'

interface DeleteApiParams {
  ids: string[]
}

interface DeleteApiResult {
  success: boolean
}

export function confirmDelete(options: {
  /** 实体名称，如 "用户"、"角色" */
  name: string
  /** 选中的 ID 列表 */
  selectedKeys: string[]
  /** 删除 API 函数 */
  deleteApi: (params: DeleteApiParams) => Promise<DeleteApiResult>
  /** 删除成功后的回调（清空选中、刷新表格等） */
  onSuccess: () => void
}) {
  Modal.confirm({
    title: '确定删除？',
    content: `将删除已选择的 ${options.selectedKeys.length} 个${options.name}，请确认。`,
    okType: 'danger',
    onOk: async () => {
      const { success } = await options.deleteApi({ ids: options.selectedKeys })
      if (success) {
        message.success(`成功删除 ${options.selectedKeys.length} 个${options.name}`)
        options.onSuccess()
      }
    },
  })
}
