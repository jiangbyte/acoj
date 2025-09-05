<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NImage, NTime } from 'naive-ui'
import { useProSetSimilarityDetailFetch } from '@/composables'

const emit = defineEmits(['close'])
const show = ref(false)
const { proSetSimilarityDetailDefaultData } = useProSetSimilarityDetailFetch()
const formData = ref<any>({ ...proSetSimilarityDetailDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...proSetSimilarityDetailDefaultData }
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
          <NDescriptionsItem label="主键">
            {{ formData.id }}
          </NDescriptionsItem>
          <NDescriptionsItem label="任务ID">
            {{ formData.taskId }}
          </NDescriptionsItem>
          <NDescriptionsItem label="手动">
            {{ formData.taskType }}
          </NDescriptionsItem>
          <NDescriptionsItem label="题集ID">
            {{ formData.setId }}
          </NDescriptionsItem>
          <NDescriptionsItem label="题目ID">
            {{ formData.problemId }}
          </NDescriptionsItem>
          <NDescriptionsItem label="编程语言">
            {{ formData.language }}
          </NDescriptionsItem>
          <NDescriptionsItem label="相似度">
            {{ formData.similarity }}
          </NDescriptionsItem>
          <NDescriptionsItem label="提交用户">
            {{ formData.submitUser }}
          </NDescriptionsItem>
          <NDescriptionsItem label="源代码">
            {{ formData.submitCode }}
          </NDescriptionsItem>
          <NDescriptionsItem label="源代码长度">
            {{ formData.submitCodeLength }}
          </NDescriptionsItem>
          <NDescriptionsItem label="提交ID">
            {{ formData.submitId }}
          </NDescriptionsItem>
          <NDescriptionsItem label="提交时间">
            <NTime :time="Number(formData.submitTime)" />
          </NDescriptionsItem>
          <NDescriptionsItem label="提交用户Token名称">
            {{ formData.submitTokenName }}
          </NDescriptionsItem>
          <NDescriptionsItem label="提交用户Token内容">
            {{ formData.submitTokenTexts }}
          </NDescriptionsItem>
          <NDescriptionsItem label="样本用户">
            {{ formData.originUser }}
          </NDescriptionsItem>
          <NDescriptionsItem label="样本源代码">
            {{ formData.originCode }}
          </NDescriptionsItem>
          <NDescriptionsItem label="样本源代码长度">
            {{ formData.originCodeLength }}
          </NDescriptionsItem>
          <NDescriptionsItem label="样本提交ID">
            {{ formData.originId }}
          </NDescriptionsItem>
          <NDescriptionsItem label="样本提交时间">
            <NTime :time="Number(formData.originTime)" />
          </NDescriptionsItem>
          <NDescriptionsItem label="样本用户Token名称">
            {{ formData.originTokenName }}
          </NDescriptionsItem>
          <NDescriptionsItem label="样本用户Token内容">
            {{ formData.originTokenTexts }}
          </NDescriptionsItem>
            <NDescriptionsItem label="创建时间">
              <NTime :time="Number(formData.createTime)" />
            </NDescriptionsItem>
            <NDescriptionsItem label="创建者">
              {{ formData.createUserName }}
            </NDescriptionsItem>
            <NDescriptionsItem label="更新时间">
              <NTime :time="Number(formData.updateTime)" />
            </NDescriptionsItem>
            <NDescriptionsItem label="更新人">
              {{ formData.updateUserName }}
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
