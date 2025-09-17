package judge

import (
	"context"
	"judge-service/internal/dto"
	"judge-service/internal/grutil"

	"github.com/zeromicro/go-zero/core/logx"
)

type Evaluater struct {
	ctx     context.Context
	logger  logx.Logger
	Sandbox Sandbox
	result  *dto.JudgeResultDto
}

// 创建执行器，传入上下文，沙箱
func NewEvaluater(ctx context.Context, sandbox Sandbox, resultData *dto.JudgeResultDto) *Evaluater {
	return &Evaluater{
		ctx:     ctx,
		logger:  logx.WithContext(ctx),
		Sandbox: sandbox,
		result:  resultData,
	}
}

// 实际执行
func (e *Evaluater) Execute() (*dto.JudgeResultDto, error) {
	maxTime := 0
	maxMemory := 0
	statusCount := make(map[dto.JudgeStatus]int)
	messageCount := make(map[string]int)

	for i := range e.result.TestCase {
		testCase := &e.result.TestCase[i] // 获取指向原始元素的指针

		// 计算最大值
		if testCase.MaxTime > maxTime {
			maxTime = testCase.MaxTime
		}
		if testCase.MaxMemory > maxMemory {
			maxMemory = testCase.MaxMemory
		}

		if testCase.Status == dto.StatusPending {
			if grutil.CompareOutput(testCase.Except, testCase.Output) {
				testCase.Status = dto.StatusAccepted
			} else {
				testCase.Status = dto.StatusWrongAnswer
			}
		}

		// 统计各种状态的数量
		statusCount[testCase.Status]++
		// 消息聚合
		messageCount[testCase.Message]++
	}

	// 设置最终的最大值
	e.result.MaxTime = maxTime
	e.result.MaxMemory = maxMemory

	// 根据状态统计结果设置最终状态
	e.result.Status = e.aggregateStatus(statusCount)
	e.result.Message = e.aggregateMessage(messageCount)

	logx.Infof("评估完成")
	return e.result, nil
}

// 聚合状态，优先异常状态，然后选择数量最多的状态
func (e *Evaluater) aggregateStatus(statusCount map[dto.JudgeStatus]int) dto.JudgeStatus {
	if len(statusCount) == 0 {
		return dto.StatusPending
	}

	// 定义异常状态优先级（从高到低）
	exceptionStatuses := []dto.JudgeStatus{
		dto.StatusCompilationError,
		dto.StatusRuntimeError,
		dto.StatusTimeLimitExceeded,
		dto.StatusMemoryLimitExceeded,
		dto.StatusOutputLimitExceeded,
		dto.StatusSystemError,
	}

	// 首先检查是否有异常状态
	for _, status := range exceptionStatuses {
		if count, exists := statusCount[status]; exists && count > 0 {
			return status
		}
	}

	// 如果没有异常状态，则按照AC、PC、WA的优先级处理
	acCount := statusCount[dto.StatusAccepted]
	pcCount := statusCount[dto.StatusPartialAccepted]
	waCount := statusCount[dto.StatusWrongAnswer]
	otherCount := 0

	// 计算其他状态的总数
	for status, count := range statusCount {
		if status != dto.StatusAccepted &&
			status != dto.StatusPartialAccepted &&
			status != dto.StatusWrongAnswer &&
			status != dto.StatusSkipped &&
			status != dto.StatusPending {
			otherCount += count
		}
	}

	// 判断逻辑
	if acCount == len(e.result.TestCase) {
		// 全部正确
		return dto.StatusAccepted
	} else if acCount > 0 && (acCount+pcCount+waCount+otherCount == len(e.result.TestCase)) {
		if pcCount > 0 || waCount > 0 || otherCount > 0 {
			// 有部分正确，也有错误
			return dto.StatusPartialAccepted
		}
	} else if waCount == len(e.result.TestCase) {
		// 全部错误
		return dto.StatusWrongAnswer
	} else if pcCount > 0 {
		// 有部分正确的情况
		return dto.StatusPartialAccepted
	}

	// 默认情况：选择数量最多的状态（排除跳过和待判状态）
	maxCount := 0
	mostCommonStatus := dto.StatusPending

	for status, count := range statusCount {
		if status != dto.StatusSkipped &&
			status != dto.StatusPending &&
			count > maxCount {
			maxCount = count
			mostCommonStatus = status
		}
	}

	// 如果所有测试用例都是跳过或待判状态
	if mostCommonStatus == dto.StatusPending &&
		(statusCount[dto.StatusSkipped] > 0 || statusCount[dto.StatusPending] > 0) {
		// 检查是否有实际的状态
		hasActualStatus := false
		for status := range statusCount {
			if status != dto.StatusSkipped && status != dto.StatusPending {
				hasActualStatus = true
				break
			}
		}
		if !hasActualStatus {
			return dto.StatusPending
		}
	}

	return mostCommonStatus
}

// aggregateMessage 聚合消息
func (e *Evaluater) aggregateMessage(messageCount map[string]int) string {
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
