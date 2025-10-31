// database/redis.go
package database

import (
	"context"
	"fmt"
	"github.com/redis/go-redis/v9"
	"judge-service/internal/config"
	"time"

	"github.com/zeromicro/go-zero/core/logx"
)

type RedisManager struct {
	Client *redis.Client
	ctx    context.Context
}

func NewRedisManager(c config.RedisConfig) (*RedisManager, error) {
	client := redis.NewClient(&redis.Options{
		Addr:     c.Host,
		Password: c.Password,
		DB:       c.DB,
		PoolSize: c.PoolSize,
	})

	ctx := context.Background()

	// 测试连接
	if err := client.Ping(ctx).Err(); err != nil {
		return nil, fmt.Errorf("Redis 连接失败: %v", err)
	}

	logx.Info("Redis 连接成功")
	return &RedisManager{
		Client: client,
		ctx:    ctx,
	}, nil
}

func (r *RedisManager) IsReady() bool {
	return r.Client != nil
}

func (r *RedisManager) Close() error {
	if r.Client != nil {
		return r.Client.Close()
	}
	return nil
}

// Set 设置缓存
func (r *RedisManager) Set(key string, value interface{}, expiration time.Duration) error {
	return r.Client.Set(r.ctx, key, value, expiration).Err()
}

// Get 获取缓存
func (r *RedisManager) Get(key string) (string, error) {
	return r.Client.Get(r.ctx, key).Result()
}

// Delete 删除缓存
func (r *RedisManager) Delete(key string) error {
	return r.Client.Del(r.ctx, key).Err()
}

// Exists 检查键是否存在
func (r *RedisManager) Exists(key string) (bool, error) {
	result, err := r.Client.Exists(r.ctx, key).Result()
	return result > 0, err
}
