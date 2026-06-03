package sandbox

import (
	"context"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/utils"
	"hei-gin/judge/registry"
	sandboxModel "hei-gin/modules/judge/sandbox/model"

	"github.com/gin-gonic/gin"
)

func Register(c *gin.Context, param *RegisterParam) {
	now := time.Now()
	entity := sandboxModel.JudgeSandboxInstance{
		ID:            utils.GenerateID(),
		Addr:          param.Addr,
		Version:       param.Version,
		CPUCores:      param.CPUCores,
		MemoryTotal:   param.MemoryTotal,
		Weight:        param.Weight,
		LastHeartbeat: now,
		CreatedAt:     &now,
		UpdatedAt:     &now,
	}
	if err := registry.Global().Register(&entity); err != nil {
		panic(exception.NewBusinessError("注册沙箱失败: "+err.Error(), 500))
	}
}

func Heartbeat(c *gin.Context, addr string) {
	if err := registry.Global().Heartbeat(addr); err != nil {
		panic(exception.NewBusinessError("心跳上报失败: "+err.Error(), 500))
	}
}

func Unregister(c *gin.Context, addr string) {
	if err := registry.Global().Unregister(addr); err != nil {
		panic(exception.NewBusinessError("摘除沙箱失败: "+err.Error(), 500))
	}
}

func ListInstances(c *gin.Context) []*SandboxVO {
	ctx := context.Background()
	var instances []sandboxModel.JudgeSandboxInstance
	db.DB.WithContext(ctx).Order("created_at DESC").Find(&instances)

	vos := make([]*SandboxVO, 0, len(instances))
	for _, inst := range instances {
		vos = append(vos, entToVO(&inst))
	}
	return vos
}

func entToVO(entity *sandboxModel.JudgeSandboxInstance) *SandboxVO {
	vo := &SandboxVO{
		ID:            entity.ID,
		Addr:          entity.Addr,
		Status:        entity.Status,
		Weight:        entity.Weight,
		Version:       entity.Version,
		CPUCores:      entity.CPUCores,
		MemoryTotal:   entity.MemoryTotal,
		LastHeartbeat: entity.LastHeartbeat.Format("2006-01-02 15:04:05"),
		FailureCount:  entity.FailureCount,
	}
	if entity.CreatedAt != nil {
		s := entity.CreatedAt.Format("2006-01-02 15:04:05")
		vo.CreatedAt = &s
	}
	if entity.UpdatedAt != nil {
		s := entity.UpdatedAt.Format("2006-01-02 15:04:05")
		vo.UpdatedAt = &s
	}
	return vo
}
