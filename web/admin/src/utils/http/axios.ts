import { createAxiosInstance as createSharedAxiosInstance } from '@hei/shared'
import type { BackendOptions, HttpInstanceOptions, RequestErrorResult } from '@hei/shared'
import { message } from 'ant-design-vue'

import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { useRouteStore } from '@/stores/route'
import { useUserStore } from '@/stores/user'
import { t } from '@/i18n'

export type { BackendOptions, HttpInstanceOptions }

function handleAuthExpired() {
  const auth = useAuthStore()
  if (!auth.token) {
    return
  }

  auth.clearSession()
  useUserStore().clear()
  useRouteStore().reset_route_store()
  useAppStore().clearAllTabs()
  message.warning(t('http.unauthorizedRelogin'))
}

function showError(error: RequestErrorResult) {
  message.error(error.message)
}

function showNetworkError(error: RequestErrorResult) {
  message.warning(error.message)
}

export function createAxiosInstance(options: HttpInstanceOptions, backendOptions?: BackendOptions) {
  return createSharedAxiosInstance(
    {
      ...options,
      hooks: {
        ...options.hooks,
        getToken: () => useAuthStore().token,
        onAuthExpired: handleAuthExpired,
        onError: showError,
        onNetworkError: showNetworkError,
      },
    },
    backendOptions,
  )
}
