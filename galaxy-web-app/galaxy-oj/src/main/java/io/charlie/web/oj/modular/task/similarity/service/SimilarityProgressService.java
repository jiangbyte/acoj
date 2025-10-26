package io.charlie.web.oj.modular.task.similarity.service;

import io.charlie.web.oj.modular.task.similarity.dto.SimilarityProgressDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class SimilarityProgressService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String PROGRESS_KEY_PREFIX = "similarity:progress:";
    private static final long EXPIRE_TIME = 24 * 60 * 60; // 24小时
    
    /**
     * 初始化进度
     */
    public void initProgress(String taskId, int totalCount) {
        String key = getProgressKey(taskId);
        Map<String, Object> progress = new HashMap<>();
        progress.put("taskId", taskId);
        progress.put("totalCount", totalCount);
        progress.put("processedCount", 0);
        progress.put("status", "processing"); // processing, completed, failed
        progress.put("startTime", System.currentTimeMillis());
        progress.put("currentStep", "开始处理");
        
        redisTemplate.opsForHash().putAll(key, progress);
        redisTemplate.expire(key, EXPIRE_TIME, TimeUnit.SECONDS);
    }
    
    /**
     * 更新进度
     */
    public void updateProgress(String taskId, int processedCount, String currentStep) {
        String key = getProgressKey(taskId);
        redisTemplate.opsForHash().put(key, "processedCount", processedCount);
        redisTemplate.opsForHash().put(key, "currentStep", currentStep);
        redisTemplate.opsForHash().put(key, "updateTime", System.currentTimeMillis());
    }
    
    /**
     * 完成进度
     */
    public void completeProgress(String taskId) {
        String key = getProgressKey(taskId);
        redisTemplate.opsForHash().put(key, "status", "completed");
        redisTemplate.opsForHash().put(key, "endTime", System.currentTimeMillis());
    }
    
    /**
     * 失败进度
     */
    public void failProgress(String taskId, String errorMessage) {
        String key = getProgressKey(taskId);
        redisTemplate.opsForHash().put(key, "status", "failed");
        redisTemplate.opsForHash().put(key, "errorMessage", errorMessage);
        redisTemplate.opsForHash().put(key, "endTime", System.currentTimeMillis());
    }
    
    /**
     * 查询进度
     */
    public SimilarityProgressDto getProgress(String taskId) {
        String key = getProgressKey(taskId);
        Map<Object, Object> progressMap = redisTemplate.opsForHash().entries(key);
        
        if (progressMap.isEmpty()) {
            return null;
        }
        
        SimilarityProgressDto progress = new SimilarityProgressDto();
        progress.setTaskId((String) progressMap.get("taskId"));
        progress.setTotalCount(Integer.parseInt(progressMap.get("totalCount").toString()));
        progress.setProcessedCount(Integer.parseInt(progressMap.get("processedCount").toString()));
        progress.setStatus((String) progressMap.get("status"));
        progress.setCurrentStep((String) progressMap.get("currentStep"));
        progress.setProgress(calculateProgress(progress.getProcessedCount(), progress.getTotalCount()));
        
        if (progressMap.get("startTime") != null) {
            progress.setStartTime(new Date(Long.parseLong(progressMap.get("startTime").toString())));
        }
        if (progressMap.get("endTime") != null) {
            progress.setEndTime(new Date(Long.parseLong(progressMap.get("endTime").toString())));
        }
        if (progressMap.get("errorMessage") != null) {
            progress.setErrorMessage((String) progressMap.get("errorMessage"));
        }
        
        return progress;
    }
    
    private String getProgressKey(String taskId) {
        return PROGRESS_KEY_PREFIX + taskId;
    }
    
    private double calculateProgress(int processed, int total) {
        if (total == 0) return 0;
        return Math.round((processed * 100.0 / total) * 100.0) / 100.0;
    }
}