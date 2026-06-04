package sys_message

import (
	"fmt"
	"time"

	"hei-gin/sdk/db"
	"hei-gin/sdk/enums"
	"hei-gin/sdk/pojo"

	sysUser "hei-gin/plugins/plugin-sys/user"
	cliUser "hei-gin/plugins/plugin-client/user"
	"hei-gin/plugins/plugin-im/group"
)

// ConversationType constants
const (
	ConvTypeSingle = "single"
	ConvTypeGroup  = "group"
)

type ConversationVO struct {
	ConversationID string `json:"conversation_id"`
	ConversationType string `json:"conversation_type"` // "single" | "group"

	// Single-chat fields
	OtherUserID   string `json:"other_user_id,omitempty"`
	OtherUserType string `json:"other_user_type,omitempty"`
	OtherNickname string `json:"other_nickname,omitempty"`
	OtherAvatar   string `json:"other_avatar,omitempty"`

	// Group fields
	GroupID     string `json:"group_id,omitempty"`
	GroupName   string `json:"group_name,omitempty"`
	GroupAvatar string `json:"group_avatar,omitempty"`
	MemberCount int    `json:"member_count,omitempty"`

	LastContent string `json:"last_content"`
	LastTime    string `json:"last_time"`
	UnreadCount int64  `json:"unread_count"`
}

type ConversationMessageVO struct {
	ID         string  `json:"id"`
	SenderID   *string `json:"sender_id"`
	SenderType string  `json:"sender_type"`
	Content    string  `json:"content"`
	MsgType    string  `json:"msg_type"`
	Extra      string  `json:"extra,omitempty"`
	Status     string  `json:"status"`
	CreatedAt  string  `json:"created_at"`
}

type convItem struct {
	ConversationID string
	OtherID        string
	OtherType      string
	LastTime       time.Time
	LastContent    string
	UnreadCount    int64
}

func Conversations(currentUserID, userType string) []ConversationVO {
	convMap := make(map[string]*convItem)

	mergeConv := func(existing *convItem, r *convItem) {
		if r.LastTime.After(existing.LastTime) {
			existing.LastContent = r.LastContent
			existing.LastTime = r.LastTime
		}
		existing.UnreadCount += r.UnreadCount
		if existing.OtherID != r.OtherID && (existing.OtherID == currentUserID || r.OtherID != currentUserID) {
			existing.OtherID = r.OtherID
			existing.OtherType = r.OtherType
		}
	}
	addConv := func(r *convItem) {
		if existing, ok := convMap[r.ConversationID]; ok {
			mergeConv(existing, r)
		} else {
			convMap[r.ConversationID] = r
		}
	}

	// Single-chat: sys_message table
	rows1 := fetchConversationRows("sys_message", currentUserID, "")
	for _, r := range rows1 {
		addConv(r)
	}
	rows2 := fetchConversationRowsSender("sys_message", currentUserID, string(enums.LoginTypeBusiness))
	for _, r := range rows2 {
		addConv(r)
	}
	// Single-chat: client_message table (C端 messages where user is receiver as Consumer)
	rows3 := fetchConversationRows("client_message", currentUserID, string(enums.LoginTypeConsumer))
	for _, r := range rows3 {
		addConv(r)
	}
	rows4 := fetchConversationRowsSender("client_message", currentUserID, string(enums.LoginTypeConsumer))
	for _, r := range rows4 {
		addConv(r)
	}

	// Build result: single-chat conversations
	businessIDs, consumerIDs := []string{}, []string{}
	for _, item := range convMap {
		switch item.OtherType {
		case string(enums.LoginTypeBusiness):
			businessIDs = append(businessIDs, item.OtherID)
		case string(enums.LoginTypeConsumer):
			consumerIDs = append(consumerIDs, item.OtherID)
		}
	}

	nicknameMap := make(map[string]string)
	avatarMap := make(map[string]string)

	if len(businessIDs) > 0 {
		var busUsers []sysUser.SysUser
		db.DB.Model(&sysUser.SysUser{}).Where("id IN ?", businessIDs).Find(&busUsers)
		for _, u := range busUsers {
			if u.Nickname != nil {
				nicknameMap[string(enums.LoginTypeBusiness)+":"+u.ID] = *u.Nickname
			}
			if u.Avatar != nil {
				avatarMap[string(enums.LoginTypeBusiness)+":"+u.ID] = *u.Avatar
			}
		}
	}
	if len(consumerIDs) > 0 {
		var conUsers []cliUser.ClientUser
		db.DB.Model(&cliUser.ClientUser{}).Where("id IN ?", consumerIDs).Find(&conUsers)
		for _, u := range conUsers {
			if u.Nickname != nil {
				nicknameMap[string(enums.LoginTypeConsumer)+":"+u.ID] = *u.Nickname
			}
			if u.Avatar != nil {
				avatarMap[string(enums.LoginTypeConsumer)+":"+u.ID] = *u.Avatar
			}
		}
	}

	result := make([]ConversationVO, 0, len(convMap)+4)
	for _, item := range convMap {
		key := item.OtherType + ":" + item.OtherID
		result = append(result, ConversationVO{
			ConversationID:   item.ConversationID,
			ConversationType: ConvTypeSingle,
			OtherUserID:      item.OtherID,
			OtherUserType:    item.OtherType,
			OtherNickname:    nicknameMap[key],
			OtherAvatar:      avatarMap[key],
			LastContent:      item.LastContent,
			LastTime:         pojo.FormatDateTime(item.LastTime),
			UnreadCount:      item.UnreadCount,
		})
	}

	// Group conversations
	groupConvs := fetchGroupConversations(currentUserID, userType)
	result = append(result, groupConvs...)

	// Sort by LastTime descending
	for i := 0; i < len(result); i++ {
		for j := i + 1; j < len(result); j++ {
			if result[j].LastTime > result[i].LastTime {
				result[i], result[j] = result[j], result[i]
			}
		}
	}

	return result
}

func fetchGroupConversations(currentUserID, userType string) []ConversationVO {
	// Get all groups the user is a member of
	var members []group.GroupMember
	db.DB.Model(&group.GroupMember{}).
		Select("group_id, joined_at").
		Where("user_id = ? AND user_type = ? AND status = ?", currentUserID, userType, group.MemberActive).
		Find(&members)
	if len(members) == 0 {
		return nil
	}

	groupIDs := make([]string, len(members))
	for i, m := range members {
		groupIDs[i] = m.GroupID
	}

	// Get group info (single batch)
	var groups []group.GroupChat
	db.DB.Where("id IN ? AND status = ?", groupIDs, group.GroupNormal).Find(&groups)
	if len(groups) == 0 {
		return nil
	}

	// Get member count per group (single batch)
	type cnt struct{ GroupID string; Count int }
	var counts []cnt
	db.DB.Model(&group.GroupMember{}).
		Select("group_id, COUNT(*) as count").
		Where("group_id IN ? AND status = ?", groupIDs, group.MemberActive).
		Group("group_id").Scan(&counts)
	countMap := make(map[string]int, len(counts))
	for _, c := range counts {
		countMap[c.GroupID] = c.Count
	}

	// Get last message per group (single batch, subquery)
	type lm struct{ GroupID, Content, CreatedAt string }
	var lastMsgs []lm
	db.DB.Raw(
		`SELECT g2.group_id, g2.content, g2.created_at FROM group_message g2
		 INNER JOIN (SELECT group_id, MAX(created_at) max_ct FROM group_message WHERE group_id IN ? GROUP BY group_id) g1
		 ON g2.group_id = g1.group_id AND g2.created_at = g1.max_ct`, groupIDs).Scan(&lastMsgs)
	lastMap := make(map[string]lm, len(lastMsgs))
	for _, l := range lastMsgs {
		lastMap[l.GroupID] = l
	}

	// Get unread count per group
	var unreads []struct {
		GroupID string
		Count   int64
	}
	db.DB.Raw(
		`SELECT gm.group_id, COUNT(*) as count FROM group_message gm
		 WHERE gm.group_id IN ? AND gm.created_at > COALESCE(
			 (SELECT MAX(gmr.read_at) FROM group_message_read gmr
			  WHERE gmr.group_id = gm.group_id AND gmr.user_id = ? AND gmr.user_type = ?), '1970-01-01'
		 )
		 GROUP BY gm.group_id`, groupIDs, currentUserID, userType).Scan(&unreads)
	unreadMap := make(map[string]int64, len(unreads))
	for _, u := range unreads {
		unreadMap[u.GroupID] = u.Count
	}

	groupMap := make(map[string]group.GroupChat, len(groups))
	for _, g := range groups {
		groupMap[g.ID] = g
	}

	result := make([]ConversationVO, 0, len(groups))
	for _, m := range members {
		g, ok := groupMap[m.GroupID]
		if !ok {
			continue
		}
		vo := ConversationVO{
			ConversationID:   "group:" + g.ID,
			ConversationType: ConvTypeGroup,
			GroupID:          g.ID,
			GroupName:        g.Name,
			GroupAvatar:      g.Avatar,
			MemberCount:      countMap[g.ID],
			UnreadCount:      unreadMap[g.ID],
		}
		if l, ok := lastMap[g.ID]; ok {
			vo.LastContent = l.Content
			vo.LastTime = l.CreatedAt
		}
		result = append(result, vo)
	}
	return result
}

func fetchConversationRows(table, userID, receiverTypeFilter string) []*convItem {
	q := "SELECT conversation_id, " +
		"ANY_VALUE(CASE WHEN receiver_id = ? THEN sender_id ELSE receiver_id END) as other_id, " +
		"ANY_VALUE(CASE WHEN receiver_id = ? THEN sender_type ELSE receiver_type END) as other_type, " +
		"MAX(created_at) as last_time, " +
		"ANY_VALUE(SUBSTRING_INDEX(GROUP_CONCAT(content ORDER BY created_at DESC), ',', 1)) as last_content, " +
		"SUM(CASE WHEN status = 'unread' AND receiver_id = ? THEN 1 ELSE 0 END) as unread_count " +
		"FROM " + table + " WHERE receiver_id = ? "
	args := []interface{}{userID, userID, userID, userID}
	if receiverTypeFilter != "" {
		q += "AND receiver_type = ? "
		args = append(args, receiverTypeFilter)
	}
	q += "GROUP BY conversation_id"
	var results []*convItem
	db.DB.Raw(q, args...).Scan(&results)
	return results
}

func fetchConversationRowsSender(table, userID, receiverType string) []*convItem {
	q := "SELECT conversation_id, " +
		"ANY_VALUE(receiver_id) as other_id, " +
		"ANY_VALUE(receiver_type) as other_type, " +
		"MAX(created_at) as last_time, " +
		"ANY_VALUE(SUBSTRING_INDEX(GROUP_CONCAT(content ORDER BY created_at DESC), ',', 1)) as last_content, " +
		"0 as unread_count " +
		"FROM " + table + " WHERE sender_id = ? AND receiver_type = ? GROUP BY conversation_id"
	var results []*convItem
	db.DB.Raw(q, userID, receiverType).Scan(&results)
	return results
}

// BusinessConversationMessages fetches single-chat messages for a BUSINESS user.
func BusinessConversationMessages(currentUserID string, conversationID string, cursor string, size int) ([]ConversationMessageVO, bool) {
	if size < 1 {
		size = 20
	}
	if size > 100 {
		size = 100
	}
	cursorCond := ""
	var cursorArgs []interface{}
	if cursor != "" {
		cursorTime, err := time.Parse("2006-01-02 15:04:05", cursor)
		if err == nil {
			cursorCond = "AND created_at < ?"
			cursorArgs = append(cursorArgs, cursorTime)
		}
	}
	q := fmt.Sprintf(`SELECT id, sender_id, sender_type, content, message_type as msg_type, extra, status, created_at FROM sys_message
		WHERE conversation_id = ? AND (receiver_id = ? OR sender_id = ?) %s
		ORDER BY created_at DESC LIMIT ?`, cursorCond)
	args := []interface{}{conversationID, currentUserID, currentUserID}
	args = append(args, cursorArgs...)
	args = append(args, size+1)

	type rawMsg struct {
		ID         string
		SenderID   *string
		SenderType string
		Content    string
		MsgType    string
		Extra      string
		Status     string
		CreatedAt  time.Time
	}
	var raw []rawMsg
	db.DB.Raw(q, args...).Scan(&raw)

	hasMore := len(raw) > size
	if hasMore {
		raw = raw[:size]
	}
	result := make([]ConversationMessageVO, len(raw))
	for i, r := range raw {
		result[i] = ConversationMessageVO{
			ID: r.ID, SenderID: r.SenderID, SenderType: r.SenderType,
			Content: r.Content, MsgType: r.MsgType, Extra: r.Extra, Status: r.Status,
			CreatedAt: pojo.FormatDateTime(r.CreatedAt),
		}
	}
	return result, hasMore
}

// ConsumerConversationMessages fetches single-chat messages for a CONSUMER user.
func ConsumerConversationMessages(currentUserID string, conversationID string, cursor string, size int) ([]ConversationMessageVO, bool) {
	if size < 1 {
		size = 20
	}
	if size > 100 {
		size = 100
	}
	cursorCond := ""
	var cursorArgs []interface{}
	if cursor != "" {
		cursorTime, err := time.Parse("2006-01-02 15:04:05", cursor)
		if err == nil {
			cursorCond = "AND created_at < ?"
			cursorArgs = append(cursorArgs, cursorTime)
		}
	}
	q := fmt.Sprintf(`SELECT id, sender_id, sender_type, content, message_type as msg_type, extra, status, created_at FROM (
		SELECT id, sender_id, sender_type, content, message_type as msg_type, extra, status, created_at FROM client_message
		WHERE conversation_id = ? AND (receiver_id = ? OR (sender_id = ? AND receiver_type = ?)) %s
		UNION ALL
		SELECT id, sender_id, sender_type, content, message_type as msg_type, extra, status, created_at FROM sys_message
		WHERE conversation_id = ? AND sender_id = ? AND sender_type = ? %s
	) AS combined ORDER BY created_at DESC LIMIT ?`, cursorCond, cursorCond)
	args := []interface{}{conversationID, currentUserID, currentUserID, enums.LoginTypeConsumer}
	args = append(args, cursorArgs...)
	args = append(args, conversationID, currentUserID, enums.LoginTypeConsumer)
	args = append(args, cursorArgs...)
	args = append(args, size+1)

	type rawMsg struct {
		ID         string
		SenderID   *string
		SenderType string
		Content    string
		MsgType    string
		Extra      string
		Status     string
		CreatedAt  time.Time
	}
	var raw []rawMsg
	db.DB.Raw(q, args...).Scan(&raw)

	hasMore := len(raw) > size
	if hasMore {
		raw = raw[:size]
	}
	result := make([]ConversationMessageVO, len(raw))
	for i, r := range raw {
		result[i] = ConversationMessageVO{
			ID: r.ID, SenderID: r.SenderID, SenderType: r.SenderType,
			Content: r.Content, MsgType: r.MsgType, Extra: r.Extra, Status: r.Status,
			CreatedAt: pojo.FormatDateTime(r.CreatedAt),
		}
	}
	return result, hasMore
}
