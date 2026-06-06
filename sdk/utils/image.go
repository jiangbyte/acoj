package utils

import (
	"bytes"
	"encoding/base64"
	"image"
	"image/jpeg"
	"image/png"
	"strings"

	"golang.org/x/image/draw"
)

// CompressBase64Image compresses a base64 data URL image, resizing to fit within
// maxWidth x maxHeight and re-encoding at the given JPEG quality.
// Returns the original string if it is not a data URL or cannot be decoded.
func CompressBase64Image(dataURL string, maxWidth, maxHeight, quality int) string {
	if !strings.HasPrefix(dataURL, "data:image/") {
		return dataURL
	}

	commaIdx := strings.Index(dataURL, ",")
	if commaIdx < 0 {
		return dataURL
	}

	b64 := dataURL[commaIdx+1:]
	raw, err := base64.StdEncoding.DecodeString(b64)
	if err != nil {
		return dataURL
	}

	img, format, err := image.Decode(bytes.NewReader(raw))
	if err != nil {
		return dataURL
	}

	bounds := img.Bounds()
	w := bounds.Dx()
	h := bounds.Dy()

	if w > maxWidth || h > maxHeight {
		ratio := float64(maxWidth) / float64(w)
		if float64(maxHeight)/float64(h) < ratio {
			ratio = float64(maxHeight) / float64(h)
		}
		nw := int(float64(w) * ratio)
		nh := int(float64(h) * ratio)
		dst := image.NewRGBA(image.Rect(0, 0, nw, nh))
		draw.BiLinear.Scale(dst, dst.Bounds(), img, bounds, draw.Over, nil)
		img = dst
	}

	var buf bytes.Buffer
	if format == "png" {
		_ = png.Encode(&buf, img)
	} else {
		_ = jpeg.Encode(&buf, img, &jpeg.Options{Quality: quality})
	}

	return dataURL[:commaIdx+1] + base64.StdEncoding.EncodeToString(buf.Bytes())
}
