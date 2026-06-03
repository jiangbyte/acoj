package message

import (
	"context"
	"time"

	"gorm.io/gorm"

	"hei-gin/core/db"
	"hei-gin/core/enums"
	"hei-gin/core/exception"
	"hei-gin/core/pojo"
	"hei-gin/core/result"
	"hei-gin/core/utils"
	"hei-gin/core/ws"

	"github.com/gin-gonic/gin"
)

func toVO(e *SysMessage) *MessageVO {
	v := &MessageVO{
		ID: e.ID, ConversationID: e.ConversationID,
		Title: e.Title, Content: e.Content,
		SenderID: e.SenderID, SenderType: string(e.SenderType),
		ReceiverID: e.ReceiverID, ReceiverType: string(e.ReceiverType),
		MessageType: e.MessageType, Status: e.Status,
		CreatedAt: pojo.FormatDateTimePtr(e.CreatedAt),
		UpdatedAt: pojo.FormatDateTimePtr(e.UpdatedAt),
	}
	if e.ReadAt != nil {
		s := pojo.FormatDateTime(*e.ReadAt)
		v.ReadAt = &s
	}
	return v
}

func toVOList(records []SysMessage) []MessageVO {
	r := make([]MessageVO, len(records))
	for i, e := range records {
		r[i] = *toVO(&e)
	}
	return r
}

func Page(c *gin.Context, receiverID string, param *MessagePageParam) gin.H {
	ctx := context.Background()
	if param.Current < 1 {
		param.Current = 1
	}
	if param.Size < 1 {
		param.Size = 10
	}
	if param.Size > 100 {
		param.Size = 100
	}

	query := db.DB.WithContext(ctx).Model(&SysMessage{}).Where("receiver_id = ?", receiverID)
	if param.Status != "" {
		query = query.Where("status = ?", param.Status)
	}

	var total int64
	query.Count(&total)

	var records []SysMessage
	query.Order("created_at DESC").Limit(param.Size).Offset((param.Current-1)*param.Size).Find(&records)
	return result.PageDataResult(c, toVOList(records), total, param.Current, param.Size)
}

func UnreadCount(receiverID string) int64 {
	var count int64
	db.DB.Model(&SysMessage{}).Where("receiver_id = ? AND status = ?", receiverID, "unread").Count(&count)
	return count
}

func Detail(id string) *MessageVO {
	var entity SysMessage
	if err := db.DB.First(&entity, "id = ?", id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil
		}
		panic(exception.NewBusinessError("查询消息失败: "+err.Error(), 500))
	}
	return toVO(&entity)
}

func Send(c *gin.Context, param *MessageSendParam, senderID string) {
	ctx := context.Background()
	now := time.Now()

	// Rate limiting
	if !ws.GlobalCrossHub.AllowMessage(senderID, enums.LoginTypeBusiness) {
		panic(exception.NewBusinessError("发送消息过于频繁，请稍后重试", 429))
	}
	receiverType := enums.LoginTypeEnum(param.ReceiverType)

	if receiverType == "" {
		receiverType = enums.LoginTypeBusiness
	}

	if receiverType == enums.LoginTypeConsumer {
		records := make([]map[string]interface{}, len(param.ReceiverIDs))
		for i, rid := range param.ReceiverIDs {
			cid := GenerateConversationID(senderID, enums.LoginTypeBusiness, rid, enums.LoginTypeConsumer)
			records[i] = map[string]interface{}{
				"id":              utils.GenerateID(),
				"conversation_id": cid,
				"title":           param.Title,
				"content":         param.Content,
				"sender_id":       senderID,
				"sender_type":     enums.LoginTypeBusiness,
				"receiver_id":     rid,
				"receiver_type":   enums.LoginTypeConsumer,
				"message_type":    "system",
				"status":          "unread",
				"created_at":      now,
				"updated_at":      now,
			}
		}
		if err := db.DB.WithContext(ctx).Table("client_message").Create(&records).Error; err != nil {
			panic(exception.NewBusinessError("发送消息失败: "+err.Error(), 500))
		}
		for i, rid := range param.ReceiverIDs {
			msgID := records[i]["id"].(string)
			ws.GlobalCrossHub.SendToConsumer(rid, ws.Message{
				Type: ws.MsgNewMessage,
				Payload: ws.NewMessagePayload{
					MessageID: msgID, Title: param.Title, CreatedAt: pojo.FormatDateTime(now),
				},
			}, msgID)
		}
		return
	}

	records := make([]SysMessage, len(param.ReceiverIDs))
	for i, rid := range param.ReceiverIDs {
		records[i] = SysMessage{
			ID: utils.GenerateID(),
			ConversationID: GenerateConversationID(senderID, enums.LoginTypeBusiness, rid, enums.LoginTypeBusiness),
			Title: param.Title, Content: param.Content,
			SenderID: &senderID, SenderType: enums.LoginTypeBusiness,
			ReceiverID: rid, ReceiverType: enums.LoginTypeBusiness,
			MessageType: "system", Status: "unread", CreatedAt: &now, UpdatedAt: &now,
		}
	}
	if err := db.DB.WithContext(ctx).Create(&records).Error; err != nil {
		panic(exception.NewBusinessError("发送消息失败: "+err.Error(), 500))
	}
	for i, rid := range param.ReceiverIDs {
		ws.GlobalCrossHub.SendToUser(rid, ws.Message{
			Type: ws.MsgNewMessage,
			Payload: ws.NewMessagePayload{
				MessageID: records[i].ID, Title: param.Title, CreatedAt: pojo.FormatDateTime(now),
			},
		}, records[i].ID)
	}
}

func MarkRead(id string) {
	now := time.Now()
	if err := db.DB.Model(&SysMessage{}).Where("id = ?", id).
		Updates(map[string]interface{}{"status": "read", "read_at": &now}).Error; err != nil {
		panic(exception.NewBusinessError("标记已读失败: "+err.Error(), 500))
	}
}

// MarkConversationRead marks all unread received messages in a conversation as read.
func MarkConversationRead(receiverID string, conversationID string) {
	now := time.Now()
	if err := db.DB.Model(&SysMessage{}).
		Where("conversation_id = ? AND receiver_id = ? AND status = ?", conversationID, receiverID, "unread").
		Updates(map[string]interface{}{"status": "read", "read_at": &now}).Error; err != nil {
		panic(exception.NewBusinessError("标记已读失败: "+err.Error(), 500))
	}
}
func MarkAllRead(receiverID string) {
	now := time.Now()
	if err := db.DB.Model(&SysMessage{}).
		Where("receiver_id = ? AND status = ?", receiverID, "unread").
		Updates(map[string]interface{}{"status": "read", "read_at": &now}).Error; err != nil {
		panic(exception.NewBusinessError("标记全部已读失败: "+err.Error(), 500))
	}
}

func Remove(ids []string) {
	if len(ids) == 0 {
		return
	}
	if err := db.DB.Where("id IN ?", ids).Delete(&SysMessage{}).Error; err != nil {
		panic(exception.NewBusinessError("删除消息失败: "+err.Error(), 500))
	}
}
