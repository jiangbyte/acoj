<script lang="ts" setup>
import { NDrawer, NDrawerContent } from 'naive-ui'

const emit = defineEmits(['close'])
const show = ref(false)
const formData = ref()
function doClose() {
  emit('close')
  show.value = false
}

function doOpen(row: any, moduleType: string) {
  show.value = true
  console.log(moduleType, row)
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="详情">
      内容
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            关闭
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
