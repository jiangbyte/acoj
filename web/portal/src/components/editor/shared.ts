import { fileApi } from '@/api'
import { normalizeUploadedFile } from '@/utils'
import type { NormalizedUploadedFile } from '@/utils'

export interface EditorUploadedFile extends NormalizedUploadedFile {}

export async function uploadEditorFile(file: File): Promise<EditorUploadedFile> {
  const response = await fileApi.upload(file)
  return normalizeUploadedFile(response.data, file)
}

export function toCssSize(value: string | number) {
  return typeof value === 'number' ? `${value}px` : value
}
