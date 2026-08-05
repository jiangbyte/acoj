import { useCallback, useEffect, useMemo, useState } from 'react'
import { Button, Drawer, Input, Space, Table, Tag, Typography, message } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { DeleteOutlined, PlusOutlined } from '@ant-design/icons'
import { problemApi } from '@/api'

export interface ProblemOption {
  id: string
  code: string
  name: string
  difficulty?: string
}

export type ProblemSelectorPageResult = {
  records: ProblemOption[]
  total: number
}

export type ProblemSelectorLoadPage = (params: {
  current: number
  size: number
  keyword?: string
}) => Promise<ProblemSelectorPageResult>

type Props = {
  open: boolean
  onClose: () => void
  mode?: 'single' | 'multiple'
  title?: string
  selected?: ProblemOption[]
  onSelect?: (problem: ProblemOption) => void
  onConfirm?: (problems: ProblemOption[]) => void
  /** 默认走门户题库分页；竞赛关联题可传入本地分页 */
  loadPage?: ProblemSelectorLoadPage
}

async function defaultLoadPage(params: {
  current: number
  size: number
  keyword?: string
}): Promise<ProblemSelectorPageResult> {
  const res = await problemApi.problemPage({
    current: params.current,
    size: params.size,
    keyword: params.keyword || undefined,
  })
  const records = (res.data?.records ?? []).map((item: any) => ({
    id: String(item.id),
    code: item.code || '',
    name: item.name || '',
    difficulty: item.difficulty,
  }))
  return { records, total: res.data?.total ?? 0 }
}

export function ProblemSelector({
  open,
  onClose,
  mode = 'single',
  title = '选择题目',
  selected = [],
  onSelect,
  onConfirm,
  loadPage = defaultLoadPage,
}: Props) {
  const [loading, setLoading] = useState(false)
  const [keyword, setKeyword] = useState('')
  const [searchKey, setSearchKey] = useState('')
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [total, setTotal] = useState(0)
  const [options, setOptions] = useState<ProblemOption[]>([])
  const [selectedData, setSelectedData] = useState<ProblemOption[]>([])

  const selectedIds = useMemo(() => new Set(selectedData.map((item) => item.id)), [selectedData])

  const fetchOptions = useCallback(async () => {
    setLoading(true)
    try {
      const res = await loadPage({
        current: page,
        size: pageSize,
        keyword: searchKey.trim() || undefined,
      })
      setOptions(res.records)
      setTotal(res.total)
    } catch {
      setOptions([])
      setTotal(0)
      message.error('加载题目失败')
    } finally {
      setLoading(false)
    }
  }, [loadPage, page, pageSize, searchKey])

  useEffect(() => {
    if (!open) return
    setSelectedData([...selected])
    setKeyword('')
    setSearchKey('')
    setPage(1)
    // 仅在打开时同步已选；避免父组件 selected 引用变化导致反复重置
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open])

  useEffect(() => {
    if (!open) return
    void fetchOptions()
  }, [open, fetchOptions])

  function doSearch() {
    setPage(1)
    setSearchKey(keyword)
  }

  function addRecord(record: ProblemOption) {
    if (selectedIds.has(record.id)) return
    setSelectedData((prev) => [...prev, record])
  }

  function addAllPage() {
    setSelectedData((prev) => {
      const map = new Map(prev.map((item) => [item.id, item]))
      options.forEach((item) => map.set(item.id, item))
      return Array.from(map.values())
    })
  }

  function delRecord(record: ProblemOption) {
    setSelectedData((prev) => prev.filter((item) => item.id !== record.id))
  }

  function handleSingleSelect(record: ProblemOption) {
    onSelect?.(record)
    onClose()
  }

  function handleConfirm() {
    onConfirm?.([...selectedData])
    onClose()
  }

  const singleColumns: ColumnsType<ProblemOption> = [
    { title: '编码', dataIndex: 'code', width: 120, ellipsis: true },
    { title: '标题', dataIndex: 'name', ellipsis: true },
    {
      title: '操作',
      key: 'action',
      width: 80,
      align: 'center',
      render: (_, row) => (
        <Button type="link" size="small" onClick={() => handleSingleSelect(row)}>
          选择
        </Button>
      ),
    },
  ]

  const leftColumns: ColumnsType<ProblemOption> = [
    {
      title: '操作',
      key: 'action',
      width: 56,
      align: 'center',
      render: (_, row) => (
        <Button
          type="link"
          size="small"
          icon={<PlusOutlined />}
          disabled={selectedIds.has(row.id)}
          onClick={() => addRecord(row)}
        />
      ),
    },
    { title: '编码', dataIndex: 'code', width: 110, ellipsis: true },
    { title: '标题', dataIndex: 'name', ellipsis: true },
  ]

  const rightColumns: ColumnsType<ProblemOption> = [
    {
      title: '操作',
      key: 'action',
      width: 56,
      align: 'center',
      render: (_, row) => (
        <Button type="link" danger size="small" icon={<DeleteOutlined />} onClick={() => delRecord(row)} />
      ),
    },
    { title: '编码', dataIndex: 'code', width: 100, ellipsis: true },
    { title: '标题', dataIndex: 'name', ellipsis: true },
  ]

  return (
    <Drawer
      title={title}
      open={open}
      onClose={onClose}
      width={mode === 'multiple' ? 960 : 720}
      destroyOnClose
      footer={
        <Space className="flex w-full justify-end">
          <Button onClick={onClose}>关闭</Button>
          {mode === 'multiple' ? (
            <Button type="primary" onClick={handleConfirm}>
              确认
            </Button>
          ) : null}
        </Space>
      }
    >
      {mode === 'single' ? (
        <Space direction="vertical" className="w-full" size="middle">
          <Space.Compact className="w-full">
            <Input
              allowClear
              placeholder="搜索编码 / 标题"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onPressEnter={doSearch}
            />
            <Button type="primary" onClick={doSearch}>
              搜索
            </Button>
          </Space.Compact>
          <Table
            rowKey="id"
            size="small"
            loading={loading}
            columns={singleColumns}
            dataSource={options}
            pagination={{
              current: page,
              pageSize,
              total,
              showSizeChanger: true,
              pageSizeOptions: [10, 20, 50, 100],
              onChange: (p, ps) => {
                setPage(p)
                setPageSize(ps)
              },
            }}
          />
        </Space>
      ) : (
        <div className="grid grid-cols-[1.4fr_1fr] gap-3">
          <Space direction="vertical" className="min-w-0" size="middle">
            <Space.Compact className="w-full">
              <Input
                allowClear
                placeholder="搜索编码 / 标题"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                onPressEnter={doSearch}
              />
              <Button type="primary" onClick={doSearch}>
                搜索
              </Button>
            </Space.Compact>
            <div className="flex items-center justify-between">
              <Typography.Text type="secondary">待选：{total}</Typography.Text>
              <Button size="small" onClick={addAllPage}>
                新增当前页
              </Button>
            </div>
            <Table
              rowKey="id"
              size="small"
              loading={loading}
              columns={leftColumns}
              dataSource={options}
              pagination={{
                current: page,
                pageSize,
                total,
                showSizeChanger: true,
                pageSizeOptions: [10, 20, 50, 100],
                onChange: (p, ps) => {
                  setPage(p)
                  setPageSize(ps)
                },
              }}
              scroll={{ y: 'calc(100vh - 280px)' }}
            />
          </Space>
          <Space direction="vertical" className="min-w-0" size="middle">
            <div className="flex items-center justify-between">
              <Typography.Text type="secondary">已选：{selectedData.length}</Typography.Text>
              <Button size="small" danger onClick={() => setSelectedData([])}>
                全部移除
              </Button>
            </div>
            <div className="flex flex-wrap gap-1">
              {selectedData.slice(0, 8).map((p) => (
                <Tag key={p.id} closable onClose={() => delRecord(p)}>
                  {p.code}
                </Tag>
              ))}
              {selectedData.length > 8 ? <Tag>+{selectedData.length - 8}</Tag> : null}
            </div>
            <Table
              rowKey="id"
              size="small"
              columns={rightColumns}
              dataSource={selectedData}
              pagination={false}
              scroll={{ y: 'calc(100vh - 280px)' }}
            />
          </Space>
        </div>
      )}
    </Drawer>
  )
}
