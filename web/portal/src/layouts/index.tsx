import { useEffect } from 'react'
import { ensureDict } from '@/utils/dict'
import { AppFooter, AppHeader, HEADER_HEIGHT } from './components'
import { Content } from './Content'

export { SolveLayout } from './SolveLayout'

export function MainLayout() {
  useEffect(() => {
    void ensureDict()
  }, [])

  return (
    <div className="min-h-screen flex flex-col bg-[#f5f5f5]">
      <AppHeader />
      <div className="flex-1 flex flex-col" style={{ paddingTop: HEADER_HEIGHT }}>
        <Content />
        <AppFooter />
      </div>
    </div>
  )
}
