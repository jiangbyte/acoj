package message

import (
	"io"
	"path/filepath"
	"time"

	"hei-gin/sdk/db"
	"hei-gin/sdk/exception"
	"hei-gin/sdk/storage"
	"hei-gin/sdk/utils"
	imModel "hei-gin/plugins/plugin-im/model"

	"github.com/gin-gonic/gin"
)

type FileUploadResult struct {
	URL          string `json:"url"`
	OriginalName string `json:"original_name"`
	FileSize     int64  `json:"file_size"`
	FileType     string `json:"file_type"`
}

func UploadFile(c *gin.Context, senderID, senderType string) *FileUploadResult {
	file, header, err := c.Request.FormFile("file")
	if err != nil {
		panic(exception.NewBusinessError("上传文件失败: "+err.Error(), 400))
	}
	defer file.Close()

	now := time.Now()
	ext := filepath.Ext(header.Filename)
	fileName := utils.GenerateID() + ext

	s := storage.GetStorage("LOCAL")
	if s == nil {
		panic(exception.NewBusinessError("存储服务不可用", 500))
	}

	data, err := io.ReadAll(file)
	if err != nil {
		panic(exception.NewBusinessError("读取文件失败: "+err.Error(), 500))
	}

	filePath, err := s.Store("im", fileName, data)
	if err != nil {
		panic(exception.NewBusinessError("保存文件失败: "+err.Error(), 500))
	}

	// Build file URL
	fileURL := filePath
	if s.GetURL("im", fileName) != "" {
		fileURL = s.GetURL("im", fileName)
	}

	msgType := c.PostForm("msg_type")
	if msgType == "" {
		msgType = "FILE"
	}

	imFile := imModel.ImFile{
		ID:           utils.GenerateID(),
		SenderID:     senderID,
		SenderType:   senderType,
		MsgType:      msgType,
		OriginalName: header.Filename,
		FileURL:      fileURL,
		FileSize:     header.Size,
		FileType:     ext,
		CreatedAt:    &now,
	}
	if cid := c.PostForm("conversation_id"); cid != "" {
		imFile.ConversationID = cid
	}

	if err := db.DB.Create(&imFile).Error; err != nil {
		panic(exception.NewBusinessError("保存文件记录失败: "+err.Error(), 500))
	}

	return &FileUploadResult{
		URL:          fileURL,
		OriginalName: header.Filename,
		FileSize:     header.Size,
		FileType:     ext,
	}
}
