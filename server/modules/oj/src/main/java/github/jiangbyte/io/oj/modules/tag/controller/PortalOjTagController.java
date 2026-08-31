package github.jiangbyte.io.oj.modules.tag.controller;

import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.oj.modules.tag.result.OjPortalTagOptionsResult;
import github.jiangbyte.io.oj.modules.tag.service.OjTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门户端 OJ 标签 API（启用中选项，匿名可读）。
 * <p>
 * Author: Charlie
 */
@Tag(name = "门户端 OJ 标签 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortalOjTagController {

    private final OjTagService ojTagService;

    /**
     * 题库筛选项：标签 + 已发布题数（聚合查询）+ 全局已发布/已通过统计。
     */
    @Operation(summary = "题库标签筛选项。")
    @GetMapping("/v1/portal/oj/tags/options")
    public ApiResponse<OjPortalTagOptionsResult> options() {
        return ApiResponse.ok(ojTagService.listPortalOptions());
    }
}
