package main

import (
	"context"
	"flag"
	"fmt"
	"log"
	"time"

	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"

	"hei-gin/config"
	"hei-gin/core/db"
	clientUser "hei-gin/modules/client/user"
	sysBanner "hei-gin/modules/sys/banner"
	sysConfigModel "hei-gin/modules/sys/config"
	sysDict "hei-gin/modules/sys/dict"
	sysFile "hei-gin/modules/sys/file"
	sysGroup "hei-gin/modules/sys/group"
	sysHome "hei-gin/modules/sys/home"
	sysLog "hei-gin/modules/sys/log"
	sysNotice "hei-gin/modules/sys/notice"
	sysOrg "hei-gin/modules/sys/org"
	sysPosition "hei-gin/modules/sys/position"
	sysResource "hei-gin/modules/sys/resource"
	sysRole "hei-gin/modules/sys/role"
	userModel "hei-gin/modules/sys/user"

	contestModel "hei-gin/modules/judge/contest"
	problemModel "hei-gin/modules/judge/problem"
	problemsetModel "hei-gin/modules/judge/problemset"
	sandboxModel "hei-gin/modules/judge/sandbox/model"
	submissionModel "hei-gin/modules/judge/submission"
)

func main() {
	skipSeed := flag.Bool("skip-seed", false, "skip seeding initial data")
	flag.Parse()

	// 1. Load config
	if err := config.Load("config.yaml"); err != nil {
		log.Fatalf("failed to load config: %v", err)
	}

	// 2. Connect to database
	if err := db.InitDB(); err != nil {
		log.Fatalf("failed to init database: %v", err)
	}
	defer db.Close()

	// 3. AutoMigrate all tables
	if err := autoMigrate(db.DB); err != nil {
		log.Fatalf("failed to apply migration: %v", err)
	}
	fmt.Println("✓ Migration applied successfully")

	if !*skipSeed {
		seedAdminUser(context.Background())
	}
}

func autoMigrate(db *gorm.DB) error {
	return db.AutoMigrate(
		// ===== Sys (Admin) =====
		&userModel.SysUser{},
		&userModel.RelUserRole{},
		&userModel.RelUserPermission{},
		&userModel.RelRolePermission{},
		&userModel.RelRoleResource{},
		&sysRole.SysRole{},
		&sysResource.SysResource{},
		&sysResource.SysModule{},
		&sysOrg.SysOrg{},
		&sysGroup.SysGroup{},
		&sysPosition.SysPosition{},
		&sysDict.SysDict{},
		&sysConfigModel.SysConfig{},
		&sysBanner.SysBanner{},
		&sysNotice.SysNotice{},
		&sysLog.SysLog{},
		&sysFile.SysFile{},

		// ===== Judge =====
		&problemModel.JudgeProblem{},
		&problemModel.JudgeProblemLanguage{},
		&problemModel.JudgeProblemSample{},
		&problemModel.JudgeProblemSubtask{},
		&problemModel.JudgeProblemSubtaskDep{},
		&problemModel.JudgeProblemTestCase{},
		&problemsetModel.JudgeProblemSet{},
		&problemsetModel.JudgeProblemSetItem{},
		&problemsetModel.JudgeProblemSetProgress{},
		&contestModel.JudgeContest{},
		&contestModel.JudgeContestProblem{},
		&contestModel.JudgeContestParticipant{},
		&contestModel.JudgeContestRankItem{},
		&sandboxModel.JudgeSandboxInstance{},
		&submissionModel.JudgeSubmission{},
		&submissionModel.JudgeTestcaseResult{},
		&sysHome.SysQuickAction{},
		// ===== Client =====
		&clientUser.ClientUser{},
	)
}

func seedAdminUser(ctx context.Context) {
	var count int64
	db.DB.Model(&userModel.SysUser{}).Where("username = ?", "admin").Count(&count)
	if count > 0 {
		fmt.Println("[Seed] Admin user already exists, skipped")
		return
	}

	now := time.Now()
	hashed, err := bcrypt.GenerateFromPassword([]byte("123456"), bcrypt.DefaultCost)
	if err != nil {
		log.Fatalf("failed to hash password: %v", err)
	}

	user := userModel.SysUser{
		Username:   strPtr("admin"),
		Password:   strPtr(string(hashed)),
		Nickname:   strPtr("超管"),
		Status:     "ACTIVE",
		LoginCount: 0,
		CreatedAt:  &now,
		UpdatedAt:  &now,
	}
	if err := db.DB.Create(&user).Error; err != nil {
		log.Fatalf("failed to create admin user: %v", err)
	}
	fmt.Println("[Seed] Admin user created (username: admin, password: 123456)")
}

func strPtr(s string) *string { return &s }
