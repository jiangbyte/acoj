package core

import (
	"github.com/zeromicro/go-zero/core/logx"
	"judge-service/internal/database/model"
	"judge-service/internal/mq"
	"judge-service/internal/utils"
)

type Evaluator interface {
	Evaluate(workspace *Workspace, results []*model.DataJudgeCase) *mq.JudgeResponse
}

type DefaultEvaluator struct{}

func (e *DefaultEvaluator) Evaluate(workspace *Workspace, results []*model.DataJudgeCase) *mq.JudgeResponse {
	judgeRequest := workspace.judgeRequest
	result := &mq.JudgeResponse{
		ID:          judgeRequest.ID,
		UserId:      judgeRequest.UserId,
		ProblemId:   judgeRequest.ProblemId,
		SetId:       judgeRequest.SetId,
		Language:    judgeRequest.Language,
		Code:        judgeRequest.Code,
		SubmitType:  judgeRequest.SubmitType,
		MaxTime:     0,
		MaxMemory:   0,
		IsSet:       judgeRequest.IsSet,
		JudgeTaskId: judgeRequest.JudgeTaskId,
	}

	// 异步插入 JudgeCase 记录
	if len(results) > 0 {
		e.asyncInsertJudgeCases(workspace, results)
	}
	//err := workspace.svcCtx.JudgeCaseRepo().BatchCreateJudgeCases(workspace.ctx, results)
	//if err != nil {
	//	result.Status = "SYSTEM_ERROR"
	//	return result
	//}

	maxTime := 0.0
	maxMemory := 0.0
	statusCount := make(map[string]int)
	messageCount := make(map[string]int)

	for _, testCase := range results {
		// 计算最大值
		if testCase.MaxTime > maxTime {
			maxTime = testCase.MaxTime
		}
		if testCase.MaxMemory > maxMemory {
			maxMemory = testCase.MaxMemory
		}

		if testCase.Status == "RUN_SUCCESS" {
			if utils.CompareOutput(testCase.ExpectedOutput, testCase.OutputData) {
				testCase.Status = "ACCEPTED"
			} else {
				testCase.Status = "WRONG_ANSWER"
			}
		}

		// 统计各种状态的数量
		statusCount[testCase.Status]++
		// 消息聚合
		messageCount[testCase.Message]++
	}

	// 设置最终的最大值
	result.MaxTime = maxTime
	result.MaxMemory = maxMemory

	// 根据状态统计结果设置最终状态
	result.Status = e.aggregateStatus(statusCount, len(results))
	result.Message = e.aggregateMessage(messageCount)
	return result
}

// 异步插入方法
func (e *DefaultEvaluator) asyncInsertJudgeCases(workspace *Workspace, results []*model.DataJudgeCase) {
	go func() {
		defer func() {
			if r := recover(); r != nil {
				logx.Errorf("异步插入 JudgeCase 时发生 panic: %v", r)
			}
		}()

		// 创建深拷贝避免数据竞争
		casesCopy := e.copyJudgeCases(results)

		if err := workspace.svcCtx.JudgeCaseRepo().BatchCreateJudgeCases(casesCopy); err != nil {
			logx.Errorf("异步批量插入 JudgeCase 记录失败: %v", err)
		} else {
			logx.Infof("成功异步插入 %d 条 JudgeCase 记录", len(casesCopy))
		}
	}()
}

// 深拷贝 JudgeCase 数据
func (e *DefaultEvaluator) copyJudgeCases(cases []*model.DataJudgeCase) []*model.DataJudgeCase {
	copies := make([]*model.DataJudgeCase, len(cases))
	for i, c := range cases {
		copyCase := *c // 创建结构体副本
		copies[i] = &copyCase
	}
	return copies
}

func (w *Workspace) evaluate(results []*model.DataJudgeCase) *mq.JudgeResponse {
	evaluator := &DefaultEvaluator{}
	return evaluator.Evaluate(w, results)
}

// 聚合状态，优先异常状态，然后选择数量最多的状态
func (e *DefaultEvaluator) aggregateStatus(statusCount map[string]int, lens int) string {
	if len(statusCount) == 0 {
		return "PENDING"
	}

	// 定义异常状态优先级（从高到低）
	exceptionStatuses := []string{
		"COMPILATION_ERROR",
		"RUNTIME_ERROR",
		"TIME_LIMIT_EXCEEDED",
		"MEMORY_LIMIT_EXCEEDED",
		"SYSTEM_ERROR",
	}

	// 首先检查是否有异常状态
	for _, status := range exceptionStatuses {
		if count, exists := statusCount[status]; exists && count > 0 {
			return status
		}
	}

	// 如果没有异常状态，则按照AC、PC、WA的优先级处理
	acCount := statusCount["ACCEPTED"]
	pcCount := statusCount["PARTIAL_ACCEPTED"]
	waCount := statusCount["WRONG_ANSWER"]
	otherCount := 0

	// 计算其他状态的总数
	for status, count := range statusCount {
		if status != "ACCEPTED" &&
			status != "PARTIAL_ACCEPTED" &&
			status != "WRONG_ANSWER" &&
			status != "PENDING" {
			otherCount += count
		}
	}

	// 判断逻辑
	if acCount == lens {
		// 全部正确
		return "ACCEPTED"
	} else if acCount > 0 && (acCount+pcCount+waCount+otherCount == lens) {
		if pcCount > 0 || waCount > 0 || otherCount > 0 {
			// 有部分正确，也有错误
			return "PARTIAL_ACCEPTED"
		}
	} else if waCount == lens {
		// 全部错误
		return "WRONG_ANSWER"
	} else if pcCount > 0 {
		// 有部分正确的情况
		return "PARTIAL_ACCEPTED"
	}

	// 默认情况：选择数量最多的状态（排除跳过和待判状态）
	maxCount := 0
	mostCommonStatus := "PENDING"

	for status, count := range statusCount {
		if status != "PENDING" &&
			count > maxCount {
			maxCount = count
			mostCommonStatus = status
		}
	}

	// 如果所有测试用例都是跳过或待判状态
	if mostCommonStatus == "PENDING" &&
		(statusCount["PENDING"] > 0) {
		// 检查是否有实际的状态
		hasActualStatus := false
		for status := range statusCount {
			if status != "PENDING" {
				hasActualStatus = true
				break
			}
		}
		if !hasActualStatus {
			return "PENDING"
		}
	}

	return mostCommonStatus
}

// aggregateMessage 聚合消息
func (e *DefaultEvaluator) aggregateMessage(messageCount map[string]int) string {
	if len(messageCount) == 0 {
		return ""
	}

	var maxCount int
	var mostFrequentMessage string

	for message, count := range messageCount {
		if count > maxCount {
			maxCount = count
			mostFrequentMessage = message
		}
	}

	return mostFrequentMessage
}
