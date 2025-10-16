import { resolve } from 'node:path'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import process from 'node:process'
// import vueDevTools from 'vite-plugin-vue-devtools'

import UnoCSS from 'unocss/vite'
import Icons from 'unplugin-icons/vite'
import IconsResolver from 'unplugin-icons/resolver'
import Components from 'unplugin-vue-components/vite'
import AutoImport from 'unplugin-auto-import/vite'
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers'

// https://vite.dev/config/
export default defineConfig(({ command, mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  // 确保VITE_GATEWAY有值
  const gateway = env.VITE_GATEWAY
  console.log('Using Gateway:', gateway)
  return {
    plugins: [
      vue(),
      vueJsx(),
      // vueDevTools(),
      UnoCSS(),
      AutoImport({
        imports: [
          'vue',
          'vue-router',
          'pinia',
          {
            'naive-ui': [
              'useDialog',
              'useMessage',
              'useNotification',
              'useLoadingBar',
              'useModal',
            ],
          },
        ],
        include: [/\.[tj]sx?$/, /\.vue$/, /\.vue\?vue/, /\.md$/],
        dts: 'src/typings/auto-imports.d.ts',
      }),
      Components({
        dts: 'src/typings/components.d.ts',
        resolvers: [
          IconsResolver({
            prefix: false,
          }),
          NaiveUiResolver(),
        ],
      }),
      Icons({
        defaultStyle: 'display:inline-block',
        compiler: 'vue3',
      }),
    ],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
      },
    },
    server: {
      host: '0.0.0.0',
      port: 80,
      // proxy: {
      //   '/oj': {
      //     target: gateway,
      //     changeOrigin: true,
      //     secure: false,
      //     ws: true,
      //   },
      // },
    },
    define: {
      global: 'window',
    },
    optimizeDeps: {
      include: [
        'sockjs-client',
        `monaco-editor/esm/vs/language/json/json.worker`,
        `monaco-editor/esm/vs/language/css/css.worker`,
        `monaco-editor/esm/vs/language/html/html.worker`,
        `monaco-editor/esm/vs/language/typescript/ts.worker`,
        `monaco-editor/esm/vs/editor/editor.worker`,
      ],
    },
  }
})
