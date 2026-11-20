<template>
  <div
    class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-shadow "
  >
    <div class="md:flex">
      <div class="md:w-1/3 h-32 md:h-auto relative">
        <n-skeleton class="absolute inset-0 w-full h-full object-cover" />
      </div>
      <div class="md:w-2/3 p-x-6 pt-6 pb-1">
        <div class="flex items-center text-sm text-gray-500 dark:text-gray-400 mb-2">
          <n-skeleton text :repeat="1" class="w-50" />
          <span class="mx-2" />
          <n-skeleton text :repeat="1" class="w-20" />
        </div>
        <n-skeleton class="w-50 h-7 mb-2" />
        <n-skeleton text :repeat="2" class="mb-2" />
      </div>
    </div>
  </div>
</template>
