package session

import (
	"context"
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	"hei-goframe/internal/consts"
	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

// scanKeys uses Redis SCAN (cursor-based, non-blocking) to find keys matching pattern.
func scanKeys(ctx context.Context, pattern string) ([]string, error) {
	var keys []string
	cursor := 0
	for {
		result, err := g.Redis().Do(ctx, "SCAN", cursor, "MATCH", pattern, "COUNT", 200)
		if err != nil {
			return nil, err
		}
		arr := result.Array()
		if len(arr) != 2 {
			break
		}
		cursor = gconv.Int(arr[0])
		batch := gconv.Strings(arr[1])
		keys = append(keys, batch...)
		if cursor == 0 {
			break
		}
	}
	return keys, nil
}

// formatTimeout formats remaining seconds to human-readable string.
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

// countTokens counts total tokens, new tokens (last hour), and max tokens per user.
func countTokens(ctx context.Context, keys []string, tokenPrefix string) (total, newTotal, maxPerUser int) {
	oneHourAgo := time.Now().UTC().Add(-time.Hour)
	for _, key := range keys {
		tokensVar, err := g.Redis().SMembers(ctx, key)
		if err != nil {
			continue
		}
		userCount := 0
		for _, t := range tokensVar {
			tokenStr := t.String()
			tokenKey := tokenPrefix + tokenStr
			dataVar, err := g.Redis().Get(ctx, tokenKey)
			if err != nil || dataVar.IsNil() {
				continue
			}
			total++
			userCount++

			var td map[string]interface{}
			if json.Unmarshal(dataVar.Bytes(), &td) != nil {
				continue
			}
			if createdAt, ok := td["created_at"].(string); ok && createdAt != "" {
				createdTime, err := time.Parse(time.RFC3339, createdAt)
				if err == nil && createdTime.After(oneHourAgo) {
					newTotal++
				}
			}
		}
		if userCount > maxPerUser {
			maxPerUser = userCount
		}
	}
	return
}

// countDaily counts new tokens per day for the last 7 days.
func countDaily(ctx context.Context, keys []string, tokenPrefix string, today time.Time, daily []int) {
	todayStart := time.Date(today.Year(), today.Month(), today.Day(), 0, 0, 0, 0, today.Location())
	for _, key := range keys {
		tokensVar, err := g.Redis().SMembers(ctx, key)
		if err != nil {
			continue
		}
		for _, t := range tokensVar {
			tokenStr := t.String()
			tokenKey := tokenPrefix + tokenStr
			dataVar, err := g.Redis().Get(ctx, tokenKey)
			if err != nil || dataVar.IsNil() {
				continue
			}
			var td map[string]interface{}
			if json.Unmarshal(dataVar.Bytes(), &td) != nil {
				continue
			}
			if createdAt, ok := td["created_at"].(string); ok && createdAt != "" {
				createdTime, err := time.Parse(time.RFC3339, createdAt)
				if err != nil {
					continue
				}
				dayStart := time.Date(createdTime.Year(), createdTime.Month(), createdTime.Day(), 0, 0, 0, 0, time.UTC)
				delta := int(todayStart.Sub(dayStart).Hours() / 24)
				if delta >= 0 && delta < 7 {
					daily[6-delta]++
				}
			}
		}
	}
}

// collectSessions collects online sessions from Redis enriched with DB user info.
// Used by Page for paginated lists.
func collectSessions(ctx context.Context, sessionPrefix, tokenPrefix string, keyword string) ([]g.Map, error) {
	keys, err := scanKeys(ctx, sessionPrefix+"*")
	if err != nil {
		return nil, err
	}

	var records []g.Map
	userCache := make(map[string]g.Map)

	for _, key := range keys {
		userID := strings.TrimPrefix(key, sessionPrefix)
		tokensVar, err := g.Redis().SMembers(ctx, key)
		if err != nil || len(tokensVar) == 0 {
			continue
		}

		// Find first valid token and count all valid tokens
		var firstToken string
		tokenCount := 0
		for _, t := range tokensVar {
			tStr := t.String()
			tokenKey := tokenPrefix + tStr
			exists, _ := g.Redis().Exists(ctx, tokenKey)
			if exists > 0 {
				if firstToken == "" {
					firstToken = tStr
				}
				tokenCount++
			}
		}
		if firstToken == "" {
			continue
		}

		// Get first valid token data for metadata
		firstTokenKey := tokenPrefix + firstToken
		dataVar, err := g.Redis().Get(ctx, firstTokenKey)
		if err != nil || dataVar.IsNil() {
			continue
		}
		var td map[string]interface{}
		if json.Unmarshal(dataVar.Bytes(), &td) != nil {
			continue
		}

		// Filter by keyword on account
		extra, _ := td["extra"].(map[string]interface{})
		account, _ := extra["account"].(string)
		if keyword != "" && !strings.Contains(strings.ToLower(account), strings.ToLower(keyword)) {
			continue
		}

		ttl, _ := g.Redis().TTL(ctx, firstTokenKey)
		createdAt, _ := td["created_at"].(string)

		// Lookup user info from DB (cached per user_id)
		nickname, _ := extra["nickname"].(string)
		avatar := ""
		status := ""
		lastLoginIP := ""
		lastLoginTime := ""

		if _, ok := userCache[userID]; !ok {
			userRecord, err := dao.SysUser.Ctx().Ctx(ctx).Where("id", userID).One()
			if err == nil && userRecord != nil {
				userCache[userID] = userRecord.Map()
			} else {
				userCache[userID] = nil
			}
		}
		if userRec := userCache[userID]; userRec != nil {
			if nickname == "" {
				nickname = gconv.String(userRec["nickname"])
			}
			avatar = gconv.String(userRec["avatar"])
			status = gconv.String(userRec["status"])
			lastLoginIP = gconv.String(userRec["last_login_ip"])
			if lastLoginAt := userRec["last_login_at"]; lastLoginAt != nil {
				lastLoginTime = gconv.String(lastLoginAt)
			}
		}

		records = append(records, g.Map{
			"user_id":                 userID,
			"account":                 account,
			"nickname":                nickname,
			"avatar":                  avatar,
			"status":                  status,
			"last_login_ip":           lastLoginIP,
			"last_login_address":      "",
			"last_login_time":         lastLoginTime,
			"session_create_time":     createdAt,
			"session_timeout":         formatTimeout(int(ttl)),
			"session_timeout_seconds": max(0, int(ttl)),
			"token_count":             tokenCount,
		})
	}

	// Sort by session_create_time descending
	for i := 0; i < len(records); i++ {
		for j := i + 1; j < len(records); j++ {
			tI := gconv.String(records[i]["session_create_time"])
			tJ := gconv.String(records[j]["session_create_time"])
			if tI < tJ {
				records[i], records[j] = records[j], records[i]
			}
		}
	}

	return records, nil
}

// Analysis returns session analysis data counting BOTH Business and Consumer sessions.
func Analysis(ctx context.Context) (g.Map, error) {
	bKeys, err := scanKeys(ctx, consts.SessionPrefixBusiness+"*")
	if err != nil {
		return nil, err
	}
	cKeys, err := scanKeys(ctx, consts.SessionPrefixConsumer+"*")
	if err != nil {
		return nil, err
	}

	bTotal, bNew, bMax := countTokens(ctx, bKeys, consts.TokenPrefixBusiness)
	cTotal, cNew, cMax := countTokens(ctx, cKeys, consts.TokenPrefixConsumer)

	return g.Map{
		"total_count":           bTotal + cTotal,
		"max_token_count":       max(bMax, cMax),
		"one_hour_newly_added":  bNew + cNew,
		"proportion_of_b_and_c": fmt.Sprintf("%d/%d", bTotal, cTotal),
	}, nil
}

// Page queries C-end sessions with pagination and user info enrichment.
func Page(ctx context.Context, keyword string, current, size int) (*utility.PageRes, error) {
	records, err := collectSessions(ctx, consts.SessionPrefixConsumer, consts.TokenPrefixConsumer, keyword)
	if err != nil {
		return nil, err
	}

	total := len(records)
	start := (current - 1) * size
	if start >= total {
		start = total
	}
	end := start + size
	if end > total {
		end = total
	}
	pageRecords := records[start:end]
	if pageRecords == nil {
		pageRecords = make([]g.Map, 0)
	}

	return utility.NewPageRes(pageRecords, total, current, size), nil
}

// Exit kicks out a user by login ID.
func Exit(ctx context.Context, userId string) error {
	return auth.ConsumerAuth.Kickout(ctx, userId)
}

// Tokens returns all active tokens with metadata for a user.
func Tokens(ctx context.Context, userId string) ([]g.Map, error) {
	sessionKey := consts.SessionPrefixConsumer + userId
	tokensVar, err := g.Redis().SMembers(ctx, sessionKey)
	if err != nil {
		return nil, err
	}

	var results []g.Map
	for _, t := range tokensVar {
		tokenStr := t.String()
		tokenKey := consts.TokenPrefixConsumer + tokenStr
		dataVar, err := g.Redis().Get(ctx, tokenKey)
		if err != nil || dataVar.IsNil() {
			continue
		}

		var td map[string]interface{}
		if err := json.Unmarshal(dataVar.Bytes(), &td); err != nil {
			continue
		}

		ttl, _ := g.Redis().TTL(ctx, tokenKey)
		extra, _ := td["extra"].(map[string]interface{})

		createdAt, _ := td["created_at"].(string)
		deviceType := ""
		deviceID := ""
		if extra != nil {
			deviceType, _ = extra["device_type"].(string)
			deviceID, _ = extra["device_id"].(string)
		}

		results = append(results, g.Map{
			"token":           tokenStr,
			"created_at":      createdAt,
			"timeout":         formatTimeout(int(ttl)),
			"timeout_seconds": max(0, int(ttl)),
			"device_type":     deviceType,
			"device_id":       deviceID,
		})
	}
	if results == nil {
		results = make([]g.Map, 0)
	}
	return results, nil
}

// ExitToken kicks out a specific token for a user.
func ExitToken(ctx context.Context, userId, token string) error {
	return auth.ConsumerAuth.KickoutToken(ctx, userId, token)
}

// ChartData returns session chart data from Redis (both B and C ends).
func ChartData(ctx context.Context) (g.Map, error) {
	bKeys, err := scanKeys(ctx, consts.SessionPrefixBusiness+"*")
	if err != nil {
		return nil, err
	}
	cKeys, err := scanKeys(ctx, consts.SessionPrefixConsumer+"*")
	if err != nil {
		return nil, err
	}

	// Pie chart: B vs C total
	bTotal, _, _ := countTokens(ctx, bKeys, consts.TokenPrefixBusiness)
	cTotal, _, _ := countTokens(ctx, cKeys, consts.TokenPrefixConsumer)

	list := []g.Map{
		{"category": "B端", "total": bTotal},
		{"category": "C端", "total": cTotal},
	}

	// Bar chart: last 7 days daily new sessions
	now := time.Now().UTC()
	today := time.Date(now.Year(), now.Month(), now.Day(), 0, 0, 0, 0, time.UTC)
	days := make([]string, 7)
	for i := 6; i >= 0; i-- {
		d := today.AddDate(0, 0, -i)
		days[6-i] = d.Format("2006-01-02")
	}

	bDaily := make([]int, 7)
	cDaily := make([]int, 7)
	countDaily(ctx, bKeys, consts.TokenPrefixBusiness, today, bDaily)
	countDaily(ctx, cKeys, consts.TokenPrefixConsumer, today, cDaily)

	return g.Map{
		"bar_chart": g.Map{
			"days": days,
			"series": []g.Map{
				{"name": "B端", "data": bDaily},
				{"name": "C端", "data": cDaily},
			},
		},
		"pie_chart": g.Map{
			"data": list,
		},
	}, nil
}

func getLoginId(ctx context.Context) string {
	if v := ctx.Value(auth.ContextKeyLoginId); v != nil {
		return v.(string)
	}
	return ""
}

func ifEmpty(s, def string) string {
	if s == "" {
		return def
	}
	return s
}
