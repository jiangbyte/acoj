import type {
  AccountItem,
  DeptNode,
  PageData,
  PageQuery,
  RoleItem,
} from '@/types/api'
import type { ResourceTreeNode } from '@/types/route'
import { delay, withPage } from '@mock/utils'
import { accounts, depts, resources, roles } from '@mock/data'

export async function listAccounts(query: PageQuery): Promise<PageData<AccountItem>> {
  return delay(withPage(accounts, query))
}

export async function listDeptTree(): Promise<DeptNode[]> {
  return delay(depts)
}

export async function listRoles(query: PageQuery): Promise<PageData<RoleItem>> {
  return delay(withPage(roles, query))
}

export async function listResourceTree(): Promise<ResourceTreeNode[]> {
  return delay(resources)
}
