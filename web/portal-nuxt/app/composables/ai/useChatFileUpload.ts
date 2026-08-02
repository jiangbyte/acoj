export interface FileWithStatus {
  id: string
  name: string
  type: string
  url: string
  status: 'pending' | 'uploading' | 'uploaded' | 'error'
}

export function useChatFileUpload() {
  const files = ref<FileWithStatus[]>([])

  function addFile(file: File) {
    const id = crypto.randomUUID()
    const entry: FileWithStatus = {
      id,
      name: file.name,
      type: file.type,
      url: URL.createObjectURL(file),
      status: 'pending',
    }
    files.value = [...files.value, entry]

    // 模拟上传
    entry.status = 'uploading'
    setTimeout(() => {
      entry.status = 'uploaded'
      files.value = [...files.value]
    }, 800)
  }

  function removeFile(id: string) {
    const entry = files.value.find((f) => f.id === id)
    if (entry?.url.startsWith('blob:')) {
      URL.revokeObjectURL(entry.url)
    }
    files.value = files.value.filter((f) => f.id !== id)
  }

  function clearFiles() {
    for (const f of files.value) {
      if (f.url.startsWith('blob:')) {
        URL.revokeObjectURL(f.url)
      }
    }
    files.value = []
  }

  function handleFileInput(event: Event) {
    const input = event.target as HTMLInputElement
    if (input.files) {
      for (const file of Array.from(input.files)) {
        addFile(file)
      }
      input.value = ''
    }
  }

  const isDragging = ref(false)

  return {
    files,
    isDragging,
    addFile,
    removeFile,
    clearFiles,
    handleFileInput,
  }
}
