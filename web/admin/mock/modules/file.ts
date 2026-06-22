import type { FileItem, PageData, PageQuery } from '@/types/api'
import { delay, withPage } from '@mock/utils'
import { files } from '@mock/data'

export async function listFiles(query: PageQuery): Promise<PageData<FileItem>> {
  return delay(withPage(files, query))
}
