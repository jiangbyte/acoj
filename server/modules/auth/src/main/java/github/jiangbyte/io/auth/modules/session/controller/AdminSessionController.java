package github.jiangbyte.io.auth.modules.session.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import github.jiangbyte.io.common.satoken.StpKit;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import github.jiangbyte.io.auth.modules.session.param.SessionExitParam;
import github.jiangbyte.io.auth.modules.session.param.SessionPageParam;
import github.jiangbyte.io.auth.modules.session.param.SessionTokenExitParam;
import github.jiangbyte.io.auth.modules.session.result.SessionAccountResult;
import github.jiangbyte.io.auth.modules.session.result.SessionAnalysisResult;
import github.jiangbyte.io.auth.modules.session.result.SessionTokenResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.common.log.annotation.OperationAudit;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.common.satoken.model.LoginUser;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端在线会话 API：统计、分页查询、Token 列表及按账号/Token 强制下线。
 *
 * Author: Charlie
 */
@Tag(name = "管理端在线会话 API")
@RestController
@RequestMapping("/api")
public class AdminSessionController {

    /** 过滤扫描硬顶，避免全量 hydrate。 */
    private static final int SCAN_HARD_CAP = 5000;
    private static final int SCAN_BATCH = 200;
    /** 分析指标扫描上限（近似值）。 */
    private static final int ANALYSIS_SCAN_CAP = 5000;

    /** 汇总在线账号/Token 数量及近一小时新增等分析指标（轻量：不读 LoginUser）。 */
    @Operation(summary = "汇总在线账号/Token 数量及近一小时新增等分析指标（轻量：不读 LoginUser）。")
    @GetMapping("/v1/admin/auth/sessions/analysis")
    @SaCheckPermission(value = "auth:session:analysis", type = StpKit.TYPE_ADMIN)
    public ApiResponse<SessionAnalysisResult> analysis() {
        SessionAnalysisResult result = new SessionAnalysisResult();
        AnalysisAccumulator admin = analyzeLogic(StpKit.ADMIN);
        AnalysisAccumulator portal = analyzeLogic(StpKit.PORTAL);
        result.setOnlineAccountCount(admin.accountCount + portal.accountCount);
        result.setOnlineTokenCount(admin.tokenCount + portal.tokenCount);
        result.setAdminAccountCount(admin.accountCount);
        result.setPortalAccountCount(portal.accountCount);
        result.setOneHourNewCount(admin.oneHourNew + portal.oneHourNew);
        result.setMaxTokenCount(Math.max(admin.maxToken, portal.maxToken));
        return ApiResponse.ok(result);
    }

    /**
     * 分页查询在线会话。无过滤时按 Sa-Token 分页检索并仅 hydrate 当前页；
     * 有过滤时有限向前扫描（硬顶 {@link #SCAN_HARD_CAP}），total 为扫描范围内匹配数（可能近似）。
     */
    @Operation(summary = "分页查询在线会话。")
    @GetMapping("/v1/admin/auth/sessions/page")
    @SaCheckPermission(value = "auth:session:page", type = StpKit.TYPE_ADMIN)
    public ApiResponse<Page<SessionAccountResult>> page(@Valid @ModelAttribute SessionPageParam query) {
        int current = Math.max(1, query.getCurrent());
        int size = Math.max(1, Math.min(query.getSize(), 200));
        boolean filtered = StringUtils.hasText(query.getAccountId())
                || StringUtils.hasText(query.getAccount())
                || StringUtils.hasText(query.getIp())
                || StringUtils.hasText(query.getKeyword());

        List<LogicSource> sources = resolveSources(query.getAccountType());
        if (sources.isEmpty()) {
            Page<SessionAccountResult> empty = new Page<>(current, size, 0);
            empty.setRecords(List.of());
            return ApiResponse.ok(empty);
        }

        if (!filtered && sources.size() == 1) {
            return ApiResponse.ok(pageSingleSource(sources.getFirst(), current, size));
        }

        // 多端合并或带过滤：有限扫描后内存过滤/分页
        List<SessionAccountResult> matched = new ArrayList<>();
        int scanned = 0;
        boolean hasMore = false;
        for (LogicSource source : sources) {
            int offset = 0;
            while (scanned < SCAN_HARD_CAP) {
                int batch = Math.min(SCAN_BATCH, SCAN_HARD_CAP - scanned);
                List<String> sessionIds = source.stpLogic().searchSessionId("", offset, batch, false);
                if (sessionIds == null || sessionIds.isEmpty()) {
                    break;
                }
                for (String sessionId : sessionIds) {
                    SessionAccountResult item = hydrateSession(source.stpLogic(), sessionId, source.accountType());
                    if (item == null) {
                        continue;
                    }
                    if (matchesFilter(item, query)) {
                        matched.add(item);
                    }
                }
                scanned += sessionIds.size();
                offset += sessionIds.size();
                if (sessionIds.size() < batch) {
                    break;
                }
                if (scanned >= SCAN_HARD_CAP) {
                    hasMore = true;
                    break;
                }
            }
            if (hasMore) {
                break;
            }
        }

        int from = Math.max(0, (current - 1) * size);
        int to = Math.min(matched.size(), from + size);
        List<SessionAccountResult> records = from >= matched.size() ? List.of() : matched.subList(from, to);
        // total 为扫描窗口内匹配数；触顶时前端可视为近似
        long total = matched.size();
        if (hasMore) {
            total = Math.max(total, (long) current * size + (records.size() < size ? 0 : 1));
        }
        Page<SessionAccountResult> page = new Page<>(current, size, total);
        page.setRecords(records);
        return ApiResponse.ok(page);
    }

    /** 列出指定账号下的全部 Token 会话。 */
    @Operation(summary = "列出指定账号下的全部 Token 会话。")
    @GetMapping("/v1/admin/auth/sessions/tokens")
    @SaCheckPermission(value = "auth:session:tokenlist", type = StpKit.TYPE_ADMIN)
    public ApiResponse<List<SessionTokenResult>> tokens(
            @RequestParam String accountId,
            @RequestParam(required = false) String accountType) {
        return ApiResponse.ok(listTokens(resolveLogic(accountType), accountId, accountType).tokens());
    }

    /** 按账号批量强制下线（踢出全部 Token）。 */
    @Operation(summary = "按账号批量强制下线（踢出全部 Token）。")
    @PostMapping("/v1/admin/auth/sessions/exit")
    @SaCheckPermission(value = "auth:session:exit", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "auth_session", action = "exit")
    public ApiResponse<Void> exit(@Valid @RequestBody SessionExitParam request) {
        List<String> targets = request.getTargets().stream()
                .map(target -> {
                    String accountId = target.getAccountId();
                    if (!StringUtils.hasText(target.getAccountType())) {
                        return accountId;
                    }
                    return accountId + "（" + target.getAccountType() + "）";
                })
                .collect(Collectors.toList());
        AuditSnapshots.after(Map.of("账号", targets));
        for (SessionExitParam.Target target : request.getTargets()) {
            resolveLogic(target.getAccountType()).logout(target.getAccountId());
        }
        return ApiResponse.ok();
    }

    /** 按 Token 值批量强制下线（同时尝试管理端与门户 StpLogic）。 */
    @Operation(summary = "按 Token 值批量强制下线（同时尝试管理端与门户 StpLogic）。")
    @PostMapping("/v1/admin/auth/sessions/token/exit")
    @SaCheckPermission(value = "auth:session:tokenexit", type = StpKit.TYPE_ADMIN)
    @OperationAudit(resourceType = "auth_session", action = "token_exit")
    public ApiResponse<Void> tokenExit(@Valid @RequestBody SessionTokenExitParam request) {
        LinkedHashSet<String> tokens = new LinkedHashSet<>(request.getTokens());
        List<String> maskedTokens = tokens.stream()
                .filter(StringUtils::hasText)
                .map(AdminSessionController::maskToken)
                .collect(Collectors.toList());
        AuditSnapshots.after(Map.of("会话", maskedTokens));
        for (String token : tokens) {
            if (!StringUtils.hasText(token)) {
                continue;
            }
            StpKit.ADMIN.logoutByTokenValue(token);
            StpKit.PORTAL.logoutByTokenValue(token);
        }
        return ApiResponse.ok();
    }

    private Page<SessionAccountResult> pageSingleSource(LogicSource source, int current, int size) {
        int start = (current - 1) * size;
        List<String> sessionIds = source.stpLogic().searchSessionId("", start, size, false);
        if (sessionIds == null) {
            sessionIds = List.of();
        }
        List<SessionAccountResult> records = new ArrayList<>(sessionIds.size());
        for (String sessionId : sessionIds) {
            SessionAccountResult item = hydrateSession(source.stpLogic(), sessionId, source.accountType());
            if (item != null) {
                records.add(item);
            }
        }
        // 探测是否还有下一页，用于近似 total
        long total = start + records.size();
        if (sessionIds.size() >= size) {
            List<String> probe = source.stpLogic().searchSessionId("", start + size, 1, false);
            if (probe != null && !probe.isEmpty()) {
                total = start + size + 1;
            }
        }
        Page<SessionAccountResult> page = new Page<>(current, size, total);
        page.setRecords(records);
        return page;
    }

    private List<LogicSource> resolveSources(String accountType) {
        List<LogicSource> sources = new ArrayList<>(2);
        if (!StringUtils.hasText(accountType) || AccountType.ADMIN.name().equalsIgnoreCase(accountType)) {
            sources.add(new LogicSource(StpKit.ADMIN, AccountType.ADMIN.name()));
        }
        if (!StringUtils.hasText(accountType) || AccountType.PORTAL.name().equalsIgnoreCase(accountType)) {
            sources.add(new LogicSource(StpKit.PORTAL, AccountType.PORTAL.name()));
        }
        return sources;
    }

    private SessionAccountResult hydrateSession(StpLogic stpLogic, String sessionId, String accountType) {
        SaSession accountSession = stpLogic.getSessionBySessionId(sessionId);
        Object loginId = accountSession == null ? null : accountSession.getLoginId();
        if (loginId == null) {
            return null;
        }
        String accountId = String.valueOf(loginId);
        TokenBundle bundle = listTokens(stpLogic, accountId, accountType);
        List<SessionTokenResult> tokens = bundle.tokens();
        SessionAccountResult item = new SessionAccountResult();
        item.setAccountId(accountId);
        item.setAccountType(accountType);
        item.setTokenCount(tokens.size());
        item.setTokens(tokens);
        if (!tokens.isEmpty()) {
            SessionTokenResult latest = tokens.getFirst();
            item.setClientIp(latest.getClientIp());
            item.setDeviceLabel(latest.getDeviceLabel());
            item.setLatestLoginIp(latest.getClientIp());
            item.setLatestLoginTime(latest.getLoginAt());
            item.setLatestActiveAt(latest.getLastActiveAt() != null ? latest.getLastActiveAt() : latest.getLoginAt());
            item.setFirstLoginAt(tokens.stream()
                    .map(SessionTokenResult::getLoginAt)
                    .filter(StringUtils::hasText)
                    .min(String::compareTo)
                    .orElse(null));
        }
        String account = bundle.account();
        if (!StringUtils.hasText(account) && accountSession != null) {
            Object user = accountSession.get(LoginHelper.LOGIN_USER_KEY);
            if (user instanceof LoginUser loginUser && StringUtils.hasText(loginUser.getAccount())) {
                account = loginUser.getAccount();
            }
        }
        item.setAccount(StringUtils.hasText(account) ? account : accountId);
        return item;
    }

    private boolean matchesFilter(SessionAccountResult item, SessionPageParam query) {
        if (StringUtils.hasText(query.getAccountId()) && !query.getAccountId().equals(item.getAccountId())) {
            return false;
        }
        if (StringUtils.hasText(query.getAccount())) {
            String keyword = query.getAccount().toLowerCase(Locale.ROOT);
            if (!(containsIgnoreCase(item.getAccount(), keyword)
                    || containsIgnoreCase(item.getName(), keyword)
                    || containsIgnoreCase(item.getNickname(), keyword))) {
                return false;
            }
        }
        if (StringUtils.hasText(query.getIp())) {
            String ip = query.getIp();
            boolean hit = item.getTokens() != null && item.getTokens().stream()
                    .anyMatch(token -> token.getClientIp() != null && token.getClientIp().contains(ip));
            if (!hit) {
                return false;
            }
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().toLowerCase(Locale.ROOT);
            if (!(containsIgnoreCase(item.getAccount(), keyword)
                    || containsIgnoreCase(item.getAccountId(), keyword))) {
                return false;
            }
        }
        return true;
    }

    /** 分析用轻量扫描：只读 session/token 元数据，不拉 LoginUser；硬顶避免全量。 */
    private AnalysisAccumulator analyzeLogic(StpLogic stpLogic) {
        AnalysisAccumulator acc = new AnalysisAccumulator();
        OffsetDateTime oneHourAgo = OffsetDateTime.now(ZoneOffset.UTC).minusHours(1);
        int offset = 0;
        while (offset < ANALYSIS_SCAN_CAP) {
            int batch = Math.min(SCAN_BATCH, ANALYSIS_SCAN_CAP - offset);
            List<String> sessionIds = stpLogic.searchSessionId("", offset, batch, false);
            if (sessionIds == null || sessionIds.isEmpty()) {
                break;
            }
            for (String sessionId : sessionIds) {
                SaSession accountSession = stpLogic.getSessionBySessionId(sessionId);
                Object loginId = accountSession == null ? null : accountSession.getLoginId();
                if (loginId == null) {
                    continue;
                }
                acc.accountCount++;
                List<String> tokenValues = stpLogic.getTokenValueListByLoginId(String.valueOf(loginId));
                int count = tokenValues == null ? 0 : tokenValues.size();
                acc.tokenCount += count;
                acc.maxToken = Math.max(acc.maxToken, count);
                if (tokenValues == null) {
                    continue;
                }
                for (String token : tokenValues) {
                    SaSession session = stpLogic.getTokenSessionByToken(token);
                    if (session == null) {
                        continue;
                    }
                    OffsetDateTime loginAt = parseTime(formatEpoch(session.getCreateTime()));
                    if (loginAt != null && !loginAt.isBefore(oneHourAgo)) {
                        acc.oneHourNew++;
                    }
                }
            }
            offset += sessionIds.size();
            if (sessionIds.size() < batch) {
                break;
            }
        }
        return acc;
    }

    private TokenBundle listTokens(StpLogic stpLogic, String accountId, String accountType) {
        List<String> tokenValues = stpLogic.getTokenValueListByLoginId(accountId);
        List<SessionTokenResult> result = new ArrayList<>();
        String account = null;
        for (String token : tokenValues) {
            SessionTokenResult info = new SessionTokenResult();
            info.setToken(token);
            info.setAccountId(accountId);
            info.setAccountType(accountType);
            SaSession session = stpLogic.getTokenSessionByToken(token);
            if (session != null) {
                info.setLoginAt(formatEpoch(session.getCreateTime()));
                Object user = session.get(LoginHelper.LOGIN_USER_KEY);
                if (user instanceof LoginUser loginUser) {
                    info.setAccountType(loginUser.getAccountType() == null
                            ? accountType
                            : loginUser.getAccountType().name());
                    info.setClientIp(loginUser.getClientIp());
                    info.setDeviceLabel(loginUser.getDeviceLabel());
                    info.setUserAgent(loginUser.getUserAgent());
                    info.setRememberMe(loginUser.isRememberMe());
                    if (!StringUtils.hasText(account) && StringUtils.hasText(loginUser.getAccount())) {
                        account = loginUser.getAccount();
                    }
                }
            }
            long timeout = stpLogic.getTokenTimeout(token);
            if (timeout > 0) {
                info.setExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(timeout).toString());
            }
            info.setLastActiveAt(info.getLoginAt());
            result.add(info);
        }
        result.sort((a, b) -> {
            String left = a.getLastActiveAt() != null ? a.getLastActiveAt() : "";
            String right = b.getLastActiveAt() != null ? b.getLastActiveAt() : "";
            return right.compareTo(left);
        });
        return new TokenBundle(result, account);
    }

    private StpLogic resolveLogic(String accountType) {
        if (AccountType.PORTAL.name().equalsIgnoreCase(accountType)) {
            return StpKit.PORTAL;
        }
        return StpKit.ADMIN;
    }

    private static boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private static String maskToken(String token) {
        if (!StringUtils.hasText(token)) {
            return "";
        }
        String value = token.trim();
        if (value.length() <= 8) {
            return "****";
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }

    private static String formatEpoch(long epochMillis) {
        if (epochMillis <= 0) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC).toString();
    }

    private static OffsetDateTime parseTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private record LogicSource(StpLogic stpLogic, String accountType) {
    }

    private record TokenBundle(List<SessionTokenResult> tokens, String account) {
    }

    private static final class AnalysisAccumulator {
        private int accountCount;
        private int tokenCount;
        private int oneHourNew;
        private int maxToken;
    }
}
