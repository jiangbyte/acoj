<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useDataProblemFetch } from '@/composables/v1'

const emit = defineEmits(['close'])
const show = ref(false)
const { dataProblemDefaultData } = useDataProblemFetch()
const formData = ref<any>({ ...dataProblemDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataProblemDefaultData }
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
        <NDescriptionsItem label="展示ID">
          {{ formData.displayId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="分类">
          {{ formData.categoryId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="标题">
          {{ formData.title }}
        </NDescriptionsItem>
        <NDescriptionsItem label="来源">
          {{ formData.source }}
        </NDescriptionsItem>
        <NDescriptionsItem label="链接">
          {{ formData.url }}
        </NDescriptionsItem>
        <NDescriptionsItem label="时间限制">
          {{ formData.maxTime }}
        </NDescriptionsItem>
        <NDescriptionsItem label="内存限制">
          {{ formData.maxMemory }}
        </NDescriptionsItem>
        <NDescriptionsItem label="描述">
          {{ formData.description }}
        </NDescriptionsItem>
        <NDescriptionsItem label="用例">
          {{ formData.testCase }}
        </NDescriptionsItem>
        <NDescriptionsItem label="开放语言">
          {{ formData.allowedLanguages }}
        </NDescriptionsItem>
        <NDescriptionsItem label="难度">
          {{ formData.difficulty }}
        </NDescriptionsItem>
        <NDescriptionsItem label="阈值">
          {{ formData.threshold }}
        </NDescriptionsItem>
        <NDescriptionsItem label="使用模板">
          {{ formData.useTemplate }}
        </NDescriptionsItem>
        <NDescriptionsItem label="模板代码">
          {{ formData.codeTemplate }}
        </NDescriptionsItem>
        <NDescriptionsItem label="是否公开">
          {{ formData.isPublic }}
        </NDescriptionsItem>
        <NDescriptionsItem label="是否可见">
          {{ formData.isVisible }}
        </NDescriptionsItem>
        <NDescriptionsItem label="是否使用AI">
          {{ formData.useAi }}
        </NDescriptionsItem>
        <NDescriptionsItem label="解决">
          {{ formData.solved }}
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
