import { http } from '@/utils'
import type { PageData } from '@/typing/api'

const prefix = '/api/v1/portal/message'

export type AccountRef = { account_type: string; account_id: string }

export interface ImFriend {
  friendship_id: string
  account_type: string
  account_id: string
  friend_account_type: string
  friend_account_id: string
  name: string | null
  nickname: string | null
  avatar: string | null
  signature: string | null
  remark: string | null
  friend_at: string
}

export interface ImSearchUser {
  account_type: string
  account_id: string
  account: string | null
  name: string | null
  nickname: string | null
  avatar: string | null
  signature: string | null
  is_friend: boolean
  has_pending_request?: boolean
}

export interface ImFriendRequest {
  id: string
  applicant_type: string
  applicant_id: string
  applicant_name: string | null
  applicant_avatar: string | null
  recipient_type: string
  recipient_id: string
  recipient_name?: string | null
  recipient_avatar?: string | null
  message: string | null
  status: string
  created_at: string
}

export interface ImGroupJoinRequest {
  id: string
  group_id: string
  group_name?: string | null
  applicant_type: string
  applicant_id: string
  applicant_name: string | null
  applicant_avatar: string | null
  message: string | null
  status: string
  created_at: string
}

export interface ImNotification {
  id: string
  title: string
  content: string
  content_type: string
  category: string
  severity: string
  is_read: boolean
  created_at: string
}

export interface ImGroup {
  id: string
  name: string
  avatar: string | null
  description: string | null
  owner_account_type: string
  owner_account_id: string
  status: string
  join_mode: string
  max_members: number
  member_count: number
  is_member?: boolean
  has_pending_request?: boolean
}

export interface ImGroupMember {
  id: string
  group_id: string
  account_type: string
  account_id: string
  role: string
  nickname: string | null
  profile_name: string | null
  profile_avatar: string | null
}

export interface ImConversationMember {
  account_type: string
  account_id: string
  role: string
  unread_count: number
  profile_name: string | null
  profile_avatar: string | null
}

export interface ImConversation {
  id: string
  conversation_type: string
  title: string | null
  avatar: string | null
  group_id: string | null
  owner_account_type: string | null
  owner_account_id: string | null
  status: string
  last_message_id: string | null
  last_message_at: string | null
  /** 读模型：最近一条消息正文（列表预览） */
  last_message?: string | null
  created_at?: string | null
  unread_count: number
  members: ImConversationMember[]
}

export interface ImMessageAttachment {
  file_id?: string | null
  name: string
  url: string
  content_type?: string | null
  size?: number | null
  attachment_type?: string
}

export interface ImMessage {
  id: string
  conversation_id: string
  msg_type: string
  parent_id: string | null
  sender_account_type: string | null
  sender_account_id: string | null
  sender_name: string | null
  sender_nickname?: string | null
  sender_avatar: string | null
  content: string
  content_type: string
  is_revoked: boolean
  created_at: string
  attachments?: ImMessageAttachment[]
}

export const imApi = {
  conversationList: (params?: { current?: number; size?: number }) =>
    http.get<PageData<ImConversation>>(`${prefix}/conversations/my-list`, { params }),
  conversationDetail: (id: string) =>
    http.get<ImConversation>(`${prefix}/conversations/detail`, { params: { id } }),
  createDirect: (data: AccountRef) =>
    http.post<ImConversation>(`${prefix}/conversations/create-direct`, data),
  markConversationRead: (data: { id: string }) =>
    http.post<null>(`${prefix}/conversations/mark-read`, data),
  leaveConversation: (data: { id: string }) =>
    http.post<null>(`${prefix}/conversations/leave`, data),

  sendMessage: (data: {
    conversation_id?: string
    group_id?: string
    content: string
    content_type?: string
    msg_type?: string
    attachments?: ImMessageAttachment[]
  }) => http.post<ImMessage>(`${prefix}/messages/send`, data),
  messagePage: (params: { conversation_id: string; current?: number; size?: number }) =>
    http.get<PageData<ImMessage>>(`${prefix}/messages/page`, { params }),
  unreadCount: (conversationId: string) =>
    http.get<{ unread_count: number }>(`${prefix}/messages/unread-count`, {
      params: { conversation_id: conversationId },
    }),
  revokeMessage: (data: { message_id: string }) =>
    http.post<null>(`${prefix}/messages/revoke`, data),

  friendList: () => http.get<ImFriend[]>(`${prefix}/friends/my-list`),
  searchUsers: (keyword: string) =>
    http.get<ImSearchUser[]>(`${prefix}/friends/search`, { params: { keyword } }),
  applyFriend: (data: {
    applicant_type: string
    applicant_id: string
    recipient_type: string
    recipient_id: string
    message?: string
  }) => http.post<null>(`${prefix}/friends/apply`, data),
  handleFriendRequest: (data: { request_id: string; action: string }) =>
    http.post<null>(`${prefix}/friends/handle-request`, data),
  removeFriend: (data: { friendship_id: string }) =>
    http.post<null>(`${prefix}/friends/remove`, data),
  myFriendRequests: () => http.get<ImFriendRequest[]>(`${prefix}/friends/my-requests`),
  myFriendRequestCount: () =>
    http.get<{ pending_count: number } | number>(`${prefix}/friends/my-request-count`),

  groupList: () => http.get<ImGroup[]>(`${prefix}/groups/my-list`),
  searchGroups: (keyword: string) =>
    http.get<ImGroup[]>(`${prefix}/groups/search`, { params: { keyword } }),
  createGroup: (data: { name: string; description?: string; join_mode?: string }) =>
    http.post<ImGroup>(`${prefix}/groups/create`, data),
  dissolveGroup: (data: { id: string }) => http.post<null>(`${prefix}/groups/dissolve`, data),
  leaveGroup: (data: { id: string }) => http.post<null>(`${prefix}/groups/leave`, data),
  groupMemberList: (id: string) =>
    http.get<ImGroupMember[]>(`${prefix}/groups/members/list`, { params: { id } }),
  addGroupMembers: (data: { group_id: string; members: AccountRef[] }) =>
    http.post<null>(`${prefix}/groups/members/add`, data),
  removeGroupMember: (data: { group_id: string } & AccountRef) =>
    http.post<null>(`${prefix}/groups/members/remove`, data),
  setGroupMemberRole: (data: { group_id: string; role: string } & AccountRef) =>
    http.post<null>(`${prefix}/groups/members/set-role`, data),
  applyJoinGroup: (data: { group_id: string; message?: string }) =>
    http.post<null>(`${prefix}/groups/join-requests/apply`, data),
  handleJoinGroupRequest: (data: { id: string; status: string }) =>
    http.post<null>(`${prefix}/groups/join-requests/handle`, data),
  myJoinRequests: () => http.get<ImGroupJoinRequest[]>(`${prefix}/groups/join-requests/my`),
  pendingJoinRequests: () => http.get<ImGroupJoinRequest[]>(`${prefix}/groups/join-requests/pending`),
  pendingJoinRequestCount: () => http.get<number>(`${prefix}/groups/join-requests/pending-count`),

  notificationPage: (params?: { current?: number; size?: number }) =>
    http.get<PageData<ImNotification>>(`${prefix}/notifications/my-page`, { params }),
  notificationDetail: (id: string) =>
    http.get<ImNotification>(`${prefix}/notifications/my-detail`, { params: { id } }),
  notificationUnreadCount: () => http.get<number>(`${prefix}/notifications/unread-count`),
  readNotifications: (ids: string[]) =>
    http.post<null>(`${prefix}/notifications/read`, { ids }),
  readAllNotifications: () => http.post<null>(`${prefix}/notifications/read-all`),
}
