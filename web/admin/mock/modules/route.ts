import { resources } from '@mock/data'
import type { ResourceTreeNode } from '@/types/route'

export async function getLoginMenu(): Promise<ResourceTreeNode[]> {
  return resources
}
