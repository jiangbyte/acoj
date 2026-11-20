<script setup lang="ts">
import { useSiteStore } from '@/stores'
import { dateEnUS, enUS } from 'naive-ui'

import hljs from 'highlight.js/lib/core'

// Implementation note.
import javascript from 'highlight.js/lib/languages/javascript'
import json from 'highlight.js/lib/languages/json'
import cpp from 'highlight.js/lib/languages/cpp'
import go from 'highlight.js/lib/languages/go'
import java from 'highlight.js/lib/languages/java'
import c from 'highlight.js/lib/languages/c'
import python from 'highlight.js/lib/languages/python'

// Implementation note.
hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('json', json)
hljs.registerLanguage('cpp', cpp)
// Implementation note.
hljs.registerLanguage('golang', go)
hljs.registerLanguage('go', go)
hljs.registerLanguage('java', java)
hljs.registerLanguage('c', c)
hljs.registerLanguage('python', python)

const siteStore = useSiteStore()
</script>

<template>
  <n-config-provider
    inline-theme-disabled
    :theme-overrides="siteStore.themeOverrides"
    :locale="enUS"
    :date-locale="dateEnUS"
    :breakpoints="{ s: 640, m: 768, l: 1024 }"
  >
    <NaiveProvider>
      <router-view />
    </NaiveProvider>
  </n-config-provider>
</template>

<style scoped>

</style>
