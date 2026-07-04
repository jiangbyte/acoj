<template>
  <n-back-top :bottom="80" :visibility-height="300">
    <n-tooltip placement="left" trigger="hover">
      <template #trigger>
        <div class="wh-full flex-center">
          <NovaIcon icon="icon-park-outline:to-top" />
        </div>
      </template>
      {{ 'Back to top' }}
    </n-tooltip>
  </n-back-top>
</template>
