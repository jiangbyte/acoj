import { http } from '@/utils'

const prefix = '/api/v1/admin/message'

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

export function notificationPage(params?: any) {
  return http.get<any>(`${prefix}/notifications/page`, { params })
}

export function notificationDetail(params: any) {
  return http.get<any>(`${prefix}/notifications/detail`, { params })
}

export function createNotification(data: any) {
  return http.post<any>(`${prefix}/notifications/create`, data)
}

export function updateNotification(data: any) {
  return http.post<any>(`${prefix}/notifications/update`, data)
}

export function publishNotification(data: any) {
  return http.post<any>(`${prefix}/notifications/publish`, data)
}

export function revokeNotification(data: any) {
  return http.post<any>(`${prefix}/notifications/revoke`, data)
}

export function removeNotification(data: any) {
  return http.post<any>(`${prefix}/notifications/delete`, data)
}

export function myGroups() {
  return http.get<any[]>(`${prefix}/messages/groups`)
}

export function groupPage(params?: any) {
  return http.get<any>(`${prefix}/groups/page`, { params })
}

export function groupDetail(params: any) {
  return http.get<any>(`${prefix}/groups/detail`, { params })
}

export function groupMembers(params: any) {
  return http.get<any[]>(`${prefix}/groups/members`, { params })
}

export function createGroup(data: any) {
  return http.post<any>(`${prefix}/groups/create`, data)
}

export function updateGroup(data: any) {
  return http.post<any>(`${prefix}/groups/update`, data)
}

export function removeGroup(data: any) {
  return http.post<any>(`${prefix}/groups/delete`, data)
}

export function addGroupMembers(data: any) {
  return http.post<any>(`${prefix}/groups/add-members`, data)
}

export function removeGroupMembers(data: any) {
  return http.post<any>(`${prefix}/groups/remove-members`, data)
}

export function myThreads(params?: any) {
  return http.get<any>(`${prefix}/messages/threads`, { params })
}

export function threadPage(params?: any) {
  return http.get<any>(`${prefix}/threads/page`, { params })
}

export function threadMessage(params?: any) {
  return http.get<any>(`${prefix}/threads/messages`, { params })
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

export function sendSystemMessage(data: any) {
  return http.post<any>(`${prefix}/threads/send-system`, data)
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

export function todoPage(params?: any) {
  return http.get<any>(`${prefix}/todos/page`, { params })
}

export function todoDetail(params: any) {
  return http.get<any>(`${prefix}/todos/detail`, { params })
}

export function createTodo(data: any) {
  return http.post<any>(`${prefix}/todos/create`, data)
}

export function updateTodo(data: any) {
  return http.post<any>(`${prefix}/todos/update`, data)
}

export function removeTodo(data: any) {
  return http.post<any>(`${prefix}/todos/delete`, data)
}

export function cancelTodoAdmin(data: any) {
  return http.post<any>(`${prefix}/todos/cancel-admin`, data)
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

export function myFriends() {
  return http.get<any[]>(`${prefix}/friends/my-list`)
}

export function friendSearch(keyword: string) {
  return http.get<any[]>(`${prefix}/friends/search`, { params: { keyword } })
}

export function applyFriend(data: any) {
  return http.post<any>(`${prefix}/friends/apply`, data)
}

export function handleFriendRequest(data: any) {
  return http.post<any>(`${prefix}/friends/handle-request`, data)
}

export function removeFriend(data: any) {
  return http.post<any>(`${prefix}/friends/remove`, data)
}

export function setFriendRemark(data: any) {
  return http.post<any>(`${prefix}/friends/set-remark`, data)
}

export function myFriendRequests() {
  return http.get<any[]>(`${prefix}/friends/my-requests`)
}

export function myFriendRequestCount() {
  return http.get<any>(`${prefix}/friends/my-request-count`)
}

export function applyJoinGroup(data: any) {
  return http.post<any>(`${prefix}/messages/groups/join-request`, data)
}

export function handleJoinGroupRequest(data: any) {
  return http.post<any>(`${prefix}/messages/groups/handle-join-request`, data)
}

export function myJoinRequests() {
  return http.get<any[]>(`${prefix}/messages/groups/my-join-requests`)
}

export function groupJoinRequests(groupId: string) {
  return http.get<any[]>(`${prefix}/messages/groups/join-requests`, { params: { group_id: groupId } })
}

export function pendingJoinRequestCount() {
  return http.get<any>(`${prefix}/messages/groups/pending-join-request-count`)
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
