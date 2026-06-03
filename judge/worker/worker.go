package worker

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"sync"
	"time"

	"hei-gin/config"
	"hei-gin/core/db"
	"hei-gin/core/utils"
	judgeclient "hei-gin/judge/client"
	"hei-gin/judge/queue"
	"hei-gin/judge/registry"
	judgerunner "hei-gin/judge/runner"
	judgetypes "hei-gin/judge/types"
	submissionModel "hei-gin/modules/judge/submission"
	contest "hei-gin/modules/judge/contest"
	sandboxModel "hei-gin/modules/judge/sandbox/model"
	problemModel "hei-gin/modules/judge/problem"
)

// Worker manages the judging goroutine pool.
type Worker struct {
	consumer  *QueueConsumer
	reg       *registry.Registry
	wg        sync.WaitGroup
	stopCh    chan struct{}
	busyCount int32
	mu        sync.Mutex
}

// NewWorker creates a new Worker.
func NewWorker() *Worker {
	w := &Worker{stopCh: make(chan struct{})}
	w.consumer = NewQueueConsumer(w)
	w.reg = registry.Global()
	return w
}

// Start begins the worker goroutines.
func (w *Worker) Start() error {
	if err := w.consumer.Start(); err != nil {
		return fmt.Errorf("failed to start queue consumer: %w", err)
	}
	concurrency := config.C.Judge.Concurrency
	if concurrency <= 0 {
		concurrency = 8
	}
	log.Printf("[Worker] Starting %d goroutines", concurrency)
	for i := 0; i < concurrency; i++ {
		w.wg.Add(1)
		go w.workLoop(i)
	}
	return nil
}

// Stop gracefully stops the worker pool.
func (w *Worker) Stop() {
	log.Println("[Worker] Stopping...")
	close(w.stopCh)
	done := make(chan struct{})
	go func() {
		w.wg.Wait()
		close(done)
	}()
	shutdownTimeout, err := time.ParseDuration(config.C.Judge.ShutdownTimeout)
	if err != nil {
		shutdownTimeout = 30 * time.Second
	}
	select {
	case <-done:
		log.Println("[Worker] All goroutines stopped")
	case <-time.After(shutdownTimeout):
		log.Println("[Worker] Shutdown timeout, forcing stop")
	}
}

func (w *Worker) consumerName() string {
	return fmt.Sprintf("worker-%d", time.Now().UnixNano())
}

func (w *Worker) workLoop(id int) {
	defer w.wg.Done()
	log.Printf("[Worker-%d] Started", id)
	for {
		select {
		case <-w.stopCh:
			log.Printf("[Worker-%d] Stopped", id)
			return
		default:
		}
		msg, err := w.consumer.Consume(context.Background())
		if err != nil {
			log.Printf("[Worker-%d] Consume error: %v", id, err)
			time.Sleep(1 * time.Second)
			continue
		}
		if msg == nil {
			w.processPending(id)
			time.Sleep(100 * time.Millisecond)
			continue
		}
		w.processMessage(id, msg.ID, msg.Values["data"])
	}
}

func (w *Worker) processPending(id int) {
	pending, err := w.consumer.ClaimPending(context.Background())
	if err != nil {
		return
	}
	for _, msg := range pending {
		w.processMessage(id, msg.ID, msg.Values["data"])
	}
}

func (w *Worker) processMessage(workerID int, msgID string, rawData interface{}) {
	w.mu.Lock()
	w.busyCount++
	w.mu.Unlock()
	defer func() {
		w.mu.Lock()
		w.busyCount--
		w.mu.Unlock()
	}()

	dataStr, ok := rawData.(string)
	if !ok {
		return
	}

	var qmsg queue.Message
	if err := json.Unmarshal([]byte(dataStr), &qmsg); err != nil {
		log.Printf("[Worker-%d] Failed to parse queue message: %v", workerID, err)
		w.consumer.Ack(context.Background(), msgID)
		return
	}

	log.Printf("[Worker-%d] Processing submission %s (problem %s, lang %s)",
		workerID, qmsg.SubmissionID, qmsg.ProblemID, qmsg.Language)

	ctx := context.Background()
	result := db.DB.WithContext(ctx).Model(&submissionModel.JudgeSubmission{}).
		Where("id = ? AND status = ?", qmsg.SubmissionID, submissionModel.StatusPending).
		Update("status", submissionModel.StatusJudging)
	if result.RowsAffected == 0 {
		log.Printf("[Worker-%d] Submission %s already judged, skipping", workerID, qmsg.SubmissionID)
		w.consumer.Ack(ctx, msgID)
		return
	}

	problem, testcases, subtasks, langCfg, depMap := w.loadJudgeData(ctx, qmsg)
	if problem == nil {
		db.DB.WithContext(ctx).Model(&submissionModel.JudgeSubmission{}).
			Where("id = ?", qmsg.SubmissionID).
			Updates(map[string]interface{}{
				"status":     submissionModel.StatusSystemError,
				"error_info": "Problem not found: " + qmsg.ProblemID,
			})
		w.consumer.Ack(ctx, msgID)
		return
	}

	var inst *sandboxModel.JudgeSandboxInstance
	for i := 0; i < 30; i++ {
		inst = w.reg.Scheduler().Pick(qmsg.SubmissionID)
		if inst != nil {
			break
		}
		if i == 0 {
			log.Printf("[Worker-%d] No sandbox instance available, retrying...", workerID)
		}
		time.Sleep(1 * time.Second)
	}
	if inst == nil {
		db.DB.WithContext(ctx).Model(&submissionModel.JudgeSubmission{}).
			Where("id = ?", qmsg.SubmissionID).
			Updates(map[string]interface{}{
				"status":     submissionModel.StatusSystemError,
				"error_info": "No available sandbox instance",
			})
		w.consumer.Ack(ctx, msgID)
		return
	}

	client, err := judgeclient.NewSandboxClient(inst.Addr)
	if err != nil {
		db.DB.WithContext(ctx).Model(&submissionModel.JudgeSubmission{}).
			Where("id = ?", qmsg.SubmissionID).
			Updates(map[string]interface{}{"status": submissionModel.StatusSystemError, "error_info": "Sandbox unavailable"})
		w.consumer.Ack(ctx, msgID)
		return
	}

	compileFileID := ""
	needsCompile := hasCompileArgs(qmsg.Language)

	if needsCompile {
		compileResult, err := client.Compile([]byte(qmsg.Code), qmsg.Language)
		if err != nil {
			db.DB.WithContext(ctx).Model(&submissionModel.JudgeSubmission{}).
				Where("id = ?", qmsg.SubmissionID).
				Updates(map[string]interface{}{"status": submissionModel.StatusCompileError, "error_info": fmt.Sprintf("Compilation failed: %v", err)})
			w.consumer.Ack(ctx, msgID)
			return
		}
		if compileResult.Status != "Accepted" {
			errMsg := fmt.Sprintf("Compile error: %s - %s", compileResult.Status, compileResult.Stderr)
			db.DB.WithContext(ctx).Model(&submissionModel.JudgeSubmission{}).
				Where("id = ?", qmsg.SubmissionID).
				Updates(map[string]interface{}{"status": submissionModel.StatusCompileError, "error_info": errMsg})
			w.consumer.Ack(ctx, msgID)
			return
		}
		compileFileID = compileResult.FileID
		log.Printf("[Worker-%d] Compile OK, fileID=%s", workerID, compileFileID)
	} else {
		log.Printf("[Worker-%d] No compilation needed for %s", workerID, qmsg.Language)
	}

	limits := judgetypes.ResolveLimits(problem, langCfg)
	jc := &judgerunner.JudgeContext{
		Problem:       problem,
		TestCases:     testcases,
		Subtasks:      subtasks,
		LangCfg:       langCfg,
		DepMap:        depMap,
		Limits:        limits,
		JudgeLanguage: qmsg.Language,
		ExecFileID:    compileFileID,
		SourceCode:    []byte(qmsg.Code),
		SourceName:    "main" + getExtension(qmsg.Language),
		Ctx:           ctx,
	}

	var judgeResult *judgerunner.RunResult
	func() {
		defer func() {
			if r := recover(); r != nil {
				errStr := fmt.Sprintf("Judge panic: %v", r)
				log.Printf("[Worker-%d] Panic judging submission %s: %v", workerID, qmsg.SubmissionID, r)
				judgeResult = &judgerunner.RunResult{
					Status:    submissionModel.StatusSystemError,
					ErrorInfo: errStr,
				}
			}
		}()
		judgeResult = judgerunner.Run(jc, client)
	}()

	for _, tcr := range judgeResult.Results {
		tcResult := submissionModel.JudgeTestcaseResult{
			ID:             utils.GenerateID(),
			SubmissionID:   qmsg.SubmissionID,
			Index:          tcr.Index,
			Status:         tcr.Status,
			Score:          tcr.Score,
			TimeUsed:       tcr.TimeUsed,
			MemoryUsed:     tcr.MemoryUsed,
			Output:         tcr.Output,
			ExpectedOutput: tcr.ExpectedOutput,
			Input:          tcr.Input,
		}
		db.DB.WithContext(ctx).Create(&tcResult)
	}

	db.DB.WithContext(ctx).Model(&submissionModel.JudgeSubmission{}).
		Where("id = ?", qmsg.SubmissionID).
		Updates(map[string]interface{}{
			"status":         judgeResult.Status,
			"score":          judgeResult.Score,
			"time_used":      judgeResult.TimeUsed,
			"memory_used":    judgeResult.MemoryUsed,
			"testcase_pass":  judgeResult.TestcasePass,
			"testcase_total": judgeResult.TestcaseTotal,
			"error_info":     judgeResult.ErrorInfo,
			"updated_at":     time.Now(),
		})

	// Update contest submission record if applicable
	if qmsg.ContestID != "" {
		contest.UpdateContestSubmissionStatus(ctx, qmsg.SubmissionID, judgeResult.Status, judgeResult.Score, judgeResult.TimeUsed, judgeResult.MemoryUsed)
	}

	log.Printf("[Worker-%d] Submission %s done: status=%s score=%d time=%dms mem=%dKB pass=%d/%d",
		workerID, qmsg.SubmissionID, judgeResult.Status, judgeResult.Score,
		judgeResult.TimeUsed/1000, judgeResult.MemoryUsed,
		judgeResult.TestcasePass, judgeResult.TestcaseTotal)

	w.consumer.Ack(ctx, msgID)
}

func (w *Worker) loadJudgeData(ctx context.Context, qmsg queue.Message) (
	*problemModel.JudgeProblem,
	[]problemModel.JudgeProblemTestCase,
	[]problemModel.JudgeProblemSubtask,
	*problemModel.JudgeProblemLanguage,
	map[string][]problemModel.JudgeProblemSubtaskDep,
) {
	var problem problemModel.JudgeProblem
	if err := db.DB.WithContext(ctx).First(&problem, "id = ?", qmsg.ProblemID).Error; err != nil {
		return nil, nil, nil, nil, nil
	}

	var testcases []problemModel.JudgeProblemTestCase
	db.DB.WithContext(ctx).Where("problem_id = ?", qmsg.ProblemID).Order("sort_order ASC").Find(&testcases)

	var subtasks []problemModel.JudgeProblemSubtask
	db.DB.WithContext(ctx).Where("problem_id = ?", qmsg.ProblemID).Order("sort_order ASC").Find(&subtasks)

	var langCfg problemModel.JudgeProblemLanguage
	err := db.DB.WithContext(ctx).
		Where("problem_id = ? AND language = ?", qmsg.ProblemID, qmsg.Language).
		First(&langCfg).Error
	var langCfgPtr *problemModel.JudgeProblemLanguage
	if err == nil {
		langCfgPtr = &langCfg
	}

	depMap := make(map[string][]problemModel.JudgeProblemSubtaskDep)
	if len(subtasks) > 0 {
		subtaskIDs := make([]string, len(subtasks))
		for i, st := range subtasks {
			subtaskIDs[i] = st.ID
		}
		var deps []problemModel.JudgeProblemSubtaskDep
		db.DB.WithContext(ctx).Where("subtask_id IN ?", subtaskIDs).Find(&deps)
		for _, dep := range deps {
			depMap[dep.SubtaskID] = append(depMap[dep.SubtaskID], dep)
		}
	}
	return &problem, testcases, subtasks, langCfgPtr, depMap
}

// BusyCount returns the number of busy worker goroutines.
func (w *Worker) BusyCount() int32 {
	w.mu.Lock()
	defer w.mu.Unlock()
	return w.busyCount
}

func hasCompileArgs(lang string) bool {
	for _, l := range config.C.Judge.Languages {
		if l.Name == lang {
			return len(l.CompileArgs) > 0
		}
	}
	return false
}

func getExtension(lang string) string {
	for _, l := range config.C.Judge.Languages {
		if l.Name == lang {
			return l.Extension
		}
	}
	return ".txt"
}
