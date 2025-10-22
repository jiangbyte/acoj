<template>
  <div
    class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-5 hover:shadow-md transition-shadow"
  >
    <div class="flex flex-col md:flex-row md:items-center justify-between">
      <div>
        <div class="flex items-center mb-2">
          <n-skeleton class="w-50 h-7" />
        </div>
        <div class="flex mb-3">
          <n-skeleton v-for="tagName in 2" :key="tagName" text :repeat="1" class="w-15 mr-3" />
        </div>
        <div class="flex items-center text-sm text-gray-500 dark:text-gray-400">
          <n-skeleton v-for="tagName in 3" :key="tagName" text :repeat="1" class="w-15 mr-3" />
        </div>
      </div>
      <div class="mt-4 md:mt-0 flex items-center text-sm text-gray-500 dark:text-gray-400">
        <n-skeleton text :repeat="1" class="w-50" />
      </div>
    </div>
  </div>
</template>
