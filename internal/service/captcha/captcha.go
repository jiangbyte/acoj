package captcha

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/mojocn/base64Captcha"

	"hei-goframe/internal/consts"
)

// redisStore implements base64Captcha.Store backed by Redis.
type redisStore struct {
	ctx       context.Context
	keyPrefix string
	expire    int
}

func (s *redisStore) Set(id string, value string) error {
	key := s.keyPrefix + id
	g.Redis().SetEX(s.ctx, key, value, int64(s.expire))
	return nil
}

func (s *redisStore) Get(id string, clear bool) string {
	key := s.keyPrefix + id
	v, err := g.Redis().Get(s.ctx, key)
	if err != nil || v.IsNil() {
		return ""
	}
	if clear {
		g.Redis().Del(s.ctx, key)
	}
	return v.String()
}

func (s *redisStore) Verify(id, answer string, clear bool) bool {
	return s.Get(id, clear) == answer
}

var BusinessCaptcha *base64Captcha.Captcha
var ConsumerCaptcha *base64Captcha.Captcha

func init() {
	BusinessCaptcha = newCaptcha(consts.CaptchaBusinessCacheKey)
	ConsumerCaptcha = newCaptcha(consts.CaptchaConsumerCacheKey)
}

func newCaptcha(prefix string) *base64Captcha.Captcha {
	driver := base64Captcha.NewDriverDigit(80, 240, 4, 0.7, 80)
	store := &redisStore{
		ctx:       context.Background(),
		keyPrefix: prefix,
		expire:    300,
	}
	return base64Captcha.NewCaptcha(driver, store)
}

// GenerateCaptcha creates a captcha and returns base64 image and captcha ID.
func GenerateCaptcha(c *base64Captcha.Captcha) (id, b64 string, err error) {
	id, b64, _, err = c.Generate()
	return
}

// VerifyCaptcha checks if the captcha answer matches.
func VerifyCaptcha(c *base64Captcha.Captcha, id, answer string) bool {
	return c.Verify(id, answer, true)
}

// GetDebugCode retrieves the captcha code for debug purposes.
func GetDebugCode(c *base64Captcha.Captcha, id string) string {
	if g.Cfg().MustGet(context.Background(), "server.debug", false).Bool() {
		return c.Store.Get(id, false)
	}
	return ""
}
