package dto

// ConvertSubmitToResult 将 JudgeSubmitDto 转换为 JudgeResultDto
func ConvertSubmitToResult(submit JudgeSubmitDto) JudgeResultDto {
	result := JudgeResultDto{
		UserId:       submit.UserId,
		ProblemId:    submit.ProblemId,
		Language:     submit.Language,
		ProblemSetId: submit.ProblemSetId,
		Code:         submit.Code,
		SubmitType:   submit.SubmitType,
		MaxTime:      submit.MaxTime,
		MaxMemory:    submit.MaxMemory,
		ID:           submit.ID,
		IsSet:        submit.IsSet,
		Status:       StatusWrongAnswer, // 初始状态为空，等待判题结果
		Message:      "",                // 初始消息为空
	}

	// 转换测试用例
	result.TestCase = make([]SubmitTestCase, len(submit.TestCase))
	for i, testCase := range submit.TestCase {
		result.TestCase[i] = SubmitTestCase{
			Input:     testCase.Input,
			Output:    "",              // 初始输出为空
			Except:    testCase.Output, // 预期输出来自提交的测试用例
			MaxTime:   0,
			MaxMemory: 0,
			Message:   "",                // 初始消息为空
			Status:    StatusWrongAnswer, // 初始状态为空
		}
	}

	return result
}

// UpdateResultWithTestCase 更新结果中的测试用例信息
func UpdateResultWithTestCase(result *JudgeResultDto, index int, output string, status JudgeStatus, message string) {
	if index >= 0 && index < len(result.TestCase) {
		result.TestCase[index].Output = output
		result.TestCase[index].Status = status
		result.TestCase[index].Message = message
	}
}

// DetermineFinalStatus 根据所有测试用例的状态确定最终状态
func DetermineFinalStatus(result *JudgeResultDto) {
	if len(result.TestCase) == 0 {
		result.Status = StatusSystemError
		result.Message = "No test cases provided"
		return
	}

	// 检查是否有编译错误或系统错误
	for _, testCase := range result.TestCase {
		if testCase.Status == StatusCompilationError || testCase.Status == StatusSystemError {
			result.Status = testCase.Status
			result.Message = testCase.Message
			return
		}
	}

	// 检查其他错误状态
	for _, testCase := range result.TestCase {
		if testCase.Status != StatusAccepted {
			result.Status = testCase.Status
			result.Message = testCase.Message
			return
		}
	}

	// 所有测试用例都通过
	result.Status = StatusAccepted
	result.Message = "All test cases passed"
}

// ConvertToFailedResult 创建一个失败的结果对象
func ConvertToFailedResult(submit JudgeSubmitDto, status JudgeStatus, message string) JudgeResultDto {
	result := ConvertSubmitToResult(submit)
	result.Status = status
	result.Message = message

	// 更新所有测试用例状态
	for i := range result.TestCase {
		result.TestCase[i].Status = status
		result.TestCase[i].Message = message
	}

	return result
}
