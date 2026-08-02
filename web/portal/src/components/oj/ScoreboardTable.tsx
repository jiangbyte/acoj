import { Alert, Table, Typography } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import type { ContestScoreboard } from '@/api/contest'

type Props = {
  board: ContestScoreboard
}

interface RowData {
  key: string
  rank: number
  accountId: string
  score: number
  cumtime: number
  formatData: Record<string, unknown>
}

function formatCell(cell: unknown) {
  const data = (cell ?? {}) as {
    solved?: boolean
    display?: number | null
    penalty?: number
    points?: number
    time?: number
  }
  if (data.solved !== undefined) {
    // ICPC: display = 解决用时(分钟)，penalty = 错误次数
    if (data.solved) {
      return { text: `+${data.display ?? 0}`, color: 'green' }
    }
    if ((data.penalty ?? 0) > 0) {
      return { text: `-${data.penalty}`, color: 'red' }
    }
    return { text: '·', color: 'gray' }
  }
  // IOI / points 赛制
  const pts = Number(data.points ?? 0)
  return { text: pts > 0 ? String(pts) : '·', color: pts > 0 ? 'green' : 'gray' }
}

export function ScoreboardTable({ board }: Props) {
  const isIcpLike = board.format_name.toLowerCase().includes('icpc') || board.format_name.toLowerCase() === 'acm'

  const columns: ColumnsType<RowData> = [
    {
      title: '#',
      dataIndex: 'rank',
      width: 56,
      align: 'center',
      render: (value: number) => <span className="font-medium">{value}</span>,
    },
    {
      title: '选手',
      dataIndex: 'accountId',
      render: (value: string) => (
        <Typography.Text copyable={{ text: value }} className="font-mono text-xs">
          {value.length > 14 ? `${value.slice(0, 14)}…` : value}
        </Typography.Text>
      ),
    },
    {
      title: isIcpLike ? '通过数' : '得分',
      dataIndex: 'score',
      width: 96,
      align: 'center' as const,
      sorter: (a, b) => a.score - b.score,
      render: (value: number) => <span className="font-semibold">{value}</span>,
    },
    ...(isIcpLike
      ? [
          {
            title: '罚时(分钟)',
            dataIndex: 'cumtime',
            width: 100,
            align: 'center' as const,
            render: (value: number) => <span>{value}</span>,
          },
        ]
      : []),
    ...board.problems.map((problem) => ({
      title: (
        <span className="font-medium">
          {problem.label}
          <span className="ml-1 text-xs font-normal text-gray-400">{problem.points}</span>
        </span>
      ),
      align: 'center' as const,
      width: 80,
      render: (_: unknown, record: RowData) => {
        const cell = formatCell(record.formatData[problem.id])
        const color = cell.color === 'green' ? 'text-green-600' : cell.color === 'red' ? 'text-red-500' : 'text-gray-400'
        return <span className={color}>{cell.text}</span>
      },
    })),
  ]

  const dataSource: RowData[] = board.rows.map((row) => ({
    key: row.participation_id,
    rank: row.rank,
    accountId: row.account_id,
    score: row.score,
    cumtime: row.cumtime,
    formatData: row.format_data as Record<string, unknown>,
  }))

  return (
    <div>
      {board.is_frozen ? (
        <Alert
          className="mb-3"
          type="warning"
          showIcon
          message="榜单已封榜，封榜后的提交不再更新排名"
        />
      ) : null}
      <Table
        size="small"
        rowKey="key"
        columns={columns}
        dataSource={dataSource}
        pagination={false}
        scroll={{ x: 'max-content' }}
      />
    </div>
  )
}
