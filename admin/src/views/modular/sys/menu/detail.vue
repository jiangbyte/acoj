<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useSysMenuFetch } from '@/composables/v1'
import { Icon } from '@iconify/vue'

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
  <NDrawer v-model:show="show" placement="right" width="800" @after-leave="doClose">
    <NDrawerContent title="详情">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="菜单ID">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="父菜单ID">
          {{ formData.pid }}
        </NDescriptionsItem>
        <NDescriptionsItem label="菜单名称">
          {{ formData.name }}
        </NDescriptionsItem>
        <NDescriptionsItem label="路由路径">
          {{ formData.path }}
        </NDescriptionsItem>
        <NDescriptionsItem label="组件路径">
          {{ formData.componentPath }}
        </NDescriptionsItem>
        <NDescriptionsItem label="菜单标题">
          {{ formData.title }}
        </NDescriptionsItem>
        <NDescriptionsItem label="图标">
          <NIcon size="20" class="mr-2">
            <Icon :icon="formData.icon" />
          </NIcon>{{ formData.icon }}
        </NDescriptionsItem>
        <NDescriptionsItem label="是否缓存">
          {{ formData.keepAliveName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="是否可见">
          {{ formData.visibleName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="排序">
          {{ formData.sort }}
        </NDescriptionsItem>
        <NDescriptionsItem label="是否固定">
          {{ formData.pinedName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="菜单类型">
          {{ formData.menuTypeName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="额外信息">
          {{ formData.exJson }}
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
