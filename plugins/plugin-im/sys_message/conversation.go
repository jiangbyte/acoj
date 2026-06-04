package sys_message

import (
	"fmt"
	"time"

	"hei-gin/sdk/db"
	"hei-gin/sdk/enums"
	"hei-gin/sdk/pojo"

	sysUser "hei-gin/plugins/plugin-sys/user"
	cliUser "hei-gin/plugins/plugin-client/user"
)

type ConversationVO struct {
	ConversationID string `json:"conversation_id"`
	OtherUserID    string `json:"other_user_id"`
	OtherUserType  string `json:"other_user_type"`
	OtherNickname  string `json:"other_nickname"`
	OtherAvatar    string `json:"other_avatar"`
	LastContent    string `json:"last_content"`
	LastTime       string `json:"last_time"`
	UnreadCount    int64  `json:"unread_count"`
}

type ConversationMessageVO struct {
	ID         string  `json:"id"`
	SenderID   *string `json:"sender_id"`
	SenderType string  `json:"sender_type"`
	Content    string  `json:"content"`
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

func Conversations(currentUserID string) []ConversationVO {
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

	rows1 := fetchConversationRows("sys_message", currentUserID, "")
	for _, r := range rows1 {
		addConv(r)
	}
	rows2 := fetchConversationRowsSender("sys_message", currentUserID, string(enums.LoginTypeBusiness))
	for _, r := range rows2 {
		addConv(r)
	}
	rows3 := fetchConversationRowsSender("client_message", currentUserID, string(enums.LoginTypeConsumer))
	for _, r := range rows3 {
		addConv(r)
	}

	if len(convMap) == 0 {
		return nil
	}

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

	result := make([]ConversationVO, 0, len(convMap))
	for _, item := range convMap {
		key := item.OtherType + ":" + item.OtherID
		result = append(result, ConversationVO{
			ConversationID: item.ConversationID,
			OtherUserID:    item.OtherID,
			OtherUserType:  item.OtherType,
			OtherNickname:  nicknameMap[key],
			OtherAvatar:    avatarMap[key],
			LastContent:    item.LastContent,
			LastTime:       pojo.FormatDateTime(item.LastTime),
			UnreadCount:    item.UnreadCount,
		})
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
	q := fmt.Sprintf(`SELECT id, sender_id, sender_type, content, status, created_at FROM sys_message
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
			Content: r.Content, Status: r.Status,
			CreatedAt: pojo.FormatDateTime(r.CreatedAt),
		}
	}
	return result, hasMore
}

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
	q := fmt.Sprintf(`SELECT id, sender_id, sender_type, content, status, created_at FROM (
		SELECT id, sender_id, sender_type, content, status, created_at FROM client_message
		WHERE conversation_id = ? AND (receiver_id = ? OR (sender_id = ? AND receiver_type = ?)) %s
		UNION ALL
		SELECT id, sender_id, sender_type, content, status, created_at FROM sys_message
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
			Content: r.Content, Status: r.Status,
			CreatedAt: pojo.FormatDateTime(r.CreatedAt),
		}
	}
	return result, hasMore
}
