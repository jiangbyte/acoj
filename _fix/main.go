package main
import (
	"fmt"
	"os"
	"regexp"
	"strings"
)
func main() {
	base := "E:\\projects\\mine\\hei\\hei-gin"
	files := []string{
		base + "\\modules\\sys\\dict\\api\\v1\\api.go",
		base + "\\modules\\sys\\resource\\api\\v1\\api.go",
	}
	re := regexp.MustCompile("Failure\\([^,]+,\\s*\"([^\"]+)")
	for _, fpath := range files {
		content, err := os.ReadFile(fpath)
		if err != nil { continue }
		s := string(content)
		matches := re.FindAllStringSubmatch(s, -1)
		for _, m := range matches {
			if len(m) >= 2 {
				msg := m[1]
				hasHigh := false
				for _, r := range msg { if r > 0x7F { hasHigh = true; break } }
				if hasHigh {
					b := []byte(msg)
					hexes := make([]string, len(b))
					for i, v := range b { hexes[i] = fmt.Sprintf("%02x", v) }
					fmt.Println(fpath + ": " + strings.Join(hexes, " "))
				}
			}
		}
	}
}
