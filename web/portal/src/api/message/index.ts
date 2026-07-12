import { http } from '@/utils'

const prefix = '/api/v1/portal/message'

export function summary() {
  return http.get<any>(`${prefix}/summary`)
}

export function headerItems() {
  return http.get<any[]>(`${prefix}/header-items`)
}

export function myNotification(params?: any) {
  return http.get<any>(`${prefix}/notifications/my-page`, { params })
}

export function myNotificationDetail(params: any) {
  return http.get<any>(`${prefix}/notifications/my-detail`, { params })
}

export function readNotification(data: any) {
  return http.post<any>(`${prefix}/notifications/read`, data)
}

export function readAllNotification() {
  return http.post<any>(`${prefix}/notifications/read-all`)
}

export function myGroups() {
  return http.get<any[]>(`${prefix}/messages/groups`)
}

export function createGroup(data: any) {
  return http.post<any>(`${prefix}/messages/groups/create`, data)
}

export function addGroupMembers(data: any) {
  return http.post<any>(`${prefix}/messages/groups/add-members`, data)
}

export function removeGroupMembers(data: any) {
  return http.post<any>(`${prefix}/messages/groups/remove-members`, data)
}

export function myThreads(params?: any) {
  return http.get<any>(`${prefix}/messages/threads`, { params })
}

export function myThreadMessage(params?: any) {
  return http.get<any>(`${prefix}/messages/thread-messages`, { params })
}

export function sendMessage(data: any) {
  return http.post<any>(`${prefix}/messages/send`, data)
}

export function replyMessage(data: any) {
  return http.post<any>(`${prefix}/messages/reply`, data)
}

export function readThread(data: any) {
  return http.post<any>(`${prefix}/messages/read-thread`, data)
}

export function reactMessage(data: any) {
  return http.post<any>(`${prefix}/messages/react`, data)
}

export function myTodos(params?: any) {
  return http.get<any>(`${prefix}/todos/my-page`, { params })
}

export function myTodoDetail(params: any) {
  return http.get<any>(`${prefix}/todos/my-detail`, { params })
}

export function startTodo(data: any) {
  return http.post<any>(`${prefix}/todos/start`, data)
}

export function completeTodo(data: any) {
  return http.post<any>(`${prefix}/todos/complete`, data)
}

export function cancelTodo(data: any) {
  return http.post<any>(`${prefix}/todos/cancel`, data)
}

export function createEventSource() {
  const token = localStorage.getItem('token')
  if (!token) {
    return null
  }
  const baseURL = import.meta.env.VITE_API_URL || ''
  const url = `${baseURL}${prefix}/realtime/events?token=${encodeURIComponent(token)}`
  return new EventSource(url)
}
