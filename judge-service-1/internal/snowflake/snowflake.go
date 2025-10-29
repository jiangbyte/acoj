package snowflake

import (
	"errors"
	"fmt"
	"net"
	"os"
	"strconv"
	"sync"
	"time"
)

const (
	// 雪花算法参数 (符合MyBatis-Plus默认配置)
	twepoch          = int64(1288834974657) // 起始时间戳 (2010-11-04 09:42:54 UTC)
	workerIDBits     = uint(5)              // 机器ID位数
	datacenterIDBits = uint(5)              // 数据中心ID位数
	sequenceBits     = uint(12)             // 序列号位数

	maxWorkerID     = int64(-1) ^ (int64(-1) << workerIDBits)
	maxDatacenterID = int64(-1) ^ (int64(-1) << datacenterIDBits)
	maxSequence     = int64(-1) ^ (int64(-1) << sequenceBits)

	workerIDShift      = sequenceBits
	datacenterIDShift  = sequenceBits + workerIDBits
	timestampLeftShift = sequenceBits + workerIDBits + datacenterIDBits
)

type Snowflake struct {
	mu            sync.Mutex
	lastTimestamp int64
	workerID      int64
	datacenterID  int64
	sequence      int64
}

var (
	instance *Snowflake
	once     sync.Once
)

// NewSnowflake 创建雪花算法实例
func NewSnowflake() *Snowflake {
	workerID := getWorkerID()
	datacenterID := getDatacenterID()

	return &Snowflake{
		workerID:     workerID,
		datacenterID: datacenterID,
	}
}

// getWorkerID 获取工作节点ID
func getWorkerID() int64 {
	// 1. 从环境变量获取
	if envID := os.Getenv("SNOWFLAKE_WORKER_ID"); envID != "" {
		if id, err := strconv.ParseInt(envID, 10, 64); err == nil && id <= maxWorkerID {
			return id
		}
	}

	// 2. 基于IP地址生成
	if interfaces, err := net.Interfaces(); err == nil {
		for _, iface := range interfaces {
			if iface.Flags&net.FlagUp != 0 && iface.Flags&net.FlagLoopback == 0 {
				if addrs, err := iface.Addrs(); err == nil {
					for _, addr := range addrs {
						if ipnet, ok := addr.(*net.IPNet); ok && !ipnet.IP.IsLoopback() {
							if ipnet.IP.To4() != nil {
								ip := ipnet.IP.To4()
								return int64(ip[3]) & maxWorkerID
							}
						}
					}
				}
			}
		}
	}

	// 3. 默认值
	return 1
}

// getDatacenterID 获取数据中心ID
func getDatacenterID() int64 {
	// 1. 从环境变量获取
	if envID := os.Getenv("SNOWFLAKE_DATACENTER_ID"); envID != "" {
		if id, err := strconv.ParseInt(envID, 10, 64); err == nil && id <= maxDatacenterID {
			return id
		}
	}

	// 2. 基于主机名生成
	hostname, err := os.Hostname()
	if err == nil && hostname != "" {
		var hash int64
		for i := 0; i < len(hostname); i++ {
			hash = (hash << 5) - hash + int64(hostname[i])
		}
		return (hash & maxDatacenterID)
	}

	// 3. 默认值
	return 1
}

// tilNextMillis 等待下一毫秒
func (s *Snowflake) tilNextMillis(lastTimestamp int64) int64 {
	timestamp := timeGen()
	for timestamp <= lastTimestamp {
		timestamp = timeGen()
	}
	return timestamp
}

// timeGen 生成当前时间戳（毫秒）
func timeGen() int64 {
	return time.Now().UnixNano() / 1e6
}

// GenerateID 生成64位ID（返回int64）
func (s *Snowflake) GenerateID() (int64, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	timestamp := timeGen()

	if timestamp < s.lastTimestamp {
		return 0, errors.New("clock moved backwards")
	}

	if s.lastTimestamp == timestamp {
		s.sequence = (s.sequence + 1) & maxSequence
		if s.sequence == 0 {
			timestamp = s.tilNextMillis(s.lastTimestamp)
		}
	} else {
		s.sequence = 0
	}

	s.lastTimestamp = timestamp

	return ((timestamp - twepoch) << timestampLeftShift) |
		(s.datacenterID << datacenterIDShift) |
		(s.workerID << workerIDShift) |
		s.sequence, nil
}

// GenerateIDString 生成字符串格式的ID（类似MyBatis-Plus格式）
func (s *Snowflake) GenerateIDString() string {
	id, err := s.GenerateID()
	if err != nil {
		// 降级方案：使用纳秒时间戳
		return fmt.Sprintf("%d", time.Now().UnixNano())
	}
	return fmt.Sprintf("%d", id)
}

// GetInstance 获取单例实例
func GetInstance() *Snowflake {
	once.Do(func() {
		instance = NewSnowflake()
	})
	return instance
}

// GenerateID 包级函数，生成字符串ID
func GenerateID() string {
	return GetInstance().GenerateIDString()
}
