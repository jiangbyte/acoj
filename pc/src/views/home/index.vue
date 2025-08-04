<script lang="ts" setup>
</script>

<template>
  <div class="max-w-1400px mx-auto flex flex-col gap-4">
    <LatestBanner />
    <n-grid
      cols="12 m:24"
      responsive="screen"
      :x-gap="16"
      :y-gap="16"
    >
      <!-- 左侧内容 -->
      <n-gi span="24 m:24" class="flex flex-col gap-4">
        <LatestNotice />
      </n-gi>
      <!-- 右侧内容 -->
      <!-- <n-gi span="12 m:8" class="flex flex-col gap-4">
        <LatestNotice />
      </n-gi> -->
    </n-grid>
  </div>
</template>

<style>

</style>
