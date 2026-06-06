package plugin_judge

import (
	"log"

	"hei-gin/api"
	"hei-gin/sdk/module"

	"hei-gin/plugins/plugin-judge/judgetypes"
	"hei-gin/plugins/plugin-judge/judge"
	"hei-gin/plugins/plugin-judge/sandbox"
	"hei-gin/plugins/plugin-judge/langconf"
	submissionApi "hei-gin/plugins/plugin-judge/submission/api/v1"
)

type JudgePlugin struct {
	module.NoopModule
	engine         *judge.JudgeEngine
	healthChecker  *sandbox.HealthChecker
}

func (p *JudgePlugin) Info() api.PluginInfo {
	return api.PluginInfo{
		Name:        "plugin-judge",
		Version:     "1.0.0",
		Description: "Judge plugin (sandbox, problem, testcase, submission, contest, problemset, tag)",
	}
}

func (p *JudgePlugin) Name() string { return "plugin-judge" }

func (p *JudgePlugin) Init() error {
	workerCount := judge.GetConfigInt("worker_count", 4)
	healthCheckInterval := judge.GetConfigInt("health_check_interval", 30)
	langconf.Init()
	maxRetry := judge.GetConfigInt("max_retry", 3)
	recoveryInterval := judge.GetConfigInt("recovery_interval", 120)

	// 初始化 gRPC 连接池
	instances, err := sandbox.ListActiveSandboxes()
	if err != nil {
		log.Printf("[plugin-judge] failed to load sandboxes: %v", err)
	} else {
		var backends []judgetypes.SandboxBackend
		for _, inst := range instances {
			backend, err := sandbox.NewBackend(inst.Endpoint, inst.Timeout)
			if err != nil {
				log.Printf("[plugin-judge] failed to connect to sandbox %s (%s): %v", inst.Name, inst.Endpoint, err)
				continue
			}
			backends = append(backends, backend)
		}
		sandbox.DefaultPool.ReplaceAll(backends)
		log.Printf("[plugin-judge] loaded %d sandbox backends", len(backends))
	}

	p.healthChecker = sandbox.NewHealthChecker(healthCheckInterval, maxRetry, recoveryInterval)
	p.engine = judge.NewJudgeEngine(workerCount)

	submissionApi.JudgeEngineRef = p.engine

	log.Printf("[plugin-judge] initialized (workers=%d, health_check=%ds)", workerCount, healthCheckInterval)
	return nil
}

func (p *JudgePlugin) Start() error {
	p.healthChecker.Start()
	p.engine.Start()
	log.Println("[plugin-judge] started")
	return nil
}

func (p *JudgePlugin) Stop() error {
	p.engine.Stop()
	p.healthChecker.Stop()
	log.Println("[plugin-judge] stopped")
	return nil
}

func init() {
	module.Register(&JudgePlugin{})
}
