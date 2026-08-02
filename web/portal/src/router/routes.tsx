import { Navigate, type RouteObject } from 'react-router-dom'
import { MainLayout } from '@/layouts'
import { HomePage } from '@/pages/home'
import { LoginPage } from '@/pages/auth/login'
import { RegisterPage } from '@/pages/auth/register'
import { ForgotPasswordPage } from '@/pages/auth/forgot-password'
import { NotFoundPage } from '@/pages/error/not-found'
import { PlaceholderPage } from '@/pages/placeholder'
import { UserCenterPage } from '@/pages/usercenter'
import { guestOnly, requireAuth } from './guard'

export const routes: RouteObject[] = [
  {
    path: '/',
    element: <MainLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'problems', element: <PlaceholderPage title="题库" /> },
      { path: 'contests', element: <PlaceholderPage title="竞赛" /> },
      { path: 'submissions', element: <PlaceholderPage title="提交" /> },
      { path: 'rank', element: <PlaceholderPage title="排名" /> },
      {
        path: 'usercenter',
        loader: requireAuth,
        element: <UserCenterPage />,
      },
      {
        path: 'auth/login',
        loader: guestOnly,
        element: <LoginPage />,
      },
      {
        path: 'auth/register',
        loader: guestOnly,
        element: <RegisterPage />,
      },
      {
        path: 'auth/forgot-password',
        loader: guestOnly,
        element: <ForgotPasswordPage />,
      },
    ],
  },
  { path: '/404', element: <NotFoundPage /> },
  { path: '*', element: <Navigate to="/404" replace /> },
]
