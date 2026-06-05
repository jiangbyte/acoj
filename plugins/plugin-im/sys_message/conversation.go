package sys_message

import (
	"context"
	"sort"
	"time"

	"hei-gin/sdk/db"
	"hei-gin/sdk/enums"
	"hei-gin/sdk/exception"
	"hei-gin/sdk/pojo"
	imModel "hei-gin/plugins/plugin-im/model"

	sysUser "hei-gin/plugins/plugin-sys/user"
	cliUser "hei-gin/plugins/plugin-client/user"
	"hei-gin/plugins/plugin-im/group"
)

// ==================== Conversations ====================

func Conversations(currentUserID, userType string, cursor string, size int) ([]ConversationVO, bool) {
	if size < 1 {
		size = 20
	}
	if size > 100 {
		size = 100
	}

	// Get conversations from im_conversation table
	var convs []imModel.Conversation
	q := db.DB.Model(&imModel.Conversation{}).
		Where("(user_id_1 = ? AND user_type_1 = ?) OR (user_id_2 = ? AND user_type_2 = ?)",
			currentUserID, userType, currentUserID, userType)
	if cursor != "" {
		if t, err := time.Parse("2006-01-02 15:04:05", cursor); err == nil {
			q = q.Where("last_time < ?", t)
		}
	}
	q.Order("last_time DESC").Limit(size + 1).Find(&convs)

	hasMore := len(convs) > size
	if hasMore {
		convs = convs[:size]
	}

	// Build single-chat conversation VOs
	type otherUser struct {
		ID   string
		Type string
	}
	others := make([]otherUser, 0, len(convs))
	resultMap := make(map[string]*ConversationVO, len(convs))

	for _, conv := range convs {
		var otherID, otherType string
		if conv.UserID1 == currentUserID && conv.UserType1 == userType {
			otherID, otherType = conv.UserID2, conv.UserType2
		} else {
			otherID, otherType = conv.UserID1, conv.UserType1
		}
		others = append(others, otherUser{ID: otherID, Type: otherType})
		lastTime := ""
		if conv.LastTime != nil {
			lastTime = pojo.FormatDateTime(*conv.LastTime)
		}
		resultMap[conv.ID] = &ConversationVO{
			ConversationID:   conv.ID,
			ConversationType: ConvTypeSingle,
			OtherUserID:      otherID,
			OtherUserType:    otherType,
			LastContent:      conv.LastMsg,
			LastTime:         lastTime,
		}
	}

	// Batch resolve nicknames/avatars
	businessIDs, consumerIDs := []string{}, []string{}
	for _, o := range others {
		switch o.Type {
		case string(enums.LoginTypeBusiness):
			businessIDs = append(businessIDs, o.ID)
		case string(enums.LoginTypeConsumer):
			consumerIDs = append(consumerIDs, o.ID)
		}
	}
	nicknameMap := make(map[string]string)
	avatarMap := make(map[string]string)
	if len(businessIDs) > 0 {
		var busUsers []sysUser.SysUser
		db.DB.Model(&sysUser.SysUser{}).Where("id IN ?", businessIDs).Find(&busUsers)
		for _, u := range busUsers {
			if u.Nickname != nil {
				nicknameMap["BUSINESS:"+u.ID] = *u.Nickname
			}
			if u.Avatar != nil {
				avatarMap["BUSINESS:"+u.ID] = *u.Avatar
			}
		}
	}
	if len(consumerIDs) > 0 {
		var conUsers []cliUser.ClientUser
		db.DB.Model(&cliUser.ClientUser{}).Where("id IN ?", consumerIDs).Find(&conUsers)
		for _, u := range conUsers {
			if u.Nickname != nil {
				nicknameMap["CONSUMER:"+u.ID] = *u.Nickname
			}
			if u.Avatar != nil {
				avatarMap["CONSUMER:"+u.ID] = *u.Avatar
			}
		}
	}
	for _, vo := range resultMap {
		key := vo.OtherUserType + ":" + vo.OtherUserID
		vo.OtherNickname = nicknameMap[key]
		vo.OtherAvatar = avatarMap[key]
	}

	// Get unread counts
	for _, vo := range resultMap {
		var cnt int64
		db.DB.Model(&imModel.Message{}).
			Where("conversation_id = ? AND receiver_id = ? AND status = ?", vo.ConversationID, currentUserID, "unread").
			Count(&cnt)
		vo.UnreadCount = cnt
	}

	// Add group conversations
	groupConvs := group.MyGroupConversations(currentUserID, userType)
	for _, gv := range groupConvs {
		resultMap["group:"+gv.GroupID] = &ConversationVO{
			ConversationID:   "group:" + gv.GroupID,
			ConversationType: ConvTypeGroup,
			GroupID:          gv.GroupID,
			GroupName:        gv.GroupName,
			GroupAvatar:      gv.GroupAvatar,
			MemberCount:      gv.MemberCount,
			LastContent:      gv.LastContent,
			LastTime:         gv.LastTime,
			UnreadCount:      gv.UnreadCount,
		}
	}

	// Convert to sorted slice
	result := make([]ConversationVO, 0, len(resultMap))
	for _, vo := range resultMap {
		result = append(result, *vo)
	}
	sort.Slice(result, func(i, j int) bool {
		return result[i].LastTime > result[j].LastTime
	})
	return result, hasMore
}

// ==================== Conversation Messages ====================

func BusinessConversationMessages(ctx context.Context, currentUserID string, conversationID string, cursor string, size int) ([]ConversationMessageVO, bool) {
	return conversationMessages(ctx, currentUserID, conversationID, cursor, size)
}

func ConsumerConversationMessages(ctx context.Context, currentUserID string, conversationID string, cursor string, size int) ([]ConversationMessageVO, bool) {
	return conversationMessages(ctx, currentUserID, conversationID, cursor, size)
}

func conversationMessages(ctx context.Context, currentUserID string, conversationID string, cursor string, size int) ([]ConversationMessageVO, bool) {
	if size < 1 {
		size = 20
	}
	if size > 100 {
		size = 100
	}

	q := db.DB.WithContext(ctx).Model(&imModel.Message{}).
		Where("conversation_id = ? AND (sender_id = ? OR receiver_id = ?) AND (deleted_by != ? OR deleted_by IS NULL)",
			conversationID, currentUserID, currentUserID, currentUserID)
	if cursor != "" {
		if t, err := time.Parse("2006-01-02 15:04:05", cursor); err == nil {
			q = q.Where("created_at < ?", t)
		}
	}
	var records []imModel.Message
	q.Order("created_at DESC").Limit(size + 1).Find(&records)

	hasMore := len(records) > size
	if hasMore {
		records = records[:size]
	}

	result := make([]ConversationMessageVO, len(records))
	for i, m := range records {
		result[i] = ConversationMessageVO{
			ID: m.ID, SenderID: m.SenderID, SenderType: m.SenderType,
			Content: m.Content, MsgType: m.MsgType, Extra: m.Extra,
			Status: m.Status,
			CreatedAt: pojo.FormatDateTimePtr(m.CreatedAt),
		}
	}
	return result, hasMore
}

// ==================== Get or Create Conversation ====================

func GetOrCreateConversation(userID string, userType string, param *GetOrCreateConversationParam) (string, string) {
	if param.UserID == "" || param.UserType == "" {
		panic(exception.NewBusinessError("参数错误", 400))
	}
	cid := imModel.GenerateConversationID(userID, enums.LoginTypeEnum(userType), param.UserID, enums.LoginTypeEnum(param.UserType))
	// Ensure the conversation cache exists
	var conv imModel.Conversation
	err := db.DB.Where("id = ?", cid).First(&conv).Error
	if err != nil {
		now := time.Now()
		conv = imModel.Conversation{
			ID:        cid,
			UserID1:   userID,
			UserType1: userType,
			UserID2:   param.UserID,
			UserType2: param.UserType,
			CreatedAt: &now,
		}
		db.DB.Create(&conv)
	}

	// Resolve display name
	displayName := param.UserID
	if param.UserType == string(enums.LoginTypeBusiness) {
		var u sysUser.SysUser
		if err := db.DB.First(&u, "id = ?", param.UserID).Error; err == nil && u.Nickname != nil {
			displayName = *u.Nickname
		}
	} else {
		var u cliUser.ClientUser
		if err := db.DB.First(&u, "id = ?", param.UserID).Error; err == nil && u.Nickname != nil {
			displayName = *u.Nickname
		}
	}
	return cid, displayName
}
