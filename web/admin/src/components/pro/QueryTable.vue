<script setup lang="ts">
import { DownOutlined, UpOutlined } from '@ant-design/icons-vue'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

const expanded = ref(false)
const { t } = useI18n()
</script>

<template>
  <div class="flex flex-col gap-4">
    <div
      v-if="$slots.search"
      class="relative rounded-2 border border-slate-200 bg-white p-4 shadow-sm dark:border-zinc-800 dark:bg-zinc-900 lg:p-6"
    >
      <slot name="search" :expanded="expanded" :toggle="() => (expanded = !expanded)">
        <div class="mt-3 text-right">
          <AButton type="link" size="small" @click="expanded = !expanded">
            {{ expanded ? t('common.collapse') : t('common.expand') }}
            <template #icon>
              <UpOutlined v-if="expanded" />
              <DownOutlined v-else />
            </template>
          </AButton>
        </div>
      </slot>
    </div>

    <div class="rounded-2 border border-slate-200 bg-white p-4 shadow-sm dark:border-zinc-800 dark:bg-zinc-900 lg:p-6">
      <div v-if="$slots.toolbar" class="mb-4 flex flex-wrap items-center justify-between gap-3">
        <slot name="toolbar" />
      </div>
      <div v-if="$slots.alert" class="mb-4">
        <slot name="alert" />
      </div>
      <slot />
    </div>
  </div>
</template>
