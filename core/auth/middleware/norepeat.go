package middleware

import (
	"bytes"
	"context"
	"crypto/md5"
	"encoding/json"
	"fmt"
	"io"
	"time"

	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/constants"
	"hei-gin/core/db"
	bizerr "hei-gin/core/exception"
	"hei-gin/core/utils"
)

type norepeatBody struct {
	Hash string `json:"hash"`
	Time int64  `json:"time"`
}

// NoRepeat returns middleware that prevents duplicate submissions within the interval.
// interval is in milliseconds.
func NoRepeat(interval int) gin.HandlerFunc {
	return func(c *gin.Context) {
		if db.Redis == nil {
			c.Next()
			return
		}

		var bodyBytes []byte
		if c.Request.Body != nil {
			bodyBytes, _ = io.ReadAll(c.Request.Body)
			c.Request.Body = io.NopCloser(bytes.NewBuffer(bodyBytes))
		}

		ip := utils.GetClientIP(c)
		userID := ""
		loginID := c.GetString("login_id")
		if loginID == "" {
			loginID = auth.AuthTool.GetLoginID(c)
		}
		if loginID == "" {
			loginID = auth.ClientAuthTool.GetLoginID(c)
		}
		if loginID != "" {
			userID = loginID
		}
		key := constants.NoRepeatPrefix + ip + ":" + userID + ":" + c.FullPath()

		hash := fmt.Sprintf("%x", md5.Sum(bodyBytes))

		ctx := context.Background()
		existing, err := db.Redis.Get(ctx, key).Result()
		if err == nil {
			var prev norepeatBody
			if json.Unmarshal([]byte(existing), &prev) == nil {
				if prev.Hash == hash {
					elapsed := time.Now().UnixMilli() - prev.Time
					if elapsed < int64(interval) {
						remaining := (int64(interval) - elapsed) / 1000
						msg := fmt.Sprintf("请求过于频繁，请%d秒后再试", remaining)
						panic(bizerr.NewBusinessError(msg))
					}
				}
			}
		}

		record, _ := json.Marshal(norepeatBody{
			Hash: hash,
			Time: time.Now().UnixMilli(),
		})
		db.Redis.Set(ctx, key, string(record), 3600*time.Second)

		c.Next()
	}
}
