<script setup lang="ts">
import { ProInput, ProPassword, ProRadioGroup, ProSelect, ProTextarea } from 'pro-naive-ui'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { genderOptions, roleOptions, statusOptions } from '../constants'

const { t } = useI18n()

const genderSelectOptions = computed(() => translateOptions(genderOptions))
const roleSelectOptions = computed(() => translateOptions(roleOptions))
const statusSelectOptions = computed(() => translateOptions(statusOptions))

function translateOptions<T extends string>(options: Array<{ labelKey: string; value: T }>) {
  return options.map((item) => ({
    label: t(item.labelKey),
    value: item.value,
  }))
}
</script>

<template>
  <ProInput
    :title="t('pages.system.user.username')"
    path="username"
    :tooltip="t('pages.system.user.usernameTooltip')"
    required
  />
  <ProInput :title="t('pages.system.user.nickname')" path="nickname" required />
  <ProRadioGroup
    :title="t('pages.system.user.gender')"
    path="gender"
    required
    :field-props="{ options: genderSelectOptions }"
  />
  <ProPassword
    :title="t('pages.system.user.password')"
    path="password"
    required
    :field-props="{ showPasswordOn: 'click' }"
  />
  <ProSelect
    :title="t('pages.system.user.role')"
    path="roleIds"
    required
    :field-props="{ options: roleSelectOptions, multiple: true }"
  />
  <ProRadioGroup
    :title="t('common.often.status')"
    path="status"
    required
    :field-props="{ options: statusSelectOptions }"
  />
  <ProInput :title="t('pages.system.user.email')" path="email" />
  <ProInput :title="t('pages.system.user.phone')" path="phone" />
  <ProTextarea
    :title="t('common.often.remark')"
    path="remark"
    :field-props="{
      autosize: {
        minRows: 3,
        maxRows: 5,
      },
    }"
  />
</template>
