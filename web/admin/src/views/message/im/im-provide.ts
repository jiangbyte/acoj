import type { InjectionKey, Ref } from 'vue'
import type { MockApplicationRequest, MockFriend, MockGroup, MockSystemNotice, MockTodoItem } from './mock'

export interface ImActions {
  goHome: () => void
  openProfileModal: () => void
  goProfileCenter: () => void
  handleLogout: () => void
  openThread: (threadId: string, syncRoute?: boolean, openChat?: boolean) => void
  closeCurrentThread: () => void
  sendMessage: () => void
  openChatSection: () => void
  openContactsSection: () => void
  openNoticeSection: () => void
  openTodosSection: () => void
  openProfileSection: () => void
  backToListPane: () => void
  openFriend: (friend: MockFriend) => void
  openGroup: (group: MockGroup) => void
  openNoticeDetail: (notice: MockSystemNotice) => void
  openTodoDetail: (todo: MockTodoItem) => void
  openPendingDetail: (request: MockApplicationRequest) => void
  closePendingDetail: () => void
  openAddModal: (mode?: "friend" | "group") => void
  markTodoDone: (todo: MockTodoItem) => void
  markTodoCancelled: (todo: MockTodoItem) => void
}

export interface ImUIState {
  activeSection: Ref<string>
  isMobile: Ref<boolean>
  mobileView: Ref<string>
  showProfileModal: Ref<boolean>
  searchText: Ref<string>
  hasSearchKeyword: Ref<boolean>
  searchScope: Ref<string>
  contactTab: Ref<string>
  noticeTab: Ref<string>
  todoTab: Ref<string>
}

export const IM_ACTIONS_KEY: InjectionKey<ImActions> = Symbol('im-actions')
export const IM_UI_STATE_KEY: InjectionKey<ImUIState> = Symbol('im-ui-state')

import type { MockImData } from './mock'
export const IM_DATA_KEY: InjectionKey<MockImData> = Symbol('im-data')
