import { resolve } from 'node:path'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import UnoCSS from 'unocss/vite'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers'
import Icons from 'unplugin-icons/vite'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, __dirname, '')
  const port = Number(env.VITE_PORT || 5173)

  return {
    server: {
      host: '0.0.0.0',
      port,
    },
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
      },
    },
    plugins: [
      vue(),
      vueJsx(),
      UnoCSS(),
      Icons(),
      AutoImport({
        dts: 'src/typing/auto-imports.d.ts',
        imports: ['vue', 'pinia', 'vue-router', '@vueuse/core'],
      }),
      Components({
        dts: 'src/typing/components.d.ts',
        resolvers: [NaiveUiResolver()],
      }),
    ],
  }
})
