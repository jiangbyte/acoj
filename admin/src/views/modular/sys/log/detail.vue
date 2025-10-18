<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useSysLogFetch } from '@/composables/v1'
import { FormatJsonString } from '@/utils'

const emit = defineEmits(['close'])
const show = ref(false)
const { sysLogDefaultData } = useSysLogFetch()
const formData = ref<any>({ ...sysLogDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysLogDefaultData }
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
          <!-- {{ formData.userId }} -->
          <NSpace align="center" size="small">
            <NAvatar
              size="small"
              :round="true"
              :src="formData.userAvatar"
            />
            <NText>{{ formData.userIdName }}</NText>
          </NSpace>
        </NDescriptionsItem>
        <NDescriptionsItem label="操作">
          {{ formData.operation }}
        </NDescriptionsItem>
        <NDescriptionsItem label="方法">
          {{ formData.method }}
        </NDescriptionsItem>
        <NDescriptionsItem label="参数">
          <!-- {{ formData.params }} -->
          <NCode
            :code="FormatJsonString(formData.params)"
            language="json"
            word-wrap
          />
        </NDescriptionsItem>
        <NDescriptionsItem label="IP">
          {{ formData.ip }}
        </NDescriptionsItem>
        <NDescriptionsItem label="操作时间">
          <NTime :time="Number(formData.operationTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="操作分类">
          {{ formData.category }}
        </NDescriptionsItem>
        <NDescriptionsItem label="模块名称">
          {{ formData.module }}
        </NDescriptionsItem>
        <NDescriptionsItem label="操作描述">
          {{ formData.description }}
        </NDescriptionsItem>
        <NDescriptionsItem label="操作状态">
          {{ formData.status }}
        </NDescriptionsItem>
        <NDescriptionsItem label="操作消息">
          {{ formData.message }}
        </NDescriptionsItem>
        <NDescriptionsItem label="创建时间">
          <NTime :time="Number(formData.createTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="创建者">
          <!-- {{ formData.createUserName }} -->
          <NSpace align="center" size="small">
            <NAvatar
              size="small"
              :round="true"
              :src="formData.createUserAvatar"
            />
            <NText>{{ formData.createUserName }}</NText>
          </NSpace>
        </NDescriptionsItem>
        <NDescriptionsItem label="更新时间">
          <NTime :time="Number(formData.updateTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="更新人">
          <!-- {{ formData.updateUserName }} -->
          <NSpace align="center" size="small">
            <NAvatar
              size="small"
              :round="true"
              :src="formData.updateUserAvatar"
            />
            <NText>{{ formData.updateUserName }}</NText>
          </NSpace>
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
