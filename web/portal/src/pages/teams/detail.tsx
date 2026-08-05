import { useEffect, useMemo, useState } from 'react'
import {
  Avatar,
  Button,
  Empty,
  Form,
  Input,
  InputNumber,
  Modal,
  Select,
  Spin,
  Switch,
  Table,
  Tag,
  Typography,
  message,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { CopyOutlined, MessageOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import {
  teamDetail,
  teamDissolve,
  teamInviteRefresh,
  teamLeave,
  teamMemberAdd,
  teamMemberRemove,
  teamMembers,
  teamUpdate,
  teamUserSearch,
} from '@/api/team'
import type { PortalTeamBrief, PortalTeamMember, PortalTeamUserSearchItem } from '@/api/team'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/time'

const statusLabel: Record<string, string> = {
  ENABLED: '正常',
  DISABLED: '已停用',
  DISSOLVED: '已解散',
}

const roleLabel: Record<string, string> = {
  OWNER: '组长',
  ADMIN: '管理员',
  MEMBER: '成员',
}

export function TeamDetailPage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const isLogin = useAuthStore((s) => s.isLogin)
  const myId = useAuthStore((s) => s.userInfo?.accountId ?? '')
  const loginHref = `/auth/login?redirect=${encodeURIComponent(`${location.pathname}${location.search}`)}`
  const [loading, setLoading] = useState(true)
  const [team, setTeam] = useState<PortalTeamBrief | null>(null)
  const [members, setMembers] = useState<PortalTeamMember[]>([])
  const [actionLoading, setActionLoading] = useState(false)
  const [settingsSaving, setSettingsSaving] = useState(false)
  const [addOpen, setAddOpen] = useState(false)
  const [searchLoading, setSearchLoading] = useState(false)
  const [searchOptions, setSearchOptions] = useState<PortalTeamUserSearchItem[]>([])
  const [selectedAccountIds, setSelectedAccountIds] = useState<string[]>([])
  const [settingsForm] = Form.useForm()

  async function load() {
    setLoading(true)
    try {
      const detailRes = await teamDetail(id)
      setTeam(detailRes.data)
      if (detailRes.data?.is_member) {
        const membersRes = await teamMembers(id)
        setMembers(membersRes.data ?? [])
      } else {
        setMembers([])
      }
      if (detailRes.data) {
        settingsForm.setFieldsValue({
          name: detailRes.data.name,
          description: detailRes.data.description ?? '',
          max_members: detailRes.data.max_members,
          visibility: detailRes.data.visibility === 'PUBLIC',
        })
      }
    } catch {
      setTeam(null)
      setMembers([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  const isOwner = team?.owner_id === myId
  const canManage = Boolean(isOwner && team?.status === 'ENABLED')
  const isIndependent = team?.scope === 'INDEPENDENT'

  const memberColumns: ColumnsType<PortalTeamMember> = useMemo(
    () => [
      {
        title: '账号 ID',
        dataIndex: 'account_id',
        render: (accountId: string) => (
          <Link to={`/profile?account_id=${accountId}`} className="font-mono text-xs">
            {accountId}
          </Link>
        ),
      },
      {
        title: '角色',
        dataIndex: 'role',
        width: 100,
        render: (role: string) => roleLabel[role] ?? role,
      },
      {
        title: '加入时间',
        dataIndex: 'joined_at',
        width: 180,
        render: (value: string) => formatDateTime(value),
      },
      ...(canManage
        ? [
            {
              title: '操作',
              width: 100,
              render: (_: unknown, record: PortalTeamMember) =>
                record.role === 'OWNER' ? (
                  '-'
                ) : (
                  <Button
                    type="link"
                    danger
                    size="small"
                    onClick={() => confirmRemove(record.account_id)}
                  >
                    移除
                  </Button>
                ),
            } as ColumnsType<PortalTeamMember>[number],
          ]
        : []),
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [canManage],
  )

  function copyInviteCode() {
    if (!team?.invite_code) return
    void navigator.clipboard.writeText(team.invite_code).then(() => {
      message.success('邀请码已复制')
    })
  }

  function confirmLeave() {
    Modal.confirm({
      title: '退出小组',
      content: '确定要退出该小组吗？',
      okText: '退出',
      cancelText: '取消',
      okButtonProps: { danger: true },
      onOk: async () => {
        setActionLoading(true)
        try {
          await teamLeave(id)
          message.success('已退出小组')
          navigate('/teams')
        } finally {
          setActionLoading(false)
        }
      },
    })
  }

  function confirmDissolve() {
    Modal.confirm({
      title: '解散小组',
      content: '解散后小组将无法恢复，确定继续吗？',
      okText: '解散',
      cancelText: '取消',
      okButtonProps: { danger: true },
      onOk: async () => {
        setActionLoading(true)
        try {
          await teamDissolve(id)
          message.success('小组已解散')
          navigate('/teams')
        } finally {
          setActionLoading(false)
        }
      },
    })
  }

  function confirmRefreshInvite() {
    Modal.confirm({
      title: '刷新邀请码',
      content: '旧邀请码将立即失效，确定刷新吗？',
      okText: '刷新',
      cancelText: '取消',
      onOk: async () => {
        setActionLoading(true)
        try {
          const res = await teamInviteRefresh(id)
          message.success('邀请码已更新')
          setTeam((prev) => (prev ? { ...prev, invite_code: res.data.invite_code } : prev))
        } finally {
          setActionLoading(false)
        }
      },
    })
  }

  function confirmRemove(accountId: string) {
    Modal.confirm({
      title: '移除成员',
      content: '确定将该成员移出小组吗？',
      okText: '移除',
      cancelText: '取消',
      okButtonProps: { danger: true },
      onOk: async () => {
        await teamMemberRemove({ team_id: id, account_id: accountId })
        message.success('已移除')
        await load()
      },
    })
  }

  async function saveSettings() {
    const values = await settingsForm.validateFields()
    setSettingsSaving(true)
    try {
      await teamUpdate({
        id,
        name: values.name,
        description: values.description || null,
        max_members: values.max_members,
        ...(isIndependent
          ? { visibility: values.visibility ? 'PUBLIC' : 'PRIVATE' }
          : {}),
      })
      message.success('设置已保存')
      await load()
    } finally {
      setSettingsSaving(false)
    }
  }

  async function onSearchUsers(keyword: string) {
    const q = keyword.trim()
    if (!q) {
      setSearchOptions([])
      return
    }
    setSearchLoading(true)
    try {
      const res = await teamUserSearch(q)
      const existing = new Set(members.map((m) => m.account_id))
      setSearchOptions((res.data ?? []).filter((u) => !existing.has(u.account_id)))
    } finally {
      setSearchLoading(false)
    }
  }

  async function submitAddMembers() {
    if (!selectedAccountIds.length) {
      message.warning('请选择要添加的成员')
      return
    }
    await teamMemberAdd({ team_id: id, account_ids: selectedAccountIds })
    message.success('已添加成员')
    setAddOpen(false)
    setSelectedAccountIds([])
    setSearchOptions([])
    await load()
  }

  if (loading && !team) {
    return (
      <div className="page-shell flex justify-center py-20">
        <Spin size="large" />
      </div>
    )
  }

  if (!team) {
    return (
      <div className="page-shell w-full">
        <div className="panel rounded-xl py-16">
          <Empty description="小组不存在或不可用" />
        </div>
      </div>
    )
  }

  if (!team.is_member) {
    return (
      <div className="page-shell w-full">
        <header className="panel mb-5 rounded-xl p-6">
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-2xl font-semibold">{team.name}</h1>
              <Tag color={team.status === 'ENABLED' ? 'success' : 'default'}>
                {statusLabel[team.status] ?? team.status}
              </Tag>
              {isIndependent ? (
                <Tag color={team.visibility === 'PUBLIC' ? 'processing' : 'default'}>
                  {team.visibility === 'PUBLIC' ? '公开' : '私有'}
                </Tag>
              ) : null}
            </div>
            <Typography.Text type="secondary" className="mt-2 block">
              {team.description || '暂无简介'}
            </Typography.Text>
            <div className="muted-text mt-3 text-sm">
              {team.member_count}/{team.max_members} 人
            </div>
          </div>
        </header>
        <div className="panel flex flex-col items-center justify-center gap-3 rounded-xl py-12">
          <Empty
            description={
              isLogin()
                ? '你还不是该小组成员，请使用邀请码加入'
                : '登录并用邀请码加入后，可查看成员与群聊'
            }
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          />
          {!isLogin() ? (
            <Link to={loginHref} className="text-sm text-[var(--ant-color-primary)]">
              去登录
            </Link>
          ) : (
            <Link to="/teams" className="text-sm text-[var(--ant-color-primary)]">
              返回小组列表加入
            </Link>
          )}
        </div>
      </div>
    )
  }

  return (
    <div className="page-shell w-full">
      <header className="panel mb-5 rounded-xl p-6">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-2xl font-semibold">{team.name}</h1>
              <Tag color={team.status === 'ENABLED' ? 'success' : 'default'}>
                {statusLabel[team.status] ?? team.status}
              </Tag>
              {isIndependent ? (
                <Tag color={team.visibility === 'PUBLIC' ? 'processing' : 'default'}>
                  {team.visibility === 'PUBLIC' ? '公开' : '私有'}
                </Tag>
              ) : (
                <Tag>课内小组</Tag>
              )}
            </div>
            <Typography.Text type="secondary" className="mt-2 block">
              {team.description || '暂无简介'}
            </Typography.Text>
            <div className="muted-text mt-3 text-sm">
              {team.member_count}/{team.max_members} 人
            </div>
          </div>
          <div className="flex flex-wrap gap-2">
            {team.conversation_id ? (
              <Link to={`/messages?conversation=${team.conversation_id}`}>
                <Button type="primary" icon={<MessageOutlined />}>
                  进入群聊
                </Button>
              </Link>
            ) : null}
            {isOwner ? (
              <Button danger loading={actionLoading} onClick={confirmDissolve}>
                解散小组
              </Button>
            ) : (
              <Button danger loading={actionLoading} onClick={confirmLeave}>
                退出小组
              </Button>
            )}
          </div>
        </div>
      </header>

      {canManage ? (
        <div className="panel mb-5 rounded-xl p-6">
          <div className="mb-4 text-base font-semibold">小组设置</div>
          <Form form={settingsForm} layout="vertical" className="max-w-xl">
            <Form.Item
              name="name"
              label="名称"
              rules={[{ required: true, message: '请输入名称' }]}
            >
              <Input maxLength={200} />
            </Form.Item>
            <Form.Item name="description" label="简介">
              <Input.TextArea rows={3} maxLength={1000} />
            </Form.Item>
            <Form.Item
              name="max_members"
              label="人数上限"
              rules={[{ required: true, message: '请输入人数上限' }]}
            >
              <InputNumber min={Math.max(2, team.member_count)} max={500} className="w-full" />
            </Form.Item>
            {isIndependent ? (
              <Form.Item
                name="visibility"
                label="公开小组"
                valuePropName="checked"
                extra="公开后会出现在小组列表，他人可浏览；加入仍需邀请码"
              >
                <Switch checkedChildren="公开" unCheckedChildren="私有" />
              </Form.Item>
            ) : null}
            <Button type="primary" loading={settingsSaving} onClick={() => void saveSettings()}>
              保存设置
            </Button>
          </Form>
        </div>
      ) : null}

      <div className="panel mb-5 rounded-xl p-6">
        <div className="mb-2 text-base font-semibold">邀请码</div>
        <div className="flex flex-wrap items-center gap-3">
          <span className="font-mono text-lg tracking-widest">{team.invite_code ?? '-'}</span>
          {team.invite_code ? (
            <Button icon={<CopyOutlined />} onClick={copyInviteCode}>
              复制
            </Button>
          ) : null}
          {canManage ? (
            <Button
              icon={<ReloadOutlined />}
              loading={actionLoading}
              onClick={confirmRefreshInvite}
            >
              刷新
            </Button>
          ) : null}
        </div>
        <Typography.Text type="secondary" className="mt-2 block text-sm">
          分享邀请码给其他同学加入小组（8 位）
          {canManage ? '；刷新后旧码立即失效' : ''}
        </Typography.Text>
      </div>

      <div className="panel overflow-hidden rounded-xl p-4">
        <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
          <div className="text-base font-semibold">成员 ({members.length})</div>
          {canManage ? (
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => {
                setSelectedAccountIds([])
                setSearchOptions([])
                setAddOpen(true)
              }}
            >
              添加成员
            </Button>
          ) : null}
        </div>
        {members.length ? (
          <Table rowKey="id" columns={memberColumns} dataSource={members} pagination={false} />
        ) : (
          <Empty description="暂无成员" />
        )}
      </div>

      <Modal
        title="添加成员"
        open={addOpen}
        onCancel={() => setAddOpen(false)}
        onOk={() => void submitAddMembers()}
        okText="添加"
        cancelText="取消"
        destroyOnClose
      >
        <Select
          mode="multiple"
          showSearch
          filterOption={false}
          placeholder="搜索用户名 / 昵称"
          className="w-full"
          loading={searchLoading}
          value={selectedAccountIds}
          onSearch={(v) => void onSearchUsers(v)}
          onChange={setSelectedAccountIds}
          options={searchOptions.map((u) => ({
            value: u.account_id,
            label: (
              <div className="flex items-center gap-2">
                <Avatar size="small" src={u.avatar ?? undefined}>
                  {(u.nickname || u.username || '?').slice(0, 1)}
                </Avatar>
                <span>{u.nickname || u.username || u.account_id}</span>
                {u.username ? (
                  <span className="muted-text text-xs">@{u.username}</span>
                ) : null}
              </div>
            ),
          }))}
          notFoundContent={searchLoading ? <Spin size="small" /> : '输入关键词搜索'}
        />
      </Modal>
    </div>
  )
}
