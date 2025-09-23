package io.charlie.galaxy.utils.ranking;

import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 18/09/2025
 * @description 通用排行榜工具实现
 */
@Slf4j
@Component
public class RankingUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String RANKING_PREFIX = "ranking:";
    private static final String TIMESTAMP_SUFFIX = ":timestamp";

    public void addOrUpdate(String rankingKey, String entityId, double score) {
        this.addOrUpdate(rankingKey, entityId, score, new Date());
    }

    public void addOrUpdateAutoScore(String rankingKey, String entityId, double score) {
        this.addOrUpdateAutoScore(rankingKey, entityId, score, new Date());
    }

    public void addOrUpdate(String rankingKey, String entityId, double score, Date time) {
        String key = RANKING_PREFIX + rankingKey;
        String timestampKey = key + TIMESTAMP_SUFFIX;

        try {
            // 存储分数到ZSet
            redisTemplate.opsForZSet().add(key, entityId, score);

            // 存储时间戳到Hash
            redisTemplate.opsForHash().put(timestampKey, entityId, time.getTime());

            log.info("Added ranking: key={}, entity={}, score={}, time={}",
                    key, entityId, score, time);
        } catch (Exception e) {
            log.error("Failed to add ranking: key={}, entity={}", key, entityId, e);
            throw new RuntimeException("Failed to add ranking", e);
        }
    }

    public void addOrUpdateAutoScore(String rankingKey, String entityId, double score, Date time) {
        String key = RANKING_PREFIX + rankingKey;
        String timestampKey = key + TIMESTAMP_SUFFIX;

        try {
            // 使用incrementScore实现分数自增而不是直接设置
            Double newScore = redisTemplate.opsForZSet().incrementScore(key, entityId, score);

            // 存储或更新时间戳到Hash
            redisTemplate.opsForHash().put(timestampKey, entityId, time.getTime());

            log.info("Updated ranking: key={}, entity={}, addedScore={}, newScore={}, time={}",
                    key, entityId, score, newScore, time);
        } catch (Exception e) {
            log.error("Failed to update ranking: key={}, entity={}", key, entityId, e);
            throw new RuntimeException("Failed to update ranking", e);
        }
    }

    public RankingInfo getRanking(String rankingKey, String entityId) {
        String key = RANKING_PREFIX + rankingKey;
        String timestampKey = key + TIMESTAMP_SUFFIX;

        try {
            Double score = redisTemplate.opsForZSet().score(key, entityId);
            if (score == null) {
                return null;
            }

            // 获取排名（ZSet排名从0开始，这里转为从1开始）
            Long rank = redisTemplate.opsForZSet().reverseRank(key, entityId);
            if (rank == null) {
                rank = 0L;
            } else {
                rank = rank + 1; // 转为从1开始
            }

            // 获取时间戳
            Long timestamp = (Long) redisTemplate.opsForHash().get(timestampKey, entityId);
            Date updateTime = timestamp != null ? new Date(timestamp) : new Date();

            return new RankingInfo(rankingKey, entityId, score, rank, updateTime);
        } catch (Exception e) {
            log.error("Failed to get ranking: key={}, entity={}", key, entityId, e);
            throw new RuntimeException("Failed to get ranking", e);
        }
    }

    public List<RankingInfo> getTopNRanking(String rankingKey, int topN) {
        String key = RANKING_PREFIX + rankingKey;
        String timestampKey = key + TIMESTAMP_SUFFIX;

        try {
            // 获取前N名的数据（按分数从高到低）
            Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                    .reverseRangeWithScores(key, 0, topN - 1);

            if (tuples == null || tuples.isEmpty()) {
                return Collections.emptyList();
            }

            List<RankingInfo> rankingInfos = new ArrayList<>();
            int rank = 1;

            for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                String entityId = (String) tuple.getValue();
                Double score = tuple.getScore();

                // 获取时间戳
                Long timestamp = (Long) redisTemplate.opsForHash().get(timestampKey, entityId);
                Date updateTime = timestamp != null ? new Date(timestamp) : new Date();

                rankingInfos.add(new RankingInfo(
                        rankingKey,
                        entityId,
                        score,
                        (long) rank++,
                        updateTime
                ));
            }

            return rankingInfos;
        } catch (Exception e) {
            log.error("Failed to get top {} ranking: key={}", topN, key, e);
            throw new RuntimeException("Failed to get top ranking", e);
        }
    }

    public List<RankingInfo> getTopNRanking(String rankingKey, int topN, Date startTime, Date endTime) {
        String key = RANKING_PREFIX + rankingKey;
        String timestampKey = key + TIMESTAMP_SUFFIX;

        try {
            // 先根据时间范围筛选实体ID
            Set<String> filteredEntities = filterEntitiesByTime(timestampKey, startTime, endTime);

            if (filteredEntities.isEmpty()) {
                return Collections.emptyList();
            }

            // 创建临时ZSet存储筛选后的数据
            String tempKey = key + ":temp:" + UUID.randomUUID().toString().replace("-", "");
            try {
                // 批量添加筛选后的数据到临时ZSet
                for (String entity : filteredEntities) {
                    Double score = redisTemplate.opsForZSet().score(key, entity);
                    if (score != null) {
                        redisTemplate.opsForZSet().add(tempKey, entity, score);
                    }
                }

                // 从临时ZSet中获取前N名
                Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                        .reverseRangeWithScores(tempKey, 0, topN - 1);

                if (tuples == null || tuples.isEmpty()) {
                    return Collections.emptyList();
                }

                List<RankingInfo> rankingInfos = new ArrayList<>();
                int rank = 1;

                for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                    String entityId = (String) tuple.getValue();
                    Double score = tuple.getScore();

                    // 获取时间戳
                    Long timestamp = (Long) redisTemplate.opsForHash().get(timestampKey, entityId);
                    Date updateTime = timestamp != null ? new Date(timestamp) : new Date();

                    rankingInfos.add(new RankingInfo(
                            rankingKey,
                            entityId,
                            score,
                            (long) rank++,
                            updateTime
                    ));
                }

                return rankingInfos;
            } finally {
                // 删除临时key
                redisTemplate.delete(tempKey);
            }
        } catch (Exception e) {
            log.error("Failed to get top {} ranking with time filter: key={}, startTime={}, endTime={}",
                    topN, key, startTime, endTime, e);
            throw new RuntimeException("Failed to get top ranking with time filter", e);
        }
    }

    private Result.PageData<RankingInfo> getRankingPageInternal(String key, int page, int size,
                                                                ISortOrderEnum order,
                                                                Date startTime, Date endTime) {
        try {
            Long total = redisTemplate.opsForZSet().zCard(key);
            if (total == null || total == 0) {
                return Result.PageData.from(Collections.emptyList(), 0L, (long) size, (long) page);
            }

            int start = page * size;
            int end = start + size - 1;

            Set<ZSetOperations.TypedTuple<Object>> tuples;
            if (order != null && order.getValue().equals(ISortOrderEnum.ASCEND)) {
                tuples = redisTemplate.opsForZSet().rangeWithScores(key, start, end);
            } else {
                tuples = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
            }

            List<RankingInfo> rankingInfos = new ArrayList<>();
            if (tuples != null) {
                int currentRank = start + 1;
                String timestampKey = key.replace(RANKING_PREFIX, "") + TIMESTAMP_SUFFIX;

                for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                    String entityId = (String) tuple.getValue();
                    Double score = tuple.getScore();

                    // 获取时间戳
                    Long timestamp = (Long) redisTemplate.opsForHash().get(timestampKey, entityId);
                    Date updateTime = timestamp != null ? new Date(timestamp) : new Date();

                    rankingInfos.add(new RankingInfo(
                            key,
                            entityId,
                            score,
                            (long) currentRank++,
                            updateTime
                    ));
                }
            }

            return Result.PageData.from(rankingInfos, total, (long) size, (long) page);
        } catch (Exception e) {
            log.error("Failed to get ranking page: key={}, page={}, size={}", key, page, size, e);
            throw new RuntimeException("Failed to get ranking page", e);
        }
    }

    public Result.PageData<RankingInfo> getRankingPage(String rankingKey, int page, int size, ISortOrderEnum order) {
        String key = RANKING_PREFIX + rankingKey;
        return getRankingPageInternal(key, page, size, order, null, null);
    }

    /**
     * 根据entityId列表获取分页排名信息
     *
     * @param rankingKey 排行榜key
     * @param entityIds  实体ID列表
     * @param page       页码（从0开始）
     * @param size       每页大小
     * @param order      排序方式
     * @return 分页排名信息
     */
    public Result.PageData<RankingInfo> getRankingPageByEntityIds(String rankingKey,
                                                                  List<String> entityIds,
                                                                  int page,
                                                                  int size,
                                                                  ISortOrderEnum order) {
        String key = RANKING_PREFIX + rankingKey;
        String timestampKey = key + TIMESTAMP_SUFFIX;

        try {
            if (entityIds == null || entityIds.isEmpty()) {
                return Result.PageData.from(Collections.emptyList(), 0L, (long) size, (long) page);
            }

            // 获取所有实体的分数和排名
            List<RankingInfo> allRankingInfos = new ArrayList<>();
            for (String entityId : entityIds) {
                RankingInfo rankingInfo = getRanking(rankingKey, entityId);
                if (rankingInfo != null) {
                    allRankingInfos.add(rankingInfo);
                }
            }

            // 根据排序方式排序
            if (order != null && order.getValue().equals(ISortOrderEnum.ASCEND)) {
                // 升序：按排名从小到大
                allRankingInfos.sort(Comparator.comparingLong(RankingInfo::getRank));
            } else {
                // 降序：按排名从大到小（默认）
                allRankingInfos.sort(Comparator.comparingLong(RankingInfo::getRank).reversed());
            }

            // 计算分页
            int total = allRankingInfos.size();
            int start = page * size;
            int end = Math.min(start + size, total);

            if (start >= total) {
                return Result.PageData.from(Collections.emptyList(), (long) total, (long) size, (long) page);
            }

            List<RankingInfo> pageData = allRankingInfos.subList(start, end);

            return Result.PageData.from(pageData, (long) total, (long) size, (long) page);
        } catch (Exception e) {
            log.error("Failed to get ranking page by entityIds: key={}, entityIds={}, page={}, size={}",
                    key, entityIds, page, size, e);
            throw new RuntimeException("Failed to get ranking page by entityIds", e);
        }
    }

    /**
     * 根据entityId列表获取分页排名信息（带时间过滤）
     *
     * @param rankingKey 排行榜key
     * @param entityIds  实体ID列表
     * @param page       页码（从0开始）
     * @param size       每页大小
     * @param order      排序方式
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 分页排名信息
     */
    public Result.PageData<RankingInfo> getRankingPageByEntityIds(String rankingKey,
                                                                  List<String> entityIds,
                                                                  int page,
                                                                  int size,
                                                                  ISortOrderEnum order,
                                                                  Date startTime,
                                                                  Date endTime) {
        String key = RANKING_PREFIX + rankingKey;
        String timestampKey = key + TIMESTAMP_SUFFIX;

        try {
            if (entityIds == null || entityIds.isEmpty()) {
                return Result.PageData.from(Collections.emptyList(), 0L, (long) size, (long) page);
            }

            // 过滤符合时间范围的实体
            Set<String> filteredEntities = filterEntitiesByTime(timestampKey, startTime, endTime);
            List<String> validEntityIds = new ArrayList<>();

            for (String entityId : entityIds) {
                if (filteredEntities.contains(entityId)) {
                    validEntityIds.add(entityId);
                }
            }

            // 调用无时间过滤的方法
            return getRankingPageByEntityIds(rankingKey, validEntityIds, page, size, order);
        } catch (Exception e) {
            log.error("Failed to get ranking page by entityIds with time filter: key={}, entityIds={}, page={}, size={}, startTime={}, endTime={}",
                    key, entityIds, page, size, startTime, endTime, e);
            throw new RuntimeException("Failed to get ranking page by entityIds with time filter", e);
        }
    }


    private Set<String> filterEntitiesByTime(String timestampKey, Date startTime, Date endTime) {
        Map<Object, Object> allTimestamps = redisTemplate.opsForHash().entries(timestampKey);
        Set<String> filteredEntities = new HashSet<>();

        long startMillis = startTime != null ? startTime.getTime() : 0L;
        long endMillis = endTime != null ? endTime.getTime() : Long.MAX_VALUE;

        for (Map.Entry<Object, Object> entry : allTimestamps.entrySet()) {
            String entityId = (String) entry.getKey();
            long timestamp = ((Number) entry.getValue()).longValue();

            if (timestamp >= startMillis && timestamp <= endMillis) {
                filteredEntities.add(entityId);
            }
        }

        return filteredEntities;
    }

    public Result.PageData<RankingInfo> getRankingPageByTime(String rankingKey, int page, int size, Date startTime, Date endTime, ISortOrderEnum order) {
        String key = RANKING_PREFIX + rankingKey;
        String timestampKey = key + TIMESTAMP_SUFFIX;

        // 先根据时间范围筛选实体ID
        Set<String> filteredEntities = filterEntitiesByTime(timestampKey, startTime, endTime);

        if (filteredEntities.isEmpty()) {
            return Result.PageData.from(Collections.emptyList(), 0L, (long) size, (long) page);
        }

        // 创建临时ZSet存储筛选后的数据
        String tempKey = key + ":temp:" + UUID.randomUUID().toString().replace("-", "");
        try {
            // 批量添加筛选后的数据到临时ZSet
            for (String entity : filteredEntities) {
                Double score = redisTemplate.opsForZSet().score(key, entity);
                if (score != null) {
                    redisTemplate.opsForZSet().add(tempKey, entity, score);
                }
            }

            return getRankingPageInternal(tempKey, page, size, order, startTime, endTime);
        } finally {
            // 删除临时key
            redisTemplate.delete(tempKey);
        }


    }

    /**
     * 判断指定实体在排行榜中是否存在数据
     *
     * @param rankingKey 排行榜key
     * @param entityId   实体ID
     * @return true-存在数据，false-不存在数据
     */
    public boolean exists(String rankingKey, String entityId) {
        String key = RANKING_PREFIX + rankingKey;
        String timestampKey = key + TIMESTAMP_SUFFIX;

        try {
            // 检查ZSet中是否存在该实体的分数
            Double score = redisTemplate.opsForZSet().score(key, entityId);

            // 如果分数存在，再检查时间戳是否存在（可选，根据业务需求）
            // 如果只需要判断分数存在即可，可以省略时间戳检查
            if (score != null) {
                // 检查时间戳是否存在
                Long timestamp = (Long) redisTemplate.opsForHash().get(timestampKey, entityId);
                // 如果业务要求时间和分数都必须存在才算有效数据，可以加上 timestamp != null 的判断
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("Failed to check if entity exists in ranking: key={}, entity={}", key, entityId, e);
            throw new RuntimeException("Failed to check entity existence in ranking", e);
        }
    }

    /**
     * 判断指定实体在排行榜中是否存在数据（带时间有效性检查）
     *
     * @param rankingKey 排行榜key
     * @param entityId   实体ID
     * @param validTime  有效时间点，如果实体的更新时间早于此时间，则认为数据已过期
     * @return true-存在有效数据，false-不存在或数据已过期
     */
    public boolean exists(String rankingKey, String entityId, Date validTime) {
        String key = RANKING_PREFIX + rankingKey;
        String timestampKey = key + TIMESTAMP_SUFFIX;

        try {
            // 检查ZSet中是否存在该实体的分数
            Double score = redisTemplate.opsForZSet().score(key, entityId);
            if (score == null) {
                return false;
            }

            // 检查时间戳是否存在且有效
            Long timestamp = (Long) redisTemplate.opsForHash().get(timestampKey, entityId);
            if (timestamp == null) {
                return false;
            }

            // 检查时间是否有效（实体的更新时间晚于或等于有效时间点）
            Date entityUpdateTime = new Date(timestamp);
            return !entityUpdateTime.before(validTime);
        } catch (Exception e) {
            log.error("Failed to check if entity exists with time validation: key={}, entity={}, validTime={}",
                    key, entityId, validTime, e);
            throw new RuntimeException("Failed to check entity existence with time validation", e);
        }
    }

    /**
     * 批量判断多个实体在排行榜中是否存在数据
     *
     * @param rankingKey 排行榜key
     * @param entityIds  实体ID列表
     * @return 存在的实体ID列表
     */
    public List<String> existsBatch(String rankingKey, List<String> entityIds) {
        String key = RANKING_PREFIX + rankingKey;

        try {
            if (entityIds == null || entityIds.isEmpty()) {
                return Collections.emptyList();
            }

            List<String> existingEntities = new ArrayList<>();
            for (String entityId : entityIds) {
                Double score = redisTemplate.opsForZSet().score(key, entityId);
                if (score != null) {
                    existingEntities.add(entityId);
                }
            }

            return existingEntities;
        } catch (Exception e) {
            log.error("Failed to check batch entity existence in ranking: key={}, entityIds={}",
                    key, entityIds, e);
            throw new RuntimeException("Failed to check batch entity existence in ranking", e);
        }
    }

    public void cleanupOldData(String rankingKey, Date expireBefore) {
        String key = RANKING_PREFIX + rankingKey;
        String timestampKey = key + TIMESTAMP_SUFFIX;

        Map<Object, Object> allTimestamps = redisTemplate.opsForHash().entries(timestampKey);
        long expireMillis = expireBefore.getTime();

        for (Map.Entry<Object, Object> entry : allTimestamps.entrySet()) {
            String entityId = (String) entry.getKey();
            long timestamp = ((Number) entry.getValue()).longValue();

            if (timestamp < expireMillis) {
                // 从ZSet中移除
                redisTemplate.opsForZSet().remove(key, entityId);
                // 从Hash中移除时间戳
                redisTemplate.opsForHash().delete(timestampKey, entityId);
            }
        }
    }
}
