import { Button, Input } from 'antd'
import { CloseOutlined, RobotOutlined, SendOutlined } from '@ant-design/icons'

type Props = {
  onClose: () => void
}

export function AiChatPanel({ onClose }: Props) {
  return (
    <div className="panel flex h-full min-w-0 flex-col rounded-md">
      <div className="panel-header shrink-0">
        <RobotOutlined />
        <span className="font-medium">AI Chat</span>
        <div className="flex-1" />
        <Button type="text" icon={<CloseOutlined />} aria-label="关闭 AI Chat" onClick={onClose} />
      </div>
      <div className="flex min-h-0 flex-1 flex-col gap-3 p-3">
        <div className="muted-box flex min-h-0 flex-1 items-center justify-center rounded-md px-3 py-6 text-center text-sm">
          AI Chat 接口待接入
        </div>
        <Input
          disabled
          placeholder="向 AI 提问"
          suffix={<SendOutlined className="muted-text" />}
        />
      </div>
    </div>
  )
}
