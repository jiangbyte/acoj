import { Empty, Typography } from 'antd'

export function PlaceholderPage({ title }: { title: string }) {
  return (
    <div className="py-12">
      <Typography.Title level={3}>{title}</Typography.Title>
      <Empty description="业务页面将在下一阶段从 portal-nuxt 迁移" />
    </div>
  )
}
