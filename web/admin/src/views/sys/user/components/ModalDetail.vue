<script setup lang="ts">
import type { User } from '../types'
import { displayValue } from '@/utils'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  genderLabelKeyMap,
  genderTagTypeMap,
  roleOptions,
  statusLabelKeyMap,
  statusTagTypeMap,
} from '../constants'
import { mockUsers } from '../mock'

const props = defineProps<{
  id: string
}>()

const { t } = useI18n()
const user = computed<User | null>(() => mockUsers.find((item) => item.id === props.id) ?? null)

const roleLabelKeyMap = computed(() => {
  return new Map(roleOptions.map((item) => [item.value, item.labelKey]))
})

const roleLabels = computed(() => {
  return (user.value?.roleIds ?? []).map((roleId) => {
    const labelKey = roleLabelKeyMap.value.get(roleId)
    return labelKey ? t(labelKey) : roleId
  })
})
</script>

<template>
  <NEmpty v-if="!user" />
  <NDescriptions v-else label-placement="left" bordered :column="1">
    <NDescriptionsItem :label="t('pages.system.user.id')">
      {{ user.id }}
    </NDescriptionsItem>
    <NDescriptionsItem :label="t('pages.system.user.username')">
      {{ user.username }}
    </NDescriptionsItem>
    <NDescriptionsItem :label="t('pages.system.user.nickname')">
      {{ user.nickname }}
    </NDescriptionsItem>
    <NDescriptionsItem :label="t('pages.system.user.gender')">
      <NTag :type="genderTagTypeMap[user.gender]" :bordered="false">
        {{ t(genderLabelKeyMap[user.gender]) }}
      </NTag>
    </NDescriptionsItem>
    <NDescriptionsItem :label="t('pages.system.user.role')">
      <NSpace size="small">
        <NTag v-for="role in roleLabels" :key="role" :bordered="false">
          {{ role }}
        </NTag>
      </NSpace>
    </NDescriptionsItem>
    <NDescriptionsItem :label="t('common.often.status')">
      <NTag :type="statusTagTypeMap[user.status]" :bordered="false">
        {{ t(statusLabelKeyMap[user.status]) }}
      </NTag>
    </NDescriptionsItem>
    <NDescriptionsItem :label="t('pages.system.user.email')">
      {{ displayValue(user.email) }}
    </NDescriptionsItem>
    <NDescriptionsItem :label="t('pages.system.user.phone')">
      {{ displayValue(user.phone) }}
    </NDescriptionsItem>
    <NDescriptionsItem :label="t('common.often.remark')">
      {{ displayValue(user.remark) }}
    </NDescriptionsItem>
    <NDescriptionsItem :label="t('pages.system.user.createTime')">
      {{ user.createTime }}
    </NDescriptionsItem>
    <NDescriptionsItem :label="t('common.often.updatedAt')">
      {{ user.updateTime }}
    </NDescriptionsItem>
  </NDescriptions>
</template>
