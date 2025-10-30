package core

import (
	"context"
	"encoding/json"
	"github.com/zeromicro/go-zero/core/logx"
	"math"
	"runtime"
	"similarity-service/internal/config"
	"similarity-service/internal/database/model"
	"similarity-service/internal/mq"
	"similarity-service/internal/similarity"
	"similarity-service/internal/svc"
	"similarity-service/internal/utils"
	"sync"
	"time"
)

// WorkspaceManager 工作空间管理器，负责工作空间的创建和清理
type WorkspaceManager struct {
	workspaces sync.Map // 存储活跃的工作空间
	basePath   string
	mu         sync.RWMutex
}

var (
	workspaceManager *WorkspaceManager
	once             sync.Once
)

func GetWorkspaceManager(basePath string) *WorkspaceManager {
	once.Do(func() {
		workspaceManager = &WorkspaceManager{
			basePath: basePath,
		}
	})
	return workspaceManager
}

type Workspace struct {
	ctx               context.Context
	config            config.Config
	startTime         time.Time
	similarityMessage mq.SimilarityMessage
	svcCtx            *svc.ServiceContext
	mu                sync.RWMutex // 工作空间内部锁
	isCleaned         bool         // 标记是否已清理
}

// NewWorkspace 创建工作空间,上下文/配置/提交信息,返回 工作空间实例 和 提交信息
func NewWorkspace(ctx context.Context, config config.Config, similarityMessage mq.SimilarityMessage, svcCtx *svc.ServiceContext) (*Workspace, *mq.SimilarityResultMessage) {
	ws := &Workspace{
		ctx:               ctx,        // 使用传入的ctx
		startTime:         time.Now(), // 记录开始时间，用来计算任务总耗时
		config:            config,     // 系统配置
		similarityMessage: similarityMessage,
		svcCtx:            svcCtx,
		isCleaned:         false,
	}
	return ws, nil
}

// buildErrorResponse 构建错误响应
func (w *Workspace) buildErrorResponse() *mq.SimilarityResultMessage {
	return &mq.SimilarityResultMessage{
		TaskID:         w.similarityMessage.TaskID,
		SubmitID:       w.similarityMessage.SubmitID,
		SetID:          w.similarityMessage.SetID,
		ProblemID:      w.similarityMessage.ProblemID,
		IsSet:          w.similarityMessage.IsSet,
		Language:       w.similarityMessage.Language,
		UserID:         w.similarityMessage.UserID,
		MinMatchLength: w.similarityMessage.MinMatchLength,
		Threshold:      w.similarityMessage.Threshold,
		TaskType:       w.similarityMessage.TaskType,
		CodeTokens:     w.similarityMessage.CodeTokens,
		CodeTokenNames: w.similarityMessage.CodeTokenNames,
		CodeTokenTexts: w.similarityMessage.CodeTokenTexts,
		Similarity:     0.0,
	}
}

// buildErrorResponse 构建响应
func (w *Workspace) buildResponse(similarity float64) *mq.SimilarityResultMessage {
	return &mq.SimilarityResultMessage{
		TaskID:         w.similarityMessage.TaskID,
		SubmitID:       w.similarityMessage.SubmitID,
		SetID:          w.similarityMessage.SetID,
		ProblemID:      w.similarityMessage.ProblemID,
		IsSet:          w.similarityMessage.IsSet,
		Language:       w.similarityMessage.Language,
		UserID:         w.similarityMessage.UserID,
		MinMatchLength: w.similarityMessage.MinMatchLength,
		Threshold:      w.similarityMessage.Threshold,
		TaskType:       w.similarityMessage.TaskType,
		CodeTokens:     w.similarityMessage.CodeTokens,
		CodeTokenNames: w.similarityMessage.CodeTokenNames,
		CodeTokenTexts: w.similarityMessage.CodeTokenTexts,
		Similarity:     similarity,
	}
}

func calculateSimilarityScorePrecise(matches int, submitTokens, libraryTokens []int) float64 {
	if matches == 0 {
		return 0.0
	}

	// 使用 int64 避免溢出
	matchesBig := int64(matches * 2)
	submitSize := int64(len(submitTokens))
	librarySize := int64(len(libraryTokens))

	denominator := submitSize + librarySize
	if denominator == 0 {
		return 0.0
	}

	similarity := float64(matchesBig) / float64(denominator)
	return math.Round(similarity*10000) / 10000 // 保留4位小数
}

// 执行代码
//func (w *Workspace) Execute() *mq.SimilarityResultMessage {
//	startTime := time.Now()
//	logx.Infof("开始执行 %v", w.similarityMessage.TaskID)
//	w.mu.Lock()
//	defer w.mu.Unlock()
//
//	sampleLibraries, err := w.svcCtx.DataLibraryRepo().GetSimilarityLibraries(
//		w.similarityMessage.IsSet,
//		w.similarityMessage.ProblemID,
//		w.similarityMessage.Language,
//		w.similarityMessage.UserID,
//		w.similarityMessage.SetID,
//		100,
//	)
//
//	if err != nil {
//		logx.Errorf("获取样本库失败: %v", err)
//		return nil
//	}
//
//	logx.Infof("样本库数量: %d", len(sampleLibraries))
//
//	var (
//		maxSimilarityScore float64
//		_                  sync.Mutex
//	)
//
//	// 使用 worker pool
//	numWorkers := runtime.NumCPU() * 2
//	jobs := make(chan model.DataLibrary, len(sampleLibraries))
//	results := make(chan float64, len(sampleLibraries))
//
//	// 启动 worker
//	for i := 0; i < numWorkers; i++ {
//		go func() {
//			for library := range jobs {
//				// 解析 CodeToken JSON 字符串为 []int
//				var codeTokens []int
//				if err := json.Unmarshal([]byte(library.CodeToken), &codeTokens); err != nil {
//					logx.Infof("解析 CodeToken JSON 失败 (ID: %s): %v", library.ID, err)
//					results <- 0.0
//					continue
//				}
//
//				matches := similarity.GreedyStringTiling(w.similarityMessage.CodeTokens, codeTokens, 5)
//				similarityScore := calculateSimilarityScorePrecise(
//					matches,
//					w.similarityMessage.CodeTokens,
//					codeTokens,
//				)
//				results <- similarityScore
//			}
//		}()
//	}
//
//	// 分发任务
//	go func() {
//		for _, library := range sampleLibraries {
//			jobs <- library
//		}
//		close(jobs)
//	}()
//
//	// 收集结果
//	for i := 0; i < len(sampleLibraries); i++ {
//		if similarityScore := <-results; similarityScore > maxSimilarityScore {
//			maxSimilarityScore = similarityScore
//		}
//	}
//
//	totalTime := time.Since(startTime)
//	logx.Infof("Execute函数总执行耗时: %v", totalTime)
//	return w.buildResponse(maxSimilarityScore)
//}

// 执行代码
func (w *Workspace) Execute() *mq.SimilarityResultMessage {
	startTime := time.Now()
	logx.Infof("开始执行 %v", w.similarityMessage.TaskID)
	w.mu.Lock()
	defer w.mu.Unlock()

	sampleLibraries, err := w.svcCtx.DataLibraryRepo().GetSimilarityLibraries(
		w.similarityMessage.IsSet,
		w.similarityMessage.ProblemID,
		w.similarityMessage.Language,
		w.similarityMessage.UserID,
		w.similarityMessage.SetID,
		100,
	)

	if err != nil {
		logx.Errorf("获取样本库失败: %v", err)
		return nil
	}

	logx.Infof("样本库数量: %d", len(sampleLibraries))

	var (
		maxSimilarityScore float64
		mu                 sync.Mutex
		taskSimilarities   []*model.TaskSimilarity // 用于收集 TaskSimilarity 记录
	)
	// 定义结果结构
	type similarityResult struct {
		similarityScore float64
		library         model.DataLibrary
	}

	// 使用 worker pool
	numWorkers := runtime.NumCPU() * 2
	jobs := make(chan model.DataLibrary, len(sampleLibraries))
	results := make(chan *similarityResult, len(sampleLibraries))

	// 启动 worker
	for i := 0; i < numWorkers; i++ {
		go func() {
			for library := range jobs {
				// 解析 CodeToken JSON 字符串为 []int
				var codeTokens []int
				if err := json.Unmarshal([]byte(library.CodeToken), &codeTokens); err != nil {
					logx.Infof("解析 CodeToken JSON 失败 (ID: %s): %v", library.ID, err)
					results <- &similarityResult{similarityScore: 0.0, library: library}
					continue
				}

				matches := similarity.GreedyStringTiling(w.similarityMessage.CodeTokens, codeTokens, 5)
				similarityScore := calculateSimilarityScorePrecise(
					matches,
					w.similarityMessage.CodeTokens,
					codeTokens,
				)
				results <- &similarityResult{similarityScore: similarityScore, library: library}
			}
		}()
	}

	// 分发任务
	go func() {
		for _, library := range sampleLibraries {
			jobs <- library
		}
		close(jobs)
	}()

	// 收集结果
	for i := 0; i < len(sampleLibraries); i++ {
		result := <-results

		// 更新最大相似度
		if result.similarityScore > maxSimilarityScore {
			maxSimilarityScore = result.similarityScore
		}

		// 创建 TaskSimilarity 记录
		taskSimilarity := w.createTaskSimilarity(result.library, result.similarityScore)

		mu.Lock()
		taskSimilarities = append(taskSimilarities, taskSimilarity)
		mu.Unlock()
	}

	// 批量插入 TaskSimilarity 记录
	if len(taskSimilarities) > 0 {
		if err := w.svcCtx.TaskSimilarityRepo().BatchCreate(taskSimilarities); err != nil {
			logx.Errorf("批量插入 TaskSimilarity 记录失败: %v", err)
		} else {
			logx.Infof("成功插入 %d 条 TaskSimilarity 记录", len(taskSimilarities))
		}
	}

	//totalTime := time.Since(startTime)
	//logx.Infof("Execute函数总执行耗时: %v", totalTime)
	//return w.buildResponse(maxSimilarityScore)

	// 构建响应
	response := w.buildResponse(maxSimilarityScore)

	// 生成报告
	if err := w.GenerateReport(response); err != nil {
		logx.Errorf("生成报告失败: %v", err)
		// 不返回错误，继续返回相似度结果
	}

	totalTime := time.Since(startTime)
	logx.Infof("Execute函数总执行耗时: %v", totalTime)
	return response
}

// 创建 TaskSimilarity 记录
func (w *Workspace) createTaskSimilarity(library model.DataLibrary, similarityScore float64) *model.TaskSimilarity {
	now := time.Now()

	// 将 token 数组转换为 JSON 字符串
	submitCodeTokenJSON, _ := json.Marshal(w.similarityMessage.CodeTokens)
	originCodeTokenJSON, _ := json.Marshal(library.CodeToken)

	submitTokenNames, _ := json.Marshal(w.similarityMessage.CodeTokenNames)
	submitTokenTexts, _ := json.Marshal(w.similarityMessage.CodeTokenTexts)

	return &model.TaskSimilarity{
		ID:         utils.GenerateID(),
		TaskID:     w.similarityMessage.TaskID,
		TaskType:   w.similarityMessage.IsSet,
		ProblemID:  w.similarityMessage.ProblemID,
		SetID:      w.similarityMessage.SetID,
		IsSet:      w.similarityMessage.IsSet,
		Language:   w.similarityMessage.Language,
		Similarity: similarityScore,
		SubmitUser: w.similarityMessage.UserID,
		//SubmitCode:       w.similarityMessage.SubmitCode,
		//SubmitCodeLength: len(w.similarityMessage.SubmitCode),
		SubmitID:         w.similarityMessage.SubmitID,
		SubmitTime:       &now,
		SubmitCodeToken:  string(submitCodeTokenJSON),
		SubmitTokenName:  string(submitTokenNames), // 根据实际情况设置
		SubmitTokenTexts: string(submitTokenTexts), // 根据实际情况设置
		OriginUser:       library.UserID,
		OriginCode:       library.Code,
		OriginCodeLength: len(library.Code),
		OriginID:         library.ID,
		OriginTime:       library.SubmitTime,
		OriginCodeToken:  string(originCodeTokenJSON),
		OriginTokenName:  library.CodeTokenName,  // 根据实际情况设置
		OriginTokenTexts: library.CodeTokenTexts, // 根据实际情况设置
		CreateTime:       &now,
		CreateUser:       "0", // 根据实际情况设置
		UpdateTime:       &now,
		UpdateUser:       "0",
	}
}

// 在Workspace结构体中添加报告生成方法
func (w *Workspace) GenerateReport(similarityResult *mq.SimilarityResultMessage) error {
	logx.Infof("开始生成报告 taskID: %s", w.similarityMessage.TaskID)

	// 设置阈值
	threshold := w.similarityMessage.Threshold

	var isSetValue int
	if w.similarityMessage.IsSet {
		isSetValue = 1
	} else {
		isSetValue = 0
	}
	// 从数据库中查询统计信息
	taskReportStats, err := w.svcCtx.TaskSimilarityRepo().SelectSimilarityStats(
		w.similarityMessage.TaskID,
		w.similarityMessage.ProblemID,
		w.similarityMessage.SetID,
		isSetValue,
	)
	if err != nil {
		logx.Errorf("查询相似度统计失败: %v", err)
		return err
	}

	// 检查相似度是否为空（类似于Java中的BigDecimal.ZERO检查）
	if taskReportStats.AvgSimilarity == 0 || taskReportStats.MaxSimilarity == 0 {
		logx.Info("代码克隆检测 -> 相似为空 忽略报告")

		//// 更新DataSubmit记录
		//dataSubmit, err := w.svcCtx.DataSubmitRepo().FindByID(w.similarityMessage.SubmitID)
		//if err != nil {
		//	logx.Errorf("查找DataSubmit记录失败: %v", err)
		//	return err
		//}
		//
		//// 更新字段
		//dataSubmit.Similarity = 0
		//dataSubmit.SimilarityCategory = enum.NOT_DETECTED
		//dataSubmit.ReportID = nil
		//dataSubmit.UpdateTime = time.Now()
		//
		//if err := w.svcCtx.DataSubmitRepo().Update(dataSubmit); err != nil {
		//	logx.Errorf("更新DataSubmit记录失败: %v", err)
		//	return err
		//}

		logx.Info("已更新DataSubmit记录，跳过报告生成")
		return nil
	}

	// 获取相似度分布数组
	distributionArray, err := w.svcCtx.TaskSimilarityRepo().SelectSimilarityDistribution(w.similarityMessage.TaskID)
	if err != nil {
		logx.Errorf("查询相似度分布失败: %v", err)
		return err
	}

	// 获取程度统计
	degreeStats, err := w.svcCtx.TaskSimilarityRepo().SelectDegreeStatistics(w.similarityMessage.TaskID, threshold)
	if err != nil {
		logx.Errorf("查询程度统计失败: %v", err)
		return err
	}

	// 将分布数组和程度统计转换为JSON字符串
	distributionJSON, err := json.Marshal(distributionArray)
	if err != nil {
		logx.Errorf("序列化相似度分布失败: %v", err)
		return err
	}

	degreeStatsJSON, err := json.Marshal(degreeStats)
	if err != nil {
		logx.Errorf("序列化程度统计失败: %v", err)
		return err
	}

	now := time.Now()
	// 创建TaskReports记录
	taskReport := &model.TaskReports{
		ID:                     utils.GenerateID(),
		TaskID:                 w.similarityMessage.TaskID,
		ProblemID:              w.similarityMessage.ProblemID,
		SetID:                  w.similarityMessage.SetID,
		SampleCount:            taskReportStats.SampleCount,
		SimilarityGroupCount:   taskReportStats.GroupCount,
		AvgSimilarity:          taskReportStats.AvgSimilarity,
		MaxSimilarity:          taskReportStats.MaxSimilarity,
		Threshold:              threshold,
		SimilarityDistribution: string(distributionJSON),
		DegreeStatistics:       string(degreeStatsJSON),
		CreateTime:             &now,
		UpdateTime:             &now,
	}

	// 插入TaskReports记录
	if err := w.svcCtx.TaskReportsRepo().Create(taskReport); err != nil {
		logx.Errorf("插入TaskReports记录失败: %v", err)
		return err
	}

	logx.Infof("成功生成报告，报告ID: %s", taskReport.ID)
	return nil
}

// GetAllWorkspaces 获取所有工作空间（用于监控）
func (wm *WorkspaceManager) GetAllWorkspaces() []*Workspace {
	var workspaces []*Workspace
	wm.workspaces.Range(func(key, value interface{}) bool {
		if ws, ok := value.(*Workspace); ok {
			workspaces = append(workspaces, ws)
		}
		return true
	})
	return workspaces
}
