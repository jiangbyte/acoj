import { useAuthStore } from '@/store'

export function usePermission() {
  const authStore = useAuthStore()
  const hasPermission = (code: string) => authStore.permissions.includes(code)
  return { hasPermission }
}
