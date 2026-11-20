package similarity

// MatchTile 表示匹配的区块
type MatchTile struct {
	Start1 int
	Start2 int
	Length int
}

// GreedyStringTiling 贪婪字符串匹配算法
func GreedyStringTiling(token1, token2 []int, minMatchLength int) int {
	if len(token1) == 0 || len(token2) == 0 {
		return 0
	}
	matched1 := make([]bool, len(token1))
	matched2 := make([]bool, len(token2))
	var tiles []MatchTile
	maxMatch := minMatchLength
	for {
		maxMatch = minMatchLength
		var maxTiles []MatchTile
		findMaxMatches(token1, token2, matched1, matched2, minMatchLength, &maxMatch, &maxTiles)
		if maxMatch <= minMatchLength {
			break
		}
		for _, tile := range maxTiles {
			for k := 0; k < tile.Length; k++ {
				matched1[tile.Start1+k] = true
				matched2[tile.Start2+k] = true
			}
			tiles = append(tiles, tile)
		}
	}
	totalMatches := 0
	for _, tile := range tiles {
		totalMatches += tile.Length
	}

	return totalMatches
}

func findMaxMatches(token1, token2 []int, matched1, matched2 []bool,
	minMatchLength int, maxMatch *int, maxTiles *[]MatchTile) {

	n1, n2 := len(token1), len(token2)
	availableStarts1 := getAvailableStarts(matched1, n1)
	availableStarts2 := getAvailableStarts(matched2, n2)

	for _, i := range availableStarts1 {
		if matched1[i] {
			continue
		}
		if n1-i < *maxMatch {
			continue
		}

		for _, j := range availableStarts2 {
			if matched2[j] {
				continue
			}
			if n2-j < *maxMatch {
				continue
			}
			if !quickCheck(token1, token2, i, j, *maxMatch) {
				continue
			}

			k := computeMatchLength(token1, token2, matched1, matched2, i, j, n1, n2)

			if k > *maxMatch {
				*maxMatch = k
				*maxTiles = (*maxTiles)[:0]
				*maxTiles = append(*maxTiles, MatchTile{i, j, k})
			} else if k == *maxMatch && k > minMatchLength {
				*maxTiles = append(*maxTiles, MatchTile{i, j, k})
			}
		}
	}
}

func getAvailableStarts(matched []bool, length int) []int {
	var starts []int
	for i := 0; i < length; i++ {
		if !matched[i] {
			starts = append(starts, i)
		}
	}
	return starts
}

func quickCheck(token1, token2 []int, i, j, n int) bool {
	if i+n > len(token1) || j+n > len(token2) {
		return false
	}
	for k := 0; k < n; k++ {
		if token1[i+k] != token2[j+k] {
			return false
		}
	}
	return true
}

func computeMatchLength(token1, token2 []int, matched1, matched2 []bool,
	i, j, n1, n2 int) int {

	k := 0
	for i+k < n1 && j+k < n2 {
		if matched1[i+k] || matched2[j+k] {
			break
		}
		if token1[i+k] != token2[j+k] {
			break
		}
		k++
	}
	return k
}
