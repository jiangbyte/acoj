import { createRouter, createWebHistory } from 'vue-router'

import PortalLayout from '@/layouts/PortalLayout.vue'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: PortalLayout,
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('@/views/home/HomeView.vue'),
          meta: { title: '首页', titleKey: 'routes.home' },
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/profile/ProfileView.vue'),
          meta: { title: '用户中心', titleKey: 'routes.profile', requiresAuth: true },
        },
      ],
    },
    {
      path: '/',
      component: () => import('@/layouts/BlankLayout.vue'),
      children: [
        {
          path: 'login',
          name: 'Login',
          component: () => import('@/views/auth/LoginView.vue'),
          meta: { title: '登录', titleKey: 'routes.login' },
        },
        {
          path: 'register',
          name: 'Register',
          component: () => import('@/views/auth/RegisterView.vue'),
          meta: { title: '注册', titleKey: 'routes.register' },
        },
      ],
    },
    {
      path: '/',
      component: PortalLayout,
      children: [
        {
          path: ':pathMatch(.*)*',
          name: 'NotFound',
          component: () => import('@/views/system/NotFoundView.vue'),
          meta: { title: '页面不存在', titleKey: 'routes.notFound' },
        },
      ],
    },
  ],
  scrollBehavior: (to) => {
    if (to.hash) {
      return { el: to.hash, top: 72, behavior: 'smooth' }
    }
    return { left: 0, top: 0 }
  },
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  const user = useUserStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    }
  }

  if (auth.isAuthenticated && (to.name === 'Login' || to.name === 'Register')) {
    return {
      path: '/',
      replace: true,
    }
  }

  if (to.meta.requiresAuth && auth.isAuthenticated) {
    try {
      await user.ensureMe()
    } catch {
      auth.clearSession()
      user.clear()
      return {
        path: '/login',
        query: { redirect: to.fullPath },
      }
    }
  }

  return true
})
