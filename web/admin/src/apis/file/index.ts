import type { FileItem, PageData, PageQuery } from '@/types/api'
import { listFiles as listMockFiles } from '@mock/modules/file'

export async function listFiles(query: PageQuery): Promise<PageData<FileItem>> {
  return listMockFiles(query)
}
