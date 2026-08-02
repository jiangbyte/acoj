import { defineConfig, presetUno } from 'unocss'

export default defineConfig({
  preflights: [
    {
      getCSS: () => `
        *,
        *::before,
        *::after {
          border-width: 0;
          border-style: solid;
          border-color: #e5e7eb;
        }
      `,
    },
  ],
  presets: [presetUno()],
  shortcuts: {
    'wh-full': 'w-full h-full',
    'flex-center': 'flex items-center justify-center',
    'flex-y-center': 'flex items-center',
  },
})
