export function useChatActions() {
  const showRenameModal = ref(false)
  const showDeleteModal = ref(false)
  const renameTarget = ref<{ id: string; title: string } | null>(null)
  const deleteTarget = ref<{ id: string; title: string } | null>(null)

  const renameResolve = ref<((value: string | null) => void) | null>(null)
  const deleteResolve = ref<((value: boolean) => void) | null>(null)

  function renameChat(id: string, currentTitle: string): Promise<string | null> {
    return new Promise((resolve) => {
      renameTarget.value = { id, title: currentTitle }
      showRenameModal.value = true
      renameResolve.value = resolve
    })
  }

  function confirmRename(newTitle: string) {
    showRenameModal.value = false
    renameResolve.value?.(newTitle)
    renameResolve.value = null
    renameTarget.value = null
  }

  function cancelRename() {
    showRenameModal.value = false
    renameResolve.value?.(null)
    renameResolve.value = null
    renameTarget.value = null
  }

  function deleteChat(id: string, title: string): Promise<boolean> {
    return new Promise((resolve) => {
      deleteTarget.value = { id, title }
      showDeleteModal.value = true
      deleteResolve.value = resolve
    })
  }

  function confirmDelete() {
    showDeleteModal.value = false
    deleteResolve.value?.(true)
    deleteResolve.value = null
    deleteTarget.value = null
  }

  function cancelDelete() {
    showDeleteModal.value = false
    deleteResolve.value?.(false)
    deleteResolve.value = null
    deleteTarget.value = null
  }

  return {
    showRenameModal,
    showDeleteModal,
    renameTarget,
    deleteTarget,
    renameChat,
    confirmRename,
    cancelRename,
    deleteChat,
    confirmDelete,
    cancelDelete,
  }
}
