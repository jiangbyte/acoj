package runner

import (
	"strings"
)

// Strategy defines the output comparison strategy.
type Strategy string

const (
	StrategyStrict          Strategy = "strict"
	StrategyIgnoreTrailSpace Strategy = "ignore_trail_space"
	StrategyIgnoreAllSpace  Strategy = "ignore_all_space"
)

// CompareResult holds the result of an output comparison.
type CompareResult struct {
	Status string // AC / WA / PE
}

// Compare compares actual output with expected output using the specified strategy.
func Compare(actual, expected string, strategy Strategy) *CompareResult {
	switch strategy {
	case StrategyStrict:
		if actual == expected {
			return &CompareResult{Status: "AC"}
		}
		return &CompareResult{Status: "WA"}

	case StrategyIgnoreTrailSpace:
		actualLines := strings.Split(actual, "\n")
		expectedLines := strings.Split(expected, "\n")

		// Trim trailing whitespace from each line
		for i := range actualLines {
			actualLines[i] = strings.TrimRight(actualLines[i], " \t\r")
		}
		for i := range expectedLines {
			expectedLines[i] = strings.TrimRight(expectedLines[i], " \t\r")
		}

		// Remove trailing empty lines
		actualLines = trimTrailingEmpty(actualLines)
		expectedLines = trimTrailingEmpty(expectedLines)

		if len(actualLines) != len(expectedLines) {
			return &CompareResult{Status: "WA"}
		}
		for i := range actualLines {
			if actualLines[i] != expectedLines[i] {
				return &CompareResult{Status: "WA"}
			}
		}
		return &CompareResult{Status: "AC"}

	case StrategyIgnoreAllSpace:
		normActual := normalizeSpace(actual)
		normExpected := normalizeSpace(expected)
		if normActual == normExpected {
			return &CompareResult{Status: "AC"}
		}
		return &CompareResult{Status: "WA"}

	default:
		return Compare(actual, expected, StrategyIgnoreTrailSpace)
	}
}

func trimTrailingEmpty(lines []string) []string {
	for len(lines) > 0 && lines[len(lines)-1] == "" {
		lines = lines[:len(lines)-1]
	}
	return lines
}

func normalizeSpace(s string) string {
	fields := strings.Fields(s)
	return strings.Join(fields, " ")
}
