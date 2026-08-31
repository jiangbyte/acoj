package github.jiangbyte.io.oj.modules.stat.controller;

import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.oj.modules.stat.result.OjUserHomepageResult;
import github.jiangbyte.io.oj.modules.stat.service.OjUserHomepageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门户端用户主页公开统计 API（匿名可读）。
 * <p>
 * Author: Charlie
 */
@Tag(name = "门户端用户主页 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortalOjUserController {

    private final OjUserHomepageService ojUserHomepageService;

    /**
     * 用户主页公开 OJ 统计：进度 / 语言 / 热力图 / 最近通过。
     */
    @Operation(summary = "用户主页公开 OJ 统计。")
    @GetMapping("/v1/portal/oj/users/homepage")
    public ApiResponse<OjUserHomepageResult> homepage(@RequestParam("account_id") String accountId) {
        return ApiResponse.ok(ojUserHomepageService.homepage(accountId));
    }
}
