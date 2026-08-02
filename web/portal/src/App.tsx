import { App as AntApp, ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import { AppRouter } from '@/router'
import { ensureDict } from '@/utils/dict'

export default function App() {
  useEffect(() => {
    // 门户字典免登录，应用启动即拉取（含登录/找回密码等无 MainLayout 的页面）
    void ensureDict()
  }, [])

  return (
    <ConfigProvider
      locale={zhCN}
      theme={{
        token: {
          colorPrimary: '#1677ff',
          borderRadius: 6,
        },
      }}
    >
      <AntApp>
        <AppRouter />
      </AntApp>
    </ConfigProvider>
  )
}
