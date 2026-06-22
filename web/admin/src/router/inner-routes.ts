import type { RouteRecordRaw } from 'vue-router'

export const INNER_ROUTE_NAMES = {
  Root: 'Root',
  AdminRoot: 'AdminRoot',
  Login: 'Login',
  Forbidden: 'Forbidden',
  ServerError: 'ServerError',
  NotFound: 'NotFound',
} as const

export const innerRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    name: INNER_ROUTE_NAMES.Root,
    component: () => import('@/layouts/BlankLayout.vue'),
    meta: { title: '首页', requiresAuth: true, withoutTab: true },
  },
  {
    path: '/auth',
    component: () => import('@/layouts/BlankLayout.vue'),
    meta: { title: '认证', hideInMenu: true, withoutTab: true },
    children: [
      {
        path: 'login',
        name: INNER_ROUTE_NAMES.Login,
        component: () => import('@/views/auth/LoginView.vue'),
        meta: { title: '登录', hideInMenu: true, withoutTab: true },
      },
    ],
  },
  {
    path: '/403',
    name: INNER_ROUTE_NAMES.Forbidden,
    component: () => import('@/views/system/ForbiddenView.vue'),
    meta: { title: '无权限', hideInMenu: true, withoutTab: true },
  },
  {
    path: '/500',
    name: INNER_ROUTE_NAMES.ServerError,
    component: () => import('@/views/system/ServerErrorView.vue'),
    meta: { title: '系统异常', hideInMenu: true, withoutTab: true },
  },
  {
    path: '/:pathMatch(.*)*',
    name: INNER_ROUTE_NAMES.NotFound,
    component: () => import('@/views/system/NotFoundView.vue'),
    meta: { title: '页面不存在', hideInMenu: true, withoutTab: true },
  },
]
