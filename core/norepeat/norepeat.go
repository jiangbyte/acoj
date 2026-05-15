package norepeat

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
	bizerr "hei-gin/core/errors"
	"hei-gin/core/utils"
)

type norepeatBody struct {
	Hash string `json:"hash"`
	Time int64  `json:"time"`
}

// NoRepeat returns a Gin middleware that prevents duplicate submissions within the interval.
// interval is in milliseconds.
func NoRepeat(interval int) gin.HandlerFunc {
	return func(c *gin.Context) {
		if db.Redis == nil {
			c.Next()
			return
		}

		// Read body
		var bodyBytes []byte
		if c.Request.Body != nil {
			bodyBytes, _ = io.ReadAll(c.Request.Body)
			c.Request.Body = io.NopCloser(bytes.NewBuffer(bodyBytes))
		}

		// Build key: norepeat:{ip}:{userId}:{path}
		ip := utils.GetClientIP(c)
		userID := ""
		loginID := c.GetString("login_id") // set by auth middleware
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

		// Hash body (excluding file params)
		hash := fmt.Sprintf("%x", md5.Sum(bodyBytes))

		ctx := context.Background()
		existing, err := db.Redis.Get(ctx, key).Result()
		if err == nil {
			// Key exists — check interval
			var prev norepeatBody
			if json.Unmarshal([]byte(existing), &prev) == nil {
				if prev.Hash == hash && time.Now().UnixMilli()-prev.Time < int64(interval) {
					panic(bizerr.NewBusinessError("请求过于频繁"))
				}
			}
		}

		// Store new record
		record, _ := json.Marshal(norepeatBody{
			Hash: hash,
			Time: time.Now().UnixMilli(),
		})
		db.Redis.Set(ctx, key, string(record), 3600*time.Second)

		c.Next()
	}
}
