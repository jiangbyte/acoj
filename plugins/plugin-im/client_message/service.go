package client_message

import (
	"time"

	"gorm.io/gorm"

	"hei-gin/sdk/db"
	"hei-gin/sdk/enums"
	"hei-gin/sdk/exception"
	"hei-gin/sdk/pojo"
	"hei-gin/sdk/result"
	"hei-gin/sdk/utils"
	"hei-gin/sdk/ws"
	imModel "hei-gin/plugins/plugin-im/model"

	"github.com/gin-gonic/gin"
)

func toVO(e *imModel.ClientMessage) *MessageVO {
	v := &MessageVO{
		ID: e.ID, ConversationID: e.ConversationID,
		Title: e.Title, Content: e.Content, Extra: e.Extra,
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

func toVOList(records []imModel.ClientMessage) []MessageVO {
	r := make([]MessageVO, len(records))
	for i, e := range records {
		r[i] = *toVO(&e)
	}
	return r
}

func Page(c *gin.Context, receiverID string, param *MessagePageParam) gin.H {
	ctx := c.Request.Context()
	if param.Current < 1 {
		param.Current = 1
	}
	if param.Size < 1 {
		param.Size = 10
	}
	if param.Size > 100 {
		param.Size = 100
	}

	query := db.DB.WithContext(ctx).Model(&imModel.ClientMessage{}).Where("receiver_id = ?", receiverID)
	if param.Status != "" {
		query = query.Where("status = ?", param.Status)
	}

	var total int64
	query.Count(&total)

	var records []imModel.ClientMessage
	query.Order("created_at DESC").Limit(param.Size).Offset((param.Current-1)*param.Size).Find(&records)
	return result.PageDataResult(c, toVOList(records), total, param.Current, param.Size)
}

func UnreadCount(receiverID string) int64 {
	var count int64
	db.DB.Model(&imModel.ClientMessage{}).Where("receiver_id = ? AND status = ?", receiverID, "unread").Count(&count)
	return count
}

func Detail(id string) *MessageVO {
	var entity imModel.ClientMessage
	if err := db.DB.First(&entity, "id = ?", id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil
		}
		panic(exception.NewBusinessError("查询消息失败: "+err.Error(), 500))
	}
	return toVO(&entity)
}

func Send(c *gin.Context, param *MessageSendParam, senderID string) {
	ctx := c.Request.Context()
	now := time.Now()

	if !ws.GlobalCrossHub.AllowMessage(senderID, enums.LoginTypeConsumer) {
		panic(exception.NewBusinessError("发送消息过于频繁，请稍后重试", 429))
	}

	msgType := param.MsgType
	if msgType == "" {
		msgType = "text"
	}
	extra := param.Extra

	receiverType := enums.LoginTypeEnum(param.ReceiverType)
	if receiverType == "" {
		receiverType = enums.LoginTypeConsumer
	}

	if receiverType == enums.LoginTypeBusiness {
		// Cross-table write: consumer → business, stored in sys_message table
		records := make([]imModel.SysMessage, len(param.ReceiverIDs))
		for i, rid := range param.ReceiverIDs {
			cid := imModel.GenerateConversationID(senderID, enums.LoginTypeConsumer, rid, enums.LoginTypeBusiness)
			records[i] = imModel.SysMessage{
				ID:             utils.GenerateID(),
				ConversationID: cid,
				Title:          param.Title,
				Content:        param.Content,
				SenderID:       &senderID,
				SenderType:     enums.LoginTypeConsumer,
				ReceiverID:     rid,
				ReceiverType:   enums.LoginTypeBusiness,
				MessageType:    msgType,
				Extra:          extra,
				Status:         "unread",
				CreatedAt:      &now,
				UpdatedAt:      &now,
			}
		}
		if err := db.DB.WithContext(ctx).Create(&records).Error; err != nil {
			panic(exception.NewBusinessError("发送消息失败: "+err.Error(), 500))
		}
		for i, rid := range param.ReceiverIDs {
			ws.GlobalCrossHub.SendToUser(rid, ws.Message{
				Type: ws.MsgNewMessage,
				Payload: ws.NewMessagePayload{
					MessageID: records[i].ID, Title: param.Title, Content: param.Content,
					MsgType: msgType, Extra: extra,
					CreatedAt: pojo.FormatDateTime(now),
				},
			}, records[i].ID)
		}
		return
	}

	records := make([]imModel.ClientMessage, len(param.ReceiverIDs))
	for i, rid := range param.ReceiverIDs {
		records[i] = imModel.ClientMessage{
			ID: utils.GenerateID(),
			ConversationID: imModel.GenerateConversationID(senderID, enums.LoginTypeConsumer, rid, enums.LoginTypeConsumer),
			Title: param.Title, Content: param.Content, Extra: extra,
			SenderID: &senderID, SenderType: enums.LoginTypeConsumer,
			ReceiverID: rid, ReceiverType: enums.LoginTypeConsumer,
			MessageType: msgType, Status: "unread", CreatedAt: &now, UpdatedAt: &now,
		}
	}
	if err := db.DB.WithContext(ctx).Create(&records).Error; err != nil {
		panic(exception.NewBusinessError("发送消息失败: "+err.Error(), 500))
	}
	for i, rid := range param.ReceiverIDs {
		ws.GlobalCrossHub.SendToConsumer(rid, ws.Message{
			Type: ws.MsgNewMessage,
			Payload: ws.NewMessagePayload{
				MessageID: records[i].ID, Title: param.Title, Content: param.Content,
				MsgType: msgType, Extra: extra,
				CreatedAt: pojo.FormatDateTime(now),
			},
		}, records[i].ID)
	}
}

func MarkRead(id string) {
	now := time.Now()
	if err := db.DB.Model(&imModel.ClientMessage{}).Where("id = ?", id).
		Updates(map[string]interface{}{"status": "read", "read_at": &now}).Error; err != nil {
		panic(exception.NewBusinessError("标记已读失败: "+err.Error(), 500))
	}
}

func MarkConversationRead(receiverID string, conversationID string) {
	now := time.Now()
	if err := db.DB.Model(&imModel.ClientMessage{}).
		Where("conversation_id = ? AND receiver_id = ? AND status = ?", conversationID, receiverID, "unread").
		Updates(map[string]interface{}{"status": "read", "read_at": &now}).Error; err != nil {
		panic(exception.NewBusinessError("标记已读失败: "+err.Error(), 500))
	}
}

func MarkAllRead(receiverID string) {
	now := time.Now()
	if err := db.DB.Model(&imModel.ClientMessage{}).
		Where("receiver_id = ? AND status = ?", receiverID, "unread").
		Updates(map[string]interface{}{"status": "read", "read_at": &now}).Error; err != nil {
		panic(exception.NewBusinessError("标记全部已读失败: "+err.Error(), 500))
	}
}

func Remove(ids []string) {
	if len(ids) == 0 {
		return
	}
	if err := db.DB.Where("id IN ?", ids).Delete(&imModel.ClientMessage{}).Error; err != nil {
		panic(exception.NewBusinessError("删除消息失败: "+err.Error(), 500))
	}
}
