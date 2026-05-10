import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store'
import { LogoutOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'

export interface UserMenuItem {
  key: string
  icon?: object
  label: string
  danger?: boolean
  onClick: () => void
}

export function useUserMenu() {
  const router = useRouter()
  const auth = useAuthStore()

  const userMenuItems: UserMenuItem[] = [
    {
      key: 'logout',
      icon: LogoutOutlined,
      label: '退出登录',
      danger: true,
      onClick: async () => {
        await auth.logout()
        message.success('已退出登录')
        router.push({ name: 'login' })
      },
    },
  ]

  return { userMenuItems }
}
