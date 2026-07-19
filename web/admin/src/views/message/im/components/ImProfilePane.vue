<script setup lang="ts">
import { computed, inject } from 'vue'
import { useThemeVars } from 'naive-ui'
import { resolveFileUrl } from '@/utils'

import { IM_ACTIONS_KEY, IM_DATA_KEY } from '../im-provide'

const themeVars = useThemeVars()

const data = inject(IM_DATA_KEY)!
const actions = inject(IM_ACTIONS_KEY)!

const avatarImgProps = { referrerPolicy: 'no-referrer' } as any
const avatarUrl = computed(() => resolveFileUrl(data.profile.avatar))
</script>

<template>
  <div class="flex h-full min-h-0 flex-col">
    <NScrollbar class="h-full">
      <div class="flex flex-col">

        <!-- 头部个人信息区域 -->
        <div
          class="flex flex-col items-center px-4 pb-6 pt-8 text-white"
          :style="{ backgroundColor: themeVars.primaryColor }"
        >
          <NAvatar
            v-if="avatarUrl"
            round
            :size="80"
            :src="avatarUrl"
            :img-props="avatarImgProps"
            class="shrink-0 border-2 shadow-lg"
            :style="{ borderColor: 'rgba(255,255,255,0.3)' }"
          />
          <NAvatar
            v-else
            round
            :size="80"
            class="shrink-0 border-2 shadow-lg"
            :style="{ borderColor: 'rgba(255,255,255,0.3)' }"
          >
            <NovaIcon icon="icon-park-outline:user" :size="40" />
          </NAvatar>
          <div class="mt-3 text-lg font-600">
            {{ data.profile.nickname }}
          </div>
          <div
            v-if="data.profile.title || data.profile.department"
            class="mt-0.5 text-sm"
            style="color: rgba(255,255,255,0.7)"
          >
            {{ data.profile.title
            }}<template v-if="data.profile.title && data.profile.department">
              ·
            </template>{{ data.profile.department }}
          </div>
        </div>

        <!-- 信息列表 -->
        <div
          class="flex flex-col gap-0 px-4 py-3"
          :style="{ backgroundColor: themeVars.cardColor }"
        >
          <div
            class="flex items-center justify-between border-b py-4"
            :style="{ borderColor: themeVars.borderColor }"
          >
            <span class="text-sm" :style="{ color: themeVars.textColor3 }">账号</span>
            <span class="text-sm">{{ data.profile.account }}</span>
          </div>
          <div
            class="flex items-center justify-between border-b py-4"
            :style="{ borderColor: themeVars.borderColor }"
          >
            <span class="text-sm" :style="{ color: themeVars.textColor3 }">标题</span>
            <span class="text-sm">{{ data.profile.title }}</span>
          </div>
          <div
            class="flex items-center justify-between border-b py-4"
            :style="{ borderColor: themeVars.borderColor }"
          >
            <span class="text-sm" :style="{ color: themeVars.textColor3 }">部门</span>
            <span class="text-sm">{{ data.profile.department }}</span>
          </div>
          <div
            class="flex items-center justify-between border-b py-4"
            :style="{ borderColor: themeVars.borderColor }"
          >
            <span class="text-sm" :style="{ color: themeVars.textColor3 }">角色</span>
            <span class="text-sm">{{ data.profile.role }}</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="flex flex-col gap-3 px-4 pt-4 pb-8">
          <NButton type="primary" block @click="actions.goProfileCenter()">
            个人中心
          </NButton>
          <NButton quaternary block type="error" @click="actions.handleLogout()">
            退出登录
          </NButton>
        </div>

      </div>
    </NScrollbar>
  </div>
</template>
