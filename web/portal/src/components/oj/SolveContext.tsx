import { createContext, useContext, type ReactNode } from 'react'
import { useSolveContext, type SolveContextValue } from '@/hooks/useSolveContext'

const SolveCtx = createContext<SolveContextValue | null>(null)

export function SolveContextProvider({
  problemId,
  children,
}: {
  problemId: string
  children: ReactNode
}) {
  const value = useSolveContext(problemId)
  return <SolveCtx.Provider value={value}>{children}</SolveCtx.Provider>
}

export function useSolveSession() {
  const ctx = useContext(SolveCtx)
  if (!ctx) {
    throw new Error('useSolveSession must be used within SolveContextProvider')
  }
  return ctx
}

export function useOptionalSolveSession() {
  return useContext(SolveCtx)
}
