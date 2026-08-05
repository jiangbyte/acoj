import { useEffect } from 'react'
import { Grid } from 'antd'
import { Outlet } from 'react-router-dom'
import { refreshDict, syncDictTree } from '@/utils/dict'
import { AppHeader, HEADER_HEIGHT } from './components'

const { useBreakpoint } = Grid

export function SolveLayout() {
  const screens = useBreakpoint()
  const isMobile = !screens.lg

  useEffect(() => {
    syncDictTree()
    void refreshDict()
  }, [])

  return (
    <div className="h-screen overflow-hidden bg-[var(--ant-color-bg-layout)] text-[var(--ant-color-text)]">
      {isMobile ? <AppHeader /> : null}
      <main
        className="h-full min-h-0 overflow-hidden"
        style={{ paddingTop: isMobile ? HEADER_HEIGHT : 0 }}
      >
        <Outlet />
      </main>
    </div>
  )
}
