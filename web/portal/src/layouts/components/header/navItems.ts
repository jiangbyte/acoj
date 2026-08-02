export const navItems = [
  { key: '/', label: '首页' },
  { key: '/problems', label: '题库' },
  { key: '/contests', label: '竞赛' },
  { key: '/submissions', label: '提交' },
  { key: '/rank', label: '排名' },
]

export function getSelectedNavKey(pathname: string) {
  return (
    navItems.find((item) =>
      item.key === '/' ? pathname === '/' : pathname.startsWith(item.key),
    )?.key || '/'
  )
}
