import type { AccountItem, DeptNode, PageData, PageQuery, RoleItem } from '@/types/api'
import type { ResourceTreeNode } from '@/types/route'
import {
  listAccounts as listMockAccounts,
  listDeptTree as listMockDeptTree,
  listResourceTree as listMockResourceTree,
  listRoles as listMockRoles,
} from '@mock/modules/iam'

export async function listAccounts(query: PageQuery): Promise<PageData<AccountItem>> {
  return listMockAccounts(query)
}

export async function listDeptTree(): Promise<DeptNode[]> {
  return listMockDeptTree()
}

export async function listRoles(query: PageQuery): Promise<PageData<RoleItem>> {
  return listMockRoles(query)
}

export async function listResourceTree(): Promise<ResourceTreeNode[]> {
  return listMockResourceTree()
}
