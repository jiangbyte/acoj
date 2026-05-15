package session

import (
	"context"
	"encoding/json"
	"strings"
	"time"

	"github.com/gogf/gf/v2/frame/g"

	"hei-goframe/internal/consts"
	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

// Analysis returns session analysis data.
func Analysis(ctx context.Context) (g.Map, error) {
	prefix := consts.SessionPrefixBusiness
	keysVar, err := g.Redis().Do(ctx, "KEYS", prefix+"*")
	if err != nil {
		return nil, err
	}
	keys := keysVar.Strings()

	totalCount := len(keys)
	maxTokenCount := 0
	oneHourNewlyAdded := 0
	now := time.Now()

	for _, key := range keys {
		tokensVar, err := g.Redis().SMembers(ctx, key)
		if err != nil {
			continue
		}
		var tokenStrs []string
		for _, t := range tokensVar {
			tokenStrs = append(tokenStrs, t.String())
		}
		if len(tokenStrs) > maxTokenCount {
			maxTokenCount = len(tokenStrs)
		}

		for _, tokenStr := range tokenStrs {
			tokenKey := consts.TokenPrefixBusiness + tokenStr
			dataVar, err := g.Redis().Get(ctx, tokenKey)
			if err != nil || dataVar.IsNil() {
				continue
			}
			var td map[string]interface{}
			if json.Unmarshal(dataVar.Bytes(), &td) != nil {
				continue
			}
			if createdAt, ok := td["created_at"].(string); ok {
				createdTime, err := time.Parse(time.RFC3339, createdAt)
				if err == nil && now.Sub(createdTime) <= time.Hour {
					oneHourNewlyAdded++
				}
			}
		}
	}

	return g.Map{
		"total_count":           totalCount,
		"max_token_count":       maxTokenCount,
		"one_hour_newly_added":  oneHourNewlyAdded,
		"proportion_of_b_and_c": "B: 100%, C: 0%",
	}, nil
}

// Page queries sessions with pagination.
func Page(ctx context.Context, keyword string, current, size int) (*utility.PageRes, error) {
	prefix := consts.SessionPrefixBusiness
	keysVar, err := g.Redis().Do(ctx, "KEYS", prefix+"*")
	if err != nil {
		return nil, err
	}
	allKeys := keysVar.Strings()

	// Filter by keyword if provided (match user_id contains keyword)
	var filteredKeys []string
	for _, key := range allKeys {
		loginId := strings.TrimPrefix(key, prefix)
		if keyword == "" || strings.Contains(loginId, keyword) {
			filteredKeys = append(filteredKeys, key)
		}
	}

	totalCount := len(filteredKeys)

	// Paginate
	start := (current - 1) * size
	if start >= totalCount {
		start = totalCount
	}
	end := start + size
	if end > totalCount {
		end = totalCount
	}
	pageKeys := filteredKeys[start:end]

	var records []g.Map
	for _, key := range pageKeys {
		loginId := strings.TrimPrefix(key, prefix)
		tokensVar, err := g.Redis().SMembers(ctx, key)
		if err != nil {
			continue
		}
		var tokenCount int
		var firstTokenCreated string
		for _, t := range tokensVar {
			tokenStr := t.String()
			tokenCount++
			if firstTokenCreated == "" {
				tokenKey := consts.TokenPrefixBusiness + tokenStr
				dataVar, err := g.Redis().Get(ctx, tokenKey)
				if err == nil && !dataVar.IsNil() {
					var td map[string]interface{}
					if json.Unmarshal(dataVar.Bytes(), &td) == nil {
						if ca, ok := td["created_at"].(string); ok {
							firstTokenCreated = ca
						}
					}
				}
			}
		}
		records = append(records, g.Map{
			"login_id":    loginId,
			"token_count": tokenCount,
			"created_at":  firstTokenCreated,
		})
	}
	if records == nil {
		records = make([]g.Map, 0)
	}

	return utility.NewPageRes(records, totalCount, current, size), nil
}

// Exit kicks out a user by login ID.
func Exit(ctx context.Context, userId string) error {
	return auth.BusinessAuth.Kickout(ctx, userId)
}

// Tokens returns all active tokens for a user.
func Tokens(ctx context.Context, userId string) ([]string, error) {
	return auth.BusinessAuth.GetTokenValuesByLoginId(ctx, userId)
}

// ExitToken kicks out a specific token for a user.
func ExitToken(ctx context.Context, userId, token string) error {
	return auth.BusinessAuth.KickoutToken(ctx, userId, token)
}

// ChartData returns session chart data.
func ChartData(ctx context.Context) (g.Map, error) {
	sevenDaysAgo := time.Now().AddDate(0, 0, -7).Format("2006-01-02 15:04:05")

	// Query sys_log for session-related activity to build chart data
	rows, err := g.DB().Model(dao.SysLog.Table).Ctx(ctx).
		Fields("DATE(op_time) as day, count(*) as count").
		WhereGTE("op_time", sevenDaysAgo).
		Group("day").
		OrderAsc("day").
		All()
	if err != nil {
		return nil, err
	}

	var days []string
	var data []int
	for _, r := range rows {
		days = append(days, r["day"].String())
		data = append(data, r["count"].Int())
	}
	if days == nil {
		days = make([]string, 0)
	}
	if data == nil {
		data = make([]int, 0)
	}

	// Pie chart: group session-related categories
	pieRows, err := g.DB().Model(dao.SysLog.Table).Ctx(ctx).
		Fields("category, count(*) as total").
		Group("category").
		All()
	if err != nil {
		return nil, err
	}

	var list []g.Map
	for _, r := range pieRows {
		list = append(list, g.Map{
			"category": r["category"].String(),
			"total":    r["total"].Int(),
		})
	}
	if list == nil {
		list = make([]g.Map, 0)
	}

	return g.Map{
		"days": days,
		"series": []g.Map{
			{
				"name": "session_count",
				"data": data,
			},
		},
		"list": list,
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
