package session

import (
	"context"
	"encoding/json"
	"fmt"
	"time"

	"hei-gin/core/constants"
	"hei-gin/core/db"
	"hei-gin/ent/gen/clientuser"
)

type PageParam struct {
	Page int `form:"page" json:"page"`
	Size int `form:"size" json:"size"`
}

type SessionVO struct {
	LoginID    string `json:"login_id"`
	Username   string `json:"username"`
	Nickname   string `json:"nickname"`
	TokenCount int    `json:"token_count"`
}

type ExitReq struct {
	LoginID string `form:"login_id" json:"login_id" binding:"required"`
}

type SessionAnalysisResult struct {
	TotalCount        int    `json:"total_count"`
	MaxTokenCount     int    `json:"max_token_count"`
	OneHourNewlyAdded int    `json:"one_hour_newly_added"`
	ProportionOfBandC string `json:"proportion_of_b_and_c"`
}

type TokenInfo struct {
	Token      string `json:"token"`
	TTL        int    `json:"ttl"`
	CreatedAt  string `json:"created_at"`
	DeviceType string `json:"device_type"`
	DeviceID   string `json:"device_id"`
}

type TokenListParam struct {
	UserID string `form:"user_id" json:"user_id" binding:"required"`
}

type ExitTokenReq struct {
	UserID string `form:"user_id" json:"user_id" binding:"required"`
	Token  string `form:"token" json:"token" binding:"required"`
}

type LogBarChartData struct {
	Days   []string            `json:"days"`
	Series []LogCategorySeries `json:"series"`
}

type LogCategorySeries struct {
	Name string `json:"name"`
	Data []int  `json:"data"`
}

type LogPieChartData struct {
	Data []LogCategoryTotal `json:"data"`
}

type LogCategoryTotal struct {
	Category string `json:"category"`
	Total    int    `json:"total"`
}

func Page(page, size int) (int, []SessionVO, error) {
	ctx := context.Background()
	pattern := constants.SessionPrefixConsumer + "*"

	var sessionKeys []string
	iter := db.Redis.Scan(ctx, 0, pattern, 0).Iterator()
	for iter.Next(ctx) {
		sessionKeys = append(sessionKeys, iter.Val())
	}
	if err := iter.Err(); err != nil {
		return 0, nil, err
	}

	total := len(sessionKeys)

	start := (page - 1) * size
	if start >= total {
		return total, nil, nil
	}
	end := start + size
	if end > total {
		end = total
	}
	pagedKeys := sessionKeys[start:end]

	sessionPrefixLen := len(constants.SessionPrefixConsumer)
	loginIDSet := make(map[string]struct{})
	var loginIDs []string
	for _, key := range pagedKeys {
		loginID := key[sessionPrefixLen:]
		if _, ok := loginIDSet[loginID]; !ok {
			loginIDSet[loginID] = struct{}{}
			loginIDs = append(loginIDs, loginID)
		}
	}

	type userCacheEntry struct {
		username string
		nickname string
	}
	userCache := make(map[string]userCacheEntry, len(loginIDs))
	if len(loginIDs) > 0 {
		users, err := db.Client.ClientUser.Query().
			Where(clientuser.IDIn(loginIDs...)).
			Select(clientuser.FieldID, clientuser.FieldUsername, clientuser.FieldNickname).
			All(ctx)
		if err == nil {
			for _, u := range users {
				userCache[u.ID] = userCacheEntry{u.Username, u.Nickname}
			}
		}
	}

	var result []SessionVO
	for _, key := range pagedKeys {
		loginID := key[sessionPrefixLen:]

		tokens, _ := db.Redis.SMembers(ctx, key).Result()
		tokenCount := len(tokens)

		vo := SessionVO{
			LoginID:    loginID,
			TokenCount: tokenCount,
		}

		if entry, ok := userCache[loginID]; ok {
			vo.Username = entry.username
			vo.Nickname = entry.nickname
		}

		result = append(result, vo)
	}

	return total, result, nil
}

func Analysis() (*SessionAnalysisResult, error) {
	ctx := context.Background()
	now := time.Now()

	totalCount := 0
	maxTokenCount := 0
	oneHourNewlyAdded := 0
	consumerCount := 0

	iter := db.Redis.Scan(ctx, 0, constants.SessionPrefixConsumer+"*", 0).Iterator()
	for iter.Next(ctx) {
		tokens, err := db.Redis.SMembers(ctx, iter.Val()).Result()
		if err != nil {
			continue
		}
		count := len(tokens)
		totalCount += count
		consumerCount += count
		if count > maxTokenCount {
			maxTokenCount = count
		}
		for _, token := range tokens {
			tokenKey := constants.TokenPrefixConsumer + token
			data, err := db.Redis.Get(ctx, tokenKey).Result()
			if err != nil {
				continue
			}
			var td struct {
				CreatedAt string `json:"created_at"`
			}
			if err := json.Unmarshal([]byte(data), &td); err != nil {
				continue
			}
			createdAt, err := time.Parse(time.RFC3339, td.CreatedAt)
			if err != nil {
				continue
			}
			if now.Sub(createdAt) <= time.Hour {
				oneHourNewlyAdded++
			}
		}
	}
	if err := iter.Err(); err != nil {
		return nil, err
	}

	proportion := fmt.Sprintf("0/%d", consumerCount)

	return &SessionAnalysisResult{
		TotalCount:        totalCount,
		MaxTokenCount:     maxTokenCount,
		OneHourNewlyAdded: oneHourNewlyAdded,
		ProportionOfBandC: proportion,
	}, nil
}

func TokenList(userID string) ([]TokenInfo, error) {
	ctx := context.Background()
	sessionKey := constants.SessionPrefixConsumer + userID

	tokens, err := db.Redis.SMembers(ctx, sessionKey).Result()
	if err != nil {
		return nil, err
	}

	var result []TokenInfo
	for _, token := range tokens {
		tokenKey := constants.TokenPrefixConsumer + token
		ttlDuration := db.Redis.TTL(ctx, tokenKey).Val()
		ttlSeconds := int(ttlDuration.Seconds())
		if ttlSeconds < 0 {
			ttlSeconds = 0
		}

		data, err := db.Redis.Get(ctx, tokenKey).Result()
		if err != nil {
			continue
		}

		var td struct {
			CreatedAt string                 `json:"created_at"`
			Extra     map[string]interface{} `json:"extra"`
		}
		if err := json.Unmarshal([]byte(data), &td); err != nil {
			continue
		}

		deviceType := ""
		deviceID := ""
		if td.Extra != nil {
			if v, ok := td.Extra["device_type"]; ok {
				deviceType = fmt.Sprintf("%v", v)
			}
			if v, ok := td.Extra["device_id"]; ok {
				deviceID = fmt.Sprintf("%v", v)
			}
		}

		result = append(result, TokenInfo{
			Token:      token,
			TTL:        ttlSeconds,
			CreatedAt:  td.CreatedAt,
			DeviceType: deviceType,
			DeviceID:   deviceID,
		})
	}

	return result, nil
}

func ExitToken(userID, token string) error {
	ctx := context.Background()
	sessionKey := constants.SessionPrefixConsumer + userID
	tokenKey := constants.TokenPrefixConsumer + token

	db.Redis.SRem(ctx, sessionKey, token)
	db.Redis.Del(ctx, tokenKey)

	return nil
}

func ChartData() (*LogBarChartData, *LogPieChartData, error) {
	ctx := context.Background()
	now := time.Now()

	days := make([]string, 7)
	dayBuckets := make(map[string]int)
	for i := 6; i >= 0; i-- {
		day := now.AddDate(0, 0, -i).Format("2006-01-02")
		days[6-i] = day
		dayBuckets[day] = 0
	}

	consumerTotal := 0

	// Process consumer sessions
	iter := db.Redis.Scan(ctx, 0, constants.SessionPrefixConsumer+"*", 0).Iterator()
	for iter.Next(ctx) {
		tokens, err := db.Redis.SMembers(ctx, iter.Val()).Result()
		if err != nil {
			continue
		}
		for _, token := range tokens {
			tokenKey := constants.TokenPrefixConsumer + token
			data, err := db.Redis.Get(ctx, tokenKey).Result()
			if err != nil {
				continue
			}
			var td struct {
				CreatedAt string `json:"created_at"`
			}
			if err := json.Unmarshal([]byte(data), &td); err != nil {
				continue
			}
			createdAt, err := time.Parse(time.RFC3339, td.CreatedAt)
			if err != nil {
				continue
			}
			day := createdAt.Format("2006-01-02")
			if _, ok := dayBuckets[day]; ok {
				dayBuckets[day]++
			}
			consumerTotal++
		}
	}
	if err := iter.Err(); err != nil {
		return nil, nil, err
	}

	barData := &LogBarChartData{
		Days: days,
		Series: []LogCategorySeries{
			{Name: "新增Token数", Data: make([]int, 7)},
		},
	}
	for i, day := range days {
		barData.Series[0].Data[i] = dayBuckets[day]
	}

	pieData := &LogPieChartData{
		Data: []LogCategoryTotal{
			{Category: "C端", Total: consumerTotal},
		},
	}

	return barData, pieData, nil
}
