<script setup lang="ts">
</script>

<template>
  <div class="flex items-center justify-between w-full flex-1">
    <div class="flex items-center gap-4">
      <Collapse />
      <Breadcrumb />
    </div>
    <div class="flex items-center gap-4">
      <!--      <n-button text> -->
      <!--        <template #icon> -->
      <!--          <icon-park-outline-search /> -->
      <!--        </template> -->
      <!--      </n-button> -->
      <!--      <n-button text> -->
      <!--        <template #icon> -->
      <!--          <icon-park-outline-message-one /> -->
      <!--        </template> -->
      <!--      </n-button> -->
      <!--      <n-button text> -->
      <!--        <template #icon> -->
      <!--          <icon-park-outline-setting-two /> -->
      <!--        </template> -->
      <!--      </n-button> -->
      <Dropdown>
        <template #avatar>
          <UserAvatar />
        </template>
      </Dropdown>
    </div>
  </div>
  <TabBar />
</template>

<style scoped>

</style>
