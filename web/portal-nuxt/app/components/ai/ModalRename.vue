<script setup lang="ts">
const props = defineProps<{
  open: boolean
  title: string
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  confirm: [value: string]
  cancel: []
}>()

const input = ref(props.title)

function handleConfirm() {
  if (input.value.trim()) {
    emit('confirm', input.value)
  }
}
</script>

<template>
  <UModal :open="open" title="重命名对话" :dismissible="false">
    <template #body>
      <UInput v-model="input" placeholder="输入对话名称" class="w-full" />
    </template>

    <template #footer>
      <div class="flex justify-end gap-2">
        <UButton
          label="取消"
          color="neutral"
          variant="outline"
          type="button"
          @click="emit('cancel')"
        />
        <UButton label="保存" color="primary" type="button" @click="handleConfirm" />
      </div>
    </template>
  </UModal>
</template>
