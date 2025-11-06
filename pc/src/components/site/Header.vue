<script setup lang="ts">
import { useTokenStore } from '@/stores'
import { RouterLink } from 'vue-router'
import { useSysUserFetch } from '@/composables/v1'
import { iconRender } from '@/utils'

// 定义props
const props = defineProps({
  className: {
    type: String,
    default: 'max-w-1400px', // 默认值
  },
})

const scrollY = ref(0)
const isMobile = ref(false)
const show = ref(false)
const profile = ref()

const { getProfile } = useSysUserFetch()

// 检测是否为移动设备
function checkIfMobile() {
  const userAgent = navigator.userAgent.toLowerCase()
  const mobileKeywords = ['mobile', 'android', 'iphone', 'ipad', 'ipod', 'windows phone']
  isMobile.value = mobileKeywords.some(keyword => userAgent.includes(keyword)) || window.innerWidth < 768
}

const menuOptions = [
  {
    label: () =>
      h(
        RouterLink,
        {
          to: {
            path: '/',
          },
        },
        { default: () => '首页' },
      ),
    key: 'home',
    icon: iconRender('icon-park-outline:home'),
  },
  {
    label: () =>
      h(
        RouterLink,
        {
          to: {
            path: '/problems',
          },
        },
        { default: () => '题库' },
      ),
    key: 'problems',
    icon: iconRender('icon-park-outline:book'),
  },
  {
    label: () =>
      h(
        RouterLink,
        {
          to: {
            path: '/sets',
          },
        },
        { default: () => '题集/训练' },
      ),
    key: 'sets',
    icon: iconRender('icon-park-outline:folder-open'),
  },
  // {
  //   label: () =>
  //     h(
  //       RouterLink,
  //       {
  //         to: {
  //           path: '/groups',
  //         },
  //       },
  //       { default: () => '群组' },
  //     ),
  //   key: 'groups',
  // },
  {
    label: () =>
      h(
        RouterLink,
        {
          to: {
            path: '/contests',
          },
        },
        { default: () => '竞赛' },
      ),
    key: 'contests',
    icon: iconRender('icon-park-outline:folder-open'),
  },
  {
    label: () =>
      h(
        RouterLink,
        {
          to: {
            path: '/ranking',
          },
        },
        { default: () => '排行榜' },
      ),
    key: 'ranking',
    icon: iconRender('icon-park-outline:ranking'),
  },
  {
    label: () =>
      h(
        RouterLink,
        {
          to: {
            path: '/status',
          },
        },
        { default: () => '状态' },
      ),
    key: 'status',
    icon: iconRender('icon-park-outline:hourglass'),
  },
]

function handleScroll() {
  scrollY.value = window.scrollY
}

function handleResize() {
  checkIfMobile()
}

function doOpen() {
  show.value = true
}

function doClose() {
  show.value = false
}

async function loadData() {
  const { data } = await getProfile()
  profile.value = data
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  window.addEventListener('resize', handleResize)
  checkIfMobile()
  if (useTokenStore().isLogined) {
    loadData()
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <header
    class="header z-9999"
    :class="{ 'header-shadow': scrollY > 10 }"
  >
    <div class="flex items-center justify-between w-full h-full p-x-5 flex-1 mx-auto" :class="props.className">
      <div class="flex items-center gap-4">
        <Logo />
        <n-menu
          v-if="!isMobile"
          mode="horizontal"
          :options="menuOptions"
          responsive
        />
      </div>
      <div class="flex items-center gap-4">
        <template v-if="!isMobile">
          <Dropdown v-if="useTokenStore().isLogined">
            <template #avatar>
              <n-space align="center">
                <n-avatar
                  round
                  :src="profile?.avatar"
                />
                <n-text strong>
                  {{ profile?.nickname }}
                </n-text>
              </n-space>
            </template>
          </Dropdown>
          <n-space v-else>
            <n-button data-testid="login-btn" @click="$router.push('/login')">
              登录
            </n-button>
            <n-button data-testid="register-btn" @click="$router.push('/register')">
              注册
            </n-button>
          </n-space>
        </template>
        <template v-else>
          <n-space v-if="useTokenStore().isLogined" align="center" @click="doOpen">
            <n-avatar
              round
              :src="profile?.avatar"
            />
            <n-text strong>
              {{ profile?.nickname }}
            </n-text>
          </n-space>
          <n-button
            v-else
            text
            @click="doOpen"
          >
            <template #icon>
              <icon-park-outline-hamburger-button />
            </template>
          </n-button>
        </template>
      </div>
    </div>

    <!-- 移动端抽屉菜单 -->
    <n-drawer
      v-if="isMobile"
      v-model:show="show"
      :width="300"
      placement="right"
      :trap-focus="false"
    >
      <n-drawer-content
        title="Astro Code"
        closable
        class="z-999"
        @close="doClose"
      >
        <template #header>
          <n-flex justify="start">
            <Logo />
          </n-flex>
        </template>
        <!-- <n-space v-if="useTokenStore().isLogined" align="center" justify="center">
          <UserAvatar />
        </n-space> -->

        <n-menu
          :options="menuOptions"
          responsive
        />

        <n-divider v-if="!useTokenStore().isLogined" />
        <div v-if="!useTokenStore().isLogined" class="mobile-auth-actions">
          <n-button
            type="primary"
            size="large"
            ghost
            block
            @click="$router.push('/login')"
          >
            登录
          </n-button>
          <n-button
            type="primary"
            size="large"
            block
            style="margin-top: 12px"
            @click="$router.push('/register')"
          >
            注册
          </n-button>
        </div>
        <Dropdown v-if="useTokenStore().isLogined" :dropdown="false" class="w-full" />
      </n-drawer-content>
    </n-drawer>
  </header>
</template>

<style scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 72px;
  background-color: white;
  z-index: 1000;
  transition: box-shadow 0.3s ease;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.header-shadow {
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.mobile-auth-actions {
  padding: 0 12px;
}
</style>
