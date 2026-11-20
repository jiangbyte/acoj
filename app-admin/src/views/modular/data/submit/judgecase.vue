<script lang="ts" setup>
import { NDrawer, NDrawerContent } from 'naive-ui'
import { useDataProblemFetch } from '@/composables/v1'
import JudgeCaseIndex from './judgecase/index.vue'

const emit = defineEmits(['close'])
const show = ref(false)
const { dataProblemDefaultData } = useDataProblemFetch()
const formData = ref<any>({ ...dataProblemDefaultData })

const submitId = ref()
function doClose() {
  emit('close')
  show.value = false
  submitId.value = '' // 重置
}

function doOpen(row: any) {
  // 先设置 problemId
  submitId.value = row.id
  console.log('提交ID-->', submitId.value)
  // 再设置显示状态
  show.value = true
  formData.value = row
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="用例数据">
      <JudgeCaseIndex :submit-id="submitId" />
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
