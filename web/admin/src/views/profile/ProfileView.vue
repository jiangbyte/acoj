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

import { useUserStore } from '@/stores/user'
import { formatDateTime } from '@hei/shared'

type ProfileSection = 'profile' | 'permissions' | 'security' | 'activity' | 'teams'

const user = useUserStore()
const profile = computed(() => user.profile)
const activeSection = ref<ProfileSection>('profile')

const profileName = computed(() => profile.value?.real_name || '系统管理员')
const profileTitle = computed(() => profile.value?.title || '平台管理员')
const avatarText = computed(() => profileName.value.slice(0, 1))
const employeeNo = computed(() => profile.value?.employee_no || '-')
const accountId = computed(() => profile.value?.account_id || user.me?.account_id || '-')
const updatedAtText = computed(() =>
  profile.value?.updated_at ? formatDateTime(profile.value.updated_at) : '-',
)

const profileDetails = [
  { label: '所属组织', value: '总部 / 平台管理部' },
  { label: '直属主管', value: 'CTO 办公室' },
  { label: '办公地点', value: '上海总部 A 座 18F' },
  { label: '岗位序列', value: '技术管理 / 平台治理' },
  { label: '邮箱地址', value: 'admin@example.com' },
  { label: '手机号码', value: '13800000001' },
]

const permissionScopes = [
  { title: '身份权限管理', description: '账号、角色、资源与数据范围维护', status: '已授权' },
  { title: '文件服务治理', description: '文件审计、存储状态与访问记录查看', status: '已授权' },
  { title: '运营分析看板', description: '治理趋势、风险分布与模块健康度查看', status: '已授权' },
  { title: '系统配置', description: '关键系统参数调整需二次审批', status: '受控' },
]

const securityItems = [
  { label: '登录密码', value: '已启用强密码策略', status: 'success', icon: LockOutlined },
  {
    label: '多因素认证',
    value: '企业微信动态口令已绑定',
    status: 'success',
    icon: SafetyCertificateOutlined,
  },
  {
    label: '最近登录',
    value: '2026-06-20 09:18 上海',
    status: 'processing',
    icon: ClockCircleOutlined,
  },
  { label: '异常提醒', value: '近 7 日未发现高危登录行为', status: 'success', icon: AuditOutlined },
]

const activityLogs = [
  { time: '09:18', title: '完成后台登录', description: '通过企业内网网关访问管理端' },
  { time: '昨天 18:42', title: '复核角色授权', description: '确认审计只读角色的数据范围' },
  { time: '06-18 17:10', title: '更新个人资料', description: '同步岗位信息和组织归属' },
  { time: '06-18 09:30', title: '查看文件审计报表', description: '导出 system-audit-202606.csv' },
]

const responsibilities = [
  '账号生命周期治理',
  '角色权限复核',
  '资源树维护',
  '文件审计确认',
  '平台风险闭环',
]

const teams = [
  { name: '平台管理组', role: '核心负责人' },
  { name: '合规审计组', role: '协同审批' },
  { name: '基础设施组', role: '文件服务对接' },
]

const profileMenus = [
  { key: 'profile', title: '个人档案', icon: UserOutlined },
  { key: 'permissions', title: '权限职责', icon: IdcardOutlined },
  { key: 'security', title: '账号安全', icon: SafetyCertificateOutlined },
  { key: 'activity', title: '近期活动', icon: AuditOutlined },
  { key: 'teams', title: '协作团队', icon: TeamOutlined },
] as const

const activeSectionTitle = computed(
  () => profileMenus.find((item) => item.key === activeSection.value)?.title || '个人档案',
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
              {{ item.title }}
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
            编辑资料
          </AButton>
          <AButton v-else-if="activeSection === 'security'" size="small">
            <template #icon><SettingOutlined /></template>
            安全设置
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
                  <ATag color="success" class="m-0">在职</ATag>
                </div>
                <div
                  class="mt-2 grid grid-cols-1 gap-x-6 gap-y-1 text-13px text-slate-500 dark:text-zinc-400 sm:grid-cols-2"
                >
                  <span class="min-w-0 truncate">员工编号：{{ employeeNo }}</span>
                  <span class="min-w-0 truncate">所属组织：平台管理部</span>
                  <span class="min-w-0 break-all">账号 ID：{{ accountId }}</span>
                  <span class="min-w-0 truncate">更新时间：{{ updatedAtText }}</span>
                </div>
              </div>
            </div>

            <div class="min-w-45">
              <div class="mb-2 flex items-center justify-between">
                <span class="text-13px text-slate-500 dark:text-zinc-400">安全评分</span>
                <span class="text-18px text-brand-600 font-700 dark:text-brand-300">96</span>
              </div>
              <AProgress :percent="96" :show-info="false" />
            </div>
          </div>
        </div>

        <div v-if="activeSection === 'profile'" class="space-y-6">
          <ADescriptions bordered :column="{ xs: 1, md: 2 }" size="middle">
            <ADescriptionsItem label="姓名">{{ profileName }}</ADescriptionsItem>
            <ADescriptionsItem label="职位">{{ profileTitle }}</ADescriptionsItem>
            <ADescriptionsItem v-for="item in profileDetails" :key="item.label" :label="item.label">
              {{ item.value }}
            </ADescriptionsItem>
            <ADescriptionsItem label="账号类型">后台管理员</ADescriptionsItem>
            <ADescriptionsItem label="登录范围">管理后台</ADescriptionsItem>
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
                  <div class="truncate text-13px text-slate-500 dark:text-zinc-400">企业邮箱</div>
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
                  <div class="truncate text-13px text-slate-500 dark:text-zinc-400">绑定手机</div>
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
              :key="item.title"
              class="rounded-2 border border-slate-100 p-4 transition hover:border-brand-300 dark:border-zinc-800 dark:hover:border-brand-500/70"
            >
              <div class="flex items-start justify-between gap-3">
                <div class="min-w-0">
                  <div class="truncate text-15px text-slate-900 font-600 dark:text-zinc-100">
                    {{ item.title }}
                  </div>
                  <div class="mt-1 text-13px text-slate-500 leading-5 dark:text-zinc-400">
                    {{ item.description }}
                  </div>
                </div>
                <ATag
                  :color="item.status === '已授权' ? 'success' : 'warning'"
                  class="m-0 shrink-0"
                  >{{ item.status }}</ATag
                >
              </div>
            </div>
          </div>

          <div>
            <div class="mb-3 text-15px text-slate-900 font-600 dark:text-zinc-100">岗位职责</div>
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
              <span class="text-15px text-slate-900 font-600 dark:text-zinc-100">企业安全基线</span>
              <span class="text-24px text-brand-600 font-700 dark:text-brand-300">96</span>
            </div>
            <AProgress :percent="96" />
            <div class="mt-2 text-13px text-slate-500 dark:text-zinc-400">
              当前账号已满足强密码、多因素认证、可信登录环境和审计留痕要求。
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
                        item.label
                      }}</span>
                      <ATag :color="item.status" class="m-0 shrink-0">正常</ATag>
                    </div>
                  </template>
                  <template #description>
                    <span class="text-13px text-slate-500 dark:text-zinc-400">{{
                      item.value
                    }}</span>
                  </template>
                </AListItemMeta>
              </AListItem>
            </template>
          </AList>
        </div>

        <div v-else-if="activeSection === 'activity'">
          <ATimeline>
            <ATimelineItem v-for="item in activityLogs" :key="item.title">
              <div class="min-w-0">
                <div class="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
                  <span class="truncate text-14px text-slate-900 font-600 dark:text-zinc-100">{{
                    item.title
                  }}</span>
                  <span class="shrink-0 text-12px text-slate-400 dark:text-zinc-500">{{
                    item.time
                  }}</span>
                </div>
                <div class="mt-1 text-13px text-slate-500 leading-5 dark:text-zinc-400">
                  {{ item.description }}
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
                      {{ item.name.slice(0, 1) }}
                    </AAvatar>
                  </template>
                  <template #title>
                    <span class="text-slate-900 font-600 dark:text-zinc-100">{{ item.name }}</span>
                  </template>
                  <template #description>{{ item.role }}</template>
                </AListItemMeta>
              </AListItem>
            </template>
          </AList>

          <div class="rounded-2 border border-slate-100 p-4 dark:border-zinc-800">
            <div class="text-15px text-slate-900 font-600 dark:text-zinc-100">协作说明</div>
            <div class="mt-2 text-13px text-slate-500 leading-6 dark:text-zinc-400">
              当前账号负责平台管理组的权限治理，并与合规审计组、基础设施组共同完成授权复核和文件审计闭环。
            </div>
          </div>
        </div>
      </ACard>
    </div>
  </div>
</template>
