<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useProSetSubmitFetch } from '@/composables'

const emit = defineEmits(['close'])
const show = ref(false)
const { proSetSubmitDefaultData } = useProSetSubmitFetch()
const formData = ref<any>({ ...proSetSubmitDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...proSetSubmitDefaultData }
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
        <NDescriptionsItem label="用户ID">
          {{ formData.userIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="题目ID">
          {{ formData.problemIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="题集ID">
          {{ formData.setIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="编程语言">
          {{ formData.languageName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="源代码">
          {{ formData.code }}
        </NDescriptionsItem>
        <NDescriptionsItem label="执行类型">
          {{ formData.submitTypeName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="最大耗时">
          {{ formData.maxTime }}
        </NDescriptionsItem>
        <NDescriptionsItem label="最大内存使用">
          {{ formData.maxMemory }}
        </NDescriptionsItem>
        <NDescriptionsItem label="执行结果消息">
          {{ formData.message }}
        </NDescriptionsItem>
        <NDescriptionsItem label="测试用例结果">
          {{ formData.testCases }}
        </NDescriptionsItem>
        <NDescriptionsItem label="执行状态">
          {{ formData.status }}
        </NDescriptionsItem>
        <NDescriptionsItem label="相似度">
          {{ formData.similarity }}
        </NDescriptionsItem>
        <NDescriptionsItem label="相似检测任务ID">
          {{ formData.taskId }}
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
