<script lang="ts" setup>
import { NAvatar, NSpace, NText } from 'naive-ui'
// import { useSysUser } from '@/composables'

// const { getProfile } = useSysUser()

const response = ref<IResult<any>>()
onMounted(async () => {
  // response.value = await getProfile() || { data: { nickname: 'No Data', avatar: '#' } }
})
</script>

<template>
  <NSpace align="center">
    <NAvatar
      round
      :src="response?.data ? response?.data.avatar : '#'"
    />
    <NText strong>
      {{ response?.data ? response?.data.nickname : 'No Data' }}
    </NText>
  </NSpace>
</template>

<style>

</style>
