<template>
  <n-grid x-gap="16" y-gap="16" cols="1 m:2 " responsive="screen">
    <n-gi v-for="i in 2" :key="i">
      <div
        class="bg-white  w-full dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-shadow"
      >
        <div class="h-40 ">
          <n-skeleton class=" w-full h-full" />
        </div>
        <div class="p-5">
          <div class="flex items-center mb-2">
            <n-skeleton text :repeat="1" class="w-15 mr-3" />
            <n-skeleton text :repeat="1" class="w-15 mr-3" />
          </div>
          <n-skeleton class="w-50 h-7 mb-2" />
          <n-skeleton text :repeat="2" class="mb-3" />
          <n-flex align="center" justify="space-between">
            <n-skeleton v-for="tagName in 2" :key="tagName" text :repeat="1" class="w-20" />
          </n-flex>
        </div>
      </div>
    </n-gi>
  </n-grid>
</template>
