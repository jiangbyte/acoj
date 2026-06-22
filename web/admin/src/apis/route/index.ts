import type { RouteResource } from '@/types/route'
import { getRouteResources as getMockRouteResources } from '@mock/modules/route'

export async function getRouteResources(): Promise<RouteResource[]> {
  return getMockRouteResources()
}
