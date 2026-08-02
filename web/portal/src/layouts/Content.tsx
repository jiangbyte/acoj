import { Outlet, useLocation } from 'react-router-dom'

export function Content() {
  const { pathname } = useLocation()
  const isAuthPage = pathname.startsWith('/auth/')

  return (
    <main
      className={
        isAuthPage
          ? 'px-4 py-6 w-full bg-[#f8fafc] min-h-[calc(100vh-64px-72px)]'
          : 'px-6 py-6 w-full min-h-[calc(100vh-64px-72px)]'
      }
    >
      <Outlet />
    </main>
  )
}
