<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useProSetFetch } from '@/composables'

const emit = defineEmits(['close'])
const show = ref(false)
const { proSetDefaultData } = useProSetFetch()
const formData = ref<any>({ ...proSetDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...proSetDefaultData }
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
        <NDescriptionsItem label="题集类型">
          {{ formData.setTypeName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="标题">
          {{ formData.title }}
        </NDescriptionsItem>
        <NDescriptionsItem label="封面">
          <NImage :src="formData.cover" width="100" height="100" object-fit="cover" />
        </NDescriptionsItem>
        <NDescriptionsItem label="描述">
          {{ formData.description }}
        </NDescriptionsItem>
        <NDescriptionsItem label="分类">
          {{ formData.categoryName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="难度">
          {{ formData.difficultyName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="开始时间">
          <NTime :time="Number(formData.startTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="结束时间">
          <NTime :time="Number(formData.endTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="配置信息">
          {{ formData.config }}
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
