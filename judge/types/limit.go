package types

import (
	"hei-gin/config"
	judgeProblem "hei-gin/modules/judge/problem"
)

// Limits holds resource limits for a single test case run.
type Limits struct {
	TimeMs int
	MemKb  int64
}

// ResolveLimits resolves resource limits with cascading overrides:
// language-level > problem-level defaults.
func ResolveLimits(problem *judgeProblem.JudgeProblem, langCfg *judgeProblem.JudgeProblemLanguage) Limits {
	limits := Limits{
		TimeMs: problem.TimeLimitMs,
		MemKb:  problem.MemoryLimitKb,
	}
	if langCfg != nil {
		if langCfg.TimeLimitMs != nil {
			limits.TimeMs = *langCfg.TimeLimitMs
		}
		if langCfg.MemoryLimitKb != nil {
			limits.MemKb = *langCfg.MemoryLimitKb
		}
	}
	return limits
}

// ResolveCompileLimits returns the default compile-time limits from config.
func ResolveCompileLimits() Limits {
	return Limits{
		TimeMs: config.C.Judge.CompileTimeLimit,
		MemKb:  config.C.Judge.CompileMemoryLimit,
	}
}
