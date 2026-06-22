import type { ResourceTreeNode } from '@/types/route'
import { getLoginMenu as getMockLoginMenu } from '@mock/modules/route'

export async function getLoginMenu(): Promise<ResourceTreeNode[]> {
  return getMockLoginMenu()
}
