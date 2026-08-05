import { Navigate, type RouteObject } from 'react-router-dom'
import { MainLayout, SolveLayout } from '@/layouts'
import { HomePage } from '@/pages/home'
import { LoginPage } from '@/pages/auth/login'
import { RegisterPage } from '@/pages/auth/register'
import { ForgotPasswordPage } from '@/pages/auth/forgot-password'
import { NotFoundPage } from '@/pages/error/not-found'
import { UserCenterPage } from '@/pages/usercenter'
import { ProfilePage } from '@/pages/profile'
import { RankPage } from '@/pages/rank'
import { ProblemListPage } from '@/pages/problems'
import { ProblemDetailPage } from '@/pages/problems/detail'
import { LearningPlanListPage } from '@/pages/plans'
import { LearningPlanDetailPage } from '@/pages/plans/detail'
import { ProblemListDetailPage } from '@/pages/lists/detail'
import { ContestListPage } from '@/pages/contests'
import { ContestDetailPage } from '@/pages/contests/detail'
import { ContestProblemPage } from '@/pages/contests/problem'
import { SubmissionListPage } from '@/pages/submissions'
import { SubmissionDetailPage } from '@/pages/submissions/detail'
import { MessagesPage } from '@/pages/messages'
import { ClassListPage } from '@/pages/classes'
import { ClassDetailPage } from '@/pages/classes/detail'
import { CourseDetailPage } from '@/pages/courses/detail'
import { CourseListPage } from '@/pages/courses'
import { CourseTaskPage } from '@/pages/courses/task'
import { TeamListPage } from '@/pages/teams'
import { TeamDetailPage } from '@/pages/teams/detail'
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
      { path: 'plans', element: <LearningPlanListPage /> },
      { path: 'plans/detail', element: <LearningPlanDetailPage /> },
      { path: 'lists/detail', element: <ProblemListDetailPage /> },
      { path: 'contests', element: <ContestListPage /> },
      { path: 'contests/:id', element: <ContestDetailPage /> },
      { path: 'classes', element: <ClassListPage /> },
      { path: 'classes/:id', element: <ClassDetailPage /> },
      { path: 'courses', element: <CourseListPage /> },
      { path: 'courses/:id', element: <CourseDetailPage /> },
      { path: 'courses/:id/tasks/:taskId', element: <CourseTaskPage /> },
      { path: 'teams', element: <TeamListPage /> },
      { path: 'teams/:id', element: <TeamDetailPage /> },
      { path: 'submissions', element: <SubmissionListPage /> },
      { path: 'submissions/:id', element: <SubmissionDetailPage /> },
      { path: 'rank', element: <RankPage /> },
      {
        path: 'messages',
        loader: requireAuth,
        element: <MessagesPage />,
      },
      {
        path: 'profile',
        element: <ProfilePage />,
      },
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
