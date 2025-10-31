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
		Skip:           false,
	}
}

// buildErrorResponse 构建错误响应
func (w *Workspace) buildSkipResponse() *mq.SimilarityResultMessage {
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
		Skip:           true,
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
		Skip:           false,
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

	if len(sampleLibraries) == 0 {
		return w.buildSkipResponse()
	}

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

	//// 批量插入 TaskSimilarity 记录
	//if len(taskSimilarities) > 0 {
	//	if err := w.svcCtx.TaskSimilarityRepo().BatchCreate(taskSimilarities); err != nil {
	//		logx.Errorf("批量插入 TaskSimilarity 记录失败: %v", err)
	//	} else {
	//		logx.Infof("成功插入 %d 条 TaskSimilarity 记录", len(taskSimilarities))
	//	}
	//}

	// 批量插入 TaskSimilarity 记录 - 异步执行
	if len(taskSimilarities) > 0 {
		go func(records []*model.TaskSimilarity) {
			defer func() {
				if r := recover(); r != nil {
					logx.Errorf("异步插入 TaskSimilarity 时发生 panic: %v", r)
				}
			}()

			if err := w.svcCtx.TaskSimilarityRepo().BatchCreate(records); err != nil {
				logx.Errorf("批量插入 TaskSimilarity 记录失败: %v", err)
			} else {
				logx.Infof("成功插入 %d 条 TaskSimilarity 记录", len(records))
			}
		}(taskSimilarities) // 注意：这里传递副本，避免数据竞争
	}

	// 立即返回响应，不等待插入完成
	response := w.buildResponse(maxSimilarityScore)

	totalTime := time.Since(startTime)
	logx.Infof("Execute函数总执行耗时: %v", totalTime)
	return response
}

//// 创建 TaskSimilarity 记录
//func (w *Workspace) createTaskSimilarity(library model.DataLibrary, similarityScore float64) *model.TaskSimilarity {
//	now := time.Now()
//
//	// 确保 token 数组正确序列化为 JSON 数组
//	var submitCodeTokenJSON, originCodeTokenJSON []byte
//	var err error
//
//	//// 将 token 数组转换为 JSON 字符串
//	//submitCodeTokenJSON, _ := json.Marshal(w.similarityMessage.CodeTokens)
//	//originCodeTokenJSON, _ := json.Marshal(library.CodeToken)
//
//	// 处理 submitCodeToken - 确保是 []int 类型
//	if w.similarityMessage.CodeTokens == nil {
//		submitCodeTokenJSON, _ = json.Marshal([]int{})
//	} else {
//		submitCodeTokenJSON, err = json.Marshal(w.similarityMessage.CodeTokens)
//		if err != nil {
//			// 处理错误，确保是整数数组
//			submitCodeTokenJSON, _ = json.Marshal([]int{})
//		}
//	}
//
//	// 处理 originCodeToken - 确保是 []int 类型
//	if library.CodeToken == nil {
//		originCodeTokenJSON, _ = json.Marshal([]int{})
//	} else {
//		// 确保 library.CodeToken 是整数切片
//		originCodeTokenJSON, err = json.Marshal(library.CodeToken)
//		if err != nil {
//			originCodeTokenJSON, _ = json.Marshal([]int{})
//		}
//	}
//
//	submitTokenNames, _ := json.Marshal(w.similarityMessage.CodeTokenNames)
//	submitTokenTexts, _ := json.Marshal(w.similarityMessage.CodeTokenTexts)
//
//	return &model.TaskSimilarity{
//		ID:               utils.GenerateID(),
//		TaskID:           w.similarityMessage.TaskID,
//		TaskType:         w.similarityMessage.IsSet,
//		ProblemID:        w.similarityMessage.ProblemID,
//		SetID:            w.similarityMessage.SetID,
//		IsSet:            w.similarityMessage.IsSet,
//		Language:         w.similarityMessage.Language,
//		Similarity:       similarityScore,
//		SubmitUser:       w.similarityMessage.UserID,
//		SubmitCode:       w.similarityMessage.Code,
//		SubmitCodeLength: w.similarityMessage.CodeLength,
//		SubmitID:         w.similarityMessage.SubmitID,
//		SubmitTime:       &now,
//		SubmitCodeToken:  string(submitCodeTokenJSON),
//		SubmitTokenName:  string(submitTokenNames), // 根据实际情况设置
//		SubmitTokenTexts: string(submitTokenTexts), // 根据实际情况设置
//		OriginUser:       library.UserID,
//		OriginCode:       library.Code,
//		OriginCodeLength: len(library.Code),
//		OriginID:         library.ID,
//		OriginTime:       library.SubmitTime,
//		OriginCodeToken:  string(originCodeTokenJSON),
//		OriginTokenName:  library.CodeTokenName,  // 根据实际情况设置
//		OriginTokenTexts: library.CodeTokenTexts, // 根据实际情况设置
//		CreateTime:       &now,
//		CreateUser:       "0", // 根据实际情况设置
//		UpdateTime:       &now,
//		UpdateUser:       "0",
//	}
//}

// 创建 TaskSimilarity 记录
func (w *Workspace) createTaskSimilarity(library model.DataLibrary, similarityScore float64) *model.TaskSimilarity {
	now := time.Now()

	// 处理 submitCodeToken - 确保是有效的JSON数组
	var submitCodeTokenJSON []byte
	if w.similarityMessage.CodeTokens == nil {
		submitCodeTokenJSON, _ = json.Marshal([]int{})
	} else {
		submitCodeTokenJSON, _ = json.Marshal(w.similarityMessage.CodeTokens)
	}

	// 处理 originCodeToken - 确保是有效的JSON数组
	var originCodeTokenJSON []byte
	if library.CodeToken == "" {
		// 如果CodeToken是空字符串，存储空数组
		originCodeTokenJSON, _ = json.Marshal([]int{})
	} else {
		// 如果CodeToken已经有值，需要验证它是否是有效的JSON数组
		// 如果不是有效的JSON数组，可能需要解析并重新序列化
		var tokenArray []int
		err := json.Unmarshal([]byte(library.CodeToken), &tokenArray)
		if err != nil {
			// 如果不是有效的JSON，尝试其他解析方式或存储为空数组
			originCodeTokenJSON, _ = json.Marshal([]int{})
		} else {
			// 重新序列化确保格式正确
			originCodeTokenJSON, _ = json.Marshal(tokenArray)
		}
	}

	// 处理其他JSON字段
	submitTokenNames, _ := json.Marshal(w.similarityMessage.CodeTokenNames)
	if submitTokenNames == nil {
		submitTokenNames, _ = json.Marshal([]string{})
	}

	submitTokenTexts, _ := json.Marshal(w.similarityMessage.CodeTokenTexts)
	if submitTokenTexts == nil {
		submitTokenTexts, _ = json.Marshal([]string{})
	}

	// 处理OriginTokenName和OriginTokenTexts
	var originTokenNameJSON, originTokenTextsJSON []byte
	if library.CodeTokenName == "" {
		originTokenNameJSON, _ = json.Marshal([]string{})
	} else {
		originTokenNameJSON = []byte(library.CodeTokenName)
	}

	if library.CodeTokenTexts == "" {
		originTokenTextsJSON, _ = json.Marshal([]string{})
	} else {
		originTokenTextsJSON = []byte(library.CodeTokenTexts)
	}

	return &model.TaskSimilarity{
		ID:               utils.GenerateID(),
		TaskID:           w.similarityMessage.TaskID,
		TaskType:         w.similarityMessage.IsSet,
		ProblemID:        w.similarityMessage.ProblemID,
		SetID:            w.similarityMessage.SetID,
		IsSet:            w.similarityMessage.IsSet,
		Language:         w.similarityMessage.Language,
		Similarity:       similarityScore,
		SubmitUser:       w.similarityMessage.UserID,
		SubmitCode:       w.similarityMessage.Code,
		SubmitCodeLength: w.similarityMessage.CodeLength,
		SubmitID:         w.similarityMessage.SubmitID,
		SubmitTime:       &now,
		SubmitCodeToken:  string(submitCodeTokenJSON),
		SubmitTokenName:  string(submitTokenNames),
		SubmitTokenTexts: string(submitTokenTexts),
		OriginUser:       library.UserID,
		OriginCode:       library.Code,
		OriginCodeLength: len(library.Code),
		OriginID:         library.ID,
		OriginTime:       library.SubmitTime,
		OriginCodeToken:  string(originCodeTokenJSON),
		OriginTokenName:  string(originTokenNameJSON),
		OriginTokenTexts: string(originTokenTextsJSON),
		CreateTime:       &now,
		CreateUser:       "0",
		UpdateTime:       &now,
		UpdateUser:       "0",
	}
}

// 在Workspace结构体中添加报告生成方法
func (w *Workspace) GenerateReport(similarityResult *mq.SimilarityResultMessage) error {
	logx.Infof("开始生成报告 taskID: %s", w.similarityMessage.TaskID)
	// 设置阈值
	threshold := w.similarityMessage.Threshold
	now := time.Now()
	// 创建TaskReports记录
	taskReport := &model.TaskReports{
		ID:                     utils.GenerateID(),
		TaskID:                 w.similarityMessage.TaskID,
		ProblemID:              w.similarityMessage.ProblemID,
		SetID:                  w.similarityMessage.SetID,
		SimilarityDistribution: "[]",
		DegreeStatistics:       "[]",
		Threshold:              threshold,
		CreateTime:             &now,
		UpdateTime:             &now,
	}
	similarityResult.ReportID = taskReport.ID

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
