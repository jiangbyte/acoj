package clientsession

import (
	"context"
	"encoding/json"
	"fmt"
	"sort"
	"strings"
	"time"

	"hei-gin/core/auth"
	"hei-gin/core/constants"
	"hei-gin/core/db"
	ent "hei-gin/ent/gen"

	"github.com/gin-gonic/gin"
	"github.com/redis/go-redis/v9"
)

var svcCtx = context.Background()

// scanKeys uses Redis SCAN (cursor loop, 200 per batch) to find all keys matching pattern.
func scanKeys(ctx context.Context, redis *redis.Client, pattern string) ([]string, error) {
	var cursor uint64
	var keys []string
	for {
		batch, nextCursor, err := redis.Scan(ctx, cursor, pattern, 200).Result()
		if err != nil {
			return nil, err
		}
		keys = append(keys, batch...)
		if nextCursor == 0 {
			break
		}
		cursor = nextCursor
	}
	return keys, nil
}

// Analysis returns an overview analysis of CONSUMER sessions only.
func Analysis(c *gin.Context) *SessionAnalysisResult {
	cKeys, _ := scanKeys(svcCtx, db.Redis, constants.SESSION_PREFIX_CONSUMER+"*")
	cTotal, cNewly, cMax := countTokens(svcCtx, db.Redis, cKeys, constants.TOKEN_PREFIX_CONSUMER)

	return &SessionAnalysisResult{
		TotalCount:        cTotal,
		MaxTokenCount:     cMax,
		OneHourNewlyAdded: cNewly,
		ProportionOfBAndC: fmt.Sprintf("0/%d", cTotal),
	}
}

// countTokens iterates session keys, SMEMBER to get tokens, GET each token's data,
// and returns total token count, count of tokens created within the last hour,
// and the maximum tokens for a single user.
func countTokens(ctx context.Context, redis *redis.Client, sessionKeys []string, tokenPrefix string) (total, oneHourNewlyAdded, maxPerUser int) {
	userTokenCounts := make(map[string]int)
	oneHourAgo := time.Now().Add(-1 * time.Hour)

	for _, sessionKey := range sessionKeys {
		parts := strings.Split(sessionKey, ":")
		userID := parts[len(parts)-1]

		tokens, err := redis.SMembers(ctx, sessionKey).Result()
		if err != nil {
			continue
		}
		userTokenCounts[userID] = len(tokens)

		for _, token := range tokens {
			total++
			tokenKey := tokenPrefix + token
			data, err := redis.Get(ctx, tokenKey).Result()
			if err != nil {
				continue
			}
			var tokenData map[string]any
			if err := json.Unmarshal([]byte(data), &tokenData); err != nil {
				continue
			}
			createdAtStr, _ := tokenData["created_at"].(string)
			if createdAtStr != "" {
				createdAt, err := time.Parse("2006-01-02 15:04:05", createdAtStr)
				if err == nil && createdAt.After(oneHourAgo) {
					oneHourNewlyAdded++
				}
			}
		}
	}

	for _, count := range userTokenCounts {
		if count > maxPerUser {
			maxPerUser = count
		}
	}
	return
}

// countDaily groups tokens by creation day (format "2006-01-02").
func countDaily(ctx context.Context, redis *redis.Client, sessionKeys []string, tokenPrefix string) map[string]int {
	daily := make(map[string]int)
	for _, sessionKey := range sessionKeys {
		tokens, err := redis.SMembers(ctx, sessionKey).Result()
		if err != nil {
			continue
		}
		for _, token := range tokens {
			tokenKey := tokenPrefix + token
			data, err := redis.Get(ctx, tokenKey).Result()
			if err != nil {
				continue
			}
			var tokenData map[string]any
			if err := json.Unmarshal([]byte(data), &tokenData); err != nil {
				continue
			}
			createdAtStr, _ := tokenData["created_at"].(string)
			if createdAtStr != "" {
				createdAt, err := time.Parse("2006-01-02 15:04:05", createdAtStr)
				if err == nil {
					day := createdAt.Format("2006-01-02")
					daily[day]++
				}
			}
		}
	}
	return daily
}

// Page returns a paginated list of CONSUMER sessions as a full gin.H response (manual pagination).
func Page(c *gin.Context, param *SessionPageParam) gin.H {
	sessions, err := collectSessions(svcCtx, db.Redis, constants.SESSION_PREFIX_CONSUMER, constants.TOKEN_PREFIX_CONSUMER, param.Keyword)
	if err != nil {
		sessions = []*SessionPageResult{}
	}

	// Sort by SessionCreateTime DESC (RFC3339 strings are lexicographically sortable)
	sort.Slice(sessions, func(i, j int) bool {
		return sessions[i].SessionCreateTime > sessions[j].SessionCreateTime
	})

	total := len(sessions)
	current := param.Current
	if current < 1 {
		current = 1
	}
	size := param.Size
	if size < 1 {
		size = 10
	}

	pages := (total + size - 1) / size
	start := (current - 1) * size
	var pageRecords []*SessionPageResult

	if start >= total {
		pageRecords = []*SessionPageResult{}
	} else {
		end := start + size
		if end > total {
			end = total
		}
		pageRecords = sessions[start:end]
	}

	return gin.H{
		"code": 200, "message": "请求成功", "success": true,
		"data": gin.H{
			"records": pageRecords, "total": total,
			"page": current, "size": size, "pages": pages,
		},
	}
}

// collectSessions scans session keys, enriches with token and ClientUser data, and optionally filters by keyword.
func collectSessions(ctx context.Context, redis *redis.Client, sessionPrefix, tokenPrefix, keyword string) ([]*SessionPageResult, error) {
	pattern := sessionPrefix + "*"
	keys, err := scanKeys(ctx, redis, pattern)
	if err != nil {
		return nil, err
	}

	var result []*SessionPageResult
	userCache := make(map[string]*ent.ClientUser)

	for _, sessionKey := range keys {
		parts := strings.Split(sessionKey, ":")
		userID := parts[len(parts)-1]

		tokens, err := redis.SMembers(ctx, sessionKey).Result()
		if err != nil {
			continue
		}

		tokenCount := len(tokens)

		// Find the first valid token (one whose data still exists in Redis)
		var sessionCreateTime string
		var username string
		found := false

		for _, token := range tokens {
			tokenKey := tokenPrefix + token
			data, err := redis.Get(ctx, tokenKey).Result()
			if err != nil {
				continue
			}
			var tokenData map[string]any
			if err := json.Unmarshal([]byte(data), &tokenData); err != nil {
				continue
			}
			extra, _ := tokenData["extra"].(map[string]any)
			if extra != nil {
				username, _ = extra["username"].(string)
			}
			sessionCreateTime, _ = tokenData["created_at"].(string)
			found = true
			break
		}

		if !found {
			continue
		}

		// Apply keyword filter against the username from token extra
		if keyword != "" && !strings.Contains(username, keyword) {
			continue
		}

		// Get session TTL
		ttl, err := redis.TTL(ctx, sessionKey).Result()
		timeoutSeconds := -1
		if err == nil {
			timeoutSeconds = int(ttl.Seconds())
		}

		// Query ClientUser for enrichment (with cache)
		user, ok := userCache[userID]
		if !ok {
			user, err = db.Client.ClientUser.Get(ctx, userID)
			if err != nil {
				user = nil
			}
			userCache[userID] = user
		}

		sr := &SessionPageResult{
			UserID:                userID,
			TokenCount:            tokenCount,
			SessionCreateTime:     sessionCreateTime,
			SessionTimeout:        formatTimeout(timeoutSeconds),
			SessionTimeoutSeconds: timeoutSeconds,
		}

		if username != "" {
			sr.Username = &username
		}

		if user != nil {
			sr.Nickname = user.Nickname
			sr.Avatar = user.Avatar
			sr.Status = user.Status
			sr.LastLoginIP = user.LastLoginIP
			if user.LastLoginAt != nil {
				sr.LastLoginTime = user.LastLoginAt.Format("2006-01-02 15:04:05")
			}
		}

		result = append(result, sr)
	}

	return result, nil
}

// Exit force-logouts all sessions for the given user (CONSUMER).
func Exit(c *gin.Context, userID string) {
	auth.NewHeiClientAuthTool().Kickout(userID)
}

// TokenList returns all active tokens for a given CONSUMER user.
func TokenList(c *gin.Context, userID string) []*SessionTokenResult {
	sessionKey := constants.SESSION_PREFIX_CONSUMER + userID
	tokens, err := db.Redis.SMembers(svcCtx, sessionKey).Result()
	if err != nil || len(tokens) == 0 {
		return []*SessionTokenResult{}
	}

	var results []*SessionTokenResult
	for _, token := range tokens {
		tokenKey := constants.TOKEN_PREFIX_CONSUMER + token
		data, err := db.Redis.Get(svcCtx, tokenKey).Result()
		if err != nil {
			continue
		}
		var tokenData map[string]any
		if err := json.Unmarshal([]byte(data), &tokenData); err != nil {
			continue
		}

		createdAt, _ := tokenData["created_at"].(string)
		extra, _ := tokenData["extra"].(map[string]any)
		deviceType, _ := extra["device_type"].(string)
		deviceID, _ := extra["device_id"].(string)

		ttl, err := db.Redis.TTL(svcCtx, tokenKey).Result()
		timeoutSeconds := -1
		if err == nil {
			timeoutSeconds = int(ttl.Seconds())
		}

		results = append(results, &SessionTokenResult{
			Token:          token,
			CreatedAt:      createdAt,
			Timeout:        formatTimeout(timeoutSeconds),
			TimeoutSeconds: timeoutSeconds,
			DeviceType:     deviceType,
			DeviceID:       deviceID,
		})
	}

	return results
}

// ExitToken force-logouts a specific token for a given CONSUMER user.
func ExitToken(c *gin.Context, userID, token string) {
	auth.NewHeiClientAuthTool().KickoutToken(userID, token)
}

// ChartData returns bar chart data (last 7 days daily new tokens) and pie chart data (CONSUMER only).
func ChartData(c *gin.Context) *SessionChartData {
	cKeys, _ := scanKeys(svcCtx, db.Redis, constants.SESSION_PREFIX_CONSUMER+"*")
	cTotal, _, _ := countTokens(svcCtx, db.Redis, cKeys, constants.TOKEN_PREFIX_CONSUMER)
	cDaily := countDaily(svcCtx, db.Redis, cKeys, constants.TOKEN_PREFIX_CONSUMER)

	days := lastNDays(7)
	series := make([]int, 7)
	for i, day := range days {
		series[i] = cDaily[day]
	}

	return &SessionChartData{
		BarChart: BarChartData{
			Days: days,
			Series: []CategorySeries{
				{Name: "新增在线数", Data: series},
			},
		},
		PieChart: PieChartData{
			Data: []CategoryTotal{
				{Category: "CONSUMER", Total: cTotal},
			},
		},
	}
}

// formatTimeout converts TTL seconds to a human-readable Chinese string.
func formatTimeout(seconds int) string {
	if seconds < 0 {
		return "已过期"
	}
	if seconds == 0 {
		return "永久"
	}
	if seconds < 60 {
		return fmt.Sprintf("剩余 %d秒", seconds)
	}
	if seconds < 3600 {
		return fmt.Sprintf("剩余 %d分钟", seconds/60)
	}
	if seconds < 86400 {
		return fmt.Sprintf("剩余 %d小时%d分钟", seconds/3600, (seconds%3600)/60)
	}
	return fmt.Sprintf("剩余 %d天%d小时", seconds/86400, (seconds%86400)/3600)
}

// lastNDays returns the last n calendar days in "2006-01-02" format (oldest first).
func lastNDays(n int) []string {
	days := make([]string, n)
	now := time.Now()
	for i := 0; i < n; i++ {
		days[i] = now.AddDate(0, 0, -(n - 1 - i)).Format("2006-01-02")
	}
	return days
}

// safeStr safely dereferences a *string, returning "" if nil.
func safeStr(s *string) string {
	if s == nil {
		return ""
	}
	return *s
}
