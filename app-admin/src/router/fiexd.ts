import type { RouteRecordRaw } from 'vue-router'

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'root',
    redirect: '/webroot',
    children: [],
  },
  // Login page
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login.vue'),
    meta: {
      title: 'Login',
      withoutTab: true,
    },
  },
  // Error page
  // 404 page
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
