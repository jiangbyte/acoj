<script lang="ts" setup>
import { useSysUserFetch } from '@/composables/v1'

const { getProfile } = useSysUserFetch()

const response = ref<IResult<any>>()
async function loadData() {
  response.value = await getProfile() || { data: { nickname: 'No Data', avatar: '#' } }
}
loadData()

function onProfileUpdated() {
  loadData()
}
onMounted(() => {
  window.addEventListener('acoj:profile-updated', onProfileUpdated)
})
onUnmounted(() => {
  window.removeEventListener('acoj:profile-updated', onProfileUpdated)
})
</script>

<template>
  <NSpace align="center">
    <SafeAvatar
      round
      :src="response?.data ? response?.data.avatar : '#'"
    />
    <NText strong>
      {{ response?.data ? response?.data.nickname : 'No Data' }}
    </NText>
  </NSpace>
</template>
