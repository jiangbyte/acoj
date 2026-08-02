import { Avatar, Button, Dropdown, Modal, Space, Typography, message } from 'antd'
import {
  HomeOutlined,
  LogoutOutlined,
  UserOutlined,
} from '@ant-design/icons'
import type { MenuProps } from 'antd'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/auth'

export function UserCenter() {
  const navigate = useNavigate()
  const location = useLocation()
  const token = useAuthStore((s) => s.token)
  const userInfo = useAuthStore((s) => s.userInfo)
  const logout = useAuthStore((s) => s.logout)

  if (!token) {
    return (
      <Button type="primary" onClick={() => navigate('/auth/login')}>
        登录
      </Button>
    )
  }

  const displayName = userInfo?.nickname || userInfo?.account || '用户'
  const avatarSrc = userInfo?.avatar || undefined

  const items: MenuProps['items'] = [
    {
      key: 'userCenter',
      icon: <UserOutlined />,
      label: '个人中心',
    },
    { type: 'divider' },
    {
      key: 'home',
      icon: <HomeOutlined />,
      label: '首页',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
    },
  ]

  const onClick: MenuProps['onClick'] = ({ key }) => {
    if (key === 'userCenter') {
      navigate('/usercenter')
      return
    }
    if (key === 'home') {
      navigate('/')
      return
    }
    if (key === 'logout') {
      Modal.confirm({
        title: '退出登录',
        content: '确定退出当前账号？',
        okText: '确认',
        cancelText: '取消',
        onOk: async () => {
          await logout(location.pathname)
          message.success('已退出登录')
        },
      })
    }
  }

  return (
    <Dropdown menu={{ items, onClick }} trigger={['click']} placement="bottomRight">
      <Space className="cursor-pointer select-none" size={8}>
        <Avatar src={avatarSrc} icon={<UserOutlined />} />
        <Typography.Text className="hidden md:inline max-w-28 truncate">
          {displayName}
        </Typography.Text>
      </Space>
    </Dropdown>
  )
}
