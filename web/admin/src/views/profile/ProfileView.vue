<script setup lang="ts">
import {
  AuditOutlined,
  ClockCircleOutlined,
  EditOutlined,
  IdcardOutlined,
  LockOutlined,
  MailOutlined,
  MobileOutlined,
  SafetyCertificateOutlined,
  SettingOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { useUserStore } from '@/stores/user'
import { formatDateTime } from '@hei/shared'

type ProfileSection = 'profile' | 'permissions' | 'security' | 'activity' | 'teams'

const user = useUserStore()
const { t, tm } = useI18n()
const profile = computed(() => user.profile)
const activeSection = ref<ProfileSection>('profile')

const profileName = computed(() => profile.value?.real_name || t('profile.fallbackName'))
const profileTitle = computed(() => profile.value?.title || t('profile.fallbackTitle'))
const avatarText = computed(() => profileName.value.slice(0, 1))
const employeeNo = computed(() => profile.value?.employee_no || '-')
const accountId = computed(() => profile.value?.account_id || user.me?.account_id || '-')
const updatedAtText = computed(() =>
  profile.value?.updated_at ? formatDateTime(profile.value.updated_at) : '-',
)

const profileDetails = [
  { labelKey: 'profile.details.organization', valueKey: 'profile.details.organizationValue' },
  { labelKey: 'profile.details.supervisor', valueKey: 'profile.details.supervisorValue' },
  { labelKey: 'profile.details.location', valueKey: 'profile.details.locationValue' },
  { labelKey: 'profile.details.sequence', valueKey: 'profile.details.sequenceValue' },
  { labelKey: 'profile.details.email', value: 'admin@example.com' },
  { labelKey: 'profile.details.phone', value: '13800000001' },
]

const permissionScopes = [
  { titleKey: 'profile.permissions.iamTitle', descriptionKey: 'profile.permissions.iamDesc', authorized: true },
  { titleKey: 'profile.permissions.fileTitle', descriptionKey: 'profile.permissions.fileDesc', authorized: true },
  { titleKey: 'profile.permissions.analysisTitle', descriptionKey: 'profile.permissions.analysisDesc', authorized: true },
  { titleKey: 'profile.permissions.systemTitle', descriptionKey: 'profile.permissions.systemDesc', authorized: false },
]

const securityItems = [
  { labelKey: 'profile.security.password', valueKey: 'profile.security.passwordDesc', status: 'success', icon: LockOutlined },
  {
    labelKey: 'profile.security.mfa',
    valueKey: 'profile.security.mfaDesc',
    status: 'success',
    icon: SafetyCertificateOutlined,
  },
  {
    labelKey: 'profile.security.lastLogin',
    valueKey: 'profile.security.lastLoginDesc',
    status: 'processing',
    icon: ClockCircleOutlined,
  },
  { labelKey: 'profile.security.alert', valueKey: 'profile.security.alertDesc', status: 'success', icon: AuditOutlined },
]

const activityLogs = [
  { time: '09:18', titleKey: 'profile.activity.login', descriptionKey: 'profile.activity.loginDesc' },
  { timeKey: 'profile.activity.yesterday', titleKey: 'profile.activity.review', descriptionKey: 'profile.activity.reviewDesc' },
  { time: '06-18 17:10', titleKey: 'profile.activity.update', descriptionKey: 'profile.activity.updateDesc' },
  { time: '06-18 09:30', titleKey: 'profile.activity.audit', descriptionKey: 'profile.activity.auditDesc' },
]

const responsibilities = computed(() => tm('profile.responsibilitiesList') as string[])

const teams = [
  { nameKey: 'profile.teamsList.platform', roleKey: 'profile.teamsList.platformRole' },
  { nameKey: 'profile.teamsList.audit', roleKey: 'profile.teamsList.auditRole' },
  { nameKey: 'profile.teamsList.infra', roleKey: 'profile.teamsList.infraRole' },
]

const profileMenus = [
  { key: 'profile', titleKey: 'profile.sections.profile', icon: UserOutlined },
  { key: 'permissions', titleKey: 'profile.sections.permissions', icon: IdcardOutlined },
  { key: 'security', titleKey: 'profile.sections.security', icon: SafetyCertificateOutlined },
  { key: 'activity', titleKey: 'profile.sections.activity', icon: AuditOutlined },
  { key: 'teams', titleKey: 'profile.sections.teams', icon: TeamOutlined },
] as const

const activeSectionTitle = computed(
  () => t(profileMenus.find((item) => item.key === activeSection.value)?.titleKey || 'profile.sections.profile'),
)

function handleMenuSelect(key: string | number) {
  activeSection.value = key as ProfileSection
}
</script>

<template>
  <div class="text-slate-700 dark:text-zinc-300">
    <div class="grid grid-cols-1 gap-6 xl:grid-cols-[220px_minmax(0,1fr)]">
      <div>
        <ACard :bordered="false" class="w-full" :body-style="{ padding: '8px' }">
          <AMenu
            mode="inline"
            :selected-keys="[activeSection]"
            class="border-0!"
            @click="({ key }) => handleMenuSelect(key)"
          >
            <AMenuItem v-for="item in profileMenus" :key="item.key">
              <template #icon>
                <component :is="item.icon" />
              </template>
              {{ t(item.titleKey) }}
            </AMenuItem>
          </AMenu>
        </ACard>
      </div>

      <ACard
        :title="activeSectionTitle"
        :bordered="false"
        class="min-w-0"
        :body-style="{ padding: '24px' }"
      >
        <template #extra>
          <AButton v-if="activeSection === 'profile'" type="primary" size="small">
            <template #icon><EditOutlined /></template>
            {{ t('profile.editProfile') }}
          </AButton>
          <AButton v-else-if="activeSection === 'security'" size="small">
            <template #icon><SettingOutlined /></template>
            {{ t('profile.securitySettings') }}
          </AButton>
        </template>

        <div class="mb-6 rounded-2 border border-slate-100 p-4 dark:border-zinc-800 sm:p-5">
          <div class="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
            <div class="flex min-w-0 items-start gap-4 sm:items-center">
              <AAvatar :size="72" class="shrink-0 bg-brand-500 text-28px text-white">
                {{ avatarText }}
              </AAvatar>
              <div class="min-w-0">
                <div class="flex flex-wrap items-center gap-2">
                  <div class="truncate text-20px text-slate-900 font-700 dark:text-zinc-100">
                    {{ profileName }}
                  </div>
                  <ATag color="processing" class="m-0">{{ profileTitle }}</ATag>
                  <ATag color="success" class="m-0">{{ t('profile.active') }}</ATag>
                </div>
                <div
                  class="mt-2 grid grid-cols-1 gap-x-6 gap-y-1 text-13px text-slate-500 dark:text-zinc-400 sm:grid-cols-2"
                >
                  <span class="min-w-0 truncate">{{ t('profile.employeeNo', { value: employeeNo }) }}</span>
                  <span class="min-w-0 truncate">{{ t('profile.organization') }}</span>
                  <span class="min-w-0 break-all">{{ t('profile.accountId', { value: accountId }) }}</span>
                  <span class="min-w-0 truncate">{{ t('profile.updatedAt', { value: updatedAtText }) }}</span>
                </div>
              </div>
            </div>

            <div class="min-w-45">
              <div class="mb-2 flex items-center justify-between">
                <span class="text-13px text-slate-500 dark:text-zinc-400">{{ t('profile.securityScore') }}</span>
                <span class="text-18px text-brand-600 font-700 dark:text-brand-300">96</span>
              </div>
              <AProgress :percent="96" :show-info="false" />
            </div>
          </div>
        </div>

        <div v-if="activeSection === 'profile'" class="space-y-6">
          <ADescriptions bordered :column="{ xs: 1, md: 2 }" size="middle">
            <ADescriptionsItem :label="t('profile.name')">{{ profileName }}</ADescriptionsItem>
            <ADescriptionsItem :label="t('profile.position')">{{ profileTitle }}</ADescriptionsItem>
            <ADescriptionsItem v-for="item in profileDetails" :key="item.labelKey" :label="t(item.labelKey)">
              {{ item.valueKey ? t(item.valueKey) : item.value }}
            </ADescriptionsItem>
            <ADescriptionsItem :label="t('profile.accountType')">{{ t('profile.adminAccount') }}</ADescriptionsItem>
            <ADescriptionsItem :label="t('profile.loginScope')">{{ t('profile.adminConsole') }}</ADescriptionsItem>
          </ADescriptions>

          <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
            <div class="rounded-2 border border-slate-100 p-4 dark:border-zinc-800">
              <div class="flex items-center gap-3">
                <span
                  class="inline-flex h-10 w-10 items-center justify-center rounded-2 bg-blue-50 text-blue-600 dark:bg-blue-500/12 dark:text-blue-300"
                >
                  <MailOutlined />
                </span>
                <div class="min-w-0">
                  <div class="truncate text-13px text-slate-500 dark:text-zinc-400">{{ t('profile.enterpriseEmail') }}</div>
                  <div class="truncate text-14px text-slate-900 font-600 dark:text-zinc-100">
                    admin@example.com
                  </div>
                </div>
              </div>
            </div>
            <div class="rounded-2 border border-slate-100 p-4 dark:border-zinc-800">
              <div class="flex items-center gap-3">
                <span
                  class="inline-flex h-10 w-10 items-center justify-center rounded-2 bg-green-50 text-green-600 dark:bg-green-500/12 dark:text-green-300"
                >
                  <MobileOutlined />
                </span>
                <div class="min-w-0">
                  <div class="truncate text-13px text-slate-500 dark:text-zinc-400">{{ t('profile.boundPhone') }}</div>
                  <div class="truncate text-14px text-slate-900 font-600 dark:text-zinc-100">
                    13800000001
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="activeSection === 'permissions'" class="space-y-6">
          <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
            <div
              v-for="item in permissionScopes"
              :key="item.titleKey"
              class="rounded-2 border border-slate-100 p-4 transition hover:border-brand-300 dark:border-zinc-800 dark:hover:border-brand-500/70"
            >
              <div class="flex items-start justify-between gap-3">
                <div class="min-w-0">
                  <div class="truncate text-15px text-slate-900 font-600 dark:text-zinc-100">
                    {{ t(item.titleKey) }}
                  </div>
                  <div class="mt-1 text-13px text-slate-500 leading-5 dark:text-zinc-400">
                    {{ t(item.descriptionKey) }}
                  </div>
                </div>
                <ATag
                  :color="item.authorized ? 'success' : 'warning'"
                  class="m-0 shrink-0"
                  >{{ item.authorized ? t('profile.authorized') : t('profile.controlled') }}</ATag
                >
              </div>
            </div>
          </div>

          <div>
            <div class="mb-3 text-15px text-slate-900 font-600 dark:text-zinc-100">{{ t('profile.responsibilities') }}</div>
            <div class="flex flex-wrap gap-2">
              <ATag v-for="item in responsibilities" :key="item" color="processing" class="m-0">
                {{ item }}
              </ATag>
            </div>
          </div>
        </div>

        <div v-else-if="activeSection === 'security'" class="space-y-6">
          <div class="rounded-2 border border-slate-100 p-5 dark:border-zinc-800">
            <div class="mb-3 flex items-center justify-between">
              <span class="text-15px text-slate-900 font-600 dark:text-zinc-100">{{ t('profile.securityBaseline') }}</span>
              <span class="text-24px text-brand-600 font-700 dark:text-brand-300">96</span>
            </div>
            <AProgress :percent="96" />
            <div class="mt-2 text-13px text-slate-500 dark:text-zinc-400">
              {{ t('profile.securityBaselineDesc') }}
            </div>
          </div>

          <AList :data-source="securityItems">
            <template #renderItem="{ item }">
              <AListItem class="px-0!">
                <AListItemMeta>
                  <template #avatar>
                    <span
                      class="inline-flex h-10 w-10 items-center justify-center rounded-2 bg-brand-50 text-brand-600 dark:bg-brand-500/12 dark:text-brand-300"
                    >
                      <component :is="item.icon" />
                    </span>
                  </template>
                  <template #title>
                    <div class="flex min-w-0 items-center justify-between gap-2">
                      <span class="truncate text-14px text-slate-900 font-600 dark:text-zinc-100">{{
                        t(item.labelKey)
                      }}</span>
                      <ATag :color="item.status" class="m-0 shrink-0">{{ t('common.normal') }}</ATag>
                    </div>
                  </template>
                  <template #description>
                    <span class="text-13px text-slate-500 dark:text-zinc-400">{{
                      t(item.valueKey)
                    }}</span>
                  </template>
                </AListItemMeta>
              </AListItem>
            </template>
          </AList>
        </div>

        <div v-else-if="activeSection === 'activity'">
          <ATimeline>
            <ATimelineItem v-for="item in activityLogs" :key="item.titleKey">
              <div class="min-w-0">
                <div class="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
                  <span class="truncate text-14px text-slate-900 font-600 dark:text-zinc-100">{{
                    t(item.titleKey)
                  }}</span>
                  <span class="shrink-0 text-12px text-slate-400 dark:text-zinc-500">{{
                    item.timeKey ? t(item.timeKey) : item.time
                  }}</span>
                </div>
                <div class="mt-1 text-13px text-slate-500 leading-5 dark:text-zinc-400">
                  {{ t(item.descriptionKey) }}
                </div>
              </div>
            </ATimelineItem>
          </ATimeline>
        </div>

        <div v-else class="space-y-4">
          <AList :data-source="teams">
            <template #renderItem="{ item }">
              <AListItem class="px-0!">
                <AListItemMeta>
                  <template #avatar>
                    <AAvatar
                      class="bg-slate-100 text-slate-600 dark:bg-zinc-800 dark:text-zinc-300"
                    >
                      {{ t(item.nameKey).slice(0, 1) }}
                    </AAvatar>
                  </template>
                  <template #title>
                    <span class="text-slate-900 font-600 dark:text-zinc-100">{{ t(item.nameKey) }}</span>
                  </template>
                  <template #description>{{ t(item.roleKey) }}</template>
                </AListItemMeta>
              </AListItem>
            </template>
          </AList>

          <div class="rounded-2 border border-slate-100 p-4 dark:border-zinc-800">
            <div class="text-15px text-slate-900 font-600 dark:text-zinc-100">{{ t('profile.collaborationNote') }}</div>
            <div class="mt-2 text-13px text-slate-500 leading-6 dark:text-zinc-400">
              {{ t('profile.collaborationDesc') }}
            </div>
          </div>
        </div>
      </ACard>
    </div>
  </div>
</template>
