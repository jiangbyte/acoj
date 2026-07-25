import type { Message } from '~/utils/ai'

export interface Conversation {
  id: string
  title: string
  updatedAt: string
  messages: Message[]
}

interface DateGroup {
  label: string
  conversations: Conversation[]
}

const STORAGE_KEY = 'hei-chat-history'

function loadConversations(): Conversation[] {
  if (import.meta.client) {
    try {
      const raw = localStorage.getItem(STORAGE_KEY)
      if (raw) return JSON.parse(raw)
    } catch {
      // ignore parse errors
    }
  }
  return []
}

function saveConversations(conversations: Conversation[]) {
  if (import.meta.client) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(conversations))
  }
}

const conversations = ref<Conversation[]>(loadConversations())
const activeId = ref<string | null>(null)
const sidebarOpen = ref(true)
const searchOpen = ref(false)

export function useChatHistory() {
  function getActiveConversation(): Conversation | undefined {
    return conversations.value.find((c) => c.id === activeId.value)
  }

  function newConversation(): string {
    const id = crypto.randomUUID()
    const now = new Date().toISOString()
    conversations.value = [
      {
        id,
        title: '新对话',
        updatedAt: now,
        messages: [],
      },
      ...conversations.value,
    ]
    activeId.value = id
    persist()
    return id
  }

  function selectConversation(id: string) {
    activeId.value = id
  }

  function deleteConversation(id: string) {
    conversations.value = conversations.value.filter((c) => c.id !== id)
    if (activeId.value === id) {
      activeId.value = conversations.value[0]?.id ?? null
    }
    persist()
  }

  function renameConversation(id: string, title: string) {
    const conv = conversations.value.find((c) => c.id === id)
    if (conv) {
      conv.title = title
      persist()
    }
  }

  function persist() {
    saveConversations(conversations.value)
  }

  const groups = computed<DateGroup[]>(() => {
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const yesterday = new Date(today)
    yesterday.setDate(yesterday.getDate() - 1)
    const lastWeek = new Date(today)
    lastWeek.setDate(lastWeek.getDate() - 7)

    const groups: Record<string, Conversation[]> = {
      今天: [],
      昨天: [],
      上周: [],
      更早: [],
    }

    for (const conv of conversations.value) {
      const date = new Date(conv.updatedAt)
      if (date >= today) {
        groups['今天'].push(conv)
      } else if (date >= yesterday) {
        groups['昨天'].push(conv)
      } else if (date >= lastWeek) {
        groups['上周'].push(conv)
      } else {
        groups['更早'].push(conv)
      }
    }

    return Object.entries(groups)
      .filter(([, convs]) => convs.length > 0)
      .map(([label, convs]) => ({ label, conversations: convs }))
  })

  return {
    conversations,
    activeId,
    sidebarOpen,
    searchOpen,
    groups,
    getActiveConversation,
    newConversation,
    selectConversation,
    deleteConversation,
    renameConversation,
    persist,
  }
}
