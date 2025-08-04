<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useProSetSolvedFetch } from '@/composables'

const emit = defineEmits(['close'])
const show = ref(false)
const { proSetSolvedDefaultData } = useProSetSolvedFetch()
const formData = ref<any>({ ...proSetSolvedDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...proSetSolvedDefaultData }
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
        <NDescriptionsItem label="用户">
          {{ formData.userIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="题目">
          {{ formData.problemIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="题单">
          {{ formData.problemSetIdName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="提交ID">
          {{ formData.submitId }}
        </NDescriptionsItem>
        <NDescriptionsItem label="是否解决">
          {{ formData.solvedName }}
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
