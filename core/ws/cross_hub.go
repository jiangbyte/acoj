package ws

import (
	"context"
	"encoding/json"
	"log"
	"net/http"
	"strconv"
	"sync"
	"time"

	"hei-gin/config"
	"hei-gin/core/enums"

	"github.com/redis/go-redis/v9"
)

// Config helpers - read from config.C.WS with fallback defaults
func hbInterval() time.Duration {
	if config.C != nil && config.C.WS.HeartbeatInterval > 0 {
		return time.Duration(config.C.WS.HeartbeatInterval) * time.Second
	}
	return 15 * time.Second
}

func instTTL() time.Duration {
	if config.C != nil && config.C.WS.InstanceTTL > 0 {
		return time.Duration(config.C.WS.InstanceTTL) * time.Second
	}
	return 60 * time.Second
}

func staleClean() time.Duration {
	if config.C != nil && config.C.WS.StaleCleanInterval > 0 {
		return time.Duration(config.C.WS.StaleCleanInterval) * time.Minute
	}
	return 5 * time.Minute
}

func rlWindow() time.Duration {
	if config.C != nil && config.C.WS.RateLimitWindow > 0 {
		return time.Duration(config.C.WS.RateLimitWindow) * time.Second
	}
	return 10 * time.Second
}

func rlMax() int64 {
	if config.C != nil && config.C.WS.RateLimitMax > 0 {
		return int64(config.C.WS.RateLimitMax)
	}
	return 30
}

func dedupTTL() time.Duration {
	if config.C != nil && config.C.WS.DedupTTL > 0 {
		return time.Duration(config.C.WS.DedupTTL) * time.Second
	}
	return 30 * time.Second
}

func pollTO() time.Duration {
	if config.C != nil && config.C.WS.PollTimeout > 0 {
		return time.Duration(config.C.WS.PollTimeout) * time.Second
	}
	return 2 * time.Second
}

// crossInstanceMessage is the envelope for Redis list cross-instance messages.
type crossInstanceMessage struct {
	ToUserID   string          `json:"to_user_id"`
	ToUserType string          `json:"to_user_type"`
	Message    json.RawMessage `json:"message"`
	MessageID  string          `json:"message_id,omitempty"`
	Timestamp  int64           `json:"timestamp"`
}

// CrossHub wraps the local Hub with Redis-backed cross-instance IM delivery.
type CrossHub struct {
	local      *Hub
	rdb        *redis.Client
	instanceID string
	ctx        context.Context
	cancel     context.CancelFunc
	wg         sync.WaitGroup
	closeOnce  sync.Once
}

// NewCrossHub creates a CrossHub. If rdb is nil, runs in single-instance (passthrough) mode.
func NewCrossHub(local *Hub, rdb *redis.Client) *CrossHub {
	instanceID := strconv.FormatInt(config.C.Snowflake.Instance, 10)

	ctx, cancel := context.WithCancel(context.Background())
	ch := &CrossHub{
		local:      local,
		rdb:        rdb,
		instanceID: instanceID,
		ctx:        ctx,
		cancel:     cancel,
	}

	// Wire up lifecycle hooks to the local hub
	local.OnClientRegistered = ch.onClientRegistered
	local.OnClientUnregistered = ch.onClientUnregistered

	if rdb != nil {
		ch.wg.Add(3)
		go ch.pollLoop()
		go ch.heartbeatLoop()
		go ch.staleCleanupLoop()
		log.Printf("[CrossHub] Cross-instance mode enabled, instance=%s", instanceID)
	} else {
		log.Printf("[CrossHub] Redis not configured, running in single-instance mode")
	}

	return ch
}

// ─── Hub lifecycle hooks ──────────────────────────────────────────────

func (ch *CrossHub) onClientRegistered(c *Client) {
	ch.TrackConnection(c.UserID, c.UserType)
	ch.broadcastPresence(c.UserID, c.UserType, true)
	// Notify client to refresh unread count on reconnect
	switch c.UserType {
	case enums.LoginTypeBusiness:
		ch.local.SendToUser(c.UserID, Message{Type: MsgUnreadCount})
	case enums.LoginTypeConsumer:
		ch.local.SendToConsumer(c.UserID, Message{Type: MsgUnreadCount})
	}
}

func (ch *CrossHub) onClientUnregistered(c *Client) {
	// Remove this instance from user's connection set
	ch.UntrackConnection(c.UserID, c.UserType)
	// If user has no more connections on any instance, broadcast offline
	if !ch.IsUserOnlineAnywhere(c.UserID, c.UserType) {
		ch.broadcastPresence(c.UserID, c.UserType, false)
	}
}

// ─── Redis key helpers ────────────────────────────────────────────────

func (ch *CrossHub) userSetKey(userType enums.LoginTypeEnum, userID string) string {
	return "ws:user:" + string(userType) + ":" + userID
}

func (ch *CrossHub) msgListKey() string {
	return "ws:messages:" + ch.instanceID
}

func (ch *CrossHub) instanceKey() string {
	return "ws:instance:" + ch.instanceID
}

func (ch *CrossHub) rateLimitKey(userID string, userType enums.LoginTypeEnum) string {
	return "ws:ratelimit:" + string(userType) + ":" + userID
}

func (ch *CrossHub) dedupKey(messageID string) string {
	return "ws:dedup:" + messageID
}

// ─── Presence ─────────────────────────────────────────────────────────

// IsUserOnlineAnywhere checks if a user is connected to any instance.
func (ch *CrossHub) IsUserOnlineAnywhere(userID string, userType enums.LoginTypeEnum) bool {
	if ch.rdb == nil {
		return ch.local.isUserConnected(userID, userType)
	}
	key := ch.userSetKey(userType, userID)
	count, err := ch.rdb.SCard(ch.ctx, key).Result()
	if err != nil {
		return false
	}
	return count > 0
}

// IsUserOnlineLocally checks if a user is connected to this instance.
func (ch *CrossHub) IsUserOnlineLocally(userID string, userType enums.LoginTypeEnum) bool {
	return ch.local.isUserConnected(userID, userType)
}

// broadcastPresence sends presence update to conversation partners.
// For now, broadcasts to all connected clients of the same type.
// Can be optimized later by only notifying relevant conversation partners.
func (ch *CrossHub) broadcastPresence(userID string, userType enums.LoginTypeEnum, online bool) {
	msg := Message{
		Type: MsgPresence,
		Payload: PresencePayload{
			UserID:   userID,
			UserType: string(userType),
			Online:   online,
		},
	}
	ch.local.BroadcastAll(msg)
}

// TrackConnection records that a user is connected to this instance.
func (ch *CrossHub) TrackConnection(userID string, userType enums.LoginTypeEnum) {
	if ch.rdb == nil {
		return
	}
	key := ch.userSetKey(userType, userID)
	if err := ch.rdb.SAdd(ch.ctx, key, ch.instanceID).Err(); err != nil {
		log.Printf("[CrossHub] TrackConnection SAdd error: %v", err)
	}
	ch.rdb.Expire(ch.ctx, key, instTTL()+30*time.Second)
}

// UntrackConnection removes the instance from the user's instance set.
func (ch *CrossHub) UntrackConnection(userID string, userType enums.LoginTypeEnum) {
	if ch.rdb == nil {
		return
	}
	key := ch.userSetKey(userType, userID)
	if err := ch.rdb.SRem(ch.ctx, key, ch.instanceID).Err(); err != nil {
		log.Printf("[CrossHub] UntrackConnection SRem error: %v", err)
	}
}

// getTargetInstances returns all instance IDs where the user is connected.
func (ch *CrossHub) getTargetInstances(userID string, userType enums.LoginTypeEnum) []string {
	if ch.rdb == nil {
		return nil
	}
	key := ch.userSetKey(userType, userID)
	instances, err := ch.rdb.SMembers(ch.ctx, key).Result()
	if err != nil {
		log.Printf("[CrossHub] SMembers error: %v", err)
		return nil
	}
	return instances
}

// ─── Rate Limiting ────────────────────────────────────────────────────

// AllowMessage checks if a user is allowed to send a message (rate-limited).
func (ch *CrossHub) AllowMessage(userID string, userType enums.LoginTypeEnum) bool {
	if ch.rdb == nil {
		return true
	}
	key := ch.rateLimitKey(userID, userType)
	count, err := ch.rdb.Incr(ch.ctx, key).Result()
	if err != nil {
		return true // allow on error
	}
	if count == 1 {
		ch.rdb.Expire(ch.ctx, key, rlWindow())
	}
	return count <= rlMax()
}

// ─── Message Deduplication (cross-instance) ───────────────────────────

// markDelivered marks a message as delivered to prevent duplicate cross-instance delivery.
func (ch *CrossHub) markDelivered(messageID string) bool {
	if ch.rdb == nil || messageID == "" {
		return true
	}
	key := ch.dedupKey(messageID)
	// SETNX: only set if key doesn't exist; returns true if set successfully
	ok, err := ch.rdb.SetNX(ch.ctx, key, ch.instanceID, dedupTTL()).Result()
	if err != nil {
		return true // allow on error
	}
	return ok
}

// ─── Polling loop ─────────────────────────────────────────────────────

func (ch *CrossHub) pollLoop() {
	defer ch.wg.Done()
	key := ch.msgListKey()

	for {
		result, err := ch.rdb.BRPop(ch.ctx, pollTO(), key).Result()
		if err != nil {
			if err == context.Canceled || err == redis.Nil {
				select {
				case <-ch.ctx.Done():
					return
				default:
					continue
				}
			}
			log.Printf("[CrossHub] BRPop error: %v", err)
			continue
		}

		if len(result) < 2 {
			continue
		}
		ch.handleMessage(result[1])
	}
}

func (ch *CrossHub) handleMessage(payload string) {
	var xMsg crossInstanceMessage
	if err := json.Unmarshal([]byte(payload), &xMsg); err != nil {
		log.Printf("[CrossHub] Failed to unmarshal message: %v", err)
		return
	}

	// Dedup check: if this message ID was already processed, skip
	if xMsg.MessageID != "" {
		if !ch.markDelivered(xMsg.MessageID) {
			return // already processed
		}
	}

	var msg Message
	if err := json.Unmarshal(xMsg.Message, &msg); err != nil {
		log.Printf("[CrossHub] Failed to unmarshal inner message: %v", err)
		return
	}

	switch enums.LoginTypeEnum(xMsg.ToUserType) {
	case enums.LoginTypeBusiness:
		ch.local.SendToUser(xMsg.ToUserID, msg)
	case enums.LoginTypeConsumer:
		ch.local.SendToConsumer(xMsg.ToUserID, msg)
	}
}

// ─── Heartbeat ────────────────────────────────────────────────────────

func (ch *CrossHub) heartbeatLoop() {
	defer ch.wg.Done()
	ticker := time.NewTicker(hbInterval())
	defer ticker.Stop()

	ch.sendHeartbeat()

	for {
		select {
		case <-ch.ctx.Done():
			return
		case <-ticker.C:
			ch.sendHeartbeat()
		}
	}
}

func (ch *CrossHub) sendHeartbeat() {
	if ch.rdb == nil {
		return
	}
	key := ch.instanceKey()
	if err := ch.rdb.SetEx(ch.ctx, key, time.Now().UnixMilli(), instTTL()).Err(); err != nil {
		log.Printf("[CrossHub] Heartbeat error: %v", err)
	}
}

// ─── Stale Instance Cleanup ───────────────────────────────────────────

func (ch *CrossHub) staleCleanupLoop() {
	defer ch.wg.Done()
	ticker := time.NewTicker(staleClean())
	defer ticker.Stop()

	for {
		select {
		case <-ch.ctx.Done():
			return
		case <-ticker.C:
			ch.cleanStaleInstances()
		}
	}
}

// cleanStaleInstances removes stale instance entries from all user sets.
func (ch *CrossHub) cleanStaleInstances() {
	if ch.rdb == nil {
		return
	}

	// Find all user set keys
	iter := ch.rdb.Scan(ch.ctx, 0, "ws:user:*", 1000).Iterator()
	cleaned := 0

	for iter.Next(ch.ctx) {
		key := iter.Val()

		// Get all instances in this user set
		members, err := ch.rdb.SMembers(ch.ctx, key).Result()
		if err != nil {
			continue
		}

		for _, instID := range members {
			if instID == ch.instanceID {
				continue // skip our own instance
			}
			// Check if the instance is still alive
			instKey := "ws:instance:" + instID
			exists, err := ch.rdb.Exists(ch.ctx, instKey).Result()
			if err != nil {
				continue
			}
			if exists == 0 {
				// Instance is dead, remove from user set
				ch.rdb.SRem(ch.ctx, key, instID)
				cleaned++
			}
		}
	}

	if cleaned > 0 {
		log.Printf("[CrossHub] Cleaned %d stale instance references", cleaned)
	}
}

// ─── Public API ───────────────────────────────────────────────────────

// SendToUser delivers a message to a business user (local + cross-instance).
func (ch *CrossHub) SendToUser(userID string, msg Message, messageID ...string) {
	ch.local.SendToUser(userID, msg)
	if ch.rdb != nil {
		mid := ""
		if len(messageID) > 0 {
			mid = messageID[0]
		}
		ch.publishToRemote(userID, enums.LoginTypeBusiness, msg, mid)
	}
}

// SendToConsumer delivers a message to a consumer user (local + cross-instance).
func (ch *CrossHub) SendToConsumer(userID string, msg Message, messageID ...string) {
	ch.local.SendToConsumer(userID, msg)
	if ch.rdb != nil {
		mid := ""
		if len(messageID) > 0 {
			mid = messageID[0]
		}
		ch.publishToRemote(userID, enums.LoginTypeConsumer, msg, mid)
	}
}

// publishToRemote pushes a message to remote instances where the user is connected.
func (ch *CrossHub) publishToRemote(userID string, userType enums.LoginTypeEnum, msg Message, messageID string) {
	instances := ch.getTargetInstances(userID, userType)
	if len(instances) == 0 {
		return
	}

	rawMsg, err := json.Marshal(msg)
	if err != nil {
		return
	}

	xMsg := crossInstanceMessage{
		ToUserID:   userID,
		ToUserType: string(userType),
		Message:    rawMsg,
		MessageID:  messageID,
		Timestamp:  time.Now().UnixMilli(),
	}

	data, err := json.Marshal(xMsg)
	if err != nil {
		return
	}

	for _, instID := range instances {
		if instID == ch.instanceID {
			continue
		}
		listKey := "ws:messages:" + instID
		if err := ch.rdb.LPush(ch.ctx, listKey, data).Err(); err != nil {
			log.Printf("[CrossHub] LPush to %s error: %v", instID, err)
		}
	}
}

// HandleWebSocket upgrades an HTTP connection to WebSocket with cross-instance tracking.
func (ch *CrossHub) HandleWebSocket(w http.ResponseWriter, r *http.Request, userID string, userType enums.LoginTypeEnum) {
	ch.local.HandleWebSocket(w, r, userID, userType)
}

// OnlineCount returns locally connected client count.
func (ch *CrossHub) OnlineCount() int {
	return ch.local.OnlineCount()
}

// Close shuts down the CrossHub gracefully.
func (ch *CrossHub) Close() {
	if ch == nil {
		return
	}
	ch.closeOnce.Do(func() {
		ch.cancel()
		if ch.rdb != nil {
			ch.rdb.Del(ch.ctx, ch.instanceKey())
		}
		ch.wg.Wait()
	})
}
