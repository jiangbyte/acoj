import type { RouteRecordRaw } from 'vue-router'

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'root',
    redirect: '/webroot',
    children: [],
  },
  // 登录页面
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login.vue'),
    meta: {
      title: 'Login',
      withoutTab: true,
    },
  },
  // 错误页面
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/404.vue'),
    name: '404',
    meta: {
      title: 'Not Found',
      icon: 'icon-park-outline:ghost',
      withoutTab: true,
    },
  },
]
