import type { dictLabel, dictColor, getDictItems } from '@/utils'

declare module 'vue' {
  interface ComponentCustomProperties {
    $dict: {
      label: typeof dictLabel
      color: typeof dictColor
      items: typeof getDictItems
    }
  }
}

export {}
