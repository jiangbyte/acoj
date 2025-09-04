<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent } from 'naive-ui'

const emit = defineEmits(['close'])
const show = ref(false)
const formData = ref<any>({})
function doClose() {
  emit('close')
  show.value = false
  formData.value = { }
}

function doOpen(row: any) {
  show.value = true
  formData.value = row
}
defineExpose({
  doOpen,
})
</script>

<template>
  <NDrawer v-model:show="show" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="详情">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="排名">
          {{ formData.ranking }}
        </NDescriptionsItem>
        <NDescriptionsItem label="用户">
          <NSpace align="center">
            <NAvatar
              round
              :src="formData.avatar ? formData.avatar : '#'"
            />
            <NText strong>
              {{ formData.nickname ? formData.nickname : 'No Data' }}
            </NText>
          </NSpace>
        </NDescriptionsItem>
        <NDescriptionsItem label="解决的总题目数">
          {{ formData.solvedCount }}
        </NDescriptionsItem>
        <NDescriptionsItem label="提交过的总题目数">
          {{ formData.attemptedCount }}
        </NDescriptionsItem>
        <NDescriptionsItem label="通过率">
          {{ formData.acceptanceRate }}
        </NDescriptionsItem>
        <NDescriptionsItem label="提交数">
          {{ formData.submissionCount }}
        </NDescriptionsItem>
        <NDescriptionsItem label="运行数">
          {{ formData.executionCount }}
        </NDescriptionsItem>
        <NDescriptionsItem label="总提交数">
          {{ formData.totalSubmissionCount }}
        </NDescriptionsItem>
      </NDescriptions>
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
