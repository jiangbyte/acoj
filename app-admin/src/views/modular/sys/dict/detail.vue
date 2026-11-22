<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useSysDictFetch } from '@/composables/v1'

const emit = defineEmits(['close'])
const show = ref(false)
const { sysDictDefaultData } = useSysDictFetch()
const formData = ref<any>({ ...sysDictDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysDictDefaultData }
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
  <NDrawer v-model:show="show" :mask-closable="false" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="Details">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="IDID">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Dictionary Type">
          {{ formData.dictType }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Type Name">
          {{ formData.typeLabel }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Dictionary Value">
          {{ formData.dictValue }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Dictionary Label">
          {{ formData.dictLabel }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Sort Order">
          {{ formData.sortOrder }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Created At">
          <NTime :time="Number(formData.createTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Created By">
          <!-- {{ formData.createUserName }} -->
          <NSpace align="center" size="small">
            <SafeAvatar
              size="small"
              :round="true"
              :src="formData.createUserAvatar"
            />
            <NText>{{ formData.createUserName }}</NText>
          </NSpace>
        </NDescriptionsItem>
        <NDescriptionsItem label="Updated At">
          <NTime :time="Number(formData.updateTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Updated By">
          <!-- {{ formData.updateUserName }} -->
          <NSpace align="center" size="small">
            <SafeAvatar
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
            Close
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
