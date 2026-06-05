# IM Module Redesign Plan

## Overview

The IM module evolved from a simple site messaging (站内信) system. This redesign unifies
the data model, simplifies the architecture, and adds complete IM features including
friend management, group chat, site-wide broadcast, and cross-instance real-time delivery.

## 1. API Routes

All IM APIs are under `/api/v1/sys/im/` (B端/admin) and `/api/v1/c/im/` (C端/consumer).

### 1.1 Message (单聊消息)

| Method | Route | Description |
|--------|-------|-------------|
| POST | `/message/send` | Send a single-chat message |
| POST | `/message/recall` | Recall a message (within 5 min) |
| POST | `/message/forward` | Forward message to another conversation |
| POST | `/message/delete` | Soft-delete message for self |
| GET  | `/message/search?keyword=` | Search messages across conversations |
| GET  | `/message/page` | Message list (admin panel) |
| GET  | `/message/detail?id=` | Message detail |
| GET  | `/message/unread-count` | Total unread count |

### 1.2 Conversation (会话)

| Method | Route | Description |
|--------|-------|-------------|
| GET  | `/conversation/list` | Conversation list (single + group merged) |
| GET  | `/conversation/messages` | Messages in a conversation (cursor pagination) |
| POST | `/conversation/read` | Mark conversation as read |
| POST | `/conversation/get-or-create` | Get or create conversation ID by user pair |

### 1.3 Friend (好友)

| Method | Route | Description |
|--------|-------|-------------|
| POST | `/friend/send-request` | Send friend request (+ WS to receiver) |
| POST | `/friend/accept` | Accept friend request (+ WS to sender) |
| POST | `/friend/reject` | Reject friend request |
| GET  | `/friend/list` | Friend list |
| GET  | `/friend/pending-requests` | Pending requests (incoming + outgoing) |
| POST | `/friend/remove` | Remove/unfriend |
| POST | `/friend/block` | Block user |
| POST | `/friend/unblock` | Unblock user |
| GET  | `/friend/block-list` | Block list |
| POST | `/friend/remark` | Edit friend remark/alias |
| GET  | `/friend/search?keyword=` | Search users |

### 1.4 Group (群组)

| Method | Route | Description |
|--------|-------|-------------|
| POST | `/group/create` | Create group |
| POST | `/group/update` | Update group info (name/avatar/notice) |
| POST | `/group/dissolve` | Dissolve group (owner only) |
| GET  | `/group/detail?group_id=` | Group detail |
| GET  | `/group/my-groups` | My groups |
| GET  | `/group/search-groups?keyword=` | Search public groups (return `is_member`) |
| POST | `/group/join` | Send join request (creates GroupJoinRequest) |
| POST | `/group/handle-join-request` | Approve/reject join request |
| GET  | `/group/pending-join-requests?group_id=` | Pending join requests (admin view) |
| POST | `/group/invite` | Invite members |
| POST | `/group/kick` | Kick member |
| POST | `/group/leave` | Leave group |
| POST | `/group/set-role` | Set/remove admin role |
| POST | `/group/transfer-owner` | Transfer ownership |
| POST | `/group/mute` | Mute member |
| POST | `/group/unmute` | Unmute member |
| POST | `/group/set-nickname` | Set group nickname |
| GET  | `/group/members?group_id=` | Member list |
| POST | `/group/send` | Send group message |
| POST | `/group/recall` | Recall group message |
| GET  | `/group/search?group_id=&keyword=` | Search messages within group |
| POST | `/group/mark-read` | Mark group conversation read |
| GET  | `/group/messages` | Group message timeline |

### 1.5 Broadcast (全站通知)

| Method | Route | Description |
|--------|-------|-------------|
| POST | `/broadcast/send` | Send broadcast (to ALL / BUSINESS / CONSUMER) |
| GET  | `/broadcast/list` | Broadcast list (admin) |
| GET  | `/broadcast/unread-list` | Unread broadcasts (current user) |
| POST | `/broadcast/read` | Mark broadcast as read |
| GET  | `/broadcast/detail?id=` | Broadcast detail |

### 1.6 WebSocket

| Method | Route | Description |
|--------|-------|-------------|
| GET  | `/ws?token=` | WebSocket connection for business users |
| GET  | `/ws?token=` | WebSocket connection for consumer users |

## 2. Data Models

All IM tables use the `im_` prefix. The models are registered via `db.RegisterModel()`
in their respective packages and auto-migrated by GORM.

### 2.1 im_message — Single-chat Messages

Replaces both `sys_message` and `client_message`. User type is distinguished by
`sender_type` / `receiver_type` fields, not by table.

```go
type Message struct {
    ID             string     `gorm:"primaryKey;size:32"`
    ConversationID string     `gorm:"size:32;not null;index"`
    Content        string     `gorm:"type:text"`
    Extra          string     `gorm:"type:text"`          // JSON
    MsgType        string     `gorm:"size:20;default:TEXT"`
    SenderID       string     `gorm:"size:32;index"`
    SenderType     string     `gorm:"size:20"`            // BUSINESS | CONSUMER
    ReceiverID     string     `gorm:"size:32;index"`
    ReceiverType   string     `gorm:"size:20"`            // BUSINESS | CONSUMER
    ReplyTo        string     `gorm:"size:32;index"`      // replied message ID
    Status         string     `gorm:"size:10;default:unread"` // unread | read | deleted
    CreatedAt      *time.Time
    UpdatedAt      *time.Time
}

func (Message) TableName() string { return "im_message" }
```

**Message Types (`MsgType`):**

| Type | Extra Fields | Description |
|------|-------------|-------------|
| `TEXT` | — | Plain text |
| `IMAGE` | `{ w, h, thumbnail, format }` | Image with thumbnail |
| `FILE` | `{ name, size, mime }` | File attachment |
| `VIDEO` | `{ duration, thumbnail, size, format }` | Video message |
| `AUDIO` | `{ duration, size }` | Voice message |
| `SYSTEM` | `{ action, operator_id, user_id }` | System notification |
| `REPLY` | `{ reply_msg_id, reply_content, reply_sender_id }` | Quoted reply |
| `FORWARD` | `{ orig_conversation_id, orig_sender_id, orig_msg_type }` | Forwarded message |

### 2.2 im_conversation — Conversation Cache

Caches the latest message info for each conversation to avoid expensive aggregation queries.

```go
type Conversation struct {
    ID            string     `gorm:"primaryKey;size:32"`
    Type          string     `gorm:"size:10;not null"`    // single | group
    LastContent   string     `gorm:"type:text"`
    LastMsgType   string     `gorm:"size:20"`
    LastSenderID  string     `gorm:"size:32"`
    LastTime      *time.Time
    CreatedAt     *time.Time
}

func (Conversation) TableName() string { return "im_conversation" }
```

### 2.3 im_conversation_unread — Per-user Unread Counter

```go
type ConversationUnread struct {
    ConversationID string `gorm:"primaryKey;size:32"`
    UserID         string `gorm:"primaryKey;size:32"`
    UserType       string `gorm:"primaryKey;size:20"`
    UnreadCount    int64  `gorm:"default:0"`
    LastReadAt     *time.Time
}

func (ConversationUnread) TableName() string { return "im_conversation_unread" }
```

### 2.4 im_friend_request — Friend Requests

```go
type FriendRequest struct {
    ID           string     `gorm:"primaryKey;size:32"`
    SenderID     string     `gorm:"size:32;not null;index:idx_sender_status"`
    SenderType   string     `gorm:"size:20;not null"`
    ReceiverID   string     `gorm:"size:32;not null;index:idx_receiver_status"`
    ReceiverType string     `gorm:"size:20;not null"`
    Remark       string     `gorm:"size:255"`
    Status       string     `gorm:"size:10;default:pending"` // pending | accepted | rejected
    CreatedAt    *time.Time
    UpdatedAt    *time.Time
}

func (FriendRequest) TableName() string { return "im_friend_request" }
```

### 2.5 im_friendship — Friend Relationships

```go
type Friendship struct {
    ID         string     `gorm:"primaryKey;size:32"`
    UserID     string     `gorm:"size:32;not null;uniqueIndex:idx_friend_pair"`
    UserType   string     `gorm:"size:20;not null;uniqueIndex:idx_friend_pair"`
    FriendID   string     `gorm:"size:32;not null;uniqueIndex:idx_friend_pair"`
    FriendType string     `gorm:"size:20;not null;uniqueIndex:idx_friend_pair"`
    Remark     string     `gorm:"size:100"`       // alias/remark for this friend
    CreatedAt  *time.Time
}

func (Friendship) TableName() string { return "im_friendship" }
```

### 2.6 im_friend_block — Block List

```go
type FriendBlock struct {
    ID        string     `gorm:"primaryKey;size:32"`
    UserID    string     `gorm:"size:32;not null;uniqueIndex:idx_block_pair"`
    UserType  string     `gorm:"size:20;not null;uniqueIndex:idx_block_pair"`
    BlockID   string     `gorm:"size:32;not null;uniqueIndex:idx_block_pair"`
    BlockType string     `gorm:"size:20;not null;uniqueIndex:idx_block_pair"`
    CreatedAt *time.Time
}

func (FriendBlock) TableName() string { return "im_friend_block" }
```

### 2.7 im_group — Groups

```go
type Group struct {
    ID         string     `gorm:"primaryKey;size:32"`
    Name       string     `gorm:"size:100;not null"`
    Avatar     string     `gorm:"size:255"`
    OwnerID    string     `gorm:"size:32;not null;index"`
    OwnerType  string     `gorm:"size:20;not null"`       // BUSINESS | CONSUMER
    GroupType  string     `gorm:"size:20;default:mixed"`  // mixed | consumer_only
    IsPublic   bool       `gorm:"default:false"`           // searchable & joinable
    Notice     string     `gorm:"type:text"`
    MaxMembers int        `gorm:"default:200"`
    Status     string     `gorm:"size:10;default:normal"` // normal | dissolved
    CreatedAt  *time.Time
    UpdatedAt  *time.Time
}

func (Group) TableName() string { return "im_group" }
```

### 2.8 im_group_member — Group Members

```go
type GroupMember struct {
    ID         string     `gorm:"primaryKey;size:32"`
    GroupID    string     `gorm:"size:32;not null;uniqueIndex:idx_group_user"`
    UserID     string     `gorm:"size:32;not null;uniqueIndex:idx_group_user"`
    UserType   string     `gorm:"size:20;not null;uniqueIndex:idx_group_user"`
    Role       string     `gorm:"size:10;default:member"`  // owner | admin | member
    Nickname   string     `gorm:"size:100"`                 // group-specific nickname
    MutedUntil *time.Time
    JoinedAt   *time.Time
    Status     string     `gorm:"size:10;default:active"`  // active | left | kicked
}

func (GroupMember) TableName() string { return "im_group_member" }
```

### 2.9 im_group_join_request — Group Join Requests

```go
type GroupJoinRequest struct {
    ID        string     `gorm:"primaryKey;size:32"`
    GroupID   string     `gorm:"size:32;not null;index"`
    UserID    string     `gorm:"size:32;not null;index"`
    UserType  string     `gorm:"size:20;not null"`
    Remark    string     `gorm:"size:255"`
    Status    string     `gorm:"size:10;default:pending"`  // pending | accepted | rejected
    HandledBy *string    `gorm:"size:32"`                   // admin who handled it
    CreatedAt *time.Time
    UpdatedAt *time.Time
}

func (GroupJoinRequest) TableName() string { return "im_group_join_request" }
```

### 2.10 im_group_message — Group Messages

```go
type GroupMessage struct {
    ID         string     `gorm:"primaryKey;size:32"`
    GroupID    string     `gorm:"size:32;not null;index:idx_group_created"`
    SenderID   string     `gorm:"size:32;not null;index"`
    SenderType string     `gorm:"size:20;not null"`
    Content    string     `gorm:"type:text"`
    Extra      string     `gorm:"type:text"`
    MsgType    string     `gorm:"size:20;default:TEXT"`
    ReplyTo    string     `gorm:"size:32;index"`
    CreatedAt  *time.Time `gorm:"index:idx_group_created"`
}

func (GroupMessage) TableName() string { return "im_group_message" }
```

### 2.11 im_group_message_read — Group Read Positions

```go
type GroupMessageRead struct {
    MessageID string     `gorm:"primaryKey;size:32"`
    GroupID   string     `gorm:"size:32;not null;index"`
    UserID    string     `gorm:"primaryKey;size:32"`
    UserType  string     `gorm:"primaryKey;size:20"`
    ReadAt    *time.Time
}

func (GroupMessageRead) TableName() string { return "im_group_message_read" }
```

### 2.12 im_broadcast — Site-wide Broadcasts

```go
type Broadcast struct {
    ID         string     `gorm:"primaryKey;size:32"`
    Title      string     `gorm:"size:255;not null"`
    Content    string     `gorm:"type:text"`
    Extra      string     `gorm:"type:text"`
    MsgType    string     `gorm:"size:20;default:TEXT"`
    TargetType string     `gorm:"size:10;not null"`  // ALL | BUSINESS | CONSUMER
    SenderID   string     `gorm:"size:32;not null"`
    SenderType string     `gorm:"size:20;not null"`
    Status     string     `gorm:"size:10;default:published"` // draft | published
    CreatedAt  *time.Time
    UpdatedAt  *time.Time
}

func (Broadcast) TableName() string { return "im_broadcast" }
```

### 2.13 im_broadcast_read — Broadcast Read Status

```go
type BroadcastRead struct {
    BroadcastID string     `gorm:"primaryKey;size:32"`
    UserID      string     `gorm:"primaryKey;size:32"`
    UserType    string     `gorm:"primaryKey;size:20"`
    ReadAt      *time.Time
}

func (BroadcastRead) TableName() string { return "im_broadcast_read" }
```

## 3. WebSocket Events

### 3.1 Event Types

| Type | Direction | Payload | Trigger |
|------|-----------|---------|---------|
| `new_message` | Server→Client | `{ message_id, conversation_id, title, content, sender_id, sender_type, msg_type, extra, created_at }` | New single-chat message |
| `group_message` | Server→Client | `{ message_id, group_id, sender_id, sender_type, content, extra, msg_type, created_at }` | New group message |
| `broadcast` | Server→Client | `{ broadcast_id, title, content, msg_type, target_type, created_at }` | New broadcast |
| `friend_request` | Server→Client | `{ request_id, sender_id, sender_type, remark, created_at }` | Friend request sent |
| `friend_accept` | Server→Client | `{ friendship_id, friend_id, friend_type }` | Friend request accepted |
| `group_join_request` | Server→Client | `{ request_id, group_id, user_id, user_type, remark }` | Join request (to admin) |
| `group_join_handled` | Server→Client | `{ request_id, group_id, status }` | Join request handled (to applicant) |
| `group_member_change` | Server→Client | `{ group_id, action(join/leave/kick), user_id, user_type }` | Group membership change |
| `group_info_change` | Server→Client | `{ group_id, name, avatar, notice }` | Group info update |
| `mention` | Server→Client | `{ group_id, message_id, sender_id, content }` | @mentioned in group |
| `typing` | Bidirectional | `{ conversation_id, user_id, user_type }` | User typing indicator |
| `presence` | Server→Client | `{ user_id, user_type, online }` | User online/offline |
| `unread_count` | Server→Client | `{ count }` | Unread count change |
| `conversation` | Server→Client | `{ conversation_id, action(update/delete) }` | Conversation list change |
| `delivery_ack` | Server→Client | `{ message_id, status }` | Message delivery confirmation |
| `online_count` | Server→Client | `{ count }` | Periodic online count |
| `heartbeat` | Bidirectional | `{ timestamp }` | Keep-alive |

### 3.2 Message Flow

```
Sender                  Backend                    Receiver
  │                        │                         │
  │── POST /message/send ──►                         │
  │                        │── Save to im_message ───│
  │                        │── Update im_conversation│
  │                        │── Update unread_count ──│
  │                        │                         │
  │                        │── WS: new_message ──────►
  │                        │   (via CrossHub,        │
  │                        │    cross-instance)       │
  │◄── WS: delivery_ack ───│                         │
  │                        │                         │
```

### 3.3 Cross-Instance Delivery (CrossHub)

```
┌──────────────┐           ┌──────────────┐
│  Instance 1  │           │  Instance 2  │
│  ┌────────┐  │  Redis    │  ┌────────┐  │
│  │  Hub   │──┼──────────►│  │  Hub   │  │
│  └────────┘  │ LPUSH     │  └────────┘  │
│  ws:user:*   │           │  ws:user:*   │
│  ws:inst:1   │  poll ◄───┼  ws:inst:2   │
└──────────────┘  LRANGE   └──────────────┘
```

- Each instance writes to the target instance's Redis list (`ws:messages:{instID}`)
- Each instance polls its own list every ~200ms via `BRPopLPush` or `LRange`
- Dedup via Redis key `ws:dedup:{messageID}` with 30s TTL
- Rate limiting via Redis key `ws:ratelimit:{userType}:{userID}`

### 3.4 Broadcast Delivery

For site-wide broadcasts (sent to ALL / BUSINESS / CONSUMER), we don't iterate over
every user. Instead:

1. Write one `im_broadcast` record
2. Push to Redis channel `ws:broadcast:all` / `ws:broadcast:business` / `ws:broadcast:consumer`
3. Each instance subscribes to these channels via Redis Pub/Sub
4. On receiving, iterate local connected clients of matching type and push via WS
5. Also write to each instance's message list for offline delivery guarantee

## 4. Key Flows

### 4.1 Friend Request Flow

```
User A                    Backend                    User B
  │                         │                         │
  │── POST /friend/send-request ──►                    │
  │   { receiver_id, receiver_type, remark }           │
  │                         │── Create im_friend_request
  │                         │   (status=pending)
  │                         │── WS: friend_request ───►
  │◄── { success: true } ───│                         │
  │                         │                         │
  │                         │              User B clicks "Accept"
  │                         │◄── POST /friend/accept ──│
  │                         │   { request_id }         │
  │                         │── Update status=accepted
  │                         │── Create bidirectional
  │                         │   im_friendship records
  │◄── WS: friend_accept ───│                         │
  │                         │── WS: success ──────────►│
```

### 4.2 Group Join Request Flow

```
User A                    Backend                    Admin B
  │                         │                         │
  │── POST /group/join ─────►                          │
  │   { group_id, remark }  │                         │
  │                         │── Create im_group_join_request
  │                         │   (status=pending)
  │                         │── WS: group_join_request►
  │◄── { success: true } ───│                         │
  │                         │                         │
  │                         │              Admin B reviews
  │                         │◄── POST /group/handle-join-request
  │                         │   { request_id, status:accepted }
  │                         │── Update status=accepted
  │                         │── Create im_group_member
  │◄── WS: group_join_handled│                         │
  │   { status:accepted }   │                         │
  │                         │── WS: group_member_change►
  │                         │   (to all members)
```

### 4.3 Chat with Friend (First Message)

```
User clicks "发消息" on a friend with no existing conversation:

  Frontend                    Backend
     │                          │
     │── POST /conversation/get-or-create ──►
     │   { user_id, user_type }
     │                          │── Generate conversation ID
     │                          │   (deterministic, or create if not exist)
     │◄── { conversation_id } ──│
     │                          │
     │── router.push(/sys/im/conversation?conversation_id=xxx)
     │                          │
     │ (normal chat flow)
```

### 4.4 Broadcast Flow

```
Admin                      Backend                    All Users
  │                          │                          │
  │── POST /broadcast/send ──►                           │
  │   { title, content,      │                          │
  │     target_type: ALL }   │                          │
  │                          │── Create im_broadcast
  │                          │── WS: broadcast ─────────►
  │                          │   (via CrossHub,         │
  │                          │    all users in ALL type) │
  │                          │                          │
  │                          │── Also write to each
  │                          │   instance's msg list
  │                          │   (for offline users)
```

## 5. Concurrency & Multi-Instance

### 5.1 Existing Capabilities (CrossHub)

| Capability | Implementation |
|------------|---------------|
| User presence tracking | Redis Set `ws:user:{type}:{id}` → instance IDs |
| Cross-instance message delivery | Redis List `ws:messages:{instID}` → LPush / LRange polling |
| Message deduplication | Redis key `ws:dedup:{messageID}` with 30s TTL |
| Rate limiting | Redis key `ws:ratelimit:{type}:{userID}` with sliding window |
| Instance heartbeat | Redis key `ws:instance:{instID}` with TTL, periodic heartbeat |
| Stale instance cleanup | Periodic scan + removal of expired instances |

### 5.2 New Capabilities Needed

| Capability | Implementation |
|------------|---------------|
| Broadcast delivery | Redis Pub/Sub channel `ws:broadcast:{targetType}` |
| Group event delivery | Redis Pub/Sub channel `ws:group:{groupID}` |
| Broadcast rate limiting | Per-admin rate limit: max 1 broadcast per 60s |
| Group message batch push | Collect group member instances → batch LPush |
| Conversation unread atomicity | Redis `INCR`/`DECR` for unread counters |

### 5.3 Concurrency Guarantees

- **At-least-once delivery**: Messages are persisted before WS push. If WS fails,
  client fetches unread on reconnect.
- **Ordering**: Messages within a conversation are ordered by `created_at`.
  Cursor-based pagination uses `created_at` as the cursor.
- **Idempotency**: Message send API is idempotent via client-generated message IDs
  (optional). Dedup at WS level via `ws:dedup:{messageID}`.
- **Atomicity**: Friend operations (accept = bidirectional insert) use DB transactions.
  Group operations (join = create member + system message) use DB transactions.

## 6. Execution Plan

### Phase 1: Data Model Refactoring

1. Create `plugin-im/model/` package with all new structs
2. Update `friend/model.go` → table names `im_friend_request`, `im_friendship`
3. Update `group/model.go` → table names `im_group`, `im_group_member`, `im_group_message`, `im_group_message_read`
   + add fields `IsPublic` to Group, `Nickname` to GroupMember
4. Remove `client_message/` directory entirely
5. Remove `sys_message/model.go` (alias no longer needed)
6. Update all `migrate.go` files to register new models
7. Update `cmd/migrate/main.go` to remove `client_message` import

### Phase 2: Message Service Rewrite

1. Rewrite `sys_message/service.go`:
   - `Send()` → write to `im_message` single table, update `im_conversation` cache
   - `Page()`, `Detail()`, `MarkRead()`, `MarkAllRead()`, `Remove()` → query `im_message`
   - Remove cross-table consumer message logic
2. Rewrite `sys_message/conversation.go`:
   - `Conversations()` → query `im_conversation` + `im_group` for groups
   - `Messages()` → query `im_message` single table (no more merging two tables in Go)
3. Add new message APIs: recall (single-chat), forward, delete, search
4. Add `conversation/get-or-create` API

### Phase 3: Friend System Completion

1. Add `BlockUser`, `UnblockUser`, `BlockList` to `friend/service.go`
2. Add `UpdateFriendRemark` to `friend/service.go`
3. Add WS push after send-request / accept / reject
4. Update `friend/api/v1/api.go` routes (already under `/im/friend/`)

### Phase 4: Group System Completion

1. Rewrite `Join()` → create `GroupJoinRequest` instead of direct insert
2. Add `HandleJoinRequest()`, `PendingJoinRequests()` to `group/service.go`
3. Add `TransferOwner()`, `SetMemberNickname()` to `group/service.go`
4. Add `@` mention parsing in `SendMessage()` → WS notification to mentioned users
5. Add WS push for group events (member change, info change)
6. Update route registrations for new APIs

### Phase 5: Broadcast System

1. Add `Broadcast` and `BroadcastRead` models
2. Add broadcast service: `SendBroadcast`, `BroadcastList`, `UnreadBroadcasts`, `MarkBroadcastRead`
3. Add `CrossHub.BroadcastToAll()`, `BroadcastToBusiness()`, `BroadcastToConsumers()`
4. Add Redis Pub/Sub channels for broadcast delivery
5. Add WS `broadcast` event type
6. Add broadcast API routes

### Phase 6: API Path Migration

1. Update `sys_message/api/v1/api.go` routes → `/api/v1/sys/im/message/*`, `/api/v1/sys/im/conversation/*`
2. Update `plugin.go` WS routes → `/api/v1/sys/im/ws`
3. Update frontend `src/api/sys/im/message.ts` paths accordingly

### Phase 7: Frontend Refactoring

1. Create shared components: `MessageList.vue`, `MessageInput.vue`
2. Expand WS store: handle `friend_request`, `group_join_request`, `broadcast`, `mention`
3. Groups tab: search → apply → admin approval list
4. Broadcast tab: notification list with read/unread
5. First-chat flow: call `get-or-create` → get conversation_id → navigate
6. Update all API paths in `*.ts` files

## 7. File Changes Summary

### Backend Files to Create

```
plugin-im/model/message.go          — Message, Conversation, ConversationUnread
plugin-im/model/broadcast.go        — Broadcast, BroadcastRead
plugin-im/model/friend.go           — FriendRequest, Friendship, FriendBlock
plugin-im/model/group.go            — Group, GroupMember, GroupJoinRequest, GroupMessage, GroupMessageRead
```

### Backend Files to Delete

```
plugins/plugin-im/client_message/    — entire directory (model, service, api, migrate)
plugins/plugin-im/sys_message/model.go  — alias no longer needed
```

### Backend Files to Modify

```
plugins/plugin-im/friend/model.go       — rename tables
plugins/plugin-im/friend/service.go     — add block, remark, WS push
plugins/plugin-im/friend/api/v1/api.go  — add block/remark routes
plugins/plugin-im/group/model.go        — rename tables, add fields
plugins/plugin-im/group/service.go      — join-request flow, transfer, nickname, @mention
plugins/plugin-im/group/api/v1/api.go   — add new routes
plugins/plugin-im/sys_message/service.go    — rewrite for im_message single table
plugins/plugin-im/sys_message/conversation.go — rewrite for im_conversation cache
plugins/plugin-im/sys_message/api/v1/api.go  — new routes + path migration
plugins/plugin-im/plugin.go                 — WS path migration
cmd/migrate/main.go                         — remove client_message import
```

### Frontend Files to Modify

```
src/api/sys/im/message.ts   — new paths + new APIs
src/api/sys/im/group.ts     — new APIs (join-request, handle-join-request, etc.)
src/api/sys/im/friend.ts    — new APIs (block, unblock, remark)
src/views/sys/im/index.vue  — three tabs: conversations, friends, groups
src/views/sys/im/conversation.vue — use shared components, new-conversation flow
src/views/sys/im/group/messages.vue — use shared components
src/hooks/im/useMessageList.ts — existing composable, expand
src/store/ws.ts             — new event types
```
