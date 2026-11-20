<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NImage, NTime } from 'naive-ui'
import { use${entity}Fetch } from '@/composables'

const emit = defineEmits(['close'])
const show = ref(false)
const { ${table.entityPath}DefaultData } = use${entity}Fetch()
const formData = ref<any>({ ...${table.entityPath}DefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...${table.entityPath}DefaultData }
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
        <#list table.fields as field>
          <#if !["deleted"]?seq_contains(field.propertyName)>
          <#if field.propertyName == 'createTime'>
            <NDescriptionsItem label="创建时间">
              <NTime :time="Number(formData.createTime)" />
            </NDescriptionsItem>
            <#continue/>
          </#if>
          <#if field.propertyName == 'updateTime'>
            <NDescriptionsItem label="更新时间">
              <NTime :time="Number(formData.updateTime)" />
            </NDescriptionsItem>
            <#continue/>
          </#if>
          <#if field.propertyType == 'LocalDateTime' || field.propertyType == 'LocalDate' || field.propertyType == 'Date'>
          <NDescriptionsItem label="${field.comment}">
            <NTime :time="Number(formData.${field.propertyName})" />
          </NDescriptionsItem>
          <#elseif field.propertyName == 'createUser'>
            <NDescriptionsItem label="创建者">
              {{ formData.createUserName }}
            </NDescriptionsItem>
          <#elseif field.propertyName == 'updateUser'>
            <NDescriptionsItem label="更新人">
              {{ formData.updateUserName }}
            </NDescriptionsItem>
            <#else>
          <NDescriptionsItem label="${field.comment}">
            {{ formData.${field.propertyName} }}
          </NDescriptionsItem>
          </#if>
          </#if>
        </#list>
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
