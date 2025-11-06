package core

import (
	"context"
	"encoding/json"
	"github.com/zeromicro/go-zero/core/logx"
	"math"
	"similarity-service/internal/config"
	"similarity-service/internal/database/model"
	"similarity-service/internal/mq"
	"similarity-service/internal/similarity"
	"similarity-service/internal/svc"
	"similarity-service/internal/utils"
	"sync"
	"time"
)

type Workspace struct {
	ctx               context.Context
	config            config.Config
	startTime         time.Time
	similarityMessage mq.SimilarityMessage
	svcCtx            *svc.ServiceContext
	mu                sync.RWMutex
	isCleaned         bool
}

// 预分配的错误变量，避免重复创建
var (
	ErrGetLibrariesFailed = "获取样本库失败"
	ErrParseTokenFailed   = "解析 CodeToken JSON 失败"
	ErrBatchCreateFailed  = "批量插入 TaskSimilarity 记录失败"
)

func NewWorkspace(ctx context.Context, config config.Config, similarityMessage mq.SimilarityMessage, svcCtx *svc.ServiceContext) (*Workspace, *mq.SimilarityResultMessage) {
	return &Workspace{
		ctx:               ctx,
		startTime:         time.Now(),
		config:            config,
		similarityMessage: similarityMessage,
		svcCtx:            svcCtx,
		isCleaned:         false,
	}, nil
}

func (w *Workspace) buildResponse(isOk bool) *mq.SimilarityResultMessage {
	return &mq.SimilarityResultMessage{
		ReportID:  w.similarityMessage.ReportID,
		TaskID:    w.similarityMessage.TaskID,
		Threshold: w.similarityMessage.Threshold,
		TaskType:  w.similarityMessage.TaskType,
		IsOK:      isOk,
	}
}

// calculateSimilarityScorePrecise 优化计算逻辑，避免不必要的类型转换
func calculateSimilarityScorePrecise(matches int, submitTokens, libraryTokens []int) float64 {
	if matches == 0 {
		return 0.0
	}

	// 直接使用 float64 计算，避免多次类型转换
	submitSize := float64(len(submitTokens))
	librarySize := float64(len(libraryTokens))
	totalSize := submitSize + librarySize

	if totalSize == 0 {
		return 0.0
	}

	similarity := (float64(matches) * 2) / totalSize
	return math.Round(similarity*10000) / 10000
}

// parseCodeTokens 解析 CodeToken JSON，添加错误处理
func parseCodeTokens(codeToken string, id string) ([]int, error) {
	var tokens []int
	if err := json.Unmarshal([]byte(codeToken), &tokens); err != nil {
		logx.Infof("%s (ID: %s): %v", ErrParseTokenFailed, id, err)
		return nil, err
	}
	return tokens, nil
}

// precomputeTokens 预处理所有样本的 tokens，避免重复解析
func (w *Workspace) precomputeTokens(sampleLibraries []model.DataLibrary) (map[string][]int, []model.DataLibrary) {
	validSamples := make([]model.DataLibrary, 0, len(sampleLibraries))
	tokenCache := make(map[string][]int, len(sampleLibraries))

	for i, sample := range sampleLibraries {
		tokens, err := parseCodeTokens(sample.CodeToken, sample.ID)
		if err != nil {
			continue // 跳过解析失败的样本
		}
		tokenCache[sample.ID] = tokens
		validSamples = append(validSamples, sampleLibraries[i])
	}

	return tokenCache, validSamples
}

// computeSimilarities 并行计算相似度，避免重复组合 使用组合数公式 C(n,2) = n*(n-1)/2 来计算预期的记录数量
// 避免了重复的 A-B 和 B-A 比较
// 外层循环 i 从 0 到 len(validSamples)-1
// 内层循环 j 从 i+1 到 len(validSamples)-1
// 这样确保每个组合 (i,j) 只比较一次，且 i < j
func (w *Workspace) computeSimilarities(validSamples []model.DataLibrary, tokenCache map[string][]int) []*model.TaskSimilarity {
	var (
		taskSimilarities []*model.TaskSimilarity
		mu               sync.Mutex
		wg               sync.WaitGroup
	)

	// 使用工作池控制并发数
	concurrency := w.getOptimalConcurrency(len(validSamples))
	semaphore := make(chan struct{}, concurrency)

	totalPairs := len(validSamples) * (len(validSamples) - 1) / 2 // 组合数公式：C(n,2) = n*(n-1)/2
	taskSimilarities = make([]*model.TaskSimilarity, 0, totalPairs)

	// 修改循环逻辑，确保每个组合只比较一次
	for i := 0; i < len(validSamples); i++ {
		for j := i + 1; j < len(validSamples); j++ {
			sample1 := validSamples[i]
			sample2 := validSamples[j]

			wg.Add(1)
			semaphore <- struct{}{} // 获取信号量

			go func(s1, s2 model.DataLibrary) {
				defer wg.Done()
				defer func() { <-semaphore }() // 释放信号量

				tokens1 := tokenCache[s1.ID]
				tokens2 := tokenCache[s2.ID]

				matches := similarity.GreedyStringTiling(tokens1, tokens2, w.similarityMessage.MinMatchLength)
				similarityScore := calculateSimilarityScorePrecise(matches, tokens1, tokens2)

				taskSimilarity := w.createTaskSimilarity(&s1, &s2, similarityScore)

				mu.Lock()
				taskSimilarities = append(taskSimilarities, taskSimilarity)
				mu.Unlock()
			}(sample1, sample2)
		}
	}

	wg.Wait()
	return taskSimilarities
}

// getOptimalConcurrency 获取最优并发数
func (w *Workspace) getOptimalConcurrency(sampleCount int) int {
	// 根据样本数量动态调整并发数
	if sampleCount <= 10 {
		return 2
	} else if sampleCount <= 50 {
		return 4
	} else if sampleCount <= 100 {
		return 8
	} else {
		return 16
	}
}

// Execute 优化后的执行方法
func (w *Workspace) Execute() *mq.SimilarityResultMessage {
	startTime := time.Now()
	logx.Infof("开始执行 %v", w.similarityMessage.TaskID)

	w.mu.Lock()
	defer w.mu.Unlock()

	// 检查上下文是否已取消
	if err := w.ctx.Err(); err != nil {
		logx.Errorf("任务执行前上下文已取消: %v", err)
		return w.buildResponse(false)
	}

	sampleLibraries, err := w.svcCtx.DataLibraryRepo().GetLibrariesByIDs(w.similarityMessage.LibIDs)
	if err != nil {
		logx.Errorf("%s: %v", ErrGetLibrariesFailed, err)
		return w.buildResponse(false)
	}

	if len(sampleLibraries) == 0 {
		logx.Info("未找到有效的样本库数据")
		return w.buildResponse(false) // 空数据
	}

	// 预处理 tokens
	tokenCache, validSamples := w.precomputeTokens(sampleLibraries)
	if len(validSamples) < 2 {
		logx.Info("有效样本数量不足，无法进行相似度计算")
		return w.buildResponse(false)
	}

	// 并行计算相似度
	taskSimilarities := w.computeSimilarities(validSamples, tokenCache)

	// 批量插入数据库
	if len(taskSimilarities) > 0 {
		if err := w.svcCtx.TaskSimilarityRepo().BatchCreate(taskSimilarities); err != nil {
			logx.Errorf("%s: %v", ErrBatchCreateFailed, err)
			// 这里可以根据业务需求决定是否返回失败
		} else {
			logx.Infof("成功插入 %d 条 TaskSimilarity 记录", len(taskSimilarities))
		}
	}

	response := w.buildResponse(true)
	totalTime := time.Since(startTime)
	logx.Infof("Execute函数总执行耗时: %v，处理了 %d 个样本，生成 %d 条相似度记录",
		totalTime, len(validSamples), len(taskSimilarities))

	return response
}

// createTaskSimilarity 优化创建方法，减少重复的 JSON 序列化
func (w *Workspace) createTaskSimilarity(sample1, sample2 *model.DataLibrary, similarityScore float64) *model.TaskSimilarity {
	now := time.Now()
	return &model.TaskSimilarity{
		ID:         utils.GenerateID(),
		TaskID:     w.similarityMessage.TaskID,
		TaskType:   true,
		ProblemID:  sample1.ProblemID,
		ModuleId:      sample1.ModuleId,
		ModuleType:      sample1.ModuleType,
		Language:   sample1.Language,
		Similarity: similarityScore,

		SubmitUser:       sample1.UserID,
		SubmitCode:       sample1.Code,
		SubmitCodeLength: sample1.CodeLength,
		SubmitID:         sample1.SubmitID,
		SubmitTime:       sample1.SubmitTime,
		SubmitCodeToken:  sample1.CodeToken,
		SubmitTokenName:  sample1.CodeTokenName,
		SubmitTokenTexts: sample1.CodeTokenTexts,

		OriginUser:       sample2.UserID,
		OriginCode:       sample2.Code,
		OriginCodeLength: sample2.CodeLength,
		OriginID:         sample2.SubmitID,
		OriginTime:       sample2.SubmitTime,
		OriginCodeToken:  sample2.CodeToken,
		OriginTokenName:  sample2.CodeTokenName,
		OriginTokenTexts: sample2.CodeTokenTexts,

		CreateTime: &now,
		CreateUser: "0",
		UpdateTime: &now,
		UpdateUser: "0",
	}
}

// Cleanup 清理资源的方法
func (w *Workspace) Cleanup() {
	w.mu.Lock()
	defer w.mu.Unlock()

	if w.isCleaned {
		return
	}

	// 执行清理操作
	logx.Infof("清理工作空间 %v", w.similarityMessage.TaskID)
	w.isCleaned = true
}
