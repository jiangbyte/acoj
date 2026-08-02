export default defineNuxtRouteMiddleware((to) => {
  const { isLoggedIn } = useAuth()
  if (isLoggedIn.value) {
    return
  }
  return navigateTo({
    path: '/auth/login',
    query: { redirect: to.fullPath },
  })
})
