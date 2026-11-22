<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useSysMenuFetch } from '@/composables/v1'
import { Icon } from '@iconify/vue/offline'

const emit = defineEmits(['close'])
const show = ref(false)
const { sysMenuDefaultData } = useSysMenuFetch()
const formData = ref<any>({ ...sysMenuDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysMenuDefaultData }
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
        <NDescriptionsItem label="Menu ID">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Icon">
          <NIcon size="20" class="mr-2">
            <Icon :icon="formData.icon" />
          </NIcon>{{ formData.icon }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Menu Title">
          {{ formData.title }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Menu Name">
          {{ formData.name }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Parent Menu">
          {{ formData.pidName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Route Path">
          {{ formData.path }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Component Path">
          {{ formData.componentPath }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Cached">
          {{ formData.keepAliveName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Visible">
          {{ formData.visibleName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Sort Order">
          {{ formData.sort }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Pinned">
          {{ formData.pinedName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Menu Type">
          {{ formData.menuTypeName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Header Parameters">
          <!-- {{ formData.submitTokenName }} -->
          <NCode
            :code="String(formData.parameters || '')"
            language="json"
            word-wrap
            show-line-numbers
          />
        </NDescriptionsItem>
        <!-- <NDescriptionsItem label="Extra Information">
          {{ formData.exJson }}
        </NDescriptionsItem> -->
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
