package main

import (
	"encoding/json"
	"fmt"
	"io"
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"syscall"
	"time"

	"github.com/sirupsen/logrus"
)

// 定义资源限制结构体
type ContainerLimits struct {
	MemoryLimit int
	PidsLimit   int
	CPULimit    int
}

// 创建 cgroup
func createCGroup(cgroupDir, containerID string, limits ContainerLimits) error {
	// 创建 cgroup 目录
	cpuDir := filepath.Join(cgroupDir, "cpu", "gocker", containerID)
	memoryDir := filepath.Join(cgroupDir, "memory", "gocker", containerID)
	pidsDir := filepath.Join(cgroupDir, "pids", "gocker", containerID)

	for _, dir := range []string{cpuDir, memoryDir, pidsDir} {
		if err := os.MkdirAll(dir, 0755); err != nil {
			return fmt.Errorf("could not create cgroup directory %s: %v", dir, err)
		}
	}

	// 设置 CPU 限制
	period := 1000000
	cfsPeriodFile := filepath.Join(cpuDir, "cpu.cfs_period_us")
	cfsQuotaFile := filepath.Join(cpuDir, "cpu.cfs_quota_us")
	if err := ioutil.WriteFile(cfsPeriodFile, []byte(strconv.Itoa(period)), 0700); err != nil {
		return fmt.Errorf("could not set CFS Period: %v", err)
	}
	if err := ioutil.WriteFile(cfsQuotaFile, []byte(strconv.Itoa(limits.CPULimit*period)), 0700); err != nil {
		return fmt.Errorf("could not set CFS Quota: %v", err)
	}

	// 设置内存限制
	memoryLimitFile := filepath.Join(memoryDir, "memory.limit_in_bytes")
	if err := ioutil.WriteFile(memoryLimitFile, []byte(strconv.Itoa(limits.MemoryLimit)), 0700); err != nil {
		return fmt.Errorf("could not set memory limit: %v", err)
	}

	// 设置进程数量限制
	pidMaxFile := filepath.Join(pidsDir, "pids.max")
	if err := ioutil.WriteFile(pidMaxFile, []byte(strconv.Itoa(limits.PidsLimit)), 0700); err != nil {
		return fmt.Errorf("could not set pids limit: %v", err)
	}

	return nil
}

// 执行沙箱任务
func runSandboxedTask(codeFilePath string, limits ContainerLimits) (*int, error) {
	// 生成容器 ID
	containerID := generateID()

	// 创建 cgroup
	cgroupDir := "/sys/fs/cgroup/"
	if err := createCGroup(cgroupDir, containerID, limits); err != nil {
		return nil, fmt.Errorf("failed to create cgroup: %v", err)
	}
	defer func() {
		if err := os.RemoveAll(filepath.Join(cgroupDir, "cpu", "gocker", containerID)); err != nil {
			logrus.Warnf("could not delete cpu cgroup: %v", err)
		}
		if err := os.RemoveAll(filepath.Join(cgroupDir, "memory", "gocker", containerID)); err != nil {
			logrus.Warnf("could not delete memory cgroup: %v", err)
		}
		if err := os.RemoveAll(filepath.Join(cgroupDir, "pids", "gocker", containerID)); err != nil {
			logrus.Warnf("could not delete pids cgroup: %v", err)
		}
	}()

	// 创建临时目录作为沙箱根目录
	sandboxRoot, err := os.MkdirTemp("", "sandbox-")
	if err != nil {
		return nil, fmt.Errorf("failed to create sandbox root: %v", err)
	}
	defer os.RemoveAll(sandboxRoot)

	// 复制代码文件到沙箱目录
	sandboxCodeFilePath := filepath.Join(sandboxRoot, filepath.Base(codeFilePath))
	if err := copyFile(codeFilePath, sandboxCodeFilePath); err != nil {
		return nil, fmt.Errorf("failed to copy code file to sandbox: %v", err)
	}

	// 执行命令
	cmd := exec.Command("bash", "-c", fmt.Sprintf("cd %s && ./%s", sandboxRoot, filepath.Base(codeFilePath)))

	// 设置命名空间
	cmd.SysProcAttr = &syscall.SysProcAttr{
		Cloneflags: syscall.CLONE_NEWUTS |
			syscall.CLONE_NEWPID |
			syscall.CLONE_NEWNS,
		Unshareflags: syscall.CLONE_NEWNS,
	}

	// 设置用户权限
	cmd.SysProcAttr.Credential = &syscall.Credential{
		Uid: uint32(0),
		Gid: uint32(0),
	}

	// 设置标准输入输出
	cmd.Stdin = os.Stdin
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	// 启动命令
	if err := cmd.Start(); err != nil {
		return nil, fmt.Errorf("failed to start container: %v", err)
	}

	// 将进程添加到 cgroup
	pidsDir := filepath.Join(cgroupDir, "pids", "gocker", containerID)
	procsFile := filepath.Join(pidsDir, "cgroup.procs")
	if err := ioutil.WriteFile(procsFile, []byte(strconv.Itoa(cmd.Process.Pid)), 0700); err != nil {
		return nil, fmt.Errorf("failed to add process to cgroup: %v", err)
	}

	// 等待命令执行完成
	err = cmd.Wait()
	if e := exitCode(err); e != nil {
		return e, nil
	}
	logrus.Info("Could not retrieve exit code, assuming its value is 0")
	e := 0
	return &e, nil
}

// 生成唯一 ID
func generateID() string {
	id, err := uuid.NewRandom()
	if err != nil {
		logrus.Fatal("Enable to generate new UUID: ", err)
	}
	return strings.ReplaceAll(id.String(), "-", "")
}

// 复制文件
func copyFile(src, dst string) error {
	srcFile, err := os.Open(src)
	if err != nil {
		return err
	}
	defer srcFile.Close()

	dstFile, err := os.Create(dst)
	if err != nil {
		return err
	}
	defer dstFile.Close()

	_, err = io.Copy(dstFile, srcFile)
	return err
}

// 获取退出码
func exitCode(err error) *int {
	if exitError, ok := err.(*exec.ExitError); ok {
		e := exitError.ExitCode()
		return &e
	}
	return nil
}

func main() {
	if len(os.Args) < 2 {
		fmt.Println("Usage: go run main.go <code_file_path>")
		return
	}

	codeFilePath := os.Args[1]

	// 设置资源限制
	limits := ContainerLimits{
		MemoryLimit: 64 * 1024 * 1024, // 64MB
		PidsLimit:   32,
		CPULimit:    1,
	}

	// 执行沙箱任务
	exitCode, err := runSandboxedTask(codeFilePath, limits)
	if err != nil {
		logrus.Fatalf("Failed to run sandboxed task: %v", err)
	}

	fmt.Printf("Exit code: %d\n", *exitCode)
}
