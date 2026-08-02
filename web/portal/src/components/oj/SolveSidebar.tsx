import { ExperimentOutlined, ProfileOutlined, TrophyOutlined } from '@ant-design/icons'
import { Button, Tooltip } from 'antd'

const items = [
  { key: 'statement', icon: <ProfileOutlined />, title: '题目描述' },
  { key: 'submissions', icon: <ExperimentOutlined />, title: '提交记录' },
  { key: 'rank', icon: <TrophyOutlined />, title: '排名' },
]

export function SolveSidebar() {
  return (
    <div className="flex h-full w-14 shrink-0 flex-col items-center gap-2 border-r border-gray-200 bg-white py-3">
      {items.map((item) => (
        <Tooltip key={item.key} title={item.title} placement="right">
          <Button
            type="text"
            className="!h-10 !w-10 !px-0 !text-base"
            icon={item.icon}
            aria-label={item.title}
          />
        </Tooltip>
      ))}
    </div>
  )
}
