import { Outlet, useLocation } from 'react-router-dom'

/** 竞赛详情：页面级沉浸封面，由页面自行控制边距 */
function isContestDetailPath(pathname: string) {
  return /^\/contests\/[^/]+$/.test(pathname)
}

/** 消息中心：IM 全高布局，避免列表/会话被页边距挤成上下堆叠 */
function isMessagesPath(pathname: string) {
  return pathname === '/messages' || pathname.startsWith('/messages/')
}

export function Content() {
  const { pathname } = useLocation()
  const isAuthPage = pathname.startsWith('/auth/')
  const isImmersiveContest = isContestDetailPath(pathname)
  const isMessages = isMessagesPath(pathname)

  let mainClass = 'w-full min-h-[calc(100vh-64px-72px)] px-6 py-6'
  if (isMessages) {
    mainClass = 'flex w-full min-h-0 flex-1 flex-col p-0'
  } else if (isImmersiveContest) {
    mainClass = 'w-full min-h-[calc(100vh-64px-72px)] p-0'
  } else if (isAuthPage) {
    mainClass =
      'w-full min-h-[calc(100vh-64px-72px)] bg-[color-mix(in_srgb,var(--ant-color-fill-quaternary)_85%,#f5f7fa)] px-0 py-0'
  }

  return (
    <main className={mainClass}>
      <Outlet />
    </main>
  )
}
