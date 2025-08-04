<script lang="ts" setup>
import { AesCrypto } from '@/utils'

const route = useRoute()

const originalId = AesCrypto.decrypt(route.query.set as string)
</script>

<template>
  <div>
    set {{ originalId }}
    123123
  </div>
</template>

<style scoped>

</style>
