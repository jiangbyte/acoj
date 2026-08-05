import { useEffect, useMemo, useState } from 'react'
import { Empty, Spin, Table, Tag, Typography } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { Link, useLocation, useParams } from 'react-router-dom'
import { courseApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/time'

const progressLabel: Record<string, string> = {
  NOT_STARTED: '未开始',
  IN_PROGRESS: '进行中',
  DONE: '已完成',
}

const modeLabel: Record<string, string> = {
  REALTIME: '限时任务',
  ASYNC: '异步任务',
}

export function CourseTaskPage() {
  const { id: courseId = '', taskId = '' } = useParams()
  const location = useLocation()
  const isLogin = useAuthStore((s) => s.isLogin)
  const loggedIn = isLogin()
  const loginHref = `/auth/login?redirect=${encodeURIComponent(`${location.pathname}${location.search}`)}`
  const [loading, setLoading] = useState(false)
  const [task, setTask] = useState<any>(null)
  const [canSubmit, setCanSubmit] = useState<boolean | null>(null)

  useEffect(() => {
    void (async () => {
      setLoading(true)
      try {
        const res = await courseApi.courseTaskDetail(taskId)
        setTask(res.data)
        if (isLogin()) {
          try {
            await courseApi.courseTaskCanSubmit(taskId)
            setCanSubmit(true)
          } catch {
            setCanSubmit(false)
          }
        } else {
          setCanSubmit(null)
        }
      } catch {
        setTask(null)
        setCanSubmit(null)
      } finally {
        setLoading(false)
      }
    })()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [taskId, loggedIn])

  const problemColumns: ColumnsType<any> = useMemo(
    () => [
      {
        title: '#',
        dataIndex: 'sort',
        width: 60,
        render: (sort: number) => <span className="font-medium">{sort + 1}</span>,
      },
      {
        title: '题目 ID',
        dataIndex: 'problem_id',
        render: (problemId: string) =>
          loggedIn ? (
            <Link
              to={`/problems/${problemId}?taskId=${taskId}`}
              className="font-mono text-sm text-[var(--ant-color-primary)]"
            >
              {problemId}
            </Link>
          ) : (
            <span className="font-mono text-sm">{problemId}</span>
          ),
      },
      {
        title: '分值',
        dataIndex: 'score',
        width: 90,
        align: 'right',
        render: (score: number | null) => (score != null ? score : '-'),
      },
      {
        title: '操作',
        width: 100,
        render: (_: unknown, record) =>
          loggedIn ? (
            <Link
              to={`/problems/${record.problem_id}?taskId=${taskId}`}
              className="text-sm text-[var(--ant-color-primary)]"
            >
              去做题
            </Link>
          ) : (
            <Link to={loginHref} className="text-sm text-[var(--ant-color-primary)]">
              登录做题
            </Link>
          ),
      },
    ],
    [taskId, loggedIn, loginHref],
  )

  if (loading && !task) {
    return (
      <div className="page-shell flex w-full justify-center py-20">
        <Spin size="large" />
      </div>
    )
  }

  if (!task) {
    return (
      <div className="page-shell w-full">
        <div className="panel flex flex-col items-center justify-center gap-2 rounded-xl py-16">
          <Empty description="任务不存在或无权访问" image={Empty.PRESENTED_IMAGE_SIMPLE} />
          <Link to={`/courses/${courseId}`} className="text-sm text-[var(--ant-color-primary)]">
            返回课程
          </Link>
        </div>
      </div>
    )
  }

  const progress = task.my_progress

  return (
    <div className="page-shell w-full">
      <header className="panel mb-5 rounded-xl p-6">
        <div className="mb-2">
          <Link to={`/courses/${courseId}`} className="text-sm text-[var(--ant-color-primary)]">
            ← 返回课程
          </Link>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <h1 className="text-2xl font-semibold">{task.title}</h1>
          <Tag>{modeLabel[task.mode] ?? task.mode}</Tag>
          {canSubmit === true ? <Tag color="success">可提交</Tag> : null}
          {canSubmit === false ? <Tag color="default">暂不可提交</Tag> : null}
          {!loggedIn ? <Tag color="warning">浏览模式</Tag> : null}
        </div>
        {task.description ? (
          <Typography.Paragraph type="secondary" className="mt-3 mb-0">
            {task.description}
          </Typography.Paragraph>
        ) : null}
        {!loggedIn ? (
          <p className="mt-3 text-sm">
            可浏览题目列表；提交作业请先
            <Link to={loginHref} className="mx-1 text-[var(--ant-color-primary)]">
              登录
            </Link>
          </p>
        ) : null}
        <div className="muted-text mt-4 flex flex-wrap gap-x-6 gap-y-1 text-sm">
          {task.open_at ? <span>开始：{formatDateTime(task.open_at)}</span> : null}
          {task.close_at ? <span>结束：{formatDateTime(task.close_at)}</span> : null}
          {task.due_at ? <span>截止：{formatDateTime(task.due_at)}</span> : null}
        </div>
        {progress ? (
          <div className="mt-4 rounded-xl bg-[var(--ant-color-fill-quaternary)] px-4 py-3 text-sm">
            我的进度：{progress.solved_count}/{progress.total_count} ·{' '}
            {progressLabel[progress.status] ?? progress.status}
            {progress.finished_at ? (
              <span className="muted-text ml-2">完成于 {formatDateTime(progress.finished_at)}</span>
            ) : null}
          </div>
        ) : null}
      </header>

      <div className="panel overflow-hidden rounded-xl p-4">
        <div className="mb-3 text-base font-semibold">
          题目列表{task.problems.length ? ` (${task.problems.length})` : ''}
        </div>
        {task.problems.length ? (
          <Table
            rowKey="id"
            columns={problemColumns}
            dataSource={task.problems}
            pagination={false}
          />
        ) : (
          <Empty description="暂无题目" />
        )}
      </div>
    </div>
  )
}
