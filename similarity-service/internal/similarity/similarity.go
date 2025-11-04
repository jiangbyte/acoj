package similarity

// MatchTile 表示匹配的区块
type MatchTile struct {
	Start1 int
	Start2 int
	Length int
}

// GreedyStringTiling 优化后的贪婪字符串匹配算法
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

		// 寻找最大匹配块
		findMaxMatches(token1, token2, matched1, matched2, minMatchLength, &maxMatch, &maxTiles)

		if maxMatch <= minMatchLength {
			break
		}

		// 标记已匹配的块
		for _, tile := range maxTiles {
			for k := 0; k < tile.Length; k++ {
				matched1[tile.Start1+k] = true
				matched2[tile.Start2+k] = true
			}
			tiles = append(tiles, tile)
		}
	}

	// 计算总匹配数
	totalMatches := 0
	for _, tile := range tiles {
		totalMatches += tile.Length
	}

	return totalMatches
}

// findMaxMatches 优化后的最大匹配查找
func findMaxMatches(token1, token2 []int, matched1, matched2 []bool,
	minMatchLength int, maxMatch *int, maxTiles *[]MatchTile) {

	n1, n2 := len(token1), len(token2)

	// 预计算可匹配的起始位置
	availableStarts1 := getAvailableStarts(matched1, n1)
	availableStarts2 := getAvailableStarts(matched2, n2)

	// 使用更智能的搜索策略
	for _, i := range availableStarts1 {
		if matched1[i] {
			continue
		}

		// 提前终止检查
		if n1-i < *maxMatch {
			continue
		}

		for _, j := range availableStarts2 {
			if matched2[j] {
				continue
			}

			// 提前终止检查
			if n2-j < *maxMatch {
				continue
			}

			// 快速检查前maxMatch个字符是否可能匹配
			if !quickCheck(token1, token2, i, j, *maxMatch) {
				continue
			}

			// 计算实际匹配长度
			k := computeMatchLength(token1, token2, matched1, matched2, i, j, n1, n2)

			if k > *maxMatch {
				*maxMatch = k
				*maxTiles = (*maxTiles)[:0] // 清空切片
				*maxTiles = append(*maxTiles, MatchTile{i, j, k})
			} else if k == *maxMatch && k > minMatchLength {
				*maxTiles = append(*maxTiles, MatchTile{i, j, k})
			}
		}
	}
}

// getAvailableStarts 获取可用的起始位置
func getAvailableStarts(matched []bool, length int) []int {
	var starts []int
	for i := 0; i < length; i++ {
		if !matched[i] {
			starts = append(starts, i)
		}
	}
	return starts
}

// quickCheck 快速检查前n个字符是否可能匹配
func quickCheck(token1, token2 []int, i, j, n int) bool {
	if i+n > len(token1) || j+n > len(token2) {
		return false
	}

	// 检查前n个字符
	for k := 0; k < n; k++ {
		if token1[i+k] != token2[j+k] {
			return false
		}
	}
	return true
}

// computeMatchLength 计算匹配长度
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

// GreedyStringTilingOptimized 进一步优化的版本，使用更少的内存分配
func GreedyStringTilingOptimized(token1, token2 []int, minMatchLength int) int {
	if len(token1) == 0 || len(token2) == 0 {
		return 0
	}

	n1, n2 := len(token1), len(token2)
	matched1 := make([]bool, n1)
	matched2 := make([]bool, n2)

	totalMatches := 0
	maxMatch := minMatchLength

	// 预分配切片以避免重复分配
	maxTiles := make([]MatchTile, 0, min(n1, n2)/minMatchLength+1)

	for {
		maxMatch = minMatchLength
		maxTiles = maxTiles[:0] // 重用切片

		// 优化的匹配查找
		findMaxMatchesOptimized(token1, token2, matched1, matched2,
			minMatchLength, &maxMatch, &maxTiles)

		if maxMatch <= minMatchLength {
			break
		}

		// 标记匹配并计数
		for _, tile := range maxTiles {
			for k := 0; k < tile.Length; k++ {
				matched1[tile.Start1+k] = true
				matched2[tile.Start2+k] = true
			}
			totalMatches += tile.Length
		}
	}

	return totalMatches
}

// findMaxMatchesOptimized 进一步优化的匹配查找
func findMaxMatchesOptimized(token1, token2 []int, matched1, matched2 []bool,
	minMatchLength int, maxMatch *int, maxTiles *[]MatchTile) {

	n1, n2 := len(token1), len(token2)

	for i := 0; i < n1; i++ {
		if matched1[i] || n1-i < *maxMatch {
			continue
		}

		for j := 0; j < n2; j++ {
			if matched2[j] || n2-j < *maxMatch {
				continue
			}

			// 快速路径：检查是否可能产生更长的匹配
			if token1[i] != token2[j] {
				continue
			}

			// 计算匹配长度
			k := 1
			for i+k < n1 && j+k < n2 &&
				!matched1[i+k] && !matched2[j+k] &&
				token1[i+k] == token2[j+k] {
				k++
			}

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

// min 返回两个整数中的较小值
func min(a, b int) int {
	if a < b {
		return a
	}
	return b
}
