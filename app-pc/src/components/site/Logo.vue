<script lang="ts" setup>
import { NText } from 'naive-ui'
</script>

<template>
  <div
    class="pc-logo"
    @click="$router.push('/')"
  >
    <NText class="pc-logo__title">
      ACOJ
    </NText>
  </div>
</template>

<style scoped>
.pc-logo {
  height: 40px;
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.pc-logo__title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.04em;
  line-height: 1;
}
</style>
