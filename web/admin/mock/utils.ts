import type { PageData, PageQuery } from '@/types/api'

export function withPage<T>(items: T[], query: PageQuery = {}): PageData<T> {
  const page = query.page ?? 1
  const pageSize = query.page_size ?? 20
  const keyword = query.keyword?.trim().toLowerCase()
  const filtered = keyword
    ? items.filter((item) => JSON.stringify(item).toLowerCase().includes(keyword))
    : items

  return {
    items: filtered.slice((page - 1) * pageSize, page * pageSize),
    total: filtered.length,
    page,
    page_size: pageSize,
  }
}

export function delay<T>(data: T, timeout = 120): Promise<T> {
  return new Promise((resolve) => {
    window.setTimeout(() => resolve(data), timeout)
  })
}
