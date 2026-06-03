package sandbox

// SandboxVO is the view object for a sandbox instance.
type SandboxVO struct {
	ID            string  `json:"id"`
	Addr          string  `json:"addr"`
	Status        string  `json:"status"`
	Weight        int     `json:"weight"`
	Version       string  `json:"version"`
	CPUCores      int     `json:"cpu_cores"`
	MemoryTotal   int64   `json:"memory_total"`
	LastHeartbeat string  `json:"last_heartbeat"`
	FailureCount  int     `json:"failure_count"`
	CreatedAt     *string `json:"created_at"`
	UpdatedAt     *string `json:"updated_at"`
}

// RegisterParam is the request body for registering a sandbox instance.
type RegisterParam struct {
	Addr        string `json:"addr"`
	Version     string `json:"version"`
	CPUCores    int    `json:"cpu_cores"`
	MemoryTotal int64  `json:"memory_total"`
	Weight      int    `json:"weight"`
}

// HeartbeatParam is the request body for heartbeat.
type HeartbeatParam struct {
	Addr string `json:"addr"`
}
