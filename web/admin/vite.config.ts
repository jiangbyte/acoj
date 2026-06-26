import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import UnoCSS from 'unocss/vite'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    UnoCSS(),
    AutoImport({
      dts: 'src/typing/auto-imports.d.ts',
      imports: ['vue', 'pinia'],
    }),
    Components({
      dts: 'src/typing/components.d.ts',
      resolvers: [NaiveUiResolver()],
    }),
  ],
})
