<script lang="ts" setup>
import { useSysConfigFetch } from '@/composables/v1'
import { NSpace, NText } from 'naive-ui'

const { getValueByCode } = useSysConfigFetch()

const appName = ref('')
const appLogo = ref('')
const appAdminShowAppName = ref(false)
async function loadData() {
  const [appConfig, logoConfig, adminShowConfig] = await Promise.all([
    getValueByCode({ code: 'APP_NAME' }),
    getValueByCode({ code: 'APP_LOGO' }),
    getValueByCode({ code: 'APP_PC_SHOW_APP_NAME' }),
  ])

  // 处理配置项
  appName.value = appConfig?.data ?? appName.value
  appLogo.value = logoConfig?.data ?? appLogo.value
  appAdminShowAppName.value = adminShowConfig?.data === 'true'
}
loadData()
</script>

<template>
  <NSpace
    align="center"
    :wrap="false"
    justify="center"
    class="h-10"
    @click="$router.push('/')"
  >
    <img
      v-if="appLogo && !appAdminShowAppName"
      :src="appLogo"
      class="h-10 flex items-center justify-center object-cover"
    >
    <NText
      v-if="appAdminShowAppName"
    >
      {{ appName }}
    </NText>
  </NSpace>
</template>

<style>

</style>
