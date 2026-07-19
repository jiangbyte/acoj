import * as messageApi from '@/api/message'
import type { MockFriend, MockGroup, MockImData, MockSystemNotice, MockThread } from './mock'

/**
 * Load IM data from API with mock fallback.
 * Returns the same MockImData structure that the IM page expects.
 */
export async function loadImData(): Promise<Partial<MockImData>> {
  try {
    const [meRes, threadsRes, friendsRes, groupsRes] = await Promise.all([
      import('@/api/auth').then(m => m.me()).catch(() => null),
      messageApi.myThreads({ current: 1, size: 50 }).catch(() => null),
      messageApi.myFriends().catch(() => null),
      messageApi.myGroups().catch(() => null),
    ])

    const profile = meRes?.data
    return {
      profile: profile ? {
        name: profile.name ?? '',
        account: profile.account ?? '',
        nickname: profile.nickname ?? profile.name ?? '',
        title: profile.profile?.title ?? '',
        department: profile.dept_id_names?.map((d: any) => d.name).join('、') ?? '',
        role: profile.role_id_names?.map((r: any) => r.name).join('、') ?? '',
        signature: profile.profile?.signature ?? '',
        phone: profile.profile?.phone ?? '',
        email: profile.profile?.email ?? '',
        avatar: profile.profile?.avatar ?? profile.avatar ?? '',
        avatarText: (profile.nickname ?? profile.name ?? '?').charAt(0),
        statusText: '在线',
      } : undefined,
      threads: mapThreads(threadsRes?.data?.records ?? threadsRes?.data),
      friends: mapFriends(friendsRes?.data),
      groups: mapGroups(groupsRes?.data),
    }
  } catch {
    return {}
  }
}

export async function loadNotifications(page = 1, size = 20): Promise<MockSystemNotice[] | null> {
  try {
    const res = await messageApi.myNotification({ current: page, size })
    const records = res?.data?.records ?? []
    return records.map((n: any) => ({
      id: n.id ?? '',
      title: n.title ?? '',
      content: n.content ?? '',
      severity: (n.severity ?? 'INFO').toLowerCase() as 'info' | 'warning' | 'error',
      read: n.is_read ?? false,
      createdAt: n.created_at ?? new Date().toISOString(),
    }))
  } catch {
    return null
  }
}

export async function readNotificationApi(ids: string[]) {
  try {
    await messageApi.readNotification({ ids })
    return true
  } catch {
    return false
  }
}

export async function readAllNotificationApi() {
  try {
    await messageApi.readAllNotification()
    return true
  } catch {
    return false
  }
}


export async function loadThreadMessages(threadId: string, page = 1, size = 20) {
  try {
    const res = await messageApi.myThreadMessage({ thread_id: threadId, current: page, size })
    return res?.data?.records ?? res?.data ?? []
  } catch {
    return null
  }
}

export async function sendMessageApi(data: {
  thread_id?: string
  group_id?: string
  participant_refs?: Array<{ account_type: string; account_id: string }>
  title?: string
  content: string
  sender_name?: string
}) {
  try {
    const res = await messageApi.sendMessage(data)
    return res?.data ?? null
  } catch {
    return null
  }
}

export async function createGroupApi(data: {
  name: string
  description?: string
  avatar?: string
  member_refs?: Array<{ account_type: string; account_id: string }>
}) {
  try {
    await messageApi.createGroup(data)
    return true
  } catch {
    return false
  }
}

export async function addGroupMembersApi(groupId: string, memberRefs: Array<{ account_type: string; account_id: string }>) {
  try {
    await messageApi.addGroupMembers({ group_id: groupId, member_refs: memberRefs })
    return true
  } catch {
    return false
  }
}

export async function deleteFriendApi(friendAccountType: string, friendAccountId: string) {
  try {
    await messageApi.removeFriend({ friend_account_type: friendAccountType, friend_account_id: friendAccountId })
    return true
  } catch {
    return false
  }
}

export async function searchUsersApi(keyword: string) {
  try {
    const res = await messageApi.friendSearch(keyword)
    return res?.data ?? []
  } catch {
    return []
  }
}

export async function applyFriendApi(friendAccountType: string, friendAccountId: string, message?: string) {
  try {
    await messageApi.applyFriend({ friend_account_type: friendAccountType, friend_account_id: friendAccountId, message })
    return true
  } catch {
    return false
  }
}

export async function applyJoinGroupApi(groupId: string, message?: string) {
  try {
    await messageApi.applyJoinGroup({ group_id: groupId, message })
    return true
  } catch {
    return false
  }
}

export async function handleFriendRequestApi(requestId: string, accept: boolean) {
  try {
    await messageApi.handleFriendRequest({ request_id: requestId, accept })
    return true
  } catch {
    return false
  }
}

export async function loadFriendRequestsApi() {
  try {
    const res = await messageApi.myFriendRequests()
    return res?.data ?? []
  } catch {
    return []
  }
}

export async function loadMyJoinRequestsApi() {
  try {
    const res = await messageApi.myJoinRequests()
    return res?.data ?? []
  } catch {
    return []
  }
}

export async function loadGroupJoinRequestsApi(groupId: string) {
  try {
    const res = await messageApi.groupJoinRequests(groupId)
    return res?.data ?? []
  } catch {
    return []
  }
}

export async function handleJoinGroupRequestApi(requestId: string, accept: boolean) {
  try {
    await messageApi.handleJoinGroupRequest({ request_id: requestId, accept })
    return true
  } catch {
    return false
  }
}

function mapThreads(raw: any[] | null | undefined): MockThread[] {
  if (!raw?.length) return []
  return raw.map((t: any) => ({
    id: t.id ?? '',
    kind: t.thread_type === 'GROUP' ? 'group' : 'direct',
    title: t.title ?? '',
    subtitle: '',
    avatarText: (t.title ?? '').charAt(0) || '?',
    lastMessage: t.last_message?.content ?? '',
    lastMessageAt: t.last_message_at ?? t.created_at ?? new Date().toISOString(),
    unreadCount: t.unread_count ?? 0,
    pinned: false,
    muted: false,
    contactId: '',
    groupId: t.group_id,
  }))
}

function mapFriends(raw: any[] | null | undefined): MockFriend[] {
  if (!raw?.length) return []
  return raw.map((f: any) => ({
    id: f.friend_account_id ?? f.id ?? '',
    name: f.friend_name ?? '',
    title: f.friend_title ?? '',
    department: f.friend_department ?? '',
    avatarText: (f.friend_name ?? '').charAt(0) || '?',
    statusText: '在线',
    signature: '',
    threadId: `thread-${f.friend_account_id ?? ''}`,
  }))
}

function mapGroups(raw: any[] | null | undefined): MockGroup[] {
  if (!raw?.length) return []
  return raw.map((g: any) => ({
    id: g.id ?? '',
    name: g.name ?? '',
    description: g.description ?? '',
    memberCount: g.member_count ?? 0,
    avatarText: (g.name ?? '').charAt(0) || '?',
    statusText: '活跃',
    threadId: g.id ? `thread-${g.id}` : '',
  }))
}
