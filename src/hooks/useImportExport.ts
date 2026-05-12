import { type Ref, ref } from 'vue'
import { message } from 'ant-design-vue'
import { downloadBlob } from '@/utils'

interface UseImportExportOptions {
  exportApi: (params: any) => Promise<Blob>
  templateApi: () => Promise<Blob>
  importApi: (file: File) => Promise<any>
  fileName: string
  templateName: string
  onSuccess?: () => void
  importModalRef?: Ref<any>
}

export function useImportExport(options: UseImportExportOptions) {
  const importOpen = ref(false)
  const exportOpen = ref(false)
  const templateLoading = ref(false)
  const modalRef = options.importModalRef || ref()

  async function handleDownloadTemplate() {
    templateLoading.value = true
    try {
      const blob = await options.templateApi()
      downloadBlob(blob, `${options.templateName}.xlsx`)
    } catch {
      message.error('下载模板失败')
    } finally {
      templateLoading.value = false
    }
  }

  async function handleExportWithParams(params: any) {
    try {
      const blob = await options.exportApi(params)
      downloadBlob(blob, `${options.fileName}_${new Date().toLocaleDateString()}.xlsx`)
      message.success('导出成功')
      exportOpen.value = false
    } catch {
      message.error('导出失败')
    }
  }

  async function handleImport(file: File) {
    try {
      const { success, data } = await options.importApi(file)
      if (success && data) {
        modalRef.value?.setResult({ success: true, message: data.message || '导入成功' })
        message.success('导入成功')
        options.onSuccess?.()
      }
    } catch {
      modalRef.value?.setResult({ success: false, message: '导入失败，请检查文件格式' })
    }
  }

  return {
    importOpen,
    exportOpen,
    templateLoading,
    handleDownloadTemplate,
    handleExportWithParams,
    handleImport,
  }
}
