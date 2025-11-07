import type { RouteRecordRaw } from 'vue-router'

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'root',
    redirect: '/home', // 默认重定向到首页
    component: () => import('@/layouts/index.vue'), // 主布局组件
    children: [
      // 首页
      {
        path: 'home',
        name: 'home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页', icon: 'home' },
      },
      // 题库
      {
        path: 'problems',
        name: 'problems',
        component: () => import('@/views/problems/index.vue'),
        meta: { title: '题库', icon: 'book' },
      },
      // 题库
      {
        path: 'groups',
        name: 'groups',
        component: () => import('@/views/groups/index.vue'),
        meta: { title: '群组', icon: 'book' },
      },
      {
        path: 'contests',
        name: 'contests',
        component: () => import('@/views/contests/index.vue'),
        meta: { title: '竞赛', icon: 'folder' },
      },
      // 题单详情
      {
        path: 'contests/detail',
        name: 'contest_detail',
        component: () => import('@/views/contests/detail.vue'),
        meta: { title: '竞赛详情', icon: 'folder' },
        beforeEnter: (to) => {
          if (!to.query.contestId) {
            return '/'
          }
        },
      },
      // 排行榜
      {
        path: 'ranking',
        name: 'ranking',
        component: () => import('@/views/ranking/index.vue'),
        meta: { title: '排行榜', icon: 'trophy' },
      },
      // 题单
      {
        path: 'sets',
        name: 'sets',
        component: () => import('@/views/sets/index.vue'),
        meta: { title: '题集/训练', icon: 'folder' },
      },
      // 题单详情
      {
        path: 'sets/detail',
        name: 'proset_detail',
        component: () => import('@/views/sets/detail.vue'),
        meta: { title: '题集/训练详情', icon: 'folder' },
        beforeEnter: (to) => {
          if (!to.query.set) {
            return '/'
          }
        },
      },
      // 提交记录（仍使用主布局）
      {
        path: 'status',
        name: 'status',
        component: () => import('@/views/status/index.vue'),
        meta: { title: '提交记录', icon: 'list' },
      },
      {
        path: 'status/problem',
        name: 'problem_submit_detail',
        component: () => import('@/views/status/problem.vue'),
        meta: { title: '提交记录', icon: 'list' },
        beforeEnter: (to) => {
          if (!to.query.submit) {
            return '/'
          }
        },
      },
      {
        path: '/user',
        name: 'user',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户主页' },
        beforeEnter: (to) => {
          if (!to.query.userId) {
            return '/'
          }
        },
      },
      {
        path: '/profile',
        name: 'profile',
        component: () => import('@/views/user/profile.vue'),
        meta: { title: '个人信息' },
        // beforeEnter: (to) => {
        //   if (!to.query.user) {
        //     return '/'
        //   }
        // },
      },
      {
        path: '/notice',
        name: 'notice_detail',
        component: () => import('@/views/notice/notice.vue'),
        meta: { title: '公开公告' },
        beforeEnter: (to) => {
          if (!to.query.notice) {
            return '/'
          }
        },
      },
    ],
  },
  // 提交布局路由（独立布局，无菜单）
  {
    path: '/submit',
    name: 'submit-root',
    component: () => import('@/layouts/ProblemSubmit.vue'), // 提交专用布局
    children: [
      // 提交页面（全屏代码编辑）
      {
        path: '/p',
        name: 'problem_submit',
        component: () => import('@/views/problems/submit.vue'),
        meta: { title: '提交代码' },
        beforeEnter: (to) => {
          if (!to.query.problemId) {
            return '/'
          }
        },
      },
      {
        path: '/s',
        name: 'set_submit',
        component: () => import('@/views/sets/submit.vue'),
        meta: { title: '提交代码' },
        // beforeEnter: (to) => {
        //   if (!to.query.setId || !to.query.problemId) {
        //     return '/'
        //   }
        // },
      },
      {
        path: '/c',
        name: 'contest_submit',
        component: () => import('@/views/contests/submit.vue'),
        meta: { title: '提交代码' },
        // beforeEnter: (to) => {
        //   if (!to.query.setId || !to.query.problemId) {
        //     return '/'
        //   }
        // },
      },
    ],
  },
  // 公开路由（无布局）
  // 登录页面
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login.vue'),
    meta: {
      title: '登录',
    },
  },
  // 注册页面
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/register.vue'),
    meta: {
      title: '注册',
    },
  },
  // 重置密码
  {
    path: '/forget',
    name: 'forget',
    component: () => import('@/views/forget.vue'),
    meta: {
      title: '重置密码',
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
    },
  },
]
