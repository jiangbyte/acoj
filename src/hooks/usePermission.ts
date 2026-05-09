import { useAuthStore } from '@/store/auth'

export function usePermission() {
  const authStore = useAuthStore()
  const hasPermission = (code: string) => authStore.permissions.includes(code)
  return { hasPermission }
}
