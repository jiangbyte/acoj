// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },

  devServer: {
    port: 5173,
    host: '0.0.0.0',
  },

  vite: {
    server: {
      ws: {
        host: 'localhost',
      },
      watch: {
        usePolling: true,
        interval: 300,
        ignored: ['**/node_modules/**', '**/.git/**', '**/.nuxt/**'],
      },
    },
  },

  nitro: {
    watchOptions: {
      ignore: ['**/node_modules/**', '**/.git/**', '**/dist/**'],
    },
  },

  runtimeConfig: {
    public: {
      apiBaseUrl: '',
    },
  },

  modules: [
    '@nuxt/ui',
    '@nuxt/eslint',
    '@vueuse/nuxt',
    '@pinia/nuxt',
    'pinia-plugin-persistedstate/nuxt',
  ],

  css: ['~/assets/css/main.css'],

  icon: {
    serverBundle: 'local',
    clientBundle: {
      scan: true,
    },
  },

  fonts: {
    providers: {
      google: false,
      googleicons: false,
      bunny: false,
      fontshare: false,
      adobe: false,
      fontsource: false,
      npm: false,
    },
  },
})
