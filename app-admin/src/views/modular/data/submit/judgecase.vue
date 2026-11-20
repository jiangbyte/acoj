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
  submitId.value = '' // Reset
}

function doOpen(row: any) {
  // Set problemId first
  submitId.value = row.id
  console.log('Submission ID-->', submitId.value)
  // Then show the drawer
  show.value = true
  formData.value = row
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="Test CasesData">
      <JudgeCaseIndex :submit-id="submitId" />
      <template #footer>
        <NSpace align="center" justify="end">
          <NButton @click="doClose">
            <template #icon>
              <IconParkOutlineClose />
            </template>
            Close
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
