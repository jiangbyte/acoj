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

func toVO(e *ClientMessage) *MessageVO {
	v := &MessageVO{
		ID: e.ID, Title: e.Title, Content: e.Content,
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

func toVOList(records []ClientMessage) []MessageVO {
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

	query := db.DB.WithContext(ctx).Model(&ClientMessage{}).Where("receiver_id = ?", receiverID)
	if param.Status != "" {
		query = query.Where("status = ?", param.Status)
	}

	var total int64
	query.Count(&total)

	var records []ClientMessage
	query.Order("created_at DESC").Limit(param.Size).Offset((param.Current-1)*param.Size).Find(&records)
	return result.PageDataResult(c, toVOList(records), total, param.Current, param.Size)
}

func UnreadCount(receiverID string) int64 {
	var count int64
	db.DB.Model(&ClientMessage{}).Where("receiver_id = ? AND status = ?", receiverID, "unread").Count(&count)
	return count
}

func Detail(id string) *MessageVO {
	var entity ClientMessage
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
	receiverType := enums.LoginTypeEnum(param.ReceiverType)

	if receiverType == "" {
		receiverType = enums.LoginTypeConsumer
	}

	if receiverType == enums.LoginTypeBusiness {
		// C端→B端: batch write to sys_message, push via SendToUser
		records := make([]map[string]interface{}, len(param.ReceiverIDs))
		for i, rid := range param.ReceiverIDs {
			records[i] = map[string]interface{}{
				"id":            utils.GenerateID(),
				"title":         param.Title,
				"content":       param.Content,
				"sender_id":     senderID,
				"sender_type":   enums.LoginTypeConsumer,
				"receiver_id":   rid,
				"receiver_type": enums.LoginTypeBusiness,
				"message_type":  "system",
				"status":        "unread",
				"created_at":    now,
				"updated_at":    now,
			}
		}
		if err := db.DB.WithContext(ctx).Table("sys_message").Create(&records).Error; err != nil {
			panic(exception.NewBusinessError("发送消息失败: "+err.Error(), 500))
		}
		for i, rid := range param.ReceiverIDs {
			ws.GlobalHub.SendToUser(rid, ws.Message{
				Type: ws.MsgNewMessage,
				Payload: ws.NewMessagePayload{
					MessageID: records[i]["id"].(string), Title: param.Title, CreatedAt: pojo.FormatDateTime(now),
				},
			})
		}
		return
	}

	// C端→C端: batch write to client_message, push via SendToConsumer
	records := make([]ClientMessage, len(param.ReceiverIDs))
	for i, rid := range param.ReceiverIDs {
		records[i] = ClientMessage{
			ID: utils.GenerateID(), Title: param.Title, Content: param.Content,
			SenderID: &senderID, SenderType: enums.LoginTypeConsumer,
			ReceiverID: rid, ReceiverType: enums.LoginTypeConsumer,
			MessageType: "system", Status: "unread", CreatedAt: &now, UpdatedAt: &now,
		}
	}
	if err := db.DB.WithContext(ctx).Create(&records).Error; err != nil {
		panic(exception.NewBusinessError("发送消息失败: "+err.Error(), 500))
	}
	for i, rid := range param.ReceiverIDs {
		ws.GlobalHub.SendToConsumer(rid, ws.Message{
			Type: ws.MsgNewMessage,
			Payload: ws.NewMessagePayload{
				MessageID: records[i].ID, Title: param.Title, CreatedAt: pojo.FormatDateTime(now),
			},
		})
	}
}

func MarkRead(id string) {
	now := time.Now()
	if err := db.DB.Model(&ClientMessage{}).Where("id = ?", id).
		Updates(map[string]interface{}{"status": "read", "read_at": &now}).Error; err != nil {
		panic(exception.NewBusinessError("标记已读失败: "+err.Error(), 500))
	}
}

func MarkAllRead(receiverID string) {
	now := time.Now()
	if err := db.DB.Model(&ClientMessage{}).
		Where("receiver_id = ? AND status = ?", receiverID, "unread").
		Updates(map[string]interface{}{"status": "read", "read_at": &now}).Error; err != nil {
		panic(exception.NewBusinessError("标记全部已读失败: "+err.Error(), 500))
	}
}

func Remove(ids []string) {
	if len(ids) == 0 {
		return
	}
	if err := db.DB.Where("id IN ?", ids).Delete(&ClientMessage{}).Error; err != nil {
		panic(exception.NewBusinessError("删除消息失败: "+err.Error(), 500))
	}
}
