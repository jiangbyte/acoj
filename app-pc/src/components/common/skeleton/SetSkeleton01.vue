<template>
  <n-grid
    cols="1 l:8"
    :x-gap="18"
    responsive="screen"
  >
    <n-gi span="1 l:3">
      <n-skeleton class="w-full h-40 l:h-40 rounded-xl object-cover" />
    </n-gi>
    <n-gi span="1 l:5" class="flex items-center w-full">
      <n-thing class="w-full">
        <template #header>
          <n-skeleton class="w-50 h-7 mt-2 mb-2" />
        </template>
        <template #description>
          <n-space vertical>
            <n-flex>
              <n-skeleton text :repeat="1" class="w-15 " />
              <n-skeleton text :repeat="1" class="w-15" />
              <n-skeleton text :repeat="1" class="w-15" />
            </n-flex>
            <n-skeleton text :repeat="2" class="w-full " />
          </n-space>
        </template>
        <template #footer>
          <n-space :size="0" align="center" justify="space-between">
            <n-space align="center" :size="0">
              <n-skeleton class="w-6 h-6 mr-2" />
              <n-skeleton text :repeat="1" class="w-15" />
            </n-space>
            <n-skeleton text :repeat="1" class="w-15 " />
          </n-space>
        </template>
      </n-thing>
    </n-gi>
  </n-grid>
</template>
