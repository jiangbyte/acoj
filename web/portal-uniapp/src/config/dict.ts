export interface OptionItem {
  label: string
  value: string
}

export const fallbackDicts: Record<string, OptionItem[]> = {
  NOTIFICATION_SEVERITY: [
    { label: '普通', value: 'INFO' },
    { label: '成功', value: 'SUCCESS' },
    { label: '警告', value: 'WARNING' },
    { label: '错误', value: 'ERROR' },
  ],
  TODO_PRIORITY: [
    { label: '低', value: 'LOW' },
    { label: '普通', value: 'NORMAL' },
    { label: '高', value: 'HIGH' },
    { label: '紧急', value: 'URGENT' },
  ],
  TODO_STATUS: [
    { label: '待处理', value: 'PENDING' },
    { label: '进行中', value: 'IN_PROGRESS' },
    { label: '已完成', value: 'COMPLETED' },
    { label: '已取消', value: 'CANCELLED' },
  ],
}
