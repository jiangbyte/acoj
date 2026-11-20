import type { RouteRecordRaw } from 'vue-router'

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'root',
    redirect: '/home', // Implementation note.
    component: () => import('@/layouts/index.vue'), // Implementation note.
    children: [
      // Home
      {
        path: 'home',
        name: 'home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: 'Home', icon: 'home' },
      },
      // Problems
      {
        path: 'problems',
        name: 'problems',
        component: () => import('@/views/problems/index.vue'),
        meta: { title: 'Problems', icon: 'book' },
      },
      // Problems
      {
        path: 'groups',
        name: 'groups',
        component: () => import('@/views/groups/index.vue'),
        meta: { title: 'Groups', icon: 'book' },
      },
      {
        path: 'contests',
        name: 'contests',
        component: () => import('@/views/contests/index.vue'),
        meta: { title: 'Contests', icon: 'folder' },
      },
      // Implementation note.
      {
        path: 'contests/detail',
        name: 'contest_detail',
        component: () => import('@/views/contests/detail.vue'),
        meta: { title: 'Contest Details', icon: 'folder' },
        beforeEnter: (to) => {
          if (!to.query.contestId) {
            return '/'
          }
        },
      },
      // Rankings
      {
        path: 'ranking',
        name: 'ranking',
        component: () => import('@/views/ranking/index.vue'),
        meta: { title: 'Rankings', icon: 'trophy' },
      },
      // Implementation note.
      {
        path: 'sets',
        name: 'sets',
        component: () => import('@/views/sets/index.vue'),
        meta: { title: 'Problem Sets', icon: 'folder' },
      },
      // Implementation note.
      {
        path: 'sets/detail',
        name: 'proset_detail',
        component: () => import('@/views/sets/detail.vue'),
        meta: { title: 'Problem Set Details', icon: 'folder' },
        beforeEnter: (to) => {
          if (!to.query.set) {
            return '/'
          }
        },
      },
      // Implementation note.
      {
        path: 'status',
        name: 'status',
        component: () => import('@/views/status/index.vue'),
        meta: { title: 'Submissions', icon: 'list' },
      },
      {
        path: 'status/problem',
        name: 'problem_submit_detail',
        component: () => import('@/views/status/problem.vue'),
        meta: { title: 'Submissions', icon: 'list' },
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
        meta: { title: 'User Profile' },
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
        meta: { title: 'Profile' },
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
        meta: { title: 'Public Notice' },
        beforeEnter: (to) => {
          if (!to.query.notice) {
            return '/'
          }
        },
      },
    ],
  },
  // Implementation note.
  {
    path: '/submit',
    name: 'submit-root',
    component: () => import('@/layouts/ProblemSubmit.vue'), // Implementation note.
    children: [
      // Implementation note.
      {
        path: '/p',
        name: 'problem_submit',
        component: () => import('@/views/problems/submit.vue'),
        meta: { title: 'Submit Code' },
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
        meta: { title: 'Submit Code' },
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
        meta: { title: 'Submit Code' },
        // beforeEnter: (to) => {
        //   if (!to.query.setId || !to.query.problemId) {
        //     return '/'
        //   }
        // },
      },
    ],
  },
  // Implementation note.
  // Implementation note.
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login.vue'),
    meta: {
      title: 'Sign In',
    },
  },
  // Implementation note.
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/register.vue'),
    meta: {
      title: 'Sign Up',
    },
  },
  // Reset Password
  {
    path: '/forget',
    name: 'forget',
    component: () => import('@/views/forget.vue'),
    meta: {
      title: 'Reset Password',
    },
  },
  // Implementation note.
  // Implementation note.
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/404.vue'),
    name: '404',
    meta: {
      title: 'Not Found',
    },
  },
]
