import { useEffect } from 'react'
import { Outlet } from 'react-router-dom'
import { ensureDict } from '@/utils/dict'
import { SolveHeader } from './components/header/SolveHeader'

export function SolveLayout() {
  useEffect(() => {
    void ensureDict()
  }, [])

  return (
    <div className="h-screen flex flex-col overflow-hidden bg-[#f5f5f5]">
      <SolveHeader />
      <main className="min-h-0 flex-1 overflow-hidden">
        <Outlet />
      </main>
    </div>
  )
}
