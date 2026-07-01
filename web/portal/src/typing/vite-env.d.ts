/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_APP_TITLE: string
  readonly VITE_COPYRIGHT_INFO: string
  readonly VITE_API_URL?: string
  readonly VITE_BASE_URL?: string
  readonly VITE_HOME_PATH: string
  readonly VITE_PUBLIC_ROUTE_PATHS?: string
  readonly VITE_ROUTE_LOAD_MODE?: 'static' | 'dynamic'
  readonly VITE_DEFAULT_LANG: App.Lang
}
