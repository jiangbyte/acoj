package clientauth

import (
	"bytes"
	"context"
	crand "crypto/rand"
	"encoding/base64"
	"fmt"
	"image"
	"image/color"
	"image/draw"
	"image/png"
	"math/big"
	"math/rand"
	"os"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"golang.org/x/image/font"
	"golang.org/x/image/font/basicfont"
	"golang.org/x/image/font/opentype"
	"golang.org/x/image/math/fixed"

	"hei-gin/config"
	"hei-gin/core/constants"
	"hei-gin/core/db"
	"hei-gin/core/result"
)

const captchaChars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

var captchaFont font.Face

func init() {
	f, err := loadFont()
	if err == nil {
		captchaFont = f
	} else {
		captchaFont = basicfont.Face7x13
	}
}

func loadFont() (font.Face, error) {
	paths := []string{
		"C:/Windows/Fonts/arial.ttf",
		"C:/Windows/Fonts/Arial.ttf",
		"/System/Library/Fonts/Arial.ttf",
		"/System/Library/Fonts/Supplemental/Arial.ttf",
	}
	for _, p := range paths {
		data, err := os.ReadFile(p)
		if err != nil {
			continue
		}
		f, err := opentype.Parse(data)
		if err != nil {
			continue
		}
		return opentype.NewFace(f, &opentype.FaceOptions{
			Size:    24,
			DPI:     72,
			Hinting: font.HintingNone,
		})
	}
	return nil, fmt.Errorf("no TrueType font found on system")
}

func generateCode(length int) string {
	code := make([]byte, length)
	for i := 0; i < length; i++ {
		n, _ := crand.Int(crand.Reader, big.NewInt(int64(len(captchaChars))))
		code[i] = captchaChars[n.Int64()]
	}
	return string(code)
}

func drawCaptchaImage(code string) (string, error) {
	img := image.NewRGBA(image.Rect(0, 0, 100, 38))
	draw.Draw(img, img.Bounds(), image.White, image.Point{}, draw.Src)

	for i, ch := range code {
		x := 10 + i*22
		y := 20 + rand.Intn(11) // baseline offset so text appears centered
		r := uint8(rand.Intn(100))
		g := uint8(rand.Intn(100))
		b := uint8(rand.Intn(100))

		d := &font.Drawer{
			Dst:  img,
			Src:  image.NewUniform(color.RGBA{r, g, b, 255}),
			Face: captchaFont,
			Dot:  fixed.P(x, y),
		}
		d.DrawString(string(ch))
	}

	// 10 random noise lines
	for i := 0; i < 10; i++ {
		x1 := rand.Intn(100)
		y1 := rand.Intn(38)
		x2 := rand.Intn(100)
		y2 := rand.Intn(38)
		r := uint8(rand.Intn(200))
		g := uint8(rand.Intn(200))
		b := uint8(rand.Intn(200))
		drawLine(img, x1, y1, x2, y2, color.RGBA{r, g, b, 255})
	}

	var buf bytes.Buffer
	if err := png.Encode(&buf, img); err != nil {
		return "", err
	}
	return "data:image/png;base64," + base64.StdEncoding.EncodeToString(buf.Bytes()), nil
}

func drawLine(img *image.RGBA, x1, y1, x2, y2 int, col color.Color) {
	dx, dy := x2-x1, y2-y1
	steps := abs(dx)
	if abs(dy) > steps {
		steps = abs(dy)
	}
	if steps == 0 {
		img.Set(x1, y1, col)
		return
	}
	xIncr := float64(dx) / float64(steps)
	yIncr := float64(dy) / float64(steps)
	x, y := float64(x1), float64(y1)
	for i := 0; i <= steps; i++ {
		img.Set(int(x+0.5), int(y+0.5), col)
		x += xIncr
		y += yIncr
	}
}

func abs(x int) int {
	if x < 0 {
		return -x
	}
	return x
}

func Captcha(c *gin.Context) {
	code := generateCode(4)
	captchaID := uuid.New().String()

	base64Img, err := drawCaptchaImage(code)
	if err != nil {
		result.Failure(c, "生成验证码失败", 500)
		return
	}

	key := constants.CaptchaConsumerPrefix + captchaID
	ctx := context.Background()
	if db.Redis != nil {
		db.Redis.Set(ctx, key, code, 5*time.Minute)
	}

	result.Success(c, CaptchaResp{
		CaptchaID:     captchaID,
		CaptchaBase64: base64Img,
	})
}

func SM2PublicKey(c *gin.Context) {
	result.Success(c, config.C.SM2.PublicKey)
}
