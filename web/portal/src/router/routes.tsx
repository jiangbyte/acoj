import { Navigate, type RouteObject } from 'react-router-dom'
import { MainLayout, SolveLayout } from '@/layouts'
import { HomePage } from '@/pages/home'
import { LoginPage } from '@/pages/auth/login'
import { RegisterPage } from '@/pages/auth/register'
import { ForgotPasswordPage } from '@/pages/auth/forgot-password'
import { NotFoundPage } from '@/pages/error/not-found'
import { PlaceholderPage } from '@/pages/placeholder'
import { UserCenterPage } from '@/pages/usercenter'
import { ProblemListPage } from '@/pages/problems'
import { ProblemDetailPage } from '@/pages/problems/detail'
import { ContestListPage } from '@/pages/contests'
import { ContestDetailPage } from '@/pages/contests/detail'
import { ContestProblemPage } from '@/pages/contests/problem'
import { SubmissionListPage } from '@/pages/submissions'
import { SubmissionDetailPage } from '@/pages/submissions/detail'
import { guestOnly, requireAuth } from './guard'

export const routes: RouteObject[] = [
  {
    element: <SolveLayout />,
    children: [
      { path: '/problems/:id', element: <ProblemDetailPage /> },
      { path: '/contests/:id/problems/:problemId', element: <ContestProblemPage /> },
    ],
  },
  {
    path: '/',
    element: <MainLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'problems', element: <ProblemListPage /> },
      { path: 'contests', element: <ContestListPage /> },
      { path: 'contests/:id', element: <ContestDetailPage /> },
      { path: 'submissions', element: <SubmissionListPage /> },
      { path: 'submissions/:id', element: <SubmissionDetailPage /> },
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
