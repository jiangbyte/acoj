import { routeResources } from '@mock/data'
import type { RouteResource } from '@/types/route'

export async function getRouteResources(): Promise<RouteResource[]> {
  return routeResources
}
