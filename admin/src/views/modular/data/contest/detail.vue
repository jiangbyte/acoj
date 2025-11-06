<script lang="ts" setup>
import { NDescriptions, NDescriptionsItem, NDrawer, NDrawerContent, NTime } from 'naive-ui'
import { useDataContestFetch } from '@/composables/v1'
import { FormatLanguages } from '@/utils'

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
    <NDrawerContent title="详情">
      <NDescriptions label-placement="left" bordered :column="1">
        <NDescriptionsItem label="主键">
          {{ formData.id }}
        </NDescriptionsItem>
        <NDescriptionsItem label="竞赛标题">
          {{ formData.title }}
        </NDescriptionsItem>
        <NDescriptionsItem label="竞赛描述">
          {{ formData.description }}
        </NDescriptionsItem>
        <NDescriptionsItem label="竞赛类型">
          {{ formData.contestTypeName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="规则类型">
          {{ formData.ruleType }}
        </NDescriptionsItem>
        <NDescriptionsItem label="分类">
          {{ formData.category }}
        </NDescriptionsItem>
        <NDescriptionsItem label="封面">
          <NImage :src="formData.cover" width="100" height="100" object-fit="cover" />
        </NDescriptionsItem>
        <NDescriptionsItem label="最大团队成员数">
          {{ formData.maxTeamMembers }}
        </NDescriptionsItem>
        <NDescriptionsItem label="是否团队赛">
          {{ formData.isTeamContestName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="是否公开">
          {{ formData.isPublicName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="访问密码">
          {{ formData.password }}
        </NDescriptionsItem>
        <NDescriptionsItem label="报名开始时间">
          <NTime :time="Number(formData.registerStartTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="报名结束时间">
          <NTime :time="Number(formData.registerEndTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="竞赛开始时间">
          <NTime :time="Number(formData.contestStartTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="竞赛结束时间">
          <NTime :time="Number(formData.contestEndTime)" />
        </NDescriptionsItem>
        <NDescriptionsItem label="封榜时间(分钟)">
          {{ formData.frozenTime }}
        </NDescriptionsItem>
        <NDescriptionsItem label="罚时(分钟)">
          {{ formData.penaltyTime }}
        </NDescriptionsItem>
        <NDescriptionsItem label="允许语言">
          <n-space align="center">
            <n-tag v-for="(item, index) in FormatLanguages(formData.allowedLanguages)" :key="index">
              {{ item }}
            </n-tag>
          </n-space>
        </NDescriptionsItem>
        <NDescriptionsItem label="状态">
          {{ formData.status }}
        </NDescriptionsItem>
        <NDescriptionsItem label="排序">
          {{ formData.sort }}
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
