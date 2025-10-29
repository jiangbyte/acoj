package judge

import (
	"encoding/json"
	"fmt"
	"judge-service/internal/dto"
	"judge-service/internal/model"
)

type Evaluator interface {
	Evaluate(workspace *Workspace, results []*model.DataJudgeCase) *dto.JudgeResponse
}

type DefaultEvaluator struct{}

func (e *DefaultEvaluator) Evaluate(workspace *Workspace, results []*model.DataJudgeCase) *dto.JudgeResponse {
	body, _ := json.MarshalIndent(results, "", "  ")
	fmt.Printf("🎯 判题结果:\n%s\n", string(body))

	return nil
}
func (e *DefaultEvaluator) compareOutput(actual, expected string) bool {
	return false
}

func (w *Workspace) evaluate(results []*model.DataJudgeCase) *dto.JudgeResponse {
	evaluator := &DefaultEvaluator{}
	return evaluator.Evaluate(w, results)
}
