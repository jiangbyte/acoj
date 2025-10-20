<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useSysRoleFetch } from '@/composables/v1'

const emit = defineEmits(['close'])
const show = ref(false)
const { sysRoleDefaultData } = useSysRoleFetch()
const formData = ref<any>({ ...sysRoleDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...sysRoleDefaultData }
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
    <NDrawerContent title="详情">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="主键">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="名称">
          {{ formData.name }}
        </NDescriptionsItem>
        <NDescriptionsItem label="编码">
          {{ formData.code }}
        </NDescriptionsItem>
        <NDescriptionsItem label="描述">
          {{ formData.description }}
        </NDescriptionsItem>
        <NDescriptionsItem label="角色层级">
          {{ formData.level }}
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
