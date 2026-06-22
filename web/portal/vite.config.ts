import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import UnoCSS from 'unocss/vite'
import Components from 'unplugin-vue-components/vite'
import { AntDesignVueResolver } from 'unplugin-vue-components/resolvers'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    UnoCSS(),
    Components({
      dts: 'src/types/components.d.ts',
      resolvers: [
        AntDesignVueResolver({
          importStyle: 'css-in-js',
        }),
      ],
    }),
  ],
})
