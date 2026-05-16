package captcha

import (
	"context"
	"image"
	"image/color"
	"image/draw"
	"image/png"
	"math/rand"
	"strings"

	"bytes"
	"encoding/base64"
	"fmt"
	"time"

	"github.com/redis/go-redis/v9"
	"golang.org/x/image/font"
	"golang.org/x/image/font/basicfont"
	"golang.org/x/image/math/fixed"

	"hei-gin/core/enums"
)

const expireSeconds = 300

// CaptchaResult represents the CAPTCHA response sent to the client.
type CaptchaResult struct {
	CaptchaBase64 string `json:"captcha_base64"`
	CaptchaID     string `json:"captcha_id"`
	CaptchaCode   string `json:"captcha_code,omitempty"`
}

// Service handles CAPTCHA generation and verification.
type Service struct {
	prefix string
	rdb    *redis.Client
}

// NewCaptchaService creates a CAPTCHA service for the given login type prefix.
func NewCaptchaService(loginType string) *Service {
	prefix := strings.ToUpper(loginType) + ":captcha:"
	return &Service{prefix: prefix}
}

// NewBusinessCaptcha creates a CAPTCHA service for BUSINESS login type.
func NewBusinessCaptcha() *Service {
	return NewCaptchaService(string(enums.LoginTypeBusiness))
}

// NewConsumerCaptcha creates a CAPTCHA service for CONSUMER login type.
func NewConsumerCaptcha() *Service {
	return NewCaptchaService(string(enums.LoginTypeConsumer))
}

// Init sets the Redis client.
func (s *Service) Init(rdb *redis.Client) {
	s.rdb = rdb
}

// Generate creates a CAPTCHA image and stores the code in Redis.
func (s *Service) Generate(debug bool) (*CaptchaResult, error) {
	captchaID := fmt.Sprintf("%d", time.Now().UnixNano())
	code := generateCode()

	img := createCaptchaImage(code)

	var buf bytes.Buffer
	if err := png.Encode(&buf, img); err != nil {
		return nil, err
	}
	b64 := base64.StdEncoding.EncodeToString(buf.Bytes())

	if s.rdb != nil {
		ctx := context.Background()
		if err := s.rdb.Set(ctx, s.prefix+captchaID, code, expireSeconds*time.Second).Err(); err != nil {
			return nil, err
		}
	}

	result := &CaptchaResult{
		CaptchaBase64: "data:image/png;base64," + b64,
		CaptchaID:     captchaID,
	}
	if debug {
		result.CaptchaCode = code
	}
	return result, nil
}

// Verify checks a CAPTCHA code against the stored value, then deletes it (one-time use).
func (s *Service) Verify(captchaID, captchaCode string) error {
	if captchaID == "" || captchaCode == "" {
		return fmt.Errorf("验证码ID或验证码内容不能为空")
	}
	if s.rdb == nil {
		return nil
	}

	ctx := context.Background()
	key := s.prefix + captchaID
	stored, err := s.rdb.Get(ctx, key).Result()
	if err == redis.Nil {
		return fmt.Errorf("验证码已过期或无效")
	}
	if err != nil {
		return err
	}

	if !strings.EqualFold(stored, captchaCode) {
		return fmt.Errorf("验证码错误")
	}

	s.rdb.Del(ctx, key)
	return nil
}

func generateCode() string {
	const charset = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
	rng := rand.New(rand.NewSource(time.Now().UnixNano()))
	code := make([]byte, 4)
	for i := range code {
		code[i] = charset[rng.Intn(len(charset))]
	}
	return string(code)
}

func createCaptchaImage(code string) image.Image {
	img := image.NewRGBA(image.Rect(0, 0, 100, 38))
	draw.Draw(img, img.Bounds(), &image.Uniform{color.White}, image.Point{}, draw.Src)

	rng := rand.New(rand.NewSource(time.Now().UnixNano()))

	face := basicfont.Face7x13

	for i, ch := range code {
		x := 10 + i*22
		y := 15 + rng.Intn(10)
		c := color.RGBA{
			R: uint8(rng.Intn(100)),
			G: uint8(rng.Intn(100)),
			B: uint8(rng.Intn(100)),
			A: 255,
		}
		drawChar(img, x, y, ch, c, face)
	}

	for range 10 {
		x1 := rng.Intn(100)
		y1 := rng.Intn(38)
		x2 := rng.Intn(100)
		y2 := rng.Intn(38)
		drawLine(img, x1, y1, x2, y2, color.RGBA{
			R: uint8(rng.Intn(200)),
			G: uint8(rng.Intn(200)),
			B: uint8(rng.Intn(200)),
			A: 255,
		})
	}

	return img
}

func drawChar(img *image.RGBA, x, y int, ch rune, c color.Color, face font.Face) {
	d := &font.Drawer{
		Dst:  img,
		Src:  image.NewUniform(c),
		Face: face,
		Dot: fixed.Point26_6{
			X: fixed.Int26_6(x * 64),
			Y: fixed.Int26_6(y * 64),
		},
	}
	d.DrawString(string(ch))
}

func drawLine(img *image.RGBA, x1, y1, x2, y2 int, c color.Color) {
	dx := x2 - x1
	dy := y2 - y1
	steps := max(abs(dx), abs(dy))
	if steps == 0 {
		return
	}
	for i := 0; i <= steps; i++ {
		x := x1 + dx*i/steps
		y := y1 + dy*i/steps
		if x >= 0 && x < 100 && y >= 0 && y < 38 {
			img.Set(x, y, c)
		}
	}
}

func abs(x int) int {
	if x < 0 {
		return -x
	}
	return x
}
