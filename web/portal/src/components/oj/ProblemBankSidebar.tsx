import type { ReactNode } from 'react'
import { useEffect, useMemo, useState } from 'react'
import {
  AppstoreOutlined,
  BookOutlined,
  GlobalOutlined,
  LockOutlined,
  PlusOutlined,
  ReadOutlined,
  StarOutlined,
} from '@ant-design/icons'
import { Input, Modal, message } from 'antd'
import { Link, useNavigate } from 'react-router-dom'
import { problemListApi } from '@/api'
import { useAuthStore } from '@/stores/auth'

type NavKey = 'problems' | 'plans' | 'lists'

const navItems: { key: NavKey; label: string; icon: ReactNode; to: string }[] = [
  { key: 'problems', label: '题目', icon: <AppstoreOutlined />, to: '/problems' },
  { key: 'plans', label: '练习路径', icon: <BookOutlined />, to: '/plans' },
]

export function ProblemBankSidebar({ active }: { active: NavKey }) {
  const navigate = useNavigate()
  const isLogin = useAuthStore((s) => s.isLogin)
  const [lists, setLists] = useState<any[]>([])
  const [createOpen, setCreateOpen] = useState(false)
  const [title, setTitle] = useState('')
  const [creating, setCreating] = useState(false)

  async function loadMine() {
    if (!isLogin()) {
      setLists([])
      return
    }
    try {
      const res = await problemListApi.mine()
      setLists(res.data ?? [])
    } catch {
      setLists([])
    }
  }

  useEffect(() => {
    void loadMine()
  }, [isLogin()])

  const favorites = useMemo(() => lists.find((l) => l.is_system) ?? null, [lists])
  const customLists = useMemo(() => lists.filter((l) => !l.is_system), [lists])

  async function onCreate() {
    if (!title.trim()) {
      message.warning('请输入题单名称')
      return
    }
    if (title.trim() === '我的收藏') {
      message.warning('「我的收藏」为系统题单，请换一个名称')
      return
    }
    setCreating(true)
    try {
      const res = await problemListApi.create({ title: title.trim(), visibility: 'PRIVATE' })
      message.success('已创建')
      setCreateOpen(false)
      setTitle('')
      await loadMine()
      if (res.data?.id) navigate(`/lists/detail?id=${res.data.id}`)
    } finally {
      setCreating(false)
    }
  }

  return (
    <aside className="hidden w-[200px] shrink-0 lg:block">
      <div className="sticky top-[80px] flex flex-col gap-4">
        <nav className="panel rounded-xl p-2">
          {navItems.map((item) => (
            <Link
              key={item.key}
              to={item.to}
              className={`flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm transition-colors ${
                active === item.key
                  ? 'bg-[var(--ant-color-fill-quaternary)] font-medium text-[var(--ant-color-text)]'
                  : 'text-[var(--ant-color-text-secondary)] hover:bg-[var(--ant-color-fill-secondary)] hover:text-[var(--ant-color-text)]'
              }`}
            >
              <span className="text-base">{item.icon}</span>
              {item.label}
            </Link>
          ))}
        </nav>

        <div className="panel rounded-xl p-3">
          <div className="mb-2 px-1 text-sm font-medium">我的题单</div>

          {isLogin() && favorites ? (
            <Link
              to={`/lists/detail?id=${favorites.id}`}
              className={`mb-2 flex w-full items-center gap-2 rounded-lg px-2 py-2 text-sm transition-colors ${
                active === 'lists'
                  ? 'bg-[var(--ant-color-primary-bg)] text-[var(--ant-color-primary)]'
                  : 'text-[var(--ant-color-text-secondary)] hover:bg-[var(--ant-color-fill-secondary)] hover:text-[var(--ant-color-text)]'
              }`}
            >
              <StarOutlined />
              <span className="min-w-0 flex-1 truncate text-left">{favorites.title}</span>
              <span className="tabular-nums text-xs opacity-70">{favorites.problem_count}</span>
            </Link>
          ) : null}

          <div className="mb-1 flex items-center justify-between px-1 pt-1">
            <span className="muted-text text-xs">自定义</span>
            <PlusOutlined
              className="muted-text cursor-pointer text-xs"
              onClick={() => {
                if (!isLogin()) {
                  message.info('请先登录')
                  navigate('/auth/login')
                  return
                }
                setCreateOpen(true)
              }}
            />
          </div>
          <div className="flex flex-col gap-1">
            {customLists.map((item) => (
              <Link
                key={item.id}
                to={`/lists/detail?id=${item.id}`}
                className="flex w-full items-center gap-2 rounded-lg px-2 py-2 text-sm text-[var(--ant-color-text-secondary)] hover:bg-[var(--ant-color-fill-secondary)] hover:text-[var(--ant-color-text)]"
              >
                <ReadOutlined />
                <span className="min-w-0 flex-1 truncate text-left">{item.title}</span>
                {item.visibility === 'PRIVATE' ? (
                  <LockOutlined className="text-xs" />
                ) : (
                  <GlobalOutlined className="text-xs" />
                )}
              </Link>
            ))}
            {!isLogin() ? (
              <div className="muted-text px-2 py-2 text-xs">登录后查看收藏与题单</div>
            ) : !customLists.length ? (
              <div className="muted-text px-2 py-1 text-xs">可新建练习题单</div>
            ) : null}
          </div>
        </div>
      </div>

      <Modal
        title="新建题单"
        open={createOpen}
        onCancel={() => setCreateOpen(false)}
        onOk={() => void onCreate()}
        confirmLoading={creating}
        okText="创建"
      >
        <Input
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="题单名称"
          onPressEnter={() => void onCreate()}
        />
      </Modal>
    </aside>
  )
}
