<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useDataContestFetch } from '@/composables/v1'
import MDViewer from '@/components/common/editor/md/MarkdownViewer.vue'

const emit = defineEmits(['close'])
const show = ref(false)
const { dataContestDefaultData } = useDataContestFetch()
const formData = ref<any>({ ...dataContestDefaultData })
function doClose() {
  emit('close')
  show.value = false
  formData.value = { ...dataContestDefaultData }
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
    <NDrawerContent title="Details">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="ID">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Contest Title">
          {{ formData.title }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Contest Description">
          <MDViewer :model-value="formData.description" />
        </NDescriptionsItem>
        <!-- <NDescriptionsItem label="Rule Type">
          {{ formData.ruleType }}
        </NDescriptionsItem> -->
        <NDescriptionsItem label="Category">
          {{ formData.category }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Cover">
          <NImage :src="formData.cover" width="100" height="100" object-fit="cover" />
        </NDescriptionsItem>
        <!-- <NDescriptionsItem label="Maximum Team Size">
          {{ formData.maxTeamMembers }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Team Contest">
          {{ formData.isTeamContestName }}
        </NDescriptionsItem> -->
        <NDescriptionsItem label="Public">
          {{ formData.isPublicName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Published">
          {{ formData.isVisibleName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Access Password">
          {{ formData.password }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Registration Starts">
          <NTime :time="Number(formData.registerStartTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Registration Ends">
          <NTime :time="Number(formData.registerEndTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Contest Starts">
          <NTime :time="Number(formData.contestStartTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Contest Ends">
          <NTime :time="Number(formData.contestEndTime)" />
        </NDescriptionsItem>
        <!-- <NDescriptionsItem label="Scoreboard Freeze (minutes)">
          {{ formData.frozenTime }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Penalty (minutes)">
          {{ formData.penaltyTime }}
        </NDescriptionsItem> -->
        <!-- <NDescriptionsItem label="Allowed Languages">
          <n-space align="center">
            <n-tag v-for="(item, index) in FormatLanguages(formData.allowedLanguages)" :key="index">
              {{ item }}
            </n-tag>
          </n-space>
        </NDescriptionsItem> -->
        <!-- <NDescriptionsItem label="Status">
          {{ formData.status }}
        </NDescriptionsItem>
        <NDescriptionsItem label="Sort Order">
          {{ formData.sort }}
        </NDescriptionsItem> -->
        <NDescriptionsItem label="Created At">
          <NTime :time="Number(formData.createTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Created By">
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
        <NDescriptionsItem label="Updated At">
          <NTime :time="Number(formData.updateTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="Updated By">
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
            Close
          </NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>

</style>
