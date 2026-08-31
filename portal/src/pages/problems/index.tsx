/** Author: Charlie */

import { useEffect, useMemo, useState } from 'react'
import {
  CheckCircleFilled,
  CloseCircleOutlined,
  DownOutlined,
  UpOutlined,
} from '@ant-design/icons'
import {
  Button,
  Empty,
  Flex,
  Input,
  Pagination,
  Select,
  Skeleton,
  Table,
  Tag,
  Typography,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { Link } from 'react-router-dom'
import { ojProblemApi, ojTagApi, type OjTagOption } from '@/api'
import { useAuthStore } from '@/stores/auth'
import {
  difficultyColor,
  difficultyLabel,
  formatPassRate,
  readPageMeta,
} from '@/utils'

const COLLAPSED_TAG_LIMIT = 16

const difficultyOptions = [
  { label: '全部难度', value: '' },
  { label: '简单', value: 'EASY' },
  { label: '中等', value: 'MEDIUM' },
  { label: '困难', value: 'HARD' },
]

const myStatusOptions = [
  { label: '全部状态', value: '' },
  { label: '已通过', value: 'ACCEPTED' },
  { label: '未通过', value: 'ATTEMPTED' },
  { label: '未尝试', value: 'UNTRIED' },
]

function StatusIcon({ status }: { status?: string | null }) {
  if (status === 'ACCEPTED') {
    return <CheckCircleFilled style={{ color: 'var(--ant-color-success)' }} />
  }
  if (status === 'ATTEMPTED') {
    return <CloseCircleOutlined style={{ color: 'var(--ant-color-error)' }} />
  }
  return <span className="muted-text">—</span>
}

export function ProblemListPage() {
  const loggedIn = useAuthStore((s) => s.isLogin())
  const [loading, setLoading] = useState(true)
  const [records, setRecords] = useState<any[]>([])
  const [total, setTotal] = useState(0)
  const [current, setCurrent] = useState(1)
  const [size, setSize] = useState(20)
  const [keyword, setKeyword] = useState('')
  const [keywordDraft, setKeywordDraft] = useState('')
  const [difficulty, setDifficulty] = useState('')
  const [tagId, setTagId] = useState('')
  const [myStatus, setMyStatus] = useState('')
  const [tags, setTags] = useState<OjTagOption[]>([])
  const [publishedCount, setPublishedCount] = useState(0)
  const [acceptedCount, setAcceptedCount] = useState<number | null>(null)
  const [tagsExpanded, setTagsExpanded] = useState(false)

  const query = useMemo(
    () => ({
      current,
      size,
      keyword: keyword || undefined,
      difficulty: difficulty || undefined,
      tag_id: tagId || undefined,
      my_status: loggedIn && myStatus ? myStatus : undefined,
    }),
    [current, size, keyword, difficulty, tagId, myStatus, loggedIn],
  )

  useEffect(() => {
    let mounted = true
    async function loadTags() {
      try {
        const res = await ojTagApi.options()
        if (!mounted) return
        const data = res.data
        setTags(Array.isArray(data?.tags) ? data.tags : [])
        setPublishedCount(Number(data?.published_count ?? 0))
        setAcceptedCount(
          data?.accepted_count === null || data?.accepted_count === undefined
            ? null
            : Number(data.accepted_count),
        )
      } catch {
        if (!mounted) return
        setTags([])
        setPublishedCount(0)
        setAcceptedCount(null)
      }
    }
    void loadTags()
    return () => {
      mounted = false
    }
  }, [loggedIn])

  useEffect(() => {
    let mounted = true
    async function load() {
      setLoading(true)
      try {
        const res = await ojProblemApi.page(query)
        if (!mounted) return
        setRecords(res.data?.records ?? [])
        setTotal(readPageMeta(res.data).total)
      } catch {
        if (!mounted) return
        setRecords([])
        setTotal(0)
      } finally {
        if (mounted) setLoading(false)
      }
    }
    void load()
    return () => {
      mounted = false
    }
  }, [query])

  const visibleTags = tagsExpanded ? tags : tags.slice(0, COLLAPSED_TAG_LIMIT)
  const showTagsToggle = tags.length > COLLAPSED_TAG_LIMIT

  function applySearch(value?: string) {
    setKeyword((value ?? keywordDraft).trim())
    setCurrent(1)
  }

  function selectTag(nextTagId: string) {
    setTagId((prev) => (prev === nextTagId ? '' : nextTagId))
    setCurrent(1)
  }

  const columns: ColumnsType<any> = [
    {
      title: '',
      dataIndex: 'my_status',
      key: 'status',
      width: 48,
      align: 'center',
      render: (status: string) => <StatusIcon status={status} />,
    },
    {
      title: '题目',
      key: 'title',
      ellipsis: true,
      render: (_, row) => (
        <Link to={`/problems/${row.id}`} className="font-medium">
          {row.problem_key ? `${row.problem_key}. ` : ''}
          {row.title}
        </Link>
      ),
    },
    {
      title: '通过率',
      key: 'pass_rate',
      width: 100,
      align: 'right',
      render: (_, row) => formatPassRate(row.accept_count, row.submit_count),
    },
    {
      title: '难度',
      dataIndex: 'difficulty',
      key: 'difficulty',
      width: 88,
      align: 'right',
      render: (value: string) => (
        <Tag color={difficultyColor(value)} className="m-0">
          {difficultyLabel(value)}
        </Tag>
      ),
    },
  ]

  return (
    <div className="page-shell w-full">
      <div className="mb-4">
        <h1 className="text-xl font-semibold">题库</h1>
        <p className="muted-text mt-1 text-sm">浏览已发布题目并进入做题工作台</p>
      </div>

      {/* 布局：标签云 → 搜索/筛选 → 列表（无题库分类，不做难度 Segmented） */}
      <section className="panel mb-4 px-4 py-3">
        {tags.length ? (
          <Flex wrap="wrap" gap={8} align="center">
            {visibleTags.map((tag) => (
              <Tag
                key={tag.id}
                color={tagId === tag.id ? 'processing' : undefined}
                className="m-0 cursor-pointer"
                onClick={() => selectTag(tag.id)}
              >
                {tag.name}
                <Typography.Text type="secondary" className="ml-1 text-xs">
                  {Number(tag.problem_count ?? 0)}
                </Typography.Text>
              </Tag>
            ))}
            {showTagsToggle ? (
              <Button
                type="link"
                size="small"
                className="px-0"
                icon={tagsExpanded ? <UpOutlined /> : <DownOutlined />}
                iconPosition="end"
                onClick={() => setTagsExpanded((v) => !v)}
              >
                {tagsExpanded ? '收起' : '展开'}
              </Button>
            ) : null}
          </Flex>
        ) : (
          <Typography.Text type="secondary">暂无标签</Typography.Text>
        )}
      </section>

      <section className="panel mb-4 px-4 py-3">
        <Flex wrap="wrap" gap={12} align="center">
          <Input.Search
            allowClear
            placeholder="搜索题目"
            className="w-full max-w-md"
            value={keywordDraft}
            onChange={(e) => setKeywordDraft(e.target.value)}
            onSearch={applySearch}
            onClear={() => {
              setKeywordDraft('')
              setKeyword('')
              setCurrent(1)
            }}
          />
          <Select
            className="w-32"
            options={difficultyOptions}
            value={difficulty}
            onChange={(value) => {
              setDifficulty(value)
              setCurrent(1)
            }}
          />
          {loggedIn ? (
            <Select
              className="w-32"
              options={myStatusOptions}
              value={myStatus}
              onChange={(value) => {
                setMyStatus(value)
                setCurrent(1)
              }}
            />
          ) : null}
          <Typography.Text type="secondary" className="ml-auto text-sm">
            {loggedIn && acceptedCount !== null
              ? `${acceptedCount}/${publishedCount} 已解答`
              : `共 ${publishedCount} 题`}
          </Typography.Text>
        </Flex>
      </section>

      <section className="panel overflow-hidden">
        <Skeleton active loading={loading} paragraph={{ rows: 8 }}>
          {records.length ? (
            <Table
              rowKey="id"
              size="middle"
              pagination={false}
              columns={columns}
              dataSource={records}
              showHeader
            />
          ) : !loading ? (
            <div className="py-16">
              <Empty description="暂无题目" />
            </div>
          ) : null}
        </Skeleton>
      </section>

      {total > 0 ? (
        <div className="mt-4 flex justify-end">
          <Pagination
            current={current}
            pageSize={size}
            total={total}
            showSizeChanger
            showTotal={(t) => `共 ${t} 条`}
            onChange={(page, pageSize) => {
              setCurrent(page)
              setSize(pageSize)
            }}
          />
        </div>
      ) : null}
    </div>
  )
}
