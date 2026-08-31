package github.jiangbyte.io.oj.modules.judge.schedule;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.oj.config.OjProperties;
import github.jiangbyte.io.oj.modules.judge.node.entity.OjJudgeNode;
import github.jiangbyte.io.oj.modules.judge.node.mapper.OjJudgeNodeMapper;
import github.jiangbyte.io.oj.modules.judge.sandbox.SparkSandboxClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 执行机选机、占坑、熔断与在途释放。
 *
 * Author: Charlie
 */
@Component
@RequiredArgsConstructor
public class NodeScheduler {

    private final OjJudgeNodeMapper ojJudgeNodeMapper;
    private final OjProperties ojProperties;

    /**
     * 选择 Eligible 节点并 CAS 占坑；失败返回 null。
     */
    @Transactional
    public OjJudgeNode selectAndAcquire(String language, List<String> triedNodeIds, String requestId) {
        transitionOpenToHalfOpen();
        List<OjJudgeNode> eligible = listEligible(language);
        if (eligible.isEmpty()) {
            return null;
        }

        Set<String> tried = triedNodeIds == null ? Set.of() : new HashSet<>(triedNodeIds);
        List<OjJudgeNode> preferred = new ArrayList<>();
        List<OjJudgeNode> fallback = new ArrayList<>();
        for (OjJudgeNode node : eligible) {
            if (tried.contains(node.getId())) {
                fallback.add(node);
            } else {
                preferred.add(node);
            }
        }
        List<OjJudgeNode> ordered = preferred.isEmpty() ? fallback : preferred;
        ordered.sort(comparator(requestId));

        for (OjJudgeNode candidate : ordered) {
            OjJudgeNode acquired = tryAcquire(candidate);
            if (acquired != null) {
                return acquired;
            }
        }
        return null;
    }

    /** 是否存在 Eligible 节点（不占坑）。 */
    public boolean hasEligible(String language, List<String> triedNodeIds) {
        transitionOpenToHalfOpen();
        return !listEligible(language).isEmpty();
    }

    public void releaseInflight(String nodeId) {
        if (!StringUtils.hasText(nodeId)) {
            return;
        }
        ojJudgeNodeMapper.update(null, Wrappers.<OjJudgeNode>lambdaUpdate()
                .setSql("inflight_count = GREATEST(inflight_count - 1, 0)")
                .eq(OjJudgeNode::getId, nodeId));
    }

    @Transactional
    public void recordRunFailure(String nodeId, int httpStatus, String errorMessage) {
        if (SparkSandboxClient.isNodeInfrastructureFailure(httpStatus)) {
            markTransportFail(nodeId, errorMessage);
        } else {
            releaseInflight(nodeId);
        }
    }

    @Transactional
    public void markTransportFail(String nodeId, String errorMessage) {
        if (!StringUtils.hasText(nodeId)) {
            return;
        }
        OjJudgeNode node = ojJudgeNodeMapper.selectById(nodeId);
        if (node == null) {
            return;
        }
        int consecutive = (node.getConsecutiveFailCount() == null ? 0 : node.getConsecutiveFailCount()) + 1;
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        var update = Wrappers.<OjJudgeNode>lambdaUpdate()
                .setSql("inflight_count = GREATEST(inflight_count - 1, 0)")
                .setSql("total_transport_fail = IFNULL(total_transport_fail, 0) + 1")
                .set(OjJudgeNode::getConsecutiveFailCount, consecutive)
                .set(OjJudgeNode::getLastErrorAt, now)
                .set(OjJudgeNode::getLastErrorMessage, truncate(errorMessage, 512))
                .eq(OjJudgeNode::getId, nodeId);

        if (consecutive >= ojProperties.getJudge().getCircuitFailThreshold()) {
            update.set(OjJudgeNode::getCircuitState, "OPEN")
                    .set(OjJudgeNode::getRuntimeStatus, "UNHEALTHY")
                    .set(OjJudgeNode::getCircuitOpenedAt, now);
        } else {
            update.set(OjJudgeNode::getRuntimeStatus, "UNHEALTHY");
        }
        ojJudgeNodeMapper.update(null, update);
    }

    @Transactional
    public void markSuccess(String nodeId) {
        if (!StringUtils.hasText(nodeId)) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        ojJudgeNodeMapper.update(null, Wrappers.<OjJudgeNode>lambdaUpdate()
                .setSql("inflight_count = GREATEST(inflight_count - 1, 0)")
                .setSql("total_success = IFNULL(total_success, 0) + 1")
                .set(OjJudgeNode::getConsecutiveFailCount, 0)
                .set(OjJudgeNode::getCircuitState, "CLOSED")
                .set(OjJudgeNode::getRuntimeStatus, "ONLINE")
                .set(OjJudgeNode::getLastSuccessAt, now)
                .eq(OjJudgeNode::getId, nodeId));
    }

    /** OPEN 冷却后转 HALF_OPEN。 */
    public void transitionOpenToHalfOpen() {
        OffsetDateTime threshold = OffsetDateTime.now(ZoneOffset.UTC)
                .minusNanos(ojProperties.getJudge().getCircuitOpenMs() * 1_000_000L);
        ojJudgeNodeMapper.update(null, Wrappers.<OjJudgeNode>lambdaUpdate()
                .set(OjJudgeNode::getCircuitState, "HALF_OPEN")
                .set(OjJudgeNode::getRuntimeStatus, "ONLINE")
                .set(OjJudgeNode::getCircuitHalfOpenAt, OffsetDateTime.now(ZoneOffset.UTC))
                .eq(OjJudgeNode::getCircuitState, "OPEN")
                .le(OjJudgeNode::getCircuitOpenedAt, threshold));
    }

    private OjJudgeNode tryAcquire(OjJudgeNode candidate) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        boolean halfOpen = "HALF_OPEN".equals(candidate.getCircuitState());
        var update = Wrappers.<OjJudgeNode>lambdaUpdate()
                .setSql("inflight_count = inflight_count + 1")
                .setSql("total_dispatch = IFNULL(total_dispatch, 0) + 1")
                .set(OjJudgeNode::getLastSelectedAt, now)
                .eq(OjJudgeNode::getId, candidate.getId())
                .eq(OjJudgeNode::getEpoch, candidate.getEpoch())
                .eq(OjJudgeNode::getAdminStatus, "ENABLED")
                .eq(OjJudgeNode::getRuntimeStatus, "ONLINE")
                .in(OjJudgeNode::getCircuitState, List.of("CLOSED", "HALF_OPEN"));
        if (halfOpen) {
            update.eq(OjJudgeNode::getInflightCount, 0);
        } else {
            int max = candidate.getMaxConcurrency() == null ? 1 : candidate.getMaxConcurrency();
            update.lt(OjJudgeNode::getInflightCount, max);
        }
        int affected = ojJudgeNodeMapper.update(null, update);
        if (affected <= 0) {
            return null;
        }
        return ojJudgeNodeMapper.selectById(candidate.getId());
    }

    private List<OjJudgeNode> listEligible(String language) {
        List<OjJudgeNode> nodes = ojJudgeNodeMapper.selectList(
                Wrappers.<OjJudgeNode>lambdaQuery()
                        .eq(OjJudgeNode::getAdminStatus, "ENABLED")
                        .eq(OjJudgeNode::getRuntimeStatus, "ONLINE")
                        .in(OjJudgeNode::getCircuitState, List.of("CLOSED", "HALF_OPEN")));
        List<OjJudgeNode> result = new ArrayList<>();
        for (OjJudgeNode node : nodes) {
            if (!languageSupported(node, language)) {
                continue;
            }
            int inflight = node.getInflightCount() == null ? 0 : node.getInflightCount();
            int max = node.getMaxConcurrency() == null ? 1 : Math.max(node.getMaxConcurrency(), 1);
            if ("HALF_OPEN".equals(node.getCircuitState())) {
                if (inflight != 0) {
                    continue;
                }
            } else if (inflight >= max) {
                continue;
            }
            result.add(node);
        }
        return result;
    }

    private static boolean languageSupported(OjJudgeNode node, String language) {
        List<String> langs = node.getSupportedLanguages();
        if (langs == null || langs.isEmpty()) {
            // 空列表：兼容全部（心跳未上报时的兼容语义）
            return true;
        }
        if (!StringUtils.hasText(language)) {
            // 节点已声明语言表时，空白提交语言不可匹配
            return false;
        }
        return langs.stream().anyMatch(l -> language.equalsIgnoreCase(l));
    }

    private Comparator<OjJudgeNode> comparator(String requestId) {
        return Comparator
                .comparingDouble(this::loadScore)
                .thenComparingInt(n -> n.getPriority() == null ? 100 : n.getPriority())
                .thenComparingInt(n -> scatter(requestId, n.getId()));
    }

    private double loadScore(OjJudgeNode node) {
        int inflight = node.getInflightCount() == null ? 0 : node.getInflightCount();
        int max = Math.max(node.getMaxConcurrency() == null ? 1 : node.getMaxConcurrency(), 1);
        int weight = Math.max(node.getWeight() == null ? 1 : node.getWeight(), 1);
        return (inflight + 1.0d) / max / weight;
    }

    private static int scatter(String requestId, String nodeId) {
        int h = Objects.hash(requestId == null ? "" : requestId, nodeId == null ? "" : nodeId);
        return h & Integer.MAX_VALUE;
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
