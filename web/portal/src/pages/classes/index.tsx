import { useEffect, useState } from 'react'
import { Button, Empty, Input, Modal, Spin, Tag, message } from 'antd'
import {
  BookOutlined,
  PlusOutlined,
  SearchOutlined,
  TeamOutlined,
} from '@ant-design/icons'
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { clazzApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { formatDateMinute } from '@/utils/time'

const thumbTones = [
  'from-[var(--ant-color-primary)] to-[var(--ant-color-primary-hover)]',
  'from-[var(--ant-color-info)] to-[var(--ant-color-info-hover)]',
  'from-[var(--ant-color-success)] to-[var(--ant-color-success-hover)]',
  'from-[var(--ant-color-warning)] to-[var(--ant-color-warning-hover)]',
  'from-[var(--ant-color-error)] to-[var(--ant-color-error-active)]',
]

export function ClassListPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const isLogin = useAuthStore((s) => s.isLogin)
  const [searchParams, setSearchParams] = useSearchParams()
  const keyword = searchParams.get('keyword') ?? ''
  const current = Number(searchParams.get('current') ?? 1)
  const size = Number(searchParams.get('size') ?? 12)

  const [loading, setLoading] = useState(true)
  const [data, setData] = useState<any[]>([])
  const [total, setTotal] = useState(0)
  const [searchText, setSearchText] = useState(keyword)
  const [myClasses, setMyClasses] = useState<any[]>([])
  const [joinOpen, setJoinOpen] = useState(false)
  const [inviteCode, setInviteCode] = useState('')
  const [joinLoading, setJoinLoading] = useState(false)

  const loginHref = `/auth/login?redirect=${encodeURIComponent(`${location.pathname}${location.search}`)}`
  const totalPages = Math.max(1, Math.ceil(total / size))

  async function loadPublic() {
    setLoading(true)
    try {
      const res = await clazzApi.clazzPage({ current, size, keyword: keyword || undefined })
      setData(res.data.records ?? [])
      setTotal(res.data.total ?? 0)
    } finally {
      setLoading(false)
    }
  }

  async function loadMine() {
    if (!isLogin()) {
      setMyClasses([])
      return
    }
    try {
      const res = await clazzApi.clazzMy()
      setMyClasses(res.data ?? [])
    } catch {
      setMyClasses([])
    }
  }

  useEffect(() => {
    void loadPublic()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [keyword, current, size])

  useEffect(() => {
    void loadMine()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isLogin()])

  function onSearch() {
    const params: Record<string, string> = {}
    if (searchText.trim()) params.keyword = searchText.trim()
    setSearchParams(params)
  }

  function requireLogin(action?: () => void) {
    if (!isLogin()) {
      navigate(loginHref)
      return
    }
    action?.()
  }

  async function handleJoin() {
    if (!isLogin()) {
      navigate(loginHref)
      return
    }
    const code = inviteCode.trim()
    if (code.length !== 8) {
      message.warning('请输入 8 位邀请码')
      return
    }
    setJoinLoading(true)
    try {
      const res = await clazzApi.clazzJoin({ invite_code: code })
      message.success('加入成功')
      setJoinOpen(false)
      setInviteCode('')
      await Promise.all([loadPublic(), loadMine()])
      if (res.data?.id) navigate(`/classes/${res.data.id}`)
    } finally {
      setJoinLoading(false)
    }
  }

  return (
    <div className="page-shell grid w-full gap-5 xl:grid-cols-[minmax(0,1fr)_300px]">
      <div className="min-w-0 flex flex-col gap-5">
        <section className="flex flex-col gap-4">
          <Input
            allowClear
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            onPressEnter={() => onSearch()}
            placeholder="搜索公开班级名称 / 编码"
            prefix={<SearchOutlined className="muted-text" />}
            suffix={
              <button
                type="button"
                onClick={() => onSearch()}
                className="rounded-md bg-[var(--ant-color-primary)] px-3 py-1 text-sm text-white"
              >
                搜索
              </button>
            }
            className="h-11"
          />

          <div className="grid gap-3 sm:grid-cols-3">
            <div className="panel rounded-xl p-4">
              <div className="text-base font-semibold">公开班级</div>
              <div className="muted-text mt-2 text-sm">浏览学校开放的班级，了解人数与简介</div>
            </div>
            <div className="panel rounded-xl p-4">
              <div className="text-base font-semibold">邀请码加入</div>
              <div className="muted-text mt-2 text-sm">持有邀请码可一键进入班级学习</div>
            </div>
            <div className="panel rounded-xl p-4">
              <div className="text-base font-semibold">课程与任务</div>
              <div className="muted-text mt-2 text-sm">进班后可查看课程、公告与 OJ 任务</div>
            </div>
          </div>
        </section>

        <section className="min-w-0">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-lg font-semibold">公开班级</h2>
            <span className="muted-text text-sm">共 {total} 个</span>
          </div>

          <Spin spinning={loading}>
            {data.length ? (
              <div className="grid gap-4 sm:grid-cols-2">
                {data.map((item, index) => {
                  const tone = thumbTones[index % thumbTones.length]
                  return (
                    <article key={item.id} className="panel flex flex-col rounded-xl p-4">
                      <div className="flex items-start gap-3">
                        <div
                          className={`flex h-14 w-14 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br ${tone} text-2xl text-white`}
                        >
                          <BookOutlined />
                        </div>
                        <div className="min-w-0 flex-1">
                          <div className="flex flex-wrap items-center gap-2">
                            <Link
                              to={`/classes/${item.id}`}
                              className="truncate text-lg font-semibold hover:text-[var(--ant-color-primary)]"
                            >
                              {item.name}
                            </Link>
                            {item.joined ? <Tag color="success">已加入</Tag> : null}
                          </div>
                          <div className="mt-1 flex flex-wrap gap-2">
                            <Tag>{item.code}</Tag>
                          </div>
                        </div>
                      </div>
                      <p className="muted-text mt-3 line-clamp-2 flex-1 text-sm">
                        {item.summary || '暂无简介'}
                      </p>
                      <div className="mt-4 flex items-center justify-between gap-2">
                        <span className="muted-text flex items-center gap-1 text-sm">
                          <TeamOutlined />
                          {item.member_count} 人 · {formatDateMinute(item.created_at)}
                        </span>
                        <Link
                          to={`/classes/${item.id}`}
                          className="rounded-lg bg-[var(--ant-color-primary)] px-3 py-1.5 text-sm text-white hover:bg-[var(--ant-color-primary-hover)]"
                        >
                          {item.joined ? '进入' : '查看'}
                        </Link>
                      </div>
                    </article>
                  )
                })}
              </div>
            ) : (
              <div className="panel rounded-xl py-16">
                <Empty description="暂无公开班级" />
              </div>
            )}
          </Spin>

          {total > 0 ? (
            <div className="mt-4 flex items-center justify-between">
              <span className="muted-text text-sm">
                第 {current} / {totalPages} 页
              </span>
              <div className="flex gap-2">
                <button
                  type="button"
                  disabled={current <= 1}
                  onClick={() => {
                    setSearchParams({
                      ...(keyword ? { keyword } : {}),
                      current: String(current - 1),
                      size: String(size),
                    })
                  }}
                  className="rounded-lg px-3 py-1.5 text-sm ring-1 ring-[var(--ant-color-border)] disabled:opacity-40"
                >
                  上一页
                </button>
                <button
                  type="button"
                  disabled={current >= totalPages}
                  onClick={() => {
                    setSearchParams({
                      ...(keyword ? { keyword } : {}),
                      current: String(current + 1),
                      size: String(size),
                    })
                  }}
                  className="rounded-lg px-3 py-1.5 text-sm ring-1 ring-[var(--ant-color-border)] disabled:opacity-40"
                >
                  下一页
                </button>
              </div>
            </div>
          ) : null}
        </section>
      </div>

      <aside className="flex flex-col gap-4">
        <div className="panel flex flex-col overflow-hidden rounded-xl" style={{ height: 280 }}>
          <div className="flex shrink-0 items-center justify-between border-b border-[var(--ant-color-border)] px-4 py-2.5">
            <h3 className="text-base font-semibold">我的班级</h3>
            <span className="muted-text text-xs">{myClasses.length} 个</span>
          </div>
          <div className="min-h-0 flex-1 overflow-y-auto">
            {myClasses.length ? (
              <div>
                {myClasses.map((item) => (
                  <Link
                    key={item.id}
                    to={`/classes/${item.id}`}
                    className="flex items-center gap-2 border-b border-[var(--ant-color-border)] px-4 py-2.5 last:border-b-0 hover:bg-[var(--ant-color-fill-secondary)]"
                  >
                    <BookOutlined className="shrink-0 text-[var(--ant-color-primary)]" />
                    <div className="min-w-0 flex-1">
                      <div className="truncate text-sm font-medium">{item.name}</div>
                      <div className="muted-text mt-0.5 truncate text-xs">
                        {item.member_count} 人 · {item.code}
                      </div>
                    </div>
                  </Link>
                ))}
              </div>
            ) : (
              <div className="flex h-full flex-col items-center justify-center gap-2 px-4">
                {isLogin() ? (
                  <Empty description="暂未加入班级" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                ) : (
                  <>
                    <Empty
                      description="登录后查看我的班级"
                      image={Empty.PRESENTED_IMAGE_SIMPLE}
                    />
                    <Link to={loginHref} className="text-sm text-[var(--ant-color-primary)]">
                      去登录
                    </Link>
                  </>
                )}
              </div>
            )}
          </div>
        </div>

        <div className="panel rounded-xl p-4">
          <div className="mb-2 text-base font-semibold">邀请码加入</div>
          <p className="muted-text mb-3 text-sm">向老师或管理员索取 8 位邀请码</p>
          <Button
            type="primary"
            block
            icon={<PlusOutlined />}
            onClick={() => requireLogin(() => setJoinOpen(true))}
          >
            输入邀请码
          </Button>
        </div>

        <div className="rounded-xl bg-[var(--ant-color-primary-bg)] px-4 py-4">
          <div className="text-sm font-semibold text-[var(--ant-color-primary)]">学习提示</div>
          <div className="mt-1 text-xs text-[var(--ant-color-primary-text)]">
            公开列表可随意浏览；加入班级后才能查看课程、任务与班级群聊。
          </div>
        </div>
      </aside>

      <Modal
        open={joinOpen}
        title="加入班级"
        okText="加入"
        cancelText="取消"
        confirmLoading={joinLoading}
        onOk={() => void handleJoin()}
        onCancel={() => setJoinOpen(false)}
      >
        <p className="mb-2 text-sm text-[var(--ant-color-text-secondary)]">
          请输入班级管理员提供的 8 位邀请码：
        </p>
        <Input
          placeholder="邀请码"
          maxLength={8}
          value={inviteCode}
          onChange={(e) => setInviteCode(e.target.value)}
          onPressEnter={() => void handleJoin()}
        />
      </Modal>
    </div>
  )
}
