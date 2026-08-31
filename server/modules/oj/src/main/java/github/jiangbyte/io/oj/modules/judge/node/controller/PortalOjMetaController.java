package github.jiangbyte.io.oj.modules.judge.node.controller;

import github.jiangbyte.io.common.core.domain.ApiResponse;
import github.jiangbyte.io.oj.modules.judge.node.result.OjJudgeLanguagesResult;
import github.jiangbyte.io.oj.modules.judge.node.service.OjJudgeNodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门户端 OJ 元数据 API（语言目录等，不暴露执行机运维字段）。
 *
 * Author: Charlie
 */
@Tag(name = "门户端 OJ 元数据 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortalOjMetaController {

    private final OjJudgeNodeService ojJudgeNodeService;

    /** 聚合当前 ENABLED 执行机支持的语言（去重）。 */
    @Operation(summary = "聚合执行机支持语言。")
    @GetMapping("/v1/portal/oj/languages")
    public ApiResponse<OjJudgeLanguagesResult> languages() {
        return ApiResponse.ok(ojJudgeNodeService.listAggregatedLanguages());
    }
}
