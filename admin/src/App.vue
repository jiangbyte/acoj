<script setup lang="ts">
import { useSiteStore } from '@/stores'
import { dateZhCN, zhCN } from 'naive-ui'

import hljs from 'highlight.js/lib/core'

// 导入语言定义
import javascript from 'highlight.js/lib/languages/javascript'
import json from 'highlight.js/lib/languages/json'
import cpp from 'highlight.js/lib/languages/cpp'
import go from 'highlight.js/lib/languages/go'
import java from 'highlight.js/lib/languages/java'
import c from 'highlight.js/lib/languages/c'
import python from 'highlight.js/lib/languages/python'

// 注册需要的语言
hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('json', json)
hljs.registerLanguage('cpp', cpp)
// Golang 在 highlight.js 中的别名是 'go',同时注册 'go' 和 'golang' 两种别名
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
    :locale="zhCN"
    :date-locale="dateZhCN"
    :breakpoints="{ s: 640, m: 768, l: 1024 }"
    :hljs="hljs"
  >
    <NaiveProvider>
      <router-view />
    </NaiveProvider>
  </n-config-provider>
</template>

<style scoped>

</style>
