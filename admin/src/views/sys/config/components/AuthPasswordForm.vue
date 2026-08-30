<!-- Author: Charlie -->

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import ConfigSectionLayout from './ConfigSectionLayout.vue'
import WeakPasswordPanel from './WeakPasswordPanel.vue'
import { loadByCategory, parseBool, saveByKeys, toBoolStr } from '../composables/useConfigForm'

const CATEGORY = 'AUTH_PASSWORD'

const passwordTabOptions = [
  { key: 'POLICY', label: '密码策略' },
  { key: 'WEAK', label: '弱密码库' },
]

const verifyOptions = [
  { label: '旧密码', value: 'OLD_PASSWORD' },
  { label: '邮箱验证码', value: 'EMAIL_OTP' },
  { label: '手机验证码', value: 'PHONE_OTP' },
]

const complexityOptions = [
  { label: '任意', value: 'ANY' },
  { label: '字母+数字', value: 'ALPHA_NUM' },
  { label: '大小写字母+数字', value: 'MIXED_ALPHA_NUM' },
  { label: '大小写字母+数字+特殊字符', value: 'STRONG' },
]

const state = reactive({
  loading: false,
  saving: false,
  subTab: 'POLICY' as 'POLICY' | 'WEAK',
  defaultPassword: '',
  changeVerifyMethod: 'OLD_PASSWORD',
  complexity: 'ANY',
  minLength: 6,
  maxLength: 32,
  maxConsecutiveChars: 3,
  forbidUserInfo: false,
  forbidWeakList: true,
  forbidHistorical: false,
  historyCheckCount: 3,
  validityDays: 0,
  expiryWarningDays: 7,
  cancelRetentionDays: 30,
  snapshot: '',
})

onMounted(() => {
  void reload()
})

async function reload() {
  state.loading = true
  try {
    const map = await loadByCategory(CATEGORY)
    state.defaultPassword = ''
    state.changeVerifyMethod = map.AUTH_PASSWORD_CHANGE_VERIFY_METHOD ?? 'OLD_PASSWORD'
    state.complexity = map.AUTH_PASSWORD_COMPLEXITY ?? 'ANY'
    state.minLength = Number(map.AUTH_PASSWORD_MIN_LENGTH ?? 6)
    state.maxLength = Number(map.AUTH_PASSWORD_MAX_LENGTH ?? 32)
    state.maxConsecutiveChars = Number(map.AUTH_PASSWORD_MAX_CONSECUTIVE_CHARS ?? 3)
    state.forbidUserInfo = parseBool(map.AUTH_PASSWORD_FORBID_USER_INFO)
    state.forbidWeakList = parseBool(map.AUTH_PASSWORD_FORBID_WEAK_LIST ?? 'true')
    state.forbidHistorical = parseBool(map.AUTH_PASSWORD_FORBID_HISTORICAL)
    state.historyCheckCount = Number(map.AUTH_PASSWORD_HISTORY_CHECK_COUNT ?? 3)
    state.validityDays = Number(map.AUTH_PASSWORD_VALIDITY_DAYS ?? 0)
    state.expiryWarningDays = Number(map.AUTH_PASSWORD_EXPIRY_WARNING_DAYS ?? 7)
    state.cancelRetentionDays = Number(map.AUTH_PASSWORD_CANCEL_RETENTION_DAYS ?? 30)
    state.snapshot = JSON.stringify({
      defaultPassword: state.defaultPassword,
      changeVerifyMethod: state.changeVerifyMethod,
      complexity: state.complexity,
      minLength: state.minLength,
      maxLength: state.maxLength,
      maxConsecutiveChars: state.maxConsecutiveChars,
      forbidUserInfo: state.forbidUserInfo,
      forbidWeakList: state.forbidWeakList,
      forbidHistorical: state.forbidHistorical,
      historyCheckCount: state.historyCheckCount,
      validityDays: state.validityDays,
      expiryWarningDays: state.expiryWarningDays,
      cancelRetentionDays: state.cancelRetentionDays,
    })
  } finally {
    state.loading = false
  }
}

function reset() {
  const snap = JSON.parse(state.snapshot || '{}')
  Object.assign(state, snap)
}

async function save() {
  state.saving = true
  try {
    const items: Array<{ config_key: string; config_value: string; category: string }> = [
      {
        config_key: 'AUTH_PASSWORD_CHANGE_VERIFY_METHOD',
        config_value: state.changeVerifyMethod,
        category: CATEGORY,
      },
      {
        config_key: 'AUTH_PASSWORD_COMPLEXITY',
        config_value: state.complexity,
        category: CATEGORY,
      },
      {
        config_key: 'AUTH_PASSWORD_MIN_LENGTH',
        config_value: String(state.minLength),
        category: CATEGORY,
      },
      {
        config_key: 'AUTH_PASSWORD_MAX_LENGTH',
        config_value: String(state.maxLength),
        category: CATEGORY,
      },
      {
        config_key: 'AUTH_PASSWORD_MAX_CONSECUTIVE_CHARS',
        config_value: String(state.maxConsecutiveChars),
        category: CATEGORY,
      },
      {
        config_key: 'AUTH_PASSWORD_FORBID_USER_INFO',
        config_value: toBoolStr(state.forbidUserInfo),
        category: CATEGORY,
      },
      {
        config_key: 'AUTH_PASSWORD_FORBID_WEAK_LIST',
        config_value: toBoolStr(state.forbidWeakList),
        category: CATEGORY,
      },
      {
        config_key: 'AUTH_PASSWORD_FORBID_HISTORICAL',
        config_value: toBoolStr(state.forbidHistorical),
        category: CATEGORY,
      },
      {
        config_key: 'AUTH_PASSWORD_HISTORY_CHECK_COUNT',
        config_value: String(state.historyCheckCount),
        category: CATEGORY,
      },
      {
        config_key: 'AUTH_PASSWORD_VALIDITY_DAYS',
        config_value: String(state.validityDays),
        category: CATEGORY,
      },
      {
        config_key: 'AUTH_PASSWORD_EXPIRY_WARNING_DAYS',
        config_value: String(state.expiryWarningDays),
        category: CATEGORY,
      },
      {
        config_key: 'AUTH_PASSWORD_CANCEL_RETENTION_DAYS',
        config_value: String(state.cancelRetentionDays),
        category: CATEGORY,
      },
    ]
    if (state.defaultPassword) {
      items.push({
        config_key: 'AUTH_PASSWORD_DEFAULT',
        config_value: state.defaultPassword,
        category: CATEGORY,
      })
    }
    await saveByKeys(items)
    window.$message.success('保存成功')
    await reload()
  } finally {
    state.saving = false
  }
}
</script>

<template>
  <NTabs
    class="sys-config-subnav"
    type="line"
    placement="left"
    :value="state.subTab"
    @update:value="(value: string) => (state.subTab = value as 'POLICY' | 'WEAK')"
  >
    <NTabPane
      v-for="opt in passwordTabOptions"
      :key="opt.key"
      :name="opt.key"
      :tab="opt.label"
    >
      <WeakPasswordPanel v-if="opt.key === 'WEAK'" />

      <NSpin
        v-else
        :show="state.loading"
      >
        <ConfigSectionLayout
          description="全局密码策略，管理端与门户共用；保存后热重载生效。"
          :saving="state.saving"
          @save="save"
          @reset="reset"
        >
          <NForm
            class="sys-config-form sys-config-form--wide"
            label-placement="top"
          >
            <NCard
              title="基础"
              size="small"
              :bordered="false"
            >
              <NGrid
                :cols="24"
                :x-gap="16"
              >
                <NGi :span="12">
                  <NFormItem label="默认用户密码">
                    <NInput
                      v-model:value="state.defaultPassword"
                      type="password"
                      show-password-on="click"
                      placeholder="留空不修改"
                    />
                  </NFormItem>
                  <p class="sys-config__hint">
                    敏感项；用于新建账户等场景
                  </p>
                </NGi>
                <NGi :span="12">
                  <NFormItem label="修改密码验证方式">
                    <NSelect
                      v-model:value="state.changeVerifyMethod"
                      :options="verifyOptions"
                    />
                  </NFormItem>
                </NGi>
              </NGrid>
            </NCard>

            <NCard
              title="强度规则"
              size="small"
              :bordered="false"
              class="mt-12px"
            >
              <NGrid
                :cols="24"
                :x-gap="16"
              >
                <NGi :span="24">
                  <NFormItem label="复杂度">
                    <NSelect
                      v-model:value="state.complexity"
                      :options="complexityOptions"
                    />
                  </NFormItem>
                </NGi>
                <NGi :span="8">
                  <NFormItem label="最小长度">
                    <NInputNumber
                      v-model:value="state.minLength"
                      class="w-full"
                      :min="1"
                    />
                  </NFormItem>
                </NGi>
                <NGi :span="8">
                  <NFormItem label="最大长度">
                    <NInputNumber
                      v-model:value="state.maxLength"
                      class="w-full"
                      :min="1"
                    />
                  </NFormItem>
                </NGi>
                <NGi :span="8">
                  <NFormItem label="连续相同字符上限">
                    <NInputNumber
                      v-model:value="state.maxConsecutiveChars"
                      class="w-full"
                      :min="1"
                    />
                  </NFormItem>
                </NGi>
              </NGrid>
            </NCard>

            <NCard
              title="禁止项"
              size="small"
              :bordered="false"
              class="mt-12px"
            >
              <div class="config-rows">
                <div class="config-row">
                  <span>禁止包含用户信息</span>
                  <NSwitch v-model:value="state.forbidUserInfo" />
                </div>

                <div class="config-row">
                  <span>禁止弱密码库</span>
                  <NSwitch v-model:value="state.forbidWeakList" />
                </div>

                <div class="config-row">
                  <span>禁止历史密码</span>
                  <NSwitch v-model:value="state.forbidHistorical" />
                  <NFormItem
                    v-if="state.forbidHistorical"
                    label="检查最近个数"
                    :show-feedback="false"
                  >
                    <NInputNumber
                      v-model:value="state.historyCheckCount"
                      class="w-full"
                      :min="0"
                    />
                  </NFormItem>
                </div>
              </div>
            </NCard>

            <NCard
              title="有效期"
              size="small"
              :bordered="false"
              class="mt-12px"
            >
              <NGrid
                :cols="24"
                :x-gap="16"
              >
                <NGi :span="12">
                  <NFormItem label="有效期（天）">
                    <NInputNumber
                      v-model:value="state.validityDays"
                      class="w-full"
                      :min="0"
                    />
                  </NFormItem>
                  <p class="sys-config__hint">
                    0 表示不过期
                  </p>
                </NGi>
                <NGi :span="12">
                  <NFormItem label="过期提醒（天）">
                    <NInputNumber
                      v-model:value="state.expiryWarningDays"
                      class="w-full"
                      :min="0"
                    />
                  </NFormItem>
                  <p class="sys-config__hint">
                    到期前 N 天登录时发送提醒邮件/短信（24 小时内不重复）
                  </p>
                </NGi>
                <NGi :span="12">
                  <NFormItem label="注销保留天数">
                    <NInputNumber
                      v-model:value="state.cancelRetentionDays"
                      class="w-full"
                      :min="1"
                    />
                  </NFormItem>
                  <p class="sys-config__hint">
                    账号注销后保留数据的天数，到期后自动清理
                  </p>
                </NGi>
              </NGrid>
            </NCard>
          </NForm>
        </ConfigSectionLayout>
      </NSpin>
    </NTabPane>
  </NTabs>
</template>
