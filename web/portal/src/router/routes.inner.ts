import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/layouts/index.vue'

/**
 * 内置基础路由。
 *
 * portalRoot 是前台门户的自定义布局根节点。静态页面在这里直接声明，授权动态页面会在
 * route store 初始化时追加到 portalRoot 下，二者可以同时存在。
 */
export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'portalRoot',
    redirect: import.meta.env.VITE_HOME_PATH,
    component: Layout,
    children: [
      {
        path: '/home',
        name: 'home',
        component: () => import('@/views/home/index.vue'),
        meta: {
          name: 'Home',
          public: true,
          icon: 'icon-park-outline:home',
        },
      },
      {
        path: '/usercenter',
        name: 'usercenter',
        component: () => import('@/views/usercenter/index.vue'),
        meta: {
          name: 'User Center',
          locale_key: 'app.user_center.title',
          icon: 'icon-park-outline:user',
        },
      },
      {
        path: '/messages',
        name: 'messages',
        component: () => import('@/views/messages/index.vue'),
        meta: {
          name: 'Messages',
          locale_key: 'app.messages_center',
          icon: 'icon-park-outline:message',
        },
      },
      {
        path: '/space',
        name: 'my-space',
        component: () => import('@/views/space/index.vue'),
        meta: {
          name: 'My Space',
          locale_key: 'app.my_space',
          icon: 'icon-park-outline:user-positioning',
        },
      },
      {
        path: '/space/:accountId',
        name: 'space',
        component: () => import('@/views/space/index.vue'),
        meta: {
          name: 'Space',
          locale_key: 'app.space.title',
          public: true,
          icon: 'icon-park-outline:user-positioning',
        },
      },
    ],
  },
  {
    path: '/not-found',
    name: 'not-found',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { name: 'Not Found', public: true },
  },
  {
    path: '/auth',
    name: 'auth',
    redirect: '/auth/login',
    meta: { public: true },
  },
  {
    path: '/auth/login',
    name: 'auth-login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { name: 'Login', public: true },
  },
  {
    path: '/auth/register',
    name: 'auth-register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { name: 'Register', public: true },
  },
  {
    path: '/auth/forgot-password',
    name: 'auth-forgot-password',
    component: () => import('@/views/auth/ForgotPassword.vue'),
    meta: { name: 'Forgot Password', public: true },
  },
  {
    path: '/auth/reset-password',
    name: 'auth-reset-password',
    component: () => import('@/views/auth/ResetPassword.vue'),
    meta: { name: 'Reset Password', public: true },
  },
  {
    // 兜底路由必须放在最后。动态路由注册完成后，守卫会重新匹配原始目标地址。
    path: '/:pathMatch(.*)*',
    name: 'not-found-catch',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { name: 'Not Found', public: true },
  },
]
